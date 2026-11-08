package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando un experto ya participa en el proyecto (o viene repetido en la lista del POST).
 * Es un conflicto con datos que ya existen, por eso responde 409.
 */
public class ExpertoRepetidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExpertoRepetidoException(long idExperto) {
		super("El experto con id " + idExperto + " ya participa en este proyecto: no puede estar dos veces");
	}

}