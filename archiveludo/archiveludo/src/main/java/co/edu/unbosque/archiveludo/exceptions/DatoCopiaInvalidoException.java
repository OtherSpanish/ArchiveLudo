package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando los datos de una copia no cumplen sus reglas
 * (por ejemplo, intentar cambiar el tipo de copia al actualizarla).
 * El mensaje explica cual regla se incumplio.
 */
public class DatoCopiaInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoCopiaInvalidoException(String mensaje) {
		super(mensaje);
	}

}