package co.edu.unbosque.archiveludo.exceptions;

public class EventoDeRiesgoNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EventoDeRiesgoNoEncontradoException(long idEvento) {
		super("No existe un evento de riesgo con id " + idEvento);
	}

}