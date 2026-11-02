package co.edu.unbosque.archiveludo.exceptions;

public class ProyectoNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ProyectoNoEncontradoException(long idProyecto) {
		super("No existe un proyecto de restauracion con id " + idProyecto);
	}

}