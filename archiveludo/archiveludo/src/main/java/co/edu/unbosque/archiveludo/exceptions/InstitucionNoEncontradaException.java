package co.edu.unbosque.archiveludo.exceptions;

public class InstitucionNoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InstitucionNoEncontradaException(long idInstitucion) {
		super("No existe una institucion con id " + idInstitucion);
	}

}