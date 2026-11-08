package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando un archivo enviado al back no cumple las reglas
 * (vacio, de un tipo no permitido o demasiado grande).
 * El mensaje explica cual regla se incumplio.
 */
public class ArchivoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ArchivoInvalidoException(String mensaje) {
		super(mensaje);
	}

}