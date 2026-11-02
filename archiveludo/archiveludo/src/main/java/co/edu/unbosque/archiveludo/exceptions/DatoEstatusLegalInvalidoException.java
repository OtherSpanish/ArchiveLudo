package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando los datos de un estatus legal no cumplen sus reglas
 * (estatus invalido, fecha de determinacion vacia o futura).
 * El mensaje explica cual regla se incumplio.
 */
public class DatoEstatusLegalInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoEstatusLegalInvalidoException(String mensaje) {
		super(mensaje);
	}

}