package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando los datos del informe final no son validos
 * o cuando el proyecto todavia no esta finalizado.
 */
public class DatosInformeInvalidosException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatosInformeInvalidosException(String mensaje) {
		super(mensaje);
	}

}