package co.edu.unbosque.archiveludo.exceptions;

public class CustodiaNoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CustodiaNoEncontradaException(long idCustodia) {
		super("No existe una custodia con id " + idCustodia);
	}

}