package co.edu.unbosque.archiveludo.exceptions;

public class CorreoYaRegistradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CorreoYaRegistradoException(String correo) {
		super("Ya existe un usuario con el correo " + correo);
	}

}