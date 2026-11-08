package co.edu.unbosque.archiveludo.exceptions;

public class CorreoElectronicoException extends Exception {
	private static final long serialVersionUID = 1L;

	public CorreoElectronicoException() {
		super("Ingrese un correo válido");
	}

}
