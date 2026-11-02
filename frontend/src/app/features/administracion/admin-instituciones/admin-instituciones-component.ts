import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  Validators
} from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { Institucion, InstitucionNueva } from '../../../shared/models/custodia-model';

/* Mayor número que guarda hoy el back en un teléfono (es un Integer de Java). */
const TELEFONO_MAXIMO: number = 2147483647;

/*
 * Pantalla de administración de instituciones (/administracion/instituciones).
 * Lista de instituciones y un formulario (crear o editar) que se despliega
 * en la misma pantalla. Tiene dos listas dinámicas: teléfonos y correos, con
 * botón "Agregar otro". Una institución debe tener AL MENOS un teléfono y un
 * correo. Curador y administrador crean y editan; solo el administrador elimina.
 *
 * Endpoints: GET/POST /api/instituciones, PUT/DELETE /api/instituciones/{id}
 */
@Component({
  selector: 'app-admin-instituciones',
  templateUrl: './admin-instituciones-component.html',
  styleUrl: './admin-instituciones-component.css',
  standalone: false
})
export class AdminInstitucionesComponent implements OnInit {

  /* Tipos de institución (texto legible, como lo acepta y lo devuelve el back). */
  tipos: string[] = ['Museo', 'Universidad', 'Archivo', 'Colección privada'];

  instituciones: Institucion[] = [];

  cargando: boolean = true;
  hayError: boolean = false;

  mostrarFormulario: boolean = false;

  /* Institución que se edita. Es undefined cuando se está creando una nueva. */
  editando?: Institucion;

  formulario: FormGroup;

  /* true después del primer intento de guardar. */
  enviado: boolean = false;

  guardando: boolean = false;

  /* Mensaje de error del guardado o de la eliminación. */
  errorMensaje: string = '';

  /* Id de la institución que está pidiendo confirmación para eliminarse (0 = ninguna). */
  idConfirmandoEliminar: number = 0;

  constructor(
    private api: ArchiveludoApiService,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      nombre: ['', [Validators.required, Validators.maxLength(255)]],
      tipo: ['Museo', Validators.required],
      pais: ['', [Validators.required, Validators.maxLength(100)]],
      telefonos: this.constructorFormulario.array([this.nuevoTelefono('')]),
      correos: this.constructorFormulario.array([this.nuevoCorreo('')])
    });
  }

  ngOnInit(): void {
    this.cargarInstituciones();
  }

  /* GET /api/instituciones */
  private cargarInstituciones(): void {
    this.api.obtenerInstituciones().subscribe({
      next: (lista) => this.alRecibirLista(lista),
      error: (error) => this.alFallarLista(error)
    });
  }

  /* Guarda la lista ordenada alfabéticamente por nombre. */
  private alRecibirLista(lista: Institucion[]): void {
    const ordenada = lista.slice();
    ordenada.sort((a, b) => a.nombre.localeCompare(b.nombre));

    this.instituciones = ordenada;
    this.cargando = false;
    this.cambios.markForCheck();
  }

  private alFallarLista(error: unknown): void {
    console.error('No se pudo cargar la lista de instituciones', error);
    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  // ------------------------------------------------------------------
  // Listas dinámicas (FormArray): una casilla por teléfono / correo
  // ------------------------------------------------------------------

  /* Crea la casilla de un teléfono: obligatoria, solo dígitos y hasta 10 cifras. */
  private nuevoTelefono(valor: string): FormControl {
    return new FormControl(valor, [Validators.required, Validators.pattern(/^[0-9]{1,10}$/)]);
  }

  /* Crea la casilla de un correo: obligatoria y con formato de correo. */
  private nuevoCorreo(valor: string): FormControl {
    return new FormControl(valor, [Validators.required, Validators.email]);
  }

  /* Las dos listas, tal como las lee el HTML. */
  get telefonos(): FormArray {
    return this.formulario.controls['telefonos'] as FormArray;
  }

  get correos(): FormArray {
    return this.formulario.controls['correos'] as FormArray;
  }

  agregarTelefono(): void {
    this.telefonos.push(this.nuevoTelefono(''));
  }

  agregarCorreo(): void {
    this.correos.push(this.nuevoCorreo(''));
  }

  /* No se puede quitar la última casilla: la institución necesita al menos una. */
  quitarTelefono(posicion: number): void {
    if (this.telefonos.length > 1) {
      this.telefonos.removeAt(posicion);
    }
  }

  quitarCorreo(posicion: number): void {
    if (this.correos.length > 1) {
      this.correos.removeAt(posicion);
    }
  }

  // ------------------------------------------------------------------
  // Abrir y cerrar el formulario
  // ------------------------------------------------------------------

  /* Abre el formulario vacío para crear una institución. */
  abrirNueva(): void {
    this.editando = undefined;
    this.llenarFormulario('', 'Museo', '', [''], ['']);
    this.abrirFormulario();
  }

  /* Abre el formulario con los datos de la institución que se va a editar. */
  abrirEditar(institucion: Institucion): void {
    const telefonos = (institucion.telefonos || []).map((numero) => String(numero));
    const correos = (institucion.correos || []).slice();

    this.editando = institucion;
    this.llenarFormulario(
      institucion.nombre,
      institucion.tipo,
      institucion.pais,
      telefonos.length > 0 ? telefonos : [''],
      correos.length > 0 ? correos : ['']
    );
    this.abrirFormulario();
  }

  /* Pone los valores en el formulario y rehace las dos listas con la cantidad justa de casillas. */
  private llenarFormulario(
    nombre: string,
    tipo: string,
    pais: string,
    telefonos: string[],
    correos: string[]
  ): void {
    this.formulario.patchValue({ nombre: nombre, tipo: tipo, pais: pais });

    this.telefonos.clear();
    for (const telefono of telefonos) {
      this.telefonos.push(this.nuevoTelefono(telefono));
    }

    this.correos.clear();
    for (const correo of correos) {
      this.correos.push(this.nuevoCorreo(correo));
    }

    this.formulario.markAsUntouched();
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

  /* Dice si hay que mostrar el error de un campo (de cualquier tipo de control). */
  mostrarError(control: AbstractControl): boolean {
    return control.invalid && (control.touched || this.enviado);
  }

  /* Texto del error de un campo, según qué validación falló. */
  textoError(control: AbstractControl): string {
    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }
    if (control.hasError('pattern')) {
      return 'Solo dígitos, sin espacios ni signos (hasta 10 cifras).';
    }
    if (control.hasError('email')) {
      return 'Escribe un correo válido.';
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

    /* El back usa un Integer para los teléfonos: avisamos antes de que falle. */
    for (const telefono of cuerpo.telefonos) {
      if (telefono > TELEFONO_MAXIMO) {
        this.errorMensaje =
          'El teléfono ' + telefono + ' supera el máximo que acepta hoy el back (' + TELEFONO_MAXIMO + ').';
        return;
      }
    }

    this.guardando = true;

    if (this.editando) {
      this.api.actualizarInstitucion(this.editando.idInstitucion, cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    } else {
      this.api.crearInstitucion(cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    }
  }

  /* Arma el cuerpo: teléfonos como números y correos limpios, sin repetidos. */
  private armarCuerpo(): InstitucionNueva {
    const datos = this.formulario.getRawValue();
    const telefonos: number[] = [];
    const correos: string[] = [];

    for (const texto of datos.telefonos) {
      const numero = Number(texto);
      if (!telefonos.includes(numero)) {
        telefonos.push(numero);
      }
    }

    for (const texto of datos.correos) {
      const correo = String(texto).trim().toLowerCase();
      if (!correos.includes(correo)) {
        correos.push(correo);
      }
    }

    return {
      nombre: String(datos.nombre).trim(),
      tipo: datos.tipo,
      pais: String(datos.pais).trim(),
      telefonos: telefonos,
      correos: correos
    };
  }

  /* Se guardó: se cierra el formulario y se vuelve a pedir la lista. */
  private alGuardar(): void {
    this.guardando = false;
    this.mostrarFormulario = false;
    this.cargarInstituciones();
  }

  // ------------------------------------------------------------------
  // Eliminar
  // ------------------------------------------------------------------

  /* Primer paso de eliminar: pide confirmación en la misma fila. */
  pedirEliminar(institucion: Institucion): void {
    this.errorMensaje = '';
    this.idConfirmandoEliminar = institucion.idInstitucion;
  }

  cancelarEliminar(): void {
    this.idConfirmandoEliminar = 0;
  }

  /* Segundo paso: DELETE /api/instituciones/{id}. */
  confirmarEliminar(institucion: Institucion): void {
    this.api.eliminarInstitucion(institucion.idInstitucion).subscribe({
      next: () => this.alEliminar(),
      error: (error) => this.alFallar(error, 'eliminar')
    });
  }

  private alEliminar(): void {
    this.idConfirmandoEliminar = 0;
    this.cargarInstituciones();
  }

  /*
   * Muestra el error según el código:
   *  - 400 / 404 / 409: el back explica qué pasó (error.error.mensaje)
   *  - 403: el rol no puede
   *  - otro: mensaje general
   */
  private alFallar(error: HttpErrorResponse, accion: string): void {
    console.error('No se pudo ' + accion + ' la institución', error);
    this.guardando = false;
    this.idConfirmandoEliminar = 0;

    const conMensaje = error.status === 400 || error.status === 404 || error.status === 409;

    if (conMensaje && error.error && error.error.mensaje) {
      this.errorMensaje = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorMensaje = 'Tu rol no permite esta acción.';
    } else {
      this.errorMensaje = 'No se pudo ' + accion + ' la institución. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }
}
