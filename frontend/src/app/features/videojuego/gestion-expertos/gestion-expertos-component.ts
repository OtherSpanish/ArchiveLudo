import { ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { Experto, nombreCompletoExperto } from '../../../shared/models/experto-model';
import { Participacion, ParticipacionEditada, ParticipacionNueva } from '../../../shared/models/participacion-model';
import { ProyectoDeRestauracion } from '../../../shared/models/proyecto-model';

/** Una opción del selector de experto: el id y el nombre ya armado. */
interface OpcionExperto {
  id: number;
  nombre: string;
}

/**
 * Gestión de los expertos de UN proyecto: ver, agregar, editar y quitar
 * participaciones.
 *
 * Uso:
 *   <app-gestion-expertos [proyecto]="p" [expertos]="catalogo"
 *      (cambio)="recargar()" (cerrado)="cerrar()"></app-gestion-expertos>
 *
 * Después de cada cambio se emite (cambio) para que el padre vuelva a pedir
 * el proyecto y pase la versión actualizada en [proyecto].
 * Reglas del back: un experto solo puede estar una vez en cada proyecto y no
 * se puede quitar al último.
 */
@Component({
  selector: 'app-gestion-expertos',
  templateUrl: './gestion-expertos-component.html',
  styleUrls: [
    '../formulario-proyecto/formulario-proyecto-component.css',
    './gestion-expertos-component.css'
  ],
  standalone: false
})
export class GestionExpertosComponent {

  @Input() proyecto!: ProyectoDeRestauracion;

  /** Catálogo de expertos (nombres y selector de "agregar"). */
  @Input() expertos: Experto[] = [];

  /** Se emite después de agregar, editar o quitar una participación. */
  @Output() cambio = new EventEmitter<void>();

  /** Se emite cuando el usuario cierra el panel. */
  @Output() cerrado = new EventEmitter<void>();

  /** Fecha máxima de los campos de fecha: hoy. */
  hoy: string = this.fechaHoy();

  /** true cuando el formulario (agregar o editar) está desplegado. */
  mostrarFormulario: boolean = false;

  /** Participación que se edita. Undefined cuando el formulario agrega. */
  editando?: Participacion;

  formulario: FormGroup;

  enviado: boolean = false;

  guardando: boolean = false;

  /** Mensaje de error de la última acción (guardar o quitar). */
  errorGeneral: string = '';

  /** Id de la participación que pide confirmación para quitarse (0 = ninguna). */
  idConfirmandoQuitar: number = 0;

  constructor(
    private constructorFormulario: FormBuilder,
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      idExperto: [null],
      rolExperto: ['', [Validators.required, Validators.maxLength(80)]],
      fechaIncorporacion: [this.hoy, Validators.required],
      fechaSalida: ['']
    });
  }

  /** Nombre del experto de una participación; si no está en el catálogo, "Experto #id". */
  nombreExperto(idExperto: number): string {
    for (const experto of this.expertos) {
      if (experto.idExperto === idExperto) {
        return nombreCompletoExperto(experto);
      }
    }
    return 'Experto #' + idExperto;
  }

  /** Expertos que todavía no están en el proyecto (los únicos que se pueden agregar). */
  get opcionesDisponibles(): OpcionExperto[] {
    const lista: OpcionExperto[] = [];

    for (const experto of this.expertos) {
      if (!this.yaParticipa(experto.idExperto)) {
        lista.push({ id: experto.idExperto, nombre: nombreCompletoExperto(experto) });
      }
    }

    return lista;
  }

  private yaParticipa(idExperto: number): boolean {
    for (const participacion of this.proyecto.participaciones) {
      if (participacion.idExperto === idExperto) {
        return true;
      }
    }
    return false;
  }

  /** El botón "Quitar" se desactiva cuando solo queda un experto. */
  get sePuedeQuitar(): boolean {
    return this.proyecto.participaciones.length > 1;
  }

  /** Abre el formulario vacío para agregar un experto. */
  abrirAgregar(): void {
    this.editando = undefined;
    this.idConfirmandoQuitar = 0;
    this.formulario.reset({ idExperto: null, rolExperto: '', fechaIncorporacion: this.hoy, fechaSalida: '' });
    this.formulario.controls['idExperto'].setValidators(Validators.required);
    this.formulario.controls['idExperto'].updateValueAndValidity();
    this.enviado = false;
    this.errorGeneral = '';
    this.mostrarFormulario = true;
  }

  /** Abre el formulario con los datos de una participación (el experto no se cambia). */
  abrirEditar(participacion: Participacion): void {
    this.editando = participacion;
    this.idConfirmandoQuitar = 0;
    this.formulario.reset({
      idExperto: participacion.idExperto,
      rolExperto: participacion.rolExperto,
      fechaIncorporacion: participacion.fechaIncorporacion,
      fechaSalida: participacion.fechaSalida || ''
    });
    this.formulario.controls['idExperto'].clearValidators();
    this.formulario.controls['idExperto'].updateValueAndValidity();
    this.enviado = false;
    this.errorGeneral = '';
    this.mostrarFormulario = true;
  }

  cancelarFormulario(): void {
    this.mostrarFormulario = false;
  }

  /** Dice si hay que mostrar el error de un campo. */
  hayError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /** Valida y envía POST (agregar) o PUT (editar). */
  guardar(): void {
    this.enviado = true;
    this.errorGeneral = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    // Las reglas de fechas las valida el back y responde 400 con el motivo.
    const datos = this.formulario.getRawValue();

    this.guardando = true;

    if (this.editando) {
      const editada: ParticipacionEditada = {
        rolExperto: datos.rolExperto.trim(),
        fechaIncorporacion: datos.fechaIncorporacion
      };
      if (datos.fechaSalida) {
        editada.fechaSalida = datos.fechaSalida;
      }

      this.api.actualizarParticipacion(this.editando.idParticipacion, editada).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error)
      });
    } else {
      const nueva: ParticipacionNueva = {
        idExperto: Number(datos.idExperto),
        rolExperto: datos.rolExperto.trim(),
        fechaIncorporacion: datos.fechaIncorporacion
      };
      if (datos.fechaSalida) {
        nueva.fechaSalida = datos.fechaSalida;
      }

      this.api.crearParticipacion(this.proyecto.idProyecto, nueva).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error)
      });
    }
  }

  private alGuardar(): void {
    this.guardando = false;
    this.mostrarFormulario = false;
    this.cambio.emit();
    this.cambios.markForCheck();
  }

  /** Primer paso de quitar: confirmación en la misma fila. */
  pedirQuitar(participacion: Participacion): void {
    this.errorGeneral = '';
    this.idConfirmandoQuitar = participacion.idParticipacion;
  }

  cancelarQuitar(): void {
    this.idConfirmandoQuitar = 0;
  }

  /** Segundo paso: DELETE /api/participaciones/{id}. */
  confirmarQuitar(participacion: Participacion): void {
    this.api.eliminarParticipacion(participacion.idParticipacion).subscribe({
      next: () => this.alQuitar(),
      error: (error) => this.alFallarQuitar(error)
    });
  }

  private alQuitar(): void {
    this.idConfirmandoQuitar = 0;
    this.cambio.emit();
    this.cambios.markForCheck();
  }

  /** 409: el back dice que es la única participación. 403: solo administrador. */
  private alFallarQuitar(error: HttpErrorResponse): void {
    console.error('No se pudo quitar al experto', error);
    this.idConfirmandoQuitar = 0;

    if (error.error && error.error.mensaje) {
      this.errorGeneral = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorGeneral = 'Solo un administrador puede quitar expertos.';
    } else {
      this.errorGeneral = 'No se pudo quitar al experto. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /** 400/404/409: el back explica qué pasó (por ejemplo, 409 si el experto ya está en el proyecto). */
  private alFallar(error: HttpErrorResponse): void {
    console.error('No se pudo guardar la participación', error);
    this.guardando = false;

    if (error.error && error.error.mensaje) {
      this.errorGeneral = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorGeneral = 'Tu rol no permite guardar expertos.';
    } else {
      this.errorGeneral = 'No se pudo guardar. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  cerrar(): void {
    this.cerrado.emit();
  }

  /** '2026-09-01' -> '01/09/2026' */
  formatearFecha(fecha: string): string {
    const partes = fecha.split('-');
    if (partes.length !== 3) {
      return fecha;
    }
    return partes[2] + '/' + partes[1] + '/' + partes[0];
  }

  /** Fecha de hoy 'yyyy-MM-dd' en hora local. */
  private fechaHoy(): string {
    const ahora = new Date();
    const mes = String(ahora.getMonth() + 1).padStart(2, '0');
    const dia = String(ahora.getDate()).padStart(2, '0');

    return ahora.getFullYear() + '-' + mes + '-' + dia;
  }
}
