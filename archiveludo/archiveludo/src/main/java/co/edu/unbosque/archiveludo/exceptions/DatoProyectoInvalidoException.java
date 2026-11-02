package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando los datos de un proyecto de restauracion o de sus participaciones no cumplen sus reglas
 * (estado o fechas invalidas, textos vacios o demasiado largos, proyecto sin expertos, experto repetido).
 * El mensaje explica cual regla se incumplio.
 */
public class DatoProyectoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoProyectoInvalidoException(String mensaje) {
		super(mensaje);
	}

}