import { Rol } from './sesion-model';

/*
 * Un usuario del sistema (GET /api/usuarios). Solo el administrador puede
 * pedir esta lista. El back NUNCA manda la contraseña ni su hash.
 */
export interface Usuario {
  idUsuario: number;
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  segundoApellido?: string;
  correo: string;
  rol: Rol;
  fechaCreacion: string;
}

/*
 * Cuerpo de POST /api/usuarios y PUT /api/usuarios/{id}.
 *  - contrasenia: obligatoria al crear; al editar es OPCIONAL (si no se
 *    envía, el back conserva la actual).
 *  - correo: solo @gmail.com, @hotmail.com o @unbosque.edu.co.
 */
export interface UsuarioNuevo {
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  segundoApellido?: string;
  correo: string;
  contrasenia?: string;
  rol: Rol;
}
