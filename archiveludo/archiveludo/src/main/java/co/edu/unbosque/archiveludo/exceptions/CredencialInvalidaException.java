package co.edu.unbosque.archiveludo.exceptions;

public class CredencialInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CredencialInvalidaException() {
		super("Correo o contraseña incorrectos");
	}

}