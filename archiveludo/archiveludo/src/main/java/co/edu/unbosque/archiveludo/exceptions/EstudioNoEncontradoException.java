package co.edu.unbosque.archiveludo.exceptions;

public class EstudioNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EstudioNoEncontradoException(long idEstudio) {
		super("No existe un estudio con id " + idEstudio);
	}

}