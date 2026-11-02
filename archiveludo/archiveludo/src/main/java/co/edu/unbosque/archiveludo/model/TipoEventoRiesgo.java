package co.edu.unbosque.archiveludo.model;

/**
 * Tipos de evento de riesgo que puede sufrir un videojuego.
 * valor: texto que se guarda en la columna tipo_evento de EVENTO_DE_RIESGO.
 * textoLegible: texto que el front muestra tal cual.
 */
public enum TipoEventoRiesgo {

	CIERRE_DE_SERVIDORES("cierre de servidores", "Cierre de servidores"),
	PERDIDA_DE_CODIGO_FUENTE("perdida de codigo fuente", "Pérdida de código fuente"),
	RETIRO_DE_TIENDA_DIGITAL("retiro de tienda digital", "Retiro de tienda digital"),
	DETERIORO_DE_MEDIOS("deterioro de medios", "Deterioro de medios"),
	DISPUTA_LEGAL("disputa legal", "Disputa legal");

	private final String valor;
	private final String textoLegible;

	private TipoEventoRiesgo(String valor, String textoLegible) {
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
	 * Busca el tipo de evento a partir del texto guardado en la BD (por ejemplo "disputa legal").
	 *
	 * @param valor texto del tipo de evento
	 * @return el tipo encontrado, o null si el texto no corresponde a ningun tipo
	 */
	public static TipoEventoRiesgo buscarPorValor(String valor) {
		for (TipoEventoRiesgo tipo : TipoEventoRiesgo.values()) {
			if (tipo.getValor().equals(valor)) {
				return tipo;
			}
		}
		return null;
	}

	/**
	 * Busca el tipo a partir de su texto legible (por ejemplo "Pérdida de código fuente"), sin importar mayusculas.
	 * Sirve para aceptar del front tanto "perdida de codigo fuente" como "Pérdida de código fuente".
	 *
	 * @param textoLegible texto legible del tipo de evento
	 * @return el tipo encontrado, o null si el texto no corresponde a ningun tipo
	 */
	public static TipoEventoRiesgo buscarPorTextoLegible(String textoLegible) {
		for (TipoEventoRiesgo tipo : TipoEventoRiesgo.values()) {
			if (tipo.getTextoLegible().equalsIgnoreCase(textoLegible)) {
				return tipo;
			}
		}
		return null;
	}

}