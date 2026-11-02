package co.edu.unbosque.archiveludo.exceptions;

public class EstadoIntegridadInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EstadoIntegridadInvalidoException() {
		super("El estado de integridad debe ser: buena, deteriorada, dañada o irrecuperable");
	}

}