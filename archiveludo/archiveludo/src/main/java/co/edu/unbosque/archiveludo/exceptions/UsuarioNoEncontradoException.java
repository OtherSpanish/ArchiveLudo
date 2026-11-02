package co.edu.unbosque.archiveludo.exceptions;

public class UsuarioNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsuarioNoEncontradoException(long idUsuario) {
		super("No existe un usuario con id " + idUsuario);
	}

}