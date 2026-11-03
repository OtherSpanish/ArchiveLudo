import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { EventoRiesgo, EventoRiesgoNuevo, TIPOS_EVENTO } from '../../../shared/models/evento-riesgo-model';

/*
 * Formulario para agregar o editar un evento de riesgo.
 *
 * Uso (agregar): <app-formulario-evento [idVideojuego]="8"
 *                   (guardado)="..." (cancelado)="..."></app-formulario-evento>
 * Uso (editar):  igual, más [evento]="eventoAEditar".
 *
 * Reglas del back (las valida él y responde 400 con el motivo): la fecha no puede
 * ser futura; la descripción es obligatoria (máximo 500); la fuente es opcional (máximo 200).
 * El tipo llega legible ("Cierre de servidores") y se reenvía tal cual.
 */
@Component({
  selector: 'app-formulario-evento',
  templateUrl: './formulario-evento-component.html',
  styleUrls: ['../formulario-proyecto/formulario-proyecto-component.css'],
  standalone: false
})
export class FormularioEventoComponent implements OnInit {

  /* Videojuego al que pertenece el evento (va en la URL del POST). */
  @Input() idVideojuego!: number;

  /* Si viene, el formulario edita este evento; si no, agrega uno nuevo. */
  @Input() evento?: EventoRiesgo;

  /* Se emite cuando el back guardó el evento. */
  @Output() guardado = new EventEmitter<void>();

  /* Se emite cuando el usuario cierra el formulario sin guardar. */
  @Output() cancelado = new EventEmitter<void>();

  /* Opciones del selector de tipo. */
  tipos: string[] = TIPOS_EVENTO;

  /* Fecha máxima del campo de fecha: hoy. */
  hoy: string = this.fechaHoy();

  formulario: FormGroup;

  enviado: boolean = false;

  guardando: boolean = false;

  errorGeneral: string = '';

  constructor(
    private constructorFormulario: FormBuilder,
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      tipoEvento: [TIPOS_EVENTO[0], Validators.required],
      fechaEvento: [this.hoy, Validators.required],
      descripcion: ['', [Validators.required, Validators.maxLength(500)]],
      fuente: ['', Validators.maxLength(200)]
    });
  }

  /* Al abrir: si se edita, se cargan los datos del evento. */
  ngOnInit(): void {
    if (this.evento) {
      this.formulario.patchValue({
        tipoEvento: this.evento.tipoEvento,
        fechaEvento: this.evento.fechaEvento,
        descripcion: this.evento.descripcion,
        fuente: this.evento.fuente || ''
      });
    }
  }

  get esEdicion(): boolean {
    return this.evento !== undefined;
  }

  /* Dice si hay que mostrar el error de un campo. */
  hayError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /* Texto del error de un campo. */
  textoError(nombre: string): string {
    const control = this.formulario.controls[nombre];

    if (control.hasError('maxlength')) {
      if (nombre === 'fuente') {
        return 'Máximo 200 caracteres.';
      }
      return 'Máximo 500 caracteres.';
    }
    return 'Este campo es obligatorio.';
  }

  /* Valida y envía POST (agregar) o PUT (editar). */
  guardar(): void {
    this.enviado = true;
    this.errorGeneral = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    // La regla de la fecha (no futura) la valida el back y responde 400 con el motivo.
    const datos = this.formulario.getRawValue();

    const cuerpo: EventoRiesgoNuevo = {
      tipoEvento: datos.tipoEvento,
      fechaEvento: datos.fechaEvento,
      descripcion: String(datos.descripcion).trim()
    };

    const fuente = String(datos.fuente || '').trim();
    if (fuente !== '') {
      cuerpo.fuente = fuente;
    }

    this.guardando = true;

    if (this.evento) {
      this.api.actualizarEventoRiesgo(this.evento.idEvento, cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error)
      });
    } else {
      this.api.crearEventoRiesgo(this.idVideojuego, cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error)
      });
    }
  }

  private alGuardar(): void {
    this.guardando = false;
    this.guardado.emit();
  }

  /* 400/404: el back explica qué pasó. 403: el rol no puede. Otro: mensaje general. */
  private alFallar(error: HttpErrorResponse): void {
    console.error('No se pudo guardar el evento de riesgo', error);
    this.guardando = false;

    if (error.error && error.error.mensaje) {
      this.errorGeneral = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorGeneral = 'Tu rol no permite guardar eventos de riesgo.';
    } else {
      this.errorGeneral = 'No se pudo guardar el evento. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  cancelar(): void {
    this.cancelado.emit();
  }

  /* Fecha de hoy 'yyyy-MM-dd' en hora local. */
  private fechaHoy(): string {
    const ahora = new Date();
    const mes = String(ahora.getMonth() + 1).padStart(2, '0');
    const dia = String(ahora.getDate()).padStart(2, '0');

    return ahora.getFullYear() + '-' + mes + '-' + dia;
  }
}
