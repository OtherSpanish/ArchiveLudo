package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando Cloudinary no pudo guardar o borrar un archivo (sin conexion, credenciales malas, etc.).
 * El controller la traduce a 502 (Bad Gateway): el error no es del usuario sino del servicio externo.
 */
public class AlmacenamientoArchivosException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AlmacenamientoArchivosException(String mensaje) {
		super(mensaje);
	}

}