package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza al intentar quitar la unica participacion de un proyecto:
 * todo proyecto de restauracion debe tener al menos un experto.
 */
public class UltimaParticipacionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UltimaParticipacionException(long idProyecto) {
		super("No se puede quitar la unica participacion del proyecto " + idProyecto
				+ ": todo proyecto debe tener al menos un experto");
	}

}