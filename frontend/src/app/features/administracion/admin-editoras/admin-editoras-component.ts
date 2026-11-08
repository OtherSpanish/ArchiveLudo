import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { CompaniaEditora, EditoraNueva } from '../../../shared/models/compania-editora-model';

/**
 * Pantalla de administración de compañías editoras (/administracion/editoras).
 * Igual que la de estudios, pero más simple: una editora solo tiene nombre y país.
 * Curador y administrador pueden crear y editar; solo el administrador elimina.
 *
 * Endpoints: GET/POST /api/editoras, PUT/DELETE /api/editoras/{id}
 */
@Component({
  selector: 'app-admin-editoras',
  templateUrl: './admin-editoras-component.html',
  styleUrl: './admin-editoras-component.css',
  standalone: false
})
export class AdminEditorasComponent implements OnInit {

  /** Editoras de la lista. */
  editoras: CompaniaEditora[] = [];

  cargando: boolean = true;
  hayError: boolean = false;

  /** true cuando el formulario está desplegado. */
  mostrarFormulario: boolean = false;

  /** Editora que se edita. Es undefined cuando se está creando una nueva. */
  editando?: CompaniaEditora;

  formulario: FormGroup;

  /** true después del primer intento de guardar. */
  enviado: boolean = false;

  guardando: boolean = false;

  /** Mensaje de error del guardado o de la eliminación. */
  errorMensaje: string = '';

  /** Id de la editora que está pidiendo confirmación para eliminarse (0 = ninguna). */
  idConfirmandoEliminar: number = 0;

  constructor(
    private api: ArchiveludoApiService,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      nombre: ['', [Validators.required, Validators.maxLength(255)]],
      pais: ['', [Validators.required, Validators.maxLength(100)]]
    });
  }

  ngOnInit(): void {
    this.cargarEditoras();
  }

  /** GET /api/editoras */
  private cargarEditoras(): void {
    this.api.obtenerEditoras().subscribe({
      next: (lista) => this.alRecibirLista(lista),
      error: (error) => this.alFallarLista(error)
    });
  }

  /** Guarda la lista ordenada alfabéticamente por nombre. */
  private alRecibirLista(lista: CompaniaEditora[]): void {
    const ordenada = lista.slice();
    ordenada.sort((a, b) => a.nombre.localeCompare(b.nombre));

    this.editoras = ordenada;
    this.cargando = false;
    this.cambios.markForCheck();
  }

  private alFallarLista(error: unknown): void {
    console.error('No se pudo cargar la lista de editoras', error);
    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  /** Abre el formulario vacío para crear una editora. */
  abrirNueva(): void {
    this.editando = undefined;
    this.formulario.reset({ nombre: '', pais: '' });
    this.abrirFormulario();
  }

  /** Abre el formulario con los datos de la editora que se va a editar. */
  abrirEditar(editora: CompaniaEditora): void {
    this.editando = editora;
    this.formulario.reset({ nombre: editora.nombre, pais: editora.pais });
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
    const cuerpo: EditoraNueva = {
      nombre: String(datos.nombre).trim(),
      pais: String(datos.pais).trim()
    };

    this.guardando = true;

    if (this.editando) {
      this.api.actualizarEditora(this.editando.idEditora, cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    } else {
      this.api.crearEditora(cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    }
  }

  /** Se guardó: se cierra el formulario y se vuelve a pedir la lista. */
  private alGuardar(): void {
    this.guardando = false;
    this.mostrarFormulario = false;
    this.cargarEditoras();
  }

  /** Primer paso de eliminar: pide confirmación en la misma fila. */
  pedirEliminar(editora: CompaniaEditora): void {
    this.errorMensaje = '';
    this.idConfirmandoEliminar = editora.idEditora;
  }

  cancelarEliminar(): void {
    this.idConfirmandoEliminar = 0;
  }

  /** Segundo paso: DELETE /api/editoras/{id}. */
  confirmarEliminar(editora: CompaniaEditora): void {
    this.api.eliminarEditora(editora.idEditora).subscribe({
      next: () => this.alEliminar(),
      error: (error) => this.alFallar(error, 'eliminar')
    });
  }

  private alEliminar(): void {
    this.idConfirmandoEliminar = 0;
    this.cargarEditoras();
  }

  /**
   * Muestra el error según el código:
   *  - 400 / 404 / 409: el back explica qué pasó (error.error.mensaje)
   *  - 403: el rol no puede
   *  - otro: mensaje general
   */
  private alFallar(error: HttpErrorResponse, accion: string): void {
    console.error('No se pudo ' + accion + ' la editora', error);
    this.guardando = false;
    this.idConfirmandoEliminar = 0;

    const conMensaje = error.status === 400 || error.status === 404 || error.status === 409;

    if (conMensaje && error.error && error.error.mensaje) {
      this.errorMensaje = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorMensaje = 'Tu rol no permite esta acción.';
    } else {
      this.errorMensaje = 'No se pudo ' + accion + ' la editora. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }
}
