package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando los datos de una institucion no cumplen sus reglas
 * (tipo invalido, telefono o correo con formato incorrecto).
 * El mensaje explica cual regla se incumplio.
 */
public class DatoInstitucionInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoInstitucionInvalidoException(String mensaje) {
		super(mensaje);
	}

}