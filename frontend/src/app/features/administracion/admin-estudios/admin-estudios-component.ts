import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { Estudio, EstudioNuevo } from '../../../shared/models/videojuego-model';

/**
 * Pantalla de administración de estudios (/administracion/estudios).
 * Muestra la lista de estudios y un formulario (para crear o editar) que se
 * despliega en la misma pantalla. Curador y administrador pueden crear y
 * editar; solo el administrador puede eliminar.
 *
 * Endpoints: GET/POST /api/estudios, PUT/DELETE /api/estudios/{id}
 */
@Component({
  selector: 'app-admin-estudios',
  templateUrl: './admin-estudios-component.html',
  styleUrl: './admin-estudios-component.css',
  standalone: false
})
export class AdminEstudiosComponent implements OnInit {

  /** Límites de año que acepta el back en anioFundacion. */
  readonly anioMinimo: number = 1800;
  readonly anioMaximo: number = new Date().getFullYear();

  /** Estudios de la lista. */
  estudios: Estudio[] = [];

  /** true mientras se espera la lista; true en hayError si el back falló. */
  cargando: boolean = true;
  hayError: boolean = false;

  /** true cuando el formulario está desplegado. */
  mostrarFormulario: boolean = false;

  /** Estudio que se edita. Es undefined cuando se está creando uno nuevo. */
  editando?: Estudio;

  /** El formulario. */
  formulario: FormGroup;

  /** true después del primer intento de guardar. */
  enviado: boolean = false;

  /** true mientras se espera la respuesta del guardado. */
  guardando: boolean = false;

  /** Mensaje de error del guardado o de la eliminación. */
  errorMensaje: string = '';

  /** Id del estudio que está pidiendo confirmación para eliminarse (0 = ninguno). */
  idConfirmandoEliminar: number = 0;

  constructor(
    private api: ArchiveludoApiService,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      nombre: ['', [Validators.required, Validators.maxLength(255)]],
      pais: ['', [Validators.required, Validators.maxLength(100)]],
      estadoActual: ['activo', Validators.required],
      anioFundacion: [
        '',
        [Validators.required, Validators.min(this.anioMinimo), Validators.max(this.anioMaximo)]
      ],
      anioCierre: ['']
    });

    /** Cada vez que cambia el estado, se ajustan las reglas del año de cierre. */
    this.formulario.controls['estadoActual'].valueChanges.subscribe({
      next: () => this.ajustarAnioCierre()
    });
  }

  ngOnInit(): void {
    this.cargarEstudios();
  }

  /** GET /api/estudios */
  private cargarEstudios(): void {
    this.api.obtenerEstudios().subscribe({
      next: (lista) => this.alRecibirLista(lista),
      error: (error) => this.alFallarLista(error)
    });
  }

  private alRecibirLista(lista: Estudio[]): void {
    const ordenada = lista.slice();
    ordenada.sort((a, b) => a.nombre.localeCompare(b.nombre));

    this.estudios = ordenada;
    this.cargando = false;
    this.cambios.markForCheck();
  }

  private alFallarLista(error: unknown): void {
    console.error('No se pudo cargar la lista de estudios', error);
    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  /** true si el estudio del formulario está desaparecido (entonces pide año de cierre). */
  get esDesaparecido(): boolean {
    return this.formulario.controls['estadoActual'].value === 'desaparecido';
  }

  /**
   * Si el estudio está "desaparecido", el año de cierre es obligatorio y
   * debe estar entre 1800 y hoy. Si está "activo", se vacía y no se valida.
   */
  private ajustarAnioCierre(): void {
    const control = this.formulario.controls['anioCierre'];

    if (this.esDesaparecido) {
      control.setValidators([
        Validators.required,
        Validators.min(this.anioMinimo),
        Validators.max(this.anioMaximo)
      ]);
    } else {
      control.clearValidators();
      control.setValue('');
    }

    control.updateValueAndValidity();
  }

  /** Abre el formulario vacío para crear un estudio. */
  abrirNuevo(): void {
    this.editando = undefined;
    this.formulario.reset({ nombre: '', pais: '', estadoActual: 'activo', anioFundacion: '', anioCierre: '' });
    this.ajustarAnioCierre();
    this.abrirFormulario();
  }

  /** Abre el formulario con los datos del estudio que se va a editar. */
  abrirEditar(estudio: Estudio): void {
    this.editando = estudio;
    this.formulario.reset({
      nombre: estudio.nombre,
      pais: estudio.pais,
      estadoActual: estudio.estadoActual,
      anioFundacion: estudio.anioFundacion,
      anioCierre: estudio.anioCierre !== undefined ? estudio.anioCierre : ''
    });
    this.ajustarAnioCierre();
    this.abrirFormulario();
  }

  private abrirFormulario(): void {
    this.enviado = false;
    this.errorMensaje = '';
    this.idConfirmandoEliminar = 0;
    this.mostrarFormulario = true;
  }

  cancelarFormulario(): void {
    this.mostrarFormulario = false;
  }

  /** Dice si hay que mostrar el error de un campo. */
  mostrarError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /** Texto del error de un campo, según qué validación falló. */
  textoError(nombre: string): string {
    const control = this.formulario.controls[nombre];

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }
    if (control.hasError('min') || control.hasError('max')) {
      return 'El año debe estar entre ' + this.anioMinimo + ' y ' + this.anioMaximo + '.';
    }
    if (control.hasError('maxlength')) {
      return 'Es demasiado largo.';
    }
    return '';
  }

  /** Valida y envía POST (crear) o PUT (editar). */
  guardar(): void {
    this.enviado = true;
    this.errorMensaje = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const datos = this.formulario.getRawValue();
    const cuerpo: EstudioNuevo = {
      nombre: String(datos.nombre).trim(),
      pais: String(datos.pais).trim(),
      estadoActual: datos.estadoActual,
      anioFundacion: Number(datos.anioFundacion)
    };

    /** El año de cierre solo viaja si el estudio está desaparecido. */
    if (datos.estadoActual === 'desaparecido') {
      cuerpo.anioCierre = Number(datos.anioCierre);

      if (cuerpo.anioCierre < cuerpo.anioFundacion) {
        this.errorMensaje = 'El año de cierre no puede ser anterior al de fundación.';
        return;
      }
    }

    this.guardando = true;

    if (this.editando) {
      this.api.actualizarEstudio(this.editando.idEstudio, cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    } else {
      this.api.crearEstudio(cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    }
  }

  /** Se guardó: se cierra el formulario y se vuelve a pedir la lista. */
  private alGuardar(): void {
    this.guardando = false;
    this.mostrarFormulario = false;
    this.cargarEstudios();
  }

  /** Primer paso de eliminar: pide confirmación en la misma fila. */
  pedirEliminar(estudio: Estudio): void {
    this.errorMensaje = '';
    this.idConfirmandoEliminar = estudio.idEstudio;
  }

  cancelarEliminar(): void {
    this.idConfirmandoEliminar = 0;
  }

  /** Segundo paso: DELETE /api/estudios/{id}. */
  confirmarEliminar(estudio: Estudio): void {
    this.api.eliminarEstudio(estudio.idEstudio).subscribe({
      next: () => this.alEliminar(),
      error: (error) => this.alFallar(error, 'eliminar')
    });
  }

  private alEliminar(): void {
    this.idConfirmandoEliminar = 0;
    this.cargarEstudios();
  }

  /**
   * Muestra el error según el código:
   *  - 400 / 404 / 409: el back explica qué pasó (error.error.mensaje)
   *  - 403: el rol no puede
   *  - otro: mensaje general
   */
  private alFallar(error: HttpErrorResponse, accion: string): void {
    console.error('No se pudo ' + accion + ' el estudio', error);
    this.guardando = false;
    this.idConfirmandoEliminar = 0;

    const conMensaje = error.status === 400 || error.status === 404 || error.status === 409;

    if (conMensaje && error.error && error.error.mensaje) {
      this.errorMensaje = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorMensaje = 'Tu rol no permite esta acción.';
    } else {
      this.errorMensaje = 'No se pudo ' + accion + ' el estudio. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /** Línea de años de un estudio: "1998 – 2006" o "Desde 1995". */
  textoAnios(estudio: Estudio): string {
    if (estudio.anioCierre !== undefined && estudio.anioCierre !== null) {
      return estudio.anioFundacion + ' – ' + estudio.anioCierre;
    }
    return 'Desde ' + estudio.anioFundacion;
  }
}
