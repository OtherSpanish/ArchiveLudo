import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { Custodia, CustodiaNueva, Institucion } from '../../../shared/models/custodia-model';

/**
 * Formulario para registrar una custodia nueva (traslado) o editar una.
 *
 * Uso (registrar): <app-formulario-custodia [idCopia]="11" [instituciones]="lista"
 *                     (guardado)="..." (cancelado)="..."></app-formulario-custodia>
 * Uso (editar):    igual, más [custodia]="custodiaAEditar".
 *
 * Reglas del back:
 *  - Registrar SIEMPRE crea la custodia vigente y cierra la anterior con la
 *    fecha de inicio de la nueva, por eso aquí no hay fecha de fin.
 *  - Al editar sí se puede poner fecha de fin.
 *  - Las reglas de fechas y de periodos las valida el back (400 con el motivo).
 */
@Component({
  selector: 'app-formulario-custodia',
  templateUrl: './formulario-custodia-component.html',
  styleUrls: ['../formulario-proyecto/formulario-proyecto-component.css'],
  standalone: false
})
export class FormularioCustodiaComponent implements OnInit {

  /** Copia a la que pertenece la custodia (va en la URL del POST). */
  @Input() idCopia!: number;

  /** Si viene, el formulario edita esta custodia; si no, registra una nueva. */
  @Input() custodia?: Custodia;

  /** Catálogo de instituciones para el selector. */
  @Input() instituciones: Institucion[] = [];

  /** Se emite cuando el back guardó la custodia. */
  @Output() guardado = new EventEmitter<void>();

  /** Se emite cuando el usuario cierra el formulario sin guardar. */
  @Output() cancelado = new EventEmitter<void>();

  /** Fecha máxima de los campos de fecha: hoy. */
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

  /** Al abrir: si se edita, se cargan los datos de la custodia. */
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

  /** Dice si hay que mostrar el error de un campo. */
  hayError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /** Texto del error de un campo. */
  textoError(nombre: string): string {
    const control = this.formulario.controls[nombre];

    if (control.hasError('maxlength')) {
      return 'Máximo 200 caracteres.';
    }
    return 'Este campo es obligatorio.';
  }

  /** Valida y envía POST (registrar) o PUT (editar). */
  guardar(): void {
    this.enviado = true;
    this.errorGeneral = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    // Las reglas de fechas y de periodos las valida el back y responde 400 con el motivo.
    const datos = this.formulario.getRawValue();

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

  private alGuardar(): void {
    this.guardando = false;
    this.guardado.emit();
  }

  /** 400/404: el back explica qué pasó (por ejemplo, periodos cruzados). 403: el rol no puede. */
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

  /** Fecha de hoy 'yyyy-MM-dd' en hora local. */
  private fechaHoy(): string {
    const ahora = new Date();
    const mes = String(ahora.getMonth() + 1).padStart(2, '0');
    const dia = String(ahora.getDate()).padStart(2, '0');

    return ahora.getFullYear() + '-' + mes + '-' + dia;
  }
}
