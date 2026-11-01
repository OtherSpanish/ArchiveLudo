package co.edu.unbosque.archiveludo.exceptions;

public class PaginacionInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PaginacionInvalidaException(String mensaje) {
		super(mensaje);
	}

}