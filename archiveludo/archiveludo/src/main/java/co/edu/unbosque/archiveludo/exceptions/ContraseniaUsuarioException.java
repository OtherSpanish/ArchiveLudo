package co.edu.unbosque.archiveludo.exceptions;

public class ContraseniaUsuarioException extends Exception {

	public ContraseniaUsuarioException() {
		super("La contraseña debe tener mínimo 8 caracteres, contener números, mayúsculas y minúsculas");
	}

}
