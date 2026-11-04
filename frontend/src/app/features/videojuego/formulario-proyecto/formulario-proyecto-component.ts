import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { Experto, nombreCompletoExperto } from '../../../shared/models/experto-model';
import { ParticipacionNueva } from '../../../shared/models/participacion-model';
import {
  EstadoProyecto,
  ProyectoDeRestauracion,
  ProyectoEditado,
  ProyectoNuevo
} from '../../../shared/models/proyecto-model';

/** Una opción del selector de estado: el valor que viaja al back y el texto que se ve. */
interface OpcionEstado {
  valor: EstadoProyecto;
  texto: string;
}

/** Una opción del selector de experto: el id y el nombre ya armado. */
interface OpcionExperto {
  id: number;
  nombre: string;
}

/**
 * Formulario para crear o editar un proyecto de restauración.
 *
 * Uso (crear):  <app-formulario-proyecto [idVideojuego]="8" [expertos]="lista"
 *                  (guardado)="..." (cancelado)="..."></app-formulario-proyecto>
 * Uso (editar): igual, más [proyecto]="proyectoAEditar".
 *
 * Al CREAR se piden también los expertos (mínimo uno, sin repetir): el back
 * guarda proyecto y participaciones en una sola operación.
 * Al EDITAR solo se cambian los datos del proyecto; los expertos se manejan
 * aparte (componente de gestión de expertos).
 * La fecha de fin solo aparece si el estado es "finalizado".
 */
@Component({
  selector: 'app-formulario-proyecto',
  templateUrl: './formulario-proyecto-component.html',
  styleUrl: './formulario-proyecto-component.css',
  standalone: false
})
export class FormularioProyectoComponent implements OnInit {

  /** Videojuego al que pertenece el proyecto (va en la URL del POST). */
  @Input() idVideojuego!: number;

  /** Si viene, el formulario edita este proyecto; si no, crea uno nuevo. */
  @Input() proyecto?: ProyectoDeRestauracion;

  /** Catálogo de expertos para los selectores. */
  @Input() expertos: Experto[] = [];

  /** Se emite cuando el back guardó el proyecto. */
  @Output() guardado = new EventEmitter<void>();

  /** Se emite cuando el usuario cierra el formulario sin guardar. */
  @Output() cancelado = new EventEmitter<void>();

  opcionesEstado: OpcionEstado[] = [
    { valor: 'activo', texto: 'Activo' },
    { valor: 'pausado', texto: 'Pausado' },
    { valor: 'finalizado', texto: 'Finalizado' }
  ];

  /** Fecha máxima de todos los campos de fecha: hoy. */
  hoy: string = this.fechaHoy();

  formulario: FormGroup;

  /** true después del primer intento de guardar; muestra errores de campos sin tocar. */
  enviado: boolean = false;

  guardando: boolean = false;

  errorGeneral: string = '';

  constructor(
    private constructorFormulario: FormBuilder,
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      nombre: ['', [Validators.required, Validators.maxLength(150)]],
      descripcion: ['', Validators.maxLength(500)],
      herramientaEmulador: ['', Validators.maxLength(100)],
      estado: ['activo', Validators.required],
      fechaInicio: [this.hoy, Validators.required],
      fechaFin: [''],
      participaciones: this.constructorFormulario.array([])
    });
  }

  /** Al abrir: si se edita, se cargan los datos; si se crea, se deja una fila de experto. */
  ngOnInit(): void {
    if (this.proyecto) {
      this.formulario.patchValue({
        nombre: this.proyecto.nombre,
        descripcion: this.proyecto.descripcion || '',
        herramientaEmulador: this.proyecto.herramientaEmulador || '',
        estado: this.proyecto.estado,
        fechaInicio: this.proyecto.fechaInicio,
        fechaFin: this.proyecto.fechaFin || ''
      });
    } else {
      this.agregarFila();
    }
  }

  get esEdicion(): boolean {
    return this.proyecto !== undefined;
  }

  /** La fecha de fin solo se muestra (y se envía) si el estado es "finalizado". */
  get mostrarFechaFin(): boolean {
    return this.formulario.controls['estado'].value === 'finalizado';
  }

  get participaciones(): FormArray {
    return this.formulario.controls['participaciones'] as FormArray;
  }

  /** Opciones del selector de experto, con el nombre armado. */
  get opcionesExperto(): OpcionExperto[] {
    const lista: OpcionExperto[] = [];

    for (const experto of this.expertos) {
      lista.push({ id: experto.idExperto, nombre: nombreCompletoExperto(experto) });
    }

    return lista;
  }

  /** Agrega una fila vacía de experto. */
  agregarFila(): void {
    this.participaciones.push(this.constructorFormulario.group({
      idExperto: [null, Validators.required],
      rolExperto: ['', [Validators.required, Validators.maxLength(80)]],
      fechaIncorporacion: [this.hoy, Validators.required],
      fechaSalida: ['']
    }));
  }

  /** Quita una fila; siempre queda al menos una. */
  quitarFila(posicion: number): void {
    if (this.participaciones.length > 1) {
      this.participaciones.removeAt(posicion);
    }
  }

  /** Dice si hay que mostrar el error de un campo del proyecto. */
  hayError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /** Dice si hay que mostrar el error de un campo de una fila de experto. */
  hayErrorFila(posicion: number, nombre: string): boolean {
    const fila = this.participaciones.at(posicion) as FormGroup;
    const control = fila.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /** Texto del error de un campo (sirve para el proyecto y para las filas). */
  textoError(nombre: string): string {
    if (nombre === 'nombre' || nombre === 'fechaInicio' || nombre === 'idExperto' || nombre === 'rolExperto' || nombre === 'fechaIncorporacion') {
      return 'Este campo es obligatorio.';
    }
    if (nombre === 'descripcion') {
      return 'Máximo 500 caracteres.';
    }
    return 'Revisa este campo.';
  }

  /** Valida, arma el cuerpo y envía POST (crear) o PUT (editar). */
  guardar(): void {
    this.enviado = true;
    this.errorGeneral = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    // Las reglas de negocio (fechas, expertos repetidos, etc.) las valida el back
    // y responde 400 con el motivo, que se muestra en alFallar.
    const datos = this.formulario.getRawValue();

    this.guardando = true;

    if (this.proyecto) {
      this.api.actualizarProyecto(this.proyecto.idProyecto, this.armarEditado(datos)).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error)
      });
    } else {
      this.api.crearProyecto(this.idVideojuego, this.armarNuevo(datos)).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error)
      });
    }
  }

  /** Cuerpo del PUT: los campos vacíos se omiten y fechaFin solo va si es finalizado. */
  private armarEditado(datos: { nombre: string; descripcion: string; herramientaEmulador: string; estado: EstadoProyecto; fechaInicio: string; fechaFin: string }): ProyectoEditado {
    const cuerpo: ProyectoEditado = {
      nombre: datos.nombre.trim(),
      estado: datos.estado,
      fechaInicio: datos.fechaInicio
    };

    const descripcion = datos.descripcion.trim();
    if (descripcion !== '') {
      cuerpo.descripcion = descripcion;
    }

    const herramienta = datos.herramientaEmulador.trim();
    if (herramienta !== '') {
      cuerpo.herramientaEmulador = herramienta;
    }

    if (datos.estado === 'finalizado') {
      cuerpo.fechaFin = datos.fechaFin;
    }

    return cuerpo;
  }

  /** Cuerpo del POST: igual que el PUT, más la lista de participaciones. */
  private armarNuevo(datos: { nombre: string; descripcion: string; herramientaEmulador: string; estado: EstadoProyecto; fechaInicio: string; fechaFin: string; participaciones: ParticipacionNueva[] }): ProyectoNuevo {
    const base = this.armarEditado(datos);
    const filas: ParticipacionNueva[] = [];

    for (const fila of datos.participaciones) {
      const participacion: ParticipacionNueva = {
        idExperto: Number(fila.idExperto),
        rolExperto: fila.rolExperto.trim(),
        fechaIncorporacion: fila.fechaIncorporacion
      };

      if (fila.fechaSalida) {
        participacion.fechaSalida = fila.fechaSalida;
      }

      filas.push(participacion);
    }

    return { ...base, participaciones: filas };
  }

  private alGuardar(): void {
    this.guardando = false;
    this.guardado.emit();
  }

  /** 400/404/409: el back explica qué pasó. 403: el rol no puede. Otro: mensaje general. */
  private alFallar(error: HttpErrorResponse): void {
    console.error('No se pudo guardar el proyecto', error);
    this.guardando = false;

    if (error.error && error.error.mensaje) {
      this.errorGeneral = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorGeneral = 'Tu rol no permite guardar proyectos.';
    } else {
      this.errorGeneral = 'No se pudo guardar el proyecto. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /** El usuario cierra el formulario sin guardar. */
  cancelar(): void {
    this.cancelado.emit();
  }

  /** Fecha de hoy 'yyyy-MM-dd' en hora local (toISOString daría la fecha en UTC). */
  private fechaHoy(): string {
    const ahora = new Date();
    const mes = String(ahora.getMonth() + 1).padStart(2, '0');
    const dia = String(ahora.getDate()).padStart(2, '0');

    return ahora.getFullYear() + '-' + mes + '-' + dia;
  }
}
