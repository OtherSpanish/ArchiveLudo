package co.edu.unbosque.archiveludo.exceptions;

public class IdInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IdInvalidoException(String nombreDueno) {
		super("El id de " + nombreDueno + " debe ser un numero entero mayor que cero");
	}

}