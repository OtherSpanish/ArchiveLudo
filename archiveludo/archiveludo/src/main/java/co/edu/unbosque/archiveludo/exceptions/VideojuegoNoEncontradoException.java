package co.edu.unbosque.archiveludo.exceptions;

public class VideojuegoNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public VideojuegoNoEncontradoException(long idVideojuego) {
		super("No existe un videojuego con id " + idVideojuego);
	}

}