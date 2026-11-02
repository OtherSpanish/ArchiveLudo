package co.edu.unbosque.archiveludo.model;

/**
 * Estado de integridad de una copia.
 * valor: texto que se guarda en la columna estado_integridad y que viaja en el JSON.
 */
public enum EstadoIntegridad {

	BUENA("buena"),
	DETERIORADA("deteriorada"),
	DANADA("dañada"),
	IRRECUPERABLE("irrecuperable");

	private final String valor;

	private EstadoIntegridad(String valor) {
		this.valor = valor;
	}

	public String getValor() {
		return valor;
	}

	/**
	 * Busca el estado de integridad a partir de su texto (por ejemplo "dañada").
	 *
	 * @param valor texto del estado
	 * @return el estado encontrado, o null si el texto no corresponde a ningun estado
	 */
	public static EstadoIntegridad buscarPorValor(String valor) {
		for (EstadoIntegridad estado : EstadoIntegridad.values()) {
			if (estado.getValor().equals(valor)) {
				return estado;
			}
		}
		return null;
	}

}