package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando los datos de un experto no cumplen sus reglas
 * (campo obligatorio vacio, texto demasiado largo, correo invalido o repetido).
 * El mensaje explica cual regla se incumplio.
 */
public class DatoExpertoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoExpertoInvalidoException(String mensaje) {
		super(mensaje);
	}

}