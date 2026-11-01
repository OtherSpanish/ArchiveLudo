package co.edu.unbosque.archiveludo.exceptions;

public class EstadoConservacionInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EstadoConservacionInvalidoException() {
		super("El estado de conservacion debe ser: activo, en riesgo, parcialmente perdido, "
				+ "totalmente perdido o restaurado");
	}

}