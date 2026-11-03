import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { Experto, ExpertoNuevo, nombreCompletoExperto } from '../../../shared/models/experto-model';

/* Correo de cualquier dominio (a diferencia de los usuarios, que solo aceptan tres). */
const PATRON_CORREO: RegExp = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

/*
 * Pantalla de administración de expertos (/administracion/expertos).
 * Entran curador y administrador (lo exige roleGuard en la ruta); solo el
 * administrador ve el botón de eliminar.
 * Lista de expertos y un formulario (crear o editar) en la misma pantalla.
 *
 * Endpoints: GET/POST /api/expertos, PUT/DELETE /api/expertos/{id}
 */
@Component({
  selector: 'app-admin-expertos',
  templateUrl: './admin-expertos-component.html',
  styleUrl: './admin-expertos-component.css',
  standalone: false
})
export class AdminExpertosComponent implements OnInit {

  /* Expertos, ya ordenados por apellido y nombre por el back. */
  expertos: Experto[] = [];

  cargando: boolean = true;
  hayError: boolean = false;

  mostrarFormulario: boolean = false;

  /* Experto que se edita. Es undefined cuando se está creando uno nuevo. */
  editando?: Experto;

  formulario: FormGroup;

  /* true después del primer intento de guardar. */
  enviado: boolean = false;

  guardando: boolean = false;

  /* Mensaje de error del guardado o de la eliminación. */
  errorMensaje: string = '';

  /* Id del experto que está pidiendo confirmación para eliminarse (0 = ninguno). */
  idConfirmandoEliminar: number = 0;

  constructor(
    private api: ArchiveludoApiService,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      primerNombreEx: ['', [Validators.required, Validators.maxLength(60)]],
      segundoNombreEx: ['', Validators.maxLength(60)],
      primerApellidoEx: ['', [Validators.required, Validators.maxLength(60)]],
      segundoApellidoEx: ['', Validators.maxLength(60)],
      especialidad: ['', [Validators.required, Validators.maxLength(100)]],
      afiliacion: ['', Validators.maxLength(150)],
      correo: ['', [Validators.required, Validators.maxLength(150), Validators.pattern(PATRON_CORREO)]]
    });
  }

  ngOnInit(): void {
    this.cargarExpertos();
  }

  /* GET /api/expertos */
  private cargarExpertos(): void {
    this.api.obtenerExpertos().subscribe({
      next: (lista) => this.alRecibirLista(lista),
      error: (error) => this.alFallarLista(error)
    });
  }

  private alRecibirLista(lista: Experto[]): void {
    this.expertos = lista;
    this.cargando = false;
    this.hayError = false;
    this.cambios.markForCheck();
  }

  private alFallarLista(error: unknown): void {
    console.error('No se pudo cargar la lista de expertos', error);
    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  /* Nombre armado con las cuatro partes ("Laura Sofia Mejia Rios"). */
  nombreCompleto(experto: Experto): string {
    return nombreCompletoExperto(experto);
  }

  /* Segunda línea de cada fila: especialidad y, si hay, afiliación. */
  detalle(experto: Experto): string {
    if (experto.afiliacion) {
      return experto.especialidad + ' · ' + experto.afiliacion;
    }
    return experto.especialidad;
  }

  // ------------------------------------------------------------------
  // Abrir y cerrar el formulario
  // ------------------------------------------------------------------

  /* Abre el formulario vacío para crear un experto. */
  abrirNuevo(): void {
    this.editando = undefined;
    this.formulario.reset({
      primerNombreEx: '', segundoNombreEx: '', primerApellidoEx: '', segundoApellidoEx: '',
      especialidad: '', afiliacion: '', correo: ''
    });
    this.abrirFormulario();
  }

  /* Abre el formulario con los datos del experto. */
  abrirEditar(experto: Experto): void {
    this.editando = experto;
    this.formulario.reset({
      primerNombreEx: experto.primerNombreEx,
      segundoNombreEx: experto.segundoNombreEx || '',
      primerApellidoEx: experto.primerApellidoEx,
      segundoApellidoEx: experto.segundoApellidoEx || '',
      especialidad: experto.especialidad,
      afiliacion: experto.afiliacion || '',
      correo: experto.correo
    });
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

  // ------------------------------------------------------------------
  // Errores de los campos
  // ------------------------------------------------------------------

  /* Dice si hay que mostrar el error de un campo. */
  mostrarError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /* Texto del error de un campo, según qué validación falló. */
  textoError(nombre: string): string {
    const control = this.formulario.controls[nombre];

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }
    if (control.hasError('pattern')) {
      return 'Escribe un correo válido, por ejemplo nombre@dominio.org.';
    }
    if (control.hasError('maxlength')) {
      return 'Es demasiado largo.';
    }
    return '';
  }

  // ------------------------------------------------------------------
  // Guardar
  // ------------------------------------------------------------------

  /* Valida y envía POST (crear) o PUT (editar). */
  guardar(): void {
    this.enviado = true;
    this.errorMensaje = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const cuerpo = this.armarCuerpo();
    this.guardando = true;

    if (this.editando) {
      this.api.actualizarExperto(this.editando.idExperto, cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    } else {
      this.api.crearExperto(cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    }
  }

  /* Arma el cuerpo. Los campos opcionales vacíos se omiten. */
  private armarCuerpo(): ExpertoNuevo {
    const datos = this.formulario.getRawValue();

    const cuerpo: ExpertoNuevo = {
      primerNombreEx: String(datos.primerNombreEx).trim(),
      primerApellidoEx: String(datos.primerApellidoEx).trim(),
      especialidad: String(datos.especialidad).trim(),
      correo: String(datos.correo).trim()
    };

    const segundoNombre = String(datos.segundoNombreEx || '').trim();
    if (segundoNombre !== '') {
      cuerpo.segundoNombreEx = segundoNombre;
    }

    const segundoApellido = String(datos.segundoApellidoEx || '').trim();
    if (segundoApellido !== '') {
      cuerpo.segundoApellidoEx = segundoApellido;
    }

    const afiliacion = String(datos.afiliacion || '').trim();
    if (afiliacion !== '') {
      cuerpo.afiliacion = afiliacion;
    }

    return cuerpo;
  }

  /* Se guardó: se cierra el formulario y se vuelve a pedir la lista. */
  private alGuardar(): void {
    this.guardando = false;
    this.mostrarFormulario = false;
    this.cargarExpertos();
  }

  // ------------------------------------------------------------------
  // Eliminar
  // ------------------------------------------------------------------

  /* Primer paso de eliminar: pide confirmación en la misma fila. */
  pedirEliminar(experto: Experto): void {
    this.errorMensaje = '';
    this.idConfirmandoEliminar = experto.idExperto;
  }

  cancelarEliminar(): void {
    this.idConfirmandoEliminar = 0;
  }

  /* Segundo paso: DELETE /api/expertos/{id}. 409 si participa en proyectos. */
  confirmarEliminar(experto: Experto): void {
    this.api.eliminarExperto(experto.idExperto).subscribe({
      next: () => this.alEliminar(),
      error: (error) => this.alFallar(error, 'eliminar')
    });
  }

  private alEliminar(): void {
    this.idConfirmandoEliminar = 0;
    this.cargarExpertos();
  }

  /*
   * Muestra el error según el código:
   *  - 400 / 404 / 409: el back explica qué pasó (error.error.mensaje); el 409
   *    sale si el experto participa (o participó) en algún proyecto
   *  - 403: el rol no puede
   *  - otro: mensaje general
   */
  private alFallar(error: HttpErrorResponse, accion: string): void {
    console.error('No se pudo ' + accion + ' el experto', error);
    this.guardando = false;
    this.idConfirmandoEliminar = 0;

    const conMensaje = error.status === 400 || error.status === 404 || error.status === 409;

    if (conMensaje && error.error && error.error.mensaje) {
      this.errorMensaje = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorMensaje = 'Tu rol no permite esta acción.';
    } else {
      this.errorMensaje = 'No se pudo ' + accion + ' el experto. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }
}
