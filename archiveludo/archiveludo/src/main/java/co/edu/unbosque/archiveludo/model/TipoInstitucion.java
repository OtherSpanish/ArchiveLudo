package co.edu.unbosque.archiveludo.model;

/**
 * Tipos de institucion custodia.
 * valor: texto del ENUM de la columna tipo de INSTITUCION.
 * textoLegible: texto que el front muestra tal cual.
 */
public enum TipoInstitucion {

	MUSEO("museo", "Museo"),
	UNIVERSIDAD("universidad", "Universidad"),
	ARCHIVO("archivo", "Archivo"),
	COLECCION_PRIVADA("coleccion privada", "Colección privada");

	private final String valor;
	private final String textoLegible;

	private TipoInstitucion(String valor, String textoLegible) {
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
	 * Busca el tipo a partir del texto guardado en la BD (por ejemplo "coleccion privada").
	 *
	 * @param valor texto del tipo
	 * @return el tipo encontrado, o null si el texto no corresponde a ningun tipo
	 */
	public static TipoInstitucion buscarPorValor(String valor) {
		for (TipoInstitucion tipo : TipoInstitucion.values()) {
			if (tipo.getValor().equals(valor)) {
				return tipo;
			}
		}
		return null;
	}

	/**
	 * Busca el tipo a partir de su texto legible (por ejemplo "Colección privada"), sin importar mayusculas.
	 * Sirve para aceptar del front tanto "coleccion privada" como "Colección privada".
	 *
	 * @param textoLegible texto legible del tipo
	 * @return el tipo encontrado, o null si el texto no corresponde a ningun tipo
	 */
	public static TipoInstitucion buscarPorTextoLegible(String textoLegible) {
		for (TipoInstitucion tipo : TipoInstitucion.values()) {
			if (tipo.getTextoLegible().equalsIgnoreCase(textoLegible)) {
				return tipo;
			}
		}
		return null;
	}

}