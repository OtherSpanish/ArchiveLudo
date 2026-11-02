package co.edu.unbosque.archiveludo.model;

/**
 * Estado actual de un estudio desarrollador.
 * valor: texto del ENUM de la columna estado_actual de ESTUDIO y que viaja en el JSON.
 */
public enum EstadoEstudio {

	ACTIVO("activo"),
	DESAPARECIDO("desaparecido");

	private final String valor;

	private EstadoEstudio(String valor) {
		this.valor = valor;
	}

	public String getValor() {
		return valor;
	}

	/**
	 * Busca el estado a partir de su texto (por ejemplo "desaparecido").
	 *
	 * @param valor texto del estado
	 * @return el estado encontrado, o null si el texto no corresponde a ningun estado
	 */
	public static EstadoEstudio buscarPorValor(String valor) {
		for (EstadoEstudio estado : EstadoEstudio.values()) {
			if (estado.getValor().equals(valor)) {
				return estado;
			}
		}
		return null;
	}

}