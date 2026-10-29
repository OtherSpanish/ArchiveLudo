/*
 * Modelos de la sesión y del login.
 * Los nombres de los campos coinciden con el JSON del back.
 */

/*
 * Los tres roles de la guía (sección 9). OJO: los valores exactos que guarda
 * la tabla USUARIO se confirman con el script SQL.
 *  - consulta: solo ve.
 *  - curador: crea y edita (Curador/Investigador).
 *  - administrador: todo, incluida la gestión de usuarios.
 */
export type Rol = 'consulta' | 'curador' | 'administrador';

/* Texto que ve el usuario para cada rol (barra superior, pantalla de login). */
export const ETIQUETAS_ROL: Record<Rol, string> = {
  consulta: 'Consulta',
  curador: 'Curador',
  administrador: 'Administrador'
};

/*
 * La sesión que guarda el front mientras el usuario está dentro.
 *  - token: el JWT que devuelve el back; se envía en cada petición.
 * NUNCA se guarda ni se muestra la contraseña ni su hash.
 */
export interface Sesion {
  idUsuario: number;
  nombreUsuario: string;
  rol: Rol;
  token: string;
}

/* Lo que escribe el usuario en el formulario de login. */
export interface CredencialesLogin {
  nombreUsuario: string;
  contrasenia: string;
}

/*
 * POST /api/auth/login
 * Cuerpo que se envía: CredencialesLogin.
 * Respuesta 200: RespuestaLogin. Si las credenciales no son válidas o el
 * usuario está desactivado, el back responde 401.
 * El back nunca devuelve la contraseña ni su hash.
 */
export interface RespuestaLogin {
  token: string;
  usuario: {
    idUsuario: number;
    nombreUsuario: string;
    rol: Rol;
  };
}
