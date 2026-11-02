package co.edu.unbosque.archiveludo.exceptions;

public class ParticipacionNoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ParticipacionNoEncontradaException(long idParticipacion) {
		super("No existe una participacion con id " + idParticipacion);
	}

}