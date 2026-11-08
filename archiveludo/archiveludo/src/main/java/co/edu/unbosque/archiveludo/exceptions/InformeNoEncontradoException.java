package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando se pide el informe final de un proyecto que todavia no tiene.
 */
public class InformeNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InformeNoEncontradoException(long idProyecto) {
		super("El proyecto con id " + idProyecto + " no tiene informe final");
	}

}