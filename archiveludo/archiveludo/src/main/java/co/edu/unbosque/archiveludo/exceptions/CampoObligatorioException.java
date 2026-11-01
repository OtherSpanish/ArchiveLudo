package co.edu.unbosque.archiveludo.exceptions;

public class CampoObligatorioException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CampoObligatorioException(String nombreCampo) {
		super("El campo '" + nombreCampo + "' es obligatorio");
	}

}