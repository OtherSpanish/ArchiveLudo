package co.edu.unbosque.archiveludo.model;

/**
 * Resultados posibles del informe final de un proyecto de restauracion.
 * valor: texto que viaja en el JSON.
 * textoLegible: texto que se escribe en el PDF.
 */
public enum ResultadoInforme {

	EXITOSO("exitoso", "Exitoso"),
	PARCIAL("parcial", "Parcial"),
	FRACASO("fracaso", "Fracaso");

	private final String valor;
	private final String textoLegible;

	private ResultadoInforme(String valor, String textoLegible) {
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
	 * Busca el resultado a partir de su texto (por ejemplo "parcial").
	 *
	 * @param valor texto del resultado
	 * @return el resultado encontrado, o null si el texto no corresponde a ninguno
	 */
	public static ResultadoInforme buscarPorValor(String valor) {
		for (ResultadoInforme resultado : ResultadoInforme.values()) {
			if (resultado.getValor().equals(valor)) {
				return resultado;
			}
		}
		return null;
	}

}