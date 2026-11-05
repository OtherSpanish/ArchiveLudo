package co.edu.unbosque.archiveludo.exceptions;

public class ContraseniaUsuarioException extends Exception {
	private static final long serialVersionUID = 1L;

	public ContraseniaUsuarioException() {
		super("La contraseña debe tener mínimo 8 caracteres, contener números, mayúsculas y minúsculas");
	}

}
