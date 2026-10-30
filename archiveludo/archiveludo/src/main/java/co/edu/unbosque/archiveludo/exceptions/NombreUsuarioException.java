package co.edu.unbosque.archiveludo.exceptions;

public class NombreUsuarioException extends Exception {

	public NombreUsuarioException() {
		super("Ingrese un nombre de usuario válido (entre 4 y 14 caracteres y que contenga una letre)");
	}

}