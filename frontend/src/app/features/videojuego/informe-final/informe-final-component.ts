import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { InformeFinal, InformeFinalSolicitud, ResultadoInforme } from '../../../shared/models/informe-final-model';
import { ProyectoDeRestauracion } from '../../../shared/models/proyecto-model';

/** Una opción del selector de resultado: el valor que viaja al back y el texto que se ve. */
interface OpcionResultado {
  valor: ResultadoInforme;
  texto: string;
}

/**
 * Sección "Informe final" de un proyecto de restauración.
 *
 * Uso: <app-informe-final [proyecto]="item" (cambio)="recargarProyectos()"></app-informe-final>
 * Solo se muestra a curador y administrador (lo decide el padre con *appHasRole).
 *
 * El informe es un PDF que arma el BACK con tres datos del formulario (fecha de
 * publicación, resumen y resultado) más los del proyecto. El back lo sube a
 * Cloudinary y entrega solo el enlace; el front nunca habla con Cloudinary.
 *  - Proyecto finalizado y sin informe: botón "Generar informe final" (POST).
 *  - Proyecto con informe: enlace para abrir el PDF, "Regenerar informe" (PUT) y,
 *    solo para el administrador, "Eliminar informe" (DELETE).
 *  - Proyecto que no está finalizado: solo una nota.
 * Las reglas (proyecto finalizado, fecha no futura, etc.) las valida el back y
 * responde 400 con el motivo, que aquí solo se muestra.
 * Cada vez que el informe se crea, se regenera o se elimina, emite "cambio" para
 * que el padre vuelva a pedir los proyectos (y tieneInforme se actualice).
 */
@Component({
  selector: 'app-informe-final',
  templateUrl: './informe-final-component.html',
  styleUrl: './informe-final-component.css',
  standalone: false
})
export class InformeFinalComponent implements OnChanges {

  /** El proyecto al que pertenece el informe. */
  @Input() proyecto!: ProyectoDeRestauracion;

  /** Se emite cuando el informe cambió (creado, regenerado o eliminado). */
  @Output() cambio = new EventEmitter<void>();

  /** Opciones del selector de resultado (valores exactos del back). */
  opcionesResultado: OpcionResultado[] = [
    { valor: 'exitoso', texto: 'Exitoso' },
    { valor: 'parcial', texto: 'Parcial' },
    { valor: 'fracaso', texto: 'Fracaso' }
  ];

  /** Máximo de caracteres del resumen (el back lo valida igual). */
  longitudMaximaResumen: number = 2000;

  /** Fecha máxima del campo de fecha: hoy. */
  hoy: string = this.fechaHoy();

  formulario: FormGroup;

  /** true cuando el formulario está desplegado. */
  mostrarFormulario: boolean = false;

  /** true si el formulario regenera un informe existente (PUT); false si lo crea (POST). */
  regenerando: boolean = false;

  /** true después del primer intento de guardar; muestra errores de campos sin tocar. */
  enviado: boolean = false;

  /** true mientras el back arma el PDF y lo sube (tarda más que un guardado normal). */
  generando: boolean = false;

  /** Enlace del PDF. Vacío mientras se carga o si el proyecto no tiene informe. */
  urlInforme: string = '';

  /** true cuando se pidió eliminar y se espera la confirmación (solo administrador). */
  confirmandoEliminar: boolean = false;

  eliminando: boolean = false;

  errorGeneral: string = '';

  constructor(
    private constructorFormulario: FormBuilder,
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      fechaPublicacion: [this.hoy, Validators.required],
      resumenResultados: ['', [Validators.required, Validators.maxLength(this.longitudMaximaResumen)]],
      resultado: ['', Validators.required]
    });
  }

  /**
   * Se ejecuta cada vez que el padre cambia el proyecto (por ejemplo, tras recargarlo).
   * Si tiene informe y todavía no se conoce el enlace, se pide al back.
   * Si ya no tiene informe, se olvida el enlace.
   */
  ngOnChanges(): void {
    if (!this.proyecto) {
      return;
    }

    if (this.proyecto.tieneInforme) {
      if (this.urlInforme === '') {
        this.cargarEnlace();
      }
    } else {
      this.urlInforme = '';
    }
  }

  /** GET /api/proyectos/{id}/informe: solo trae el enlace del PDF. */
  private cargarEnlace(): void {
    this.api.obtenerInformeFinal(this.proyecto.idProyecto).subscribe({
      next: (informe) => this.alRecibirEnlace(informe),
      error: (error) => this.alFallarCargarEnlace(error)
    });
  }

  private alRecibirEnlace(informe: InformeFinal): void {
    this.urlInforme = informe.urlInforme;
    this.cambios.markForCheck();
  }

  private alFallarCargarEnlace(error: HttpErrorResponse): void {
    console.error('No se pudo cargar el enlace del informe', error);
    this.errorGeneral = 'No se pudo cargar el enlace del informe.';
    this.cambios.markForCheck();
  }

  /** Cuántos caracteres lleva el resumen (para el contador). */
  get cantidadCaracteres(): number {
    const texto = this.formulario.controls['resumenResultados'].value as string;
    return texto.length;
  }

  /** Abre el formulario vacío para generar el primer informe (POST). */
  abrirFormularioGenerar(): void {
    this.prepararFormulario(false);
  }

  /** Abre el formulario vacío para regenerar el informe (PUT). El back no guarda los datos anteriores. */
  abrirFormularioRegenerar(): void {
    this.prepararFormulario(true);
  }

  private prepararFormulario(regenerar: boolean): void {
    this.formulario.reset({ fechaPublicacion: this.hoy, resumenResultados: '', resultado: '' });
    this.regenerando = regenerar;
    this.enviado = false;
    this.errorGeneral = '';
    this.confirmandoEliminar = false;
    this.mostrarFormulario = true;
  }

  /** El usuario cierra el formulario sin guardar. */
  cerrarFormulario(): void {
    this.mostrarFormulario = false;
  }

  /** Dice si hay que mostrar el error de un campo. */
  hayError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /** Texto del error de un campo. */
  textoError(nombre: string): string {
    if (nombre === 'resultado') {
      return 'Selecciona un resultado.';
    }
    if (nombre === 'resumenResultados' && this.formulario.controls[nombre].hasError('maxlength')) {
      return 'Máximo ' + this.longitudMaximaResumen + ' caracteres.';
    }
    return 'Este campo es obligatorio.';
  }

  /** Valida lo básico, arma el cuerpo y envía POST (generar) o PUT (regenerar). */
  guardar(): void {
    this.enviado = true;
    this.errorGeneral = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const datos = this.formulario.getRawValue();

    const cuerpo: InformeFinalSolicitud = {
      fechaPublicacion: datos.fechaPublicacion,
      resumenResultados: datos.resumenResultados.trim(),
      resultado: datos.resultado
    };

    this.generando = true;

    if (this.regenerando) {
      this.api.actualizarInformeFinal(this.proyecto.idProyecto, cuerpo).subscribe({
        next: (informe) => this.alGuardar(informe),
        error: (error) => this.alFallarGuardar(error)
      });
    } else {
      this.api.crearInformeFinal(this.proyecto.idProyecto, cuerpo).subscribe({
        next: (informe) => this.alGuardar(informe),
        error: (error) => this.alFallarGuardar(error)
      });
    }
  }

  /** El back devolvió el informe con su enlace (nuevo, si se regeneró). */
  private alGuardar(informe: InformeFinal): void {
    this.generando = false;
    this.urlInforme = informe.urlInforme;
    this.mostrarFormulario = false;
    this.cambios.markForCheck();
    this.cambio.emit();
  }

  /**
   * 400/404/409: el back explica qué pasó. 403: el rol no puede.
   * 502: Cloudinary falló. Otro: mensaje general.
   */
  private alFallarGuardar(error: HttpErrorResponse): void {
    console.error('No se pudo guardar el informe', error);
    this.generando = false;

    if (error.error && error.error.mensaje && error.status !== 502) {
      this.errorGeneral = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorGeneral = 'Tu rol no permite generar informes.';
    } else if (error.status === 502) {
      this.errorGeneral = 'No se pudo guardar el informe. Inténtalo de nuevo en unos minutos.';
    } else {
      this.errorGeneral = 'No se pudo guardar el informe. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /** Primer paso de eliminar: pide confirmación. */
  pedirEliminar(): void {
    this.errorGeneral = '';
    this.mostrarFormulario = false;
    this.confirmandoEliminar = true;
  }

  /** El usuario se arrepintió. */
  cancelarEliminar(): void {
    this.confirmandoEliminar = false;
  }

  /** Segundo paso: DELETE /api/proyectos/{id}/informe (204 sin cuerpo). */
  confirmarEliminar(): void {
    this.eliminando = true;
    this.errorGeneral = '';

    this.api.eliminarInformeFinal(this.proyecto.idProyecto).subscribe({
      next: () => this.alEliminar(),
      error: (error) => this.alFallarEliminar(error)
    });
  }

  private alEliminar(): void {
    this.eliminando = false;
    this.confirmandoEliminar = false;
    this.urlInforme = '';
    this.cambios.markForCheck();
    this.cambio.emit();
  }

  /** 403: solo administrador. 502: Cloudinary falló y el informe sigue ahí. */
  private alFallarEliminar(error: HttpErrorResponse): void {
    console.error('No se pudo eliminar el informe', error);
    this.eliminando = false;
    this.confirmandoEliminar = false;

    if (error.status === 403) {
      this.errorGeneral = 'Solo un administrador puede eliminar informes.';
    } else if (error.status === 502) {
      this.errorGeneral = 'No se pudo eliminar el informe. Inténtalo de nuevo en unos minutos.';
    } else {
      this.errorGeneral = 'No se pudo eliminar el informe. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /** Fecha de hoy 'yyyy-MM-dd' en hora local (toISOString daría la fecha en UTC). */
  private fechaHoy(): string {
    const ahora = new Date();
    const mes = String(ahora.getMonth() + 1).padStart(2, '0');
    const dia = String(ahora.getDate()).padStart(2, '0');

    return ahora.getFullYear() + '-' + mes + '-' + dia;
  }
}
