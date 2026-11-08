package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando se intenta crear un segundo informe final para un proyecto.
 */
public class InformeYaExisteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InformeYaExisteException(long idProyecto) {
		super("El proyecto con id " + idProyecto + " ya tiene un informe final: solo puede tener uno");
	}

}