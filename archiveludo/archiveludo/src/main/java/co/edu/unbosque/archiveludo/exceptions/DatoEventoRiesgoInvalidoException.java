package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando los datos de un evento de riesgo no cumplen sus reglas
 * (tipo invalido, fecha vacia o futura, descripcion vacia o textos demasiado largos).
 * El mensaje explica cual regla se incumplio.
 */
public class DatoEventoRiesgoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoEventoRiesgoInvalidoException(String mensaje) {
		super(mensaje);
	}

}