package co.edu.unbosque.archiveludo.exceptions;

public class TamanioInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TamanioInvalidoException() {
		super("El tamanio del archivo debe ser un numero de bytes mayor que cero");
	}

}