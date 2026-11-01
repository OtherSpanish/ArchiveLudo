package co.edu.unbosque.archiveludo.exceptions;

public class RolInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RolInvalidoException() {
		super("El rol debe ser administrador, curador o consulta");
	}

}