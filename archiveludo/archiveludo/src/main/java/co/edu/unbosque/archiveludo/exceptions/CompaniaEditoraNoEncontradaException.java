package co.edu.unbosque.archiveludo.exceptions;

public class CompaniaEditoraNoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CompaniaEditoraNoEncontradaException(long idEditora) {
		super("No existe una compañia editora con id " + idEditora);
	}

}