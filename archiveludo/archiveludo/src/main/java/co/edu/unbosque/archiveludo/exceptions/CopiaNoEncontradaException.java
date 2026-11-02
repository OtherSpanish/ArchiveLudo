package co.edu.unbosque.archiveludo.exceptions;

public class CopiaNoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CopiaNoEncontradaException(long idCopia) {
		super("No existe una copia con id " + idCopia);
	}

}