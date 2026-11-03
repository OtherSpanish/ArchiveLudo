/**
 * Modelos de la sesión y del login.
 * Los nombres de los campos coinciden con el JSON del back.
 */

/**
 * Los tres roles de la guía (sección 9). El back los envía exactamente así
 * (CAMBIOS_BACKEND_Y_AJUSTES_FRONT, sección 8.8).
 *  - consulta: solo ve.
 *  - curador: crea y edita (Curador/Investigador).
 *  - administrador: todo, incluida la gestión de usuarios.
 */
export type Rol = 'consulta' | 'curador' | 'administrador';

/** Texto que ve el usuario para cada rol (barra superior, pantalla de login). */
export const ETIQUETAS_ROL: Record<Rol, string> = {
  consulta: 'Consulta',
  curador: 'Curador',
  administrador: 'Administrador'
};

/**
 * La sesión que guarda el front mientras el usuario está dentro.
 *  - correo: con el que inició sesión.
 *  - nombreUsuario: el nombre para MOSTRAR ("Ana Perez", primer nombre y primer
 *    apellido). Se llama así desde antes del back, y la barra superior ya lo usa.
 *  - token: el JWT que devuelve el back (sin el prefijo "Bearer "); el
 *    interceptor lo envía en cada petición.
 * NUNCA se guarda ni se muestra la contraseña ni su hash.
 */
export interface Sesion {
  idUsuario: number;
  correo: string;
  nombreUsuario: string;
  rol: Rol;
  token: string;
}

/** Lo que escribe el usuario en el formulario de login. */
export interface CredencialesLogin {
  correo: string;
  contrasenia: string;
}

/**
 * POST /api/auth/login
 * Cuerpo que se envía: CredencialesLogin.
 *
 * Respuesta 200:
 *  - el TOKEN llega en la cabecera "Authorization" ("Bearer eyJ...");
 *  - el CUERPO es el usuario (UsuarioLogin), sin contraseña ni hash.
 * Si las credenciales no son válidas, el back responde 401; si falta el
 * correo o la contraseña, 400 con { estado, mensaje }.
 */
export interface UsuarioLogin {
  idUsuario: number;
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  segundoApellido?: string;
  correo: string;
  rol: Rol;
  fechaCreacion?: string;
}
