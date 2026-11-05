package co.edu.unbosque.archiveludo.exceptions;

public class NombreUsuarioException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public NombreUsuarioException() {
		super("Ingrese un nombre de usuario válido (entre 4 y 14 caracteres y que contenga una letre)");
	}

}