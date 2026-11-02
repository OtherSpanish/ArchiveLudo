package co.edu.unbosque.archiveludo.model;

/**
 * Tipos de copia (especializacion disjunta y total de COPIA).
 * valor: texto que se guarda en la columna tipo_copia y que viaja en el JSON.
 */
public enum TipoCopia {

	FISICA("fisica"),
	DIGITAL("digital");

	private final String valor;

	private TipoCopia(String valor) {
		this.valor = valor;
	}

	public String getValor() {
		return valor;
	}

	/**
	 * Busca el tipo de copia a partir de su texto (por ejemplo "fisica").
	 *
	 * @param valor texto del tipo
	 * @return el tipo encontrado, o null si el texto no corresponde a ningun tipo
	 */
	public static TipoCopia buscarPorValor(String valor) {
		for (TipoCopia tipo : TipoCopia.values()) {
			if (tipo.getValor().equals(valor)) {
				return tipo;
			}
		}
		return null;
	}

}