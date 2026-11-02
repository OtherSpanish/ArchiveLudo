package co.edu.unbosque.archiveludo.exceptions;

public class EstatusLegalNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EstatusLegalNoEncontradoException(long idEstatus) {
		super("No existe un estatus legal con id " + idEstatus);
	}

}