package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando los datos de una custodia no cumplen sus reglas
 * (fechas invalidas, periodos que se cruzan, condicion vacia o demasiado larga).
 * El mensaje explica cual regla se incumplio.
 */
public class DatoCustodiaInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoCustodiaInvalidoException(String mensaje) {
		super(mensaje);
	}

}