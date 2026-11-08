package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza cuando OpenPDF no logra construir el documento del informe.
 */
public class GeneracionPdfException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GeneracionPdfException(long idProyecto) {
		super("No se pudo generar el PDF del informe del proyecto con id " + idProyecto);
	}

}