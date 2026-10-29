import { Injectable, inject, signal } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { CredencialesLogin, RespuestaLogin, Rol, Sesion } from '../shared/models/sesion-model';

// ===== BACK: descomentar estas 2 líneas al conectar =====
// import { map, tap } from 'rxjs';
// import { ArchiveludoApiService } from './archiveludo-api-service';

/* Una cuenta de prueba del hardcode: lo que "existiría" en la tabla USUARIO. */
interface UsuarioPrueba {
  idUsuario: number;
  nombreUsuario: string;
  contrasenia: string;
  rol: Rol;
}

/*
 * Servicio que guarda quién tiene la sesión iniciada.
 * Lo usan el login (iniciar sesión), la barra superior (mostrar el usuario),
 * los guards (dejar pasar o no) y, al conectar el back, el interceptor
 * (agregar el token a cada petición).
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  /* Nombre con el que se guarda la sesión en sessionStorage. */
  private claveSesion: string = 'archiveludo.sesion';

  /*
   * La sesión actual, o null si nadie ha iniciado sesión.
   * Es un "signal": una caja que avisa a toda plantilla que la lea cuando
   * su contenido cambia. Así la barra superior se actualiza sola al entrar
   * o salir, aunque el cambio lo haga otro componente.
   * Se lee con paréntesis: sesion(). Se cambia con .set(valor).
   */
  readonly sesion = signal<Sesion | null>(null);

  /* Al crearse el servicio, recupera la sesión guardada (por si el usuario refrescó la página). */
  constructor() {
    this.restaurarSesion();
  }

  // ==================================================================
  // ===== HARDCODE: borrar este bloque al conectar el back =====
  // (incluye usuariosPrueba y la función iniciarSesion)
  // ==================================================================

  /* Cuentas de prueba. Todas tienen la contraseña 1234. */
  private usuariosPrueba: UsuarioPrueba[] = [
    { idUsuario: 1, nombreUsuario: 'consulta', contrasenia: '1234', rol: 'consulta' },
    { idUsuario: 2, nombreUsuario: 'curador', contrasenia: '1234', rol: 'curador' },
    { idUsuario: 3, nombreUsuario: 'admin', contrasenia: '1234', rol: 'administrador' }
  ];

  /*
   * Simula el login: busca la cuenta de prueba y, si usuario y contraseña
   * coinciden, guarda la sesión. Devuelve un Observable igual que el login
   * real, así el componente del login no cambia al conectar el back.
   */
  iniciarSesion(credenciales: CredencialesLogin): Observable<Sesion> {
    for (const cuenta of this.usuariosPrueba) {
      const coincideUsuario = cuenta.nombreUsuario === credenciales.nombreUsuario.trim();
      const coincideClave = cuenta.contrasenia === credenciales.contrasenia;

      if (coincideUsuario && coincideClave) {
        const sesion: Sesion = {
          idUsuario: cuenta.idUsuario,
          nombreUsuario: cuenta.nombreUsuario,
          rol: cuenta.rol,
          token: 'token-de-prueba'
        };

        this.guardarSesion(sesion);
        return of(sesion);
      }
    }

    return throwError(() => new Error('Usuario o contraseña incorrectos'));
  }

  // ==================================================================
  // ===== FIN DEL HARDCODE =====
  // ==================================================================


  // ==================================================================
  // ===== BACK: descomentar todo este bloque al conectar =====
  // (en WebStorm: seleccionarlo y Ctrl + /)
  // ==================================================================

  // // inject() pide el servicio de la API a Angular, igual que un constructor.
  // private api = inject(ArchiveludoApiService);
  //
  // // Login real: POST /api/auth/login. Si responde bien, convierte la
  // // respuesta en una Sesion y la guarda. Si el back responde 401, el
  // // error le llega al componente del login (el subscribe de allá).
  // iniciarSesion(credenciales: CredencialesLogin): Observable<Sesion> {
  //   return this.api.iniciarSesion(credenciales).pipe(
  //     map((respuesta) => this.respuestaASesion(respuesta)),
  //     tap((sesion) => this.guardarSesion(sesion))
  //   );
  // }
  //
  // private respuestaASesion(respuesta: RespuestaLogin): Sesion {
  //   return {
  //     idUsuario: respuesta.usuario.idUsuario,
  //     nombreUsuario: respuesta.usuario.nombreUsuario,
  //     rol: respuesta.usuario.rol,
  //     token: respuesta.token
  //   };
  // }

  // ==================================================================
  // ===== FIN DEL BLOQUE BACK =====
  // ==================================================================


  /* Cierra la sesión: borra lo guardado y deja la sesión en null. */
  cerrarSesion(): void {
    this.sesion.set(null);

    try {
      sessionStorage.removeItem(this.claveSesion);
    } catch {
      // Si el navegador no deja usar sessionStorage, la sesión igual queda cerrada.
    }
  }

  /* true si hay alguien con sesión iniciada. */
  estaAutenticado(): boolean {
    return this.sesion() !== null;
  }

  /* El token JWT de la sesión, o '' si no hay sesión. Lo usará el interceptor. */
  get token(): string {
    const actual = this.sesion();
    if (actual === null) {
      return '';
    }
    return actual.token;
  }

  /* true si la persona tiene alguno de los roles de la lista. Sin sesión, siempre false. */
  tieneRol(roles: Rol[]): boolean {
    const actual = this.sesion();
    if (actual === null) {
      return false;
    }
    return roles.includes(actual.rol);
  }

  /* Guarda la sesión en el signal y en sessionStorage (para sobrevivir a un refresco). */
  private guardarSesion(sesion: Sesion): void {
    this.sesion.set(sesion);

    try {
      sessionStorage.setItem(this.claveSesion, JSON.stringify(sesion));
    } catch {
      // Si no se puede guardar, la sesión funciona igual hasta que se refresque la página.
    }
  }

  /* Lee la sesión guardada, si existe y tiene el formato esperado. */
  private restaurarSesion(): void {
    try {
      const texto = sessionStorage.getItem(this.claveSesion);
      if (texto === null) {
        return;
      }

      const guardada = JSON.parse(texto);
      if (guardada && guardada.token && guardada.rol && guardada.nombreUsuario) {
        this.sesion.set(guardada as Sesion);
      }
    } catch {
      // Si lo guardado está dañado o no se puede leer, se ignora y se empieza sin sesión.
    }
  }
}
