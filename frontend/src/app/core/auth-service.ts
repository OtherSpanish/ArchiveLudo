import { Injectable, inject, signal } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { CredencialesLogin, Rol, Sesion, UsuarioLogin } from '../shared/models/sesion-model';
import { ArchiveludoApiService } from './archiveludo-api-service';

/**
 * Servicio que guarda quién tiene la sesión iniciada.
 * Lo usan el login (iniciar sesión), la barra superior (mostrar el usuario),
 * los guards (dejar pasar o no) y el interceptor (agregar el token a cada
 * petición).
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  /** Nombre con el que se guarda la sesión en sessionStorage. */
  private claveSesion: string = 'archiveludo.sesion';

  /** inject() pide el servicio de la API a Angular, igual que un constructor. */
  private api = inject(ArchiveludoApiService);

  /**
   * La sesión actual, o null si nadie ha iniciado sesión.
   * Es un "signal": una caja que avisa a toda plantilla que la lea cuando
   * su contenido cambia. Así la barra superior se actualiza sola al entrar
   * o salir, aunque el cambio lo haga otro componente.
   * Se lee con paréntesis: sesion(). Se cambia con .set(valor).
   */
  readonly sesion = signal<Sesion | null>(null);

  /** Al crearse el servicio, recupera la sesión guardada (por si el usuario refrescó la página). */
  constructor() {
    this.restaurarSesion();
  }

  /**
   * Login real: POST /api/auth/login. Si responde bien, convierte la respuesta
   * en una Sesion y la guarda. Si el back responde 401, el error le llega al
   * componente del login (el subscribe de allá).
   */
  iniciarSesion(credenciales: CredencialesLogin): Observable<Sesion> {
    return this.api.iniciarSesion(credenciales).pipe(
      map((respuesta) => this.respuestaASesion(respuesta)),
      tap((sesion) => this.guardarSesion(sesion))
    );
  }

  /**
   * Arma la Sesion con las dos partes de la respuesta: los datos del usuario
   * (cuerpo) y el token (cabecera "Authorization", que llega como "Bearer eyJ...").
   * El token se guarda SIN el prefijo "Bearer ": el interceptor lo agrega.
   */
  private respuestaASesion(respuesta: HttpResponse<UsuarioLogin>): Sesion {
    const usuario = respuesta.body;
    const cabecera = respuesta.headers.get('Authorization');

    if (usuario === null || cabecera === null) {
      throw new Error('El servidor no devolvió los datos del usuario o el token');
    }

    return {
      idUsuario: usuario.idUsuario,
      correo: usuario.correo,
      nombreUsuario: usuario.primerNombre + ' ' + usuario.primerApellido,
      rol: usuario.rol,
      token: cabecera.replace('Bearer ', '')
    };
  }

  /** Cierra la sesión: borra lo guardado y deja la sesión en null. */
  cerrarSesion(): void {
    this.sesion.set(null);

    try {
      sessionStorage.removeItem(this.claveSesion);
    } catch {
      // Si el navegador no deja usar sessionStorage, la sesión igual queda cerrada.
    }
  }

  /** true si hay alguien con sesión iniciada. */
  estaAutenticado(): boolean {
    return this.sesion() !== null;
  }

  /** El token JWT de la sesión, o '' si no hay sesión. Lo usa el interceptor. */
  get token(): string {
    const actual = this.sesion();
    if (actual === null) {
      return '';
    }
    return actual.token;
  }

  /** true si la persona tiene alguno de los roles de la lista. Sin sesión, siempre false. */
  tieneRol(roles: Rol[]): boolean {
    const actual = this.sesion();
    if (actual === null) {
      return false;
    }
    return roles.includes(actual.rol);
  }

  /** Guarda la sesión en el signal y en sessionStorage (para sobrevivir a un refresco). */
  private guardarSesion(sesion: Sesion): void {
    this.sesion.set(sesion);

    try {
      sessionStorage.setItem(this.claveSesion, JSON.stringify(sesion));
    } catch {
      // Si no se puede guardar, la sesión funciona igual hasta que se refresque la página.
    }
  }

  /**
   * Lee la sesión guardada, si existe y tiene el formato esperado.
   * Exige "correo": así se descartan las sesiones viejas (guardadas antes de
   * conectar el back), que no lo tenían y llevaban un token falso.
   */
  private restaurarSesion(): void {
    try {
      const texto = sessionStorage.getItem(this.claveSesion);
      if (texto === null) {
        return;
      }

      const guardada = JSON.parse(texto);
      if (guardada && guardada.token && guardada.rol && guardada.nombreUsuario && guardada.correo) {
        this.sesion.set(guardada as Sesion);
      }
    } catch {
      // Si lo guardado está dañado o no se puede leer, se ignora y se empieza sin sesión.
    }
  }
}
