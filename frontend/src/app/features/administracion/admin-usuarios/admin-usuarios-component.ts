import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { AuthService } from '../../../core/auth-service';
import { ETIQUETAS_ROL, Rol } from '../../../shared/models/sesion-model';
import { Usuario, UsuarioNuevo } from '../../../shared/models/usuario-model';

/* Correo con uno de los dominios permitidos por el back. */
const PATRON_CORREO: RegExp = /^[^@\s]+@(gmail\.com|hotmail\.com|unbosque\.edu\.co)$/i;

/* Contraseña: mínimo 8 caracteres, con una minúscula, una mayúscula y un número. */
const PATRON_CONTRASENIA: RegExp = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,}$/;

/* Una opción del selector de rol. */
interface OpcionRol {
  valor: Rol;
  etiqueta: string;
}

/*
 * Pantalla de administración de usuarios (/administracion/usuarios).
 * SOLO el administrador entra (lo exige roleGuard en la ruta).
 * Lista de usuarios y un formulario (crear o editar) en la misma pantalla.
 * Al editar, la contraseña vacía significa "no cambiarla".
 *
 * Endpoints: GET/POST /api/usuarios, PUT/DELETE /api/usuarios/{id}
 */
@Component({
  selector: 'app-admin-usuarios',
  templateUrl: './admin-usuarios-component.html',
  styleUrl: './admin-usuarios-component.css',
  standalone: false
})
export class AdminUsuariosComponent implements OnInit {

  /* Opciones del rol, con los valores que usa el back. */
  opcionesRol: OpcionRol[] = [
    { valor: 'consulta', etiqueta: ETIQUETAS_ROL['consulta'] },
    { valor: 'curador', etiqueta: ETIQUETAS_ROL['curador'] },
    { valor: 'administrador', etiqueta: ETIQUETAS_ROL['administrador'] }
  ];

  usuarios: Usuario[] = [];

  cargando: boolean = true;
  hayError: boolean = false;

  mostrarFormulario: boolean = false;

  /* Usuario que se edita. Es undefined cuando se está creando uno nuevo. */
  editando?: Usuario;

  formulario: FormGroup;

  /* true después del primer intento de guardar. */
  enviado: boolean = false;

  guardando: boolean = false;

  /* Mensaje de error del guardado o de la eliminación. */
  errorMensaje: string = '';

  /* Id del usuario que está pidiendo confirmación para eliminarse (0 = ninguno). */
  idConfirmandoEliminar: number = 0;

  constructor(
    private api: ArchiveludoApiService,
    private auth: AuthService,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      primerNombre: ['', [Validators.required, Validators.maxLength(100)]],
      segundoNombre: ['', Validators.maxLength(100)],
      primerApellido: ['', [Validators.required, Validators.maxLength(100)]],
      segundoApellido: ['', Validators.maxLength(100)],
      correo: ['', [Validators.required, Validators.pattern(PATRON_CORREO)]],
      contrasenia: [''],
      rol: ['consulta', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  /* GET /api/usuarios */
  private cargarUsuarios(): void {
    this.api.obtenerUsuarios().subscribe({
      next: (lista) => this.alRecibirLista(lista),
      error: (error) => this.alFallarLista(error)
    });
  }

  /* Guarda la lista ordenada por primer nombre. */
  private alRecibirLista(lista: Usuario[]): void {
    const ordenada = lista.slice();
    ordenada.sort((a, b) => a.primerNombre.localeCompare(b.primerNombre));

    this.usuarios = ordenada;
    this.cargando = false;
    this.cambios.markForCheck();
  }

  private alFallarLista(error: unknown): void {
    console.error('No se pudo cargar la lista de usuarios', error);
    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  /* Nombre completo: "Carlos Andres Rojas Mejia" (sin los segundos que falten). */
  nombreCompleto(usuario: Usuario): string {
    const partes = [
      usuario.primerNombre,
      usuario.segundoNombre,
      usuario.primerApellido,
      usuario.segundoApellido
    ];

    return partes.filter((parte) => parte).join(' ');
  }

  /* "Curador" a partir de 'curador'. */
  textoRol(rol: Rol): string {
    return ETIQUETAS_ROL[rol];
  }

  /* true si la fila es de la persona que tiene la sesión: no puede eliminarse a sí misma. */
  esUsuarioActual(usuario: Usuario): boolean {
    const sesion = this.auth.sesion();
    return sesion !== null && sesion.idUsuario === usuario.idUsuario;
  }

  /* true si el formulario está editando (la contraseña es opcional). */
  get esEdicion(): boolean {
    return this.editando !== undefined;
  }

  // ------------------------------------------------------------------
  // Abrir y cerrar el formulario
  // ------------------------------------------------------------------

  /* Abre el formulario vacío para crear un usuario (la contraseña es obligatoria). */
  abrirNuevo(): void {
    this.editando = undefined;
    this.formulario.reset({
      primerNombre: '', segundoNombre: '', primerApellido: '', segundoApellido: '',
      correo: '', contrasenia: '', rol: 'consulta'
    });
    this.ajustarContrasenia();
    this.abrirFormulario();
  }

  /* Abre el formulario con los datos del usuario (la contraseña queda vacía = no se cambia). */
  abrirEditar(usuario: Usuario): void {
    this.editando = usuario;
    this.formulario.reset({
      primerNombre: usuario.primerNombre,
      segundoNombre: usuario.segundoNombre || '',
      primerApellido: usuario.primerApellido,
      segundoApellido: usuario.segundoApellido || '',
      correo: usuario.correo,
      contrasenia: '',
      rol: usuario.rol
    });
    this.ajustarContrasenia();
    this.abrirFormulario();
  }

  /*
   * Al crear, la contraseña es obligatoria y debe cumplir las reglas.
   * Al editar, puede quedar vacía (no se cambia); si se escribe, debe cumplirlas.
   * Validators.pattern no se queja de un campo vacío, por eso "required" se agrega aparte.
   */
  private ajustarContrasenia(): void {
    const control = this.formulario.controls['contrasenia'];

    if (this.esEdicion) {
      control.setValidators([Validators.pattern(PATRON_CONTRASENIA)]);
    } else {
      control.setValidators([Validators.required, Validators.pattern(PATRON_CONTRASENIA)]);
    }

    control.updateValueAndValidity();
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
      if (nombre === 'correo') {
        return 'Usa un correo @gmail.com, @hotmail.com o @unbosque.edu.co.';
      }
      return 'Mínimo 8 caracteres, con una mayúscula, una minúscula y un número.';
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
      this.api.actualizarUsuario(this.editando.idUsuario, cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    } else {
      this.api.crearUsuario(cuerpo).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallar(error, 'guardar')
      });
    }
  }

  /*
   * Arma el cuerpo. Los nombres opcionales vacíos se omiten, y la contraseña
   * solo viaja si se escribió una (al editar, vacía = no se cambia).
   */
  private armarCuerpo(): UsuarioNuevo {
    const datos = this.formulario.getRawValue();

    const cuerpo: UsuarioNuevo = {
      primerNombre: String(datos.primerNombre).trim(),
      primerApellido: String(datos.primerApellido).trim(),
      correo: String(datos.correo).trim().toLowerCase(),
      rol: datos.rol
    };

    const segundoNombre = String(datos.segundoNombre || '').trim();
    if (segundoNombre !== '') {
      cuerpo.segundoNombre = segundoNombre;
    }

    const segundoApellido = String(datos.segundoApellido || '').trim();
    if (segundoApellido !== '') {
      cuerpo.segundoApellido = segundoApellido;
    }

    if (datos.contrasenia !== '') {
      cuerpo.contrasenia = datos.contrasenia;
    }

    return cuerpo;
  }

  /* Se guardó: se cierra el formulario y se vuelve a pedir la lista. */
  private alGuardar(): void {
    this.guardando = false;
    this.mostrarFormulario = false;
    this.cargarUsuarios();
  }

  // ------------------------------------------------------------------
  // Eliminar
  // ------------------------------------------------------------------

  /* Primer paso de eliminar: pide confirmación en la misma fila. */
  pedirEliminar(usuario: Usuario): void {
    this.errorMensaje = '';
    this.idConfirmandoEliminar = usuario.idUsuario;
  }

  cancelarEliminar(): void {
    this.idConfirmandoEliminar = 0;
  }

  /* Segundo paso: DELETE /api/usuarios/{id}. */
  confirmarEliminar(usuario: Usuario): void {
    this.api.eliminarUsuario(usuario.idUsuario).subscribe({
      next: () => this.alEliminar(),
      error: (error) => this.alFallar(error, 'eliminar')
    });
  }

  private alEliminar(): void {
    this.idConfirmandoEliminar = 0;
    this.cargarUsuarios();
  }

  /*
   * Muestra el error según el código:
   *  - 400 / 404 / 409: el back explica qué pasó (error.error.mensaje);
   *    el 409 sale, por ejemplo, con un correo repetido o si el usuario
   *    registró videojuegos, copias, estatus o eventos
   *  - 403: el rol no puede
   *  - otro: mensaje general
   */
  private alFallar(error: HttpErrorResponse, accion: string): void {
    console.error('No se pudo ' + accion + ' el usuario', error);
    this.guardando = false;
    this.idConfirmandoEliminar = 0;

    const conMensaje = error.status === 400 || error.status === 404 || error.status === 409;

    if (conMensaje && error.error && error.error.mensaje) {
      this.errorMensaje = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorMensaje = 'Tu rol no permite esta acción.';
    } else {
      this.errorMensaje = 'No se pudo ' + accion + ' el usuario. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }
}
