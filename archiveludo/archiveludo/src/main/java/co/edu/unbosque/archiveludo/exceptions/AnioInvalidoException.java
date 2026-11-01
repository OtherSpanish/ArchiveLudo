package co.edu.unbosque.archiveludo.exceptions;

public class AnioInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AnioInvalidoException(int anioMinimo, int anioMaximo) {
		super("El año de lanzamiento debe ser un numero entre " + anioMinimo + " y " + anioMaximo);
	}

}