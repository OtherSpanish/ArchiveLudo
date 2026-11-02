package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando los datos de un estudio no cumplen sus reglas
 * (estado invalido, anios fuera de rango o incoherentes con el estado).
 * El mensaje explica cual regla se incumplio.
 */
public class DatoEstudioInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoEstudioInvalidoException(String mensaje) {
		super(mensaje);
	}

}