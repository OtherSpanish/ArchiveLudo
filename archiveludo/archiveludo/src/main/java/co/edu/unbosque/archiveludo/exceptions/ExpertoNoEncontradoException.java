package co.edu.unbosque.archiveludo.exceptions;

public class ExpertoNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExpertoNoEncontradoException(long idExperto) {
		super("No existe un experto con id " + idExperto);
	}

}