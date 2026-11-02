package co.edu.unbosque.archiveludo.model;

/**
 * Tipos de estatus legal de un videojuego.
 * valor: texto que se guarda en la columna estatus de ESTATUS_LEGAL.
 * textoLegible: texto que el front muestra tal cual.
 */
public enum TipoEstatusLegal {

	PROTEGIDO("protegido", "Protegido"),
	DOMINIO_PUBLICO("dominio publico", "Dominio público"),
	HUERFANO("huerfano", "Huérfano"),
	ABANDONWARE("abandonware", "Abandonware");

	private final String valor;
	private final String textoLegible;

	private TipoEstatusLegal(String valor, String textoLegible) {
		this.valor = valor;
		this.textoLegible = textoLegible;
	}

	public String getValor() {
		return valor;
	}

	public String getTextoLegible() {
		return textoLegible;
	}

	/**
	 * Busca el tipo de estatus a partir del texto guardado en la BD (por ejemplo "dominio publico").
	 *
	 * @param valor texto del estatus
	 * @return el tipo encontrado, o null si el texto no corresponde a ningun tipo
	 */
	public static TipoEstatusLegal buscarPorValor(String valor) {
		for (TipoEstatusLegal tipo : TipoEstatusLegal.values()) {
			if (tipo.getValor().equals(valor)) {
				return tipo;
			}
		}
		return null;
	}

}