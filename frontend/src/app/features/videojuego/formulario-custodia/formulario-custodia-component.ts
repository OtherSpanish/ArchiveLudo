import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { Custodia, CustodiaNueva, Institucion } from '../../../shared/models/custodia-model';

/*
 * Formulario para registrar una custodia nueva (traslado) o editar una.
 *
 * Uso (registrar): <app-formulario-custodia [idCopia]="11" [instituciones]="lista"
 *                     [inicioVigente]="'2025-06-30'"
 *                     (guardado)="..." (cancelado)="..."></app-formulario-custodia>
 * Uso (editar):    igual, más [custodia]="custodiaAEditar".
 *
 * Reglas del back:
 *  - Registrar SIEMPRE crea la custodia vigente y cierra la anterior con la
 *    fecha de inicio de la nueva, por eso aquí no hay fecha de fin.
 *  - La custodia nueva no puede empezar antes que la vigente (inicioVigente).
 *  - Al editar sí se puede poner fecha de fin; los periodos no pueden cruzarse
 *    (si se cruzan, el back responde 400 con el motivo).
 */
@Component({
  selector: 'app-formulario-custodia',
  templateUrl: './formulario-custodia-component.html',
  styleUrls: ['../formulario-proyecto/formulario-proyecto-component.css'],
  standalone: false
})
export class FormularioCustodiaComponent implements OnInit {

  /* Copia a la que pertenece la custodia (va en la URL del POST). */
  @Input() idCopia!: number;

  /* Si viene, el formulario edita esta custodia; si no, registra una nueva. */
  @Input() custodia?: Custodia;

  /* Catálogo de instituciones para el selector. */
  @Input() instituciones: Institucion[] = [];

  /* Fecha de inicio de la custodia vigente actual (si la copia tiene una). Sirve para validar. */
  @Input() inicioVigente?: string;

  /* Se emite cuando el back guardó la custodia. */
  @Output() guardado = new EventEmitter<void>();

  /* Se emite cuando el usuario cierra el formulario sin guardar. */
  @Output() cancelado = new EventEmitter<void>();

  /* Fecha máxima de los campos de fecha: hoy. */
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
      idInstitucion: [null, Validators.required],
      fechaInicio: [this.hoy, Validators.required],
      fechaFin: [''],
      condicionAlmacenamiento: ['', [Validators.required, Validators.maxLength(200)]]
    });
  }

  /* Al abrir: si se edita, se cargan los datos de la custodia. */
  ngOnInit(): void {
    if (this.custodia) {
      this.formulario.patchValue({
        idInstitucion: this.custodia.idInstitucion,
        fechaInicio: this.custodia.fechaInicio,
        fechaFin: this.custodia.fechaFin || '',
        condicionAlmacenamiento: this.custodia.condicionAlmacenamiento
      });
    }
  }

  get esEdicion(): boolean {
    return this.custodia !== undefined;
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
      return 'Máximo 200 caracteres.';
    }
    return 'Este campo es obligatorio.';
  }

  /* Valida y envía POST (registrar) o PUT (editar). */
  guardar(): void {
    this.enviado = true;
    this.errorGeneral = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const datos = this.formulario.getRawValue();

    this.errorGeneral = this.validarFechas(datos.fechaInicio, datos.fechaFin);
    if (this.errorGeneral !== '') {
      return;
    }

    const cuerpo: CustodiaNueva = {
      idInstitucion: Number(datos.idInstitucion),
      fechaInicio: datos.fechaInicio,
      condicionAlmacenamiento: datos.condicionAlmacenamiento.trim()
    };

    this.guardando = true;

    if (this.custodia) {
      // Solo al editar se envía la fecha de fin (si se escribió).
      if (datos.fechaFin) {
        cuerpo.fechaFin = datos.fechaFin;
      }

      this.api.actualizarCustodia(this.custodia.idCustodia, cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error)
      });
    } else {
      this.api.crearCustodia(this.idCopia, cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error)
      });
    }
  }

  /*
   * Fechas: la de inicio no puede ser futura; al registrar tampoco puede ser
   * anterior al inicio de la vigente; la de fin (solo al editar) no puede ser
   * anterior al inicio ni futura.
   */
  private validarFechas(inicio: string, fin: string): string {
    if (inicio > this.hoy) {
      return 'La fecha de inicio no puede ser futura.';
    }

    if (!this.esEdicion && this.inicioVigente && inicio < this.inicioVigente) {
      return 'La custodia nueva no puede empezar antes que la vigente (' + this.formatearFecha(this.inicioVigente) + ').';
    }

    if (this.esEdicion && fin) {
      if (fin < inicio) {
        return 'La fecha de fin no puede ser anterior al inicio.';
      }
      if (fin > this.hoy) {
        return 'La fecha de fin no puede ser futura.';
      }
    }

    return '';
  }

  private alGuardar(): void {
    this.guardando = false;
    this.guardado.emit();
  }

  /* 400/404: el back explica qué pasó (por ejemplo, periodos cruzados). 403: el rol no puede. */
  private alFallar(error: HttpErrorResponse): void {
    console.error('No se pudo guardar la custodia', error);
    this.guardando = false;

    if (error.error && error.error.mensaje) {
      this.errorGeneral = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorGeneral = 'Tu rol no permite guardar custodias.';
    } else {
      this.errorGeneral = 'No se pudo guardar la custodia. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  cancelar(): void {
    this.cancelado.emit();
  }

  /* '2026-09-01' -> '01/09/2026' */
  private formatearFecha(fecha: string): string {
    const partes = fecha.split('-');
    if (partes.length !== 3) {
      return fecha;
    }
    return partes[2] + '/' + partes[1] + '/' + partes[0];
  }

  /* Fecha de hoy 'yyyy-MM-dd' en hora local. */
  private fechaHoy(): string {
    const ahora = new Date();
    const mes = String(ahora.getMonth() + 1).padStart(2, '0');
    const dia = String(ahora.getDate()).padStart(2, '0');

    return ahora.getFullYear() + '-' + mes + '-' + dia;
  }
}
