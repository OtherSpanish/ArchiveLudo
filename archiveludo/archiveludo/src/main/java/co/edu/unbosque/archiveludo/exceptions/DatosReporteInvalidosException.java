package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando el tipo de reporte no existe o los filtros no son validos
 * (fecha mal escrita, o desde posterior a hasta).
 */
public class DatosReporteInvalidosException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatosReporteInvalidosException(String mensaje) {
		super(mensaje);
	}

}