package co.edu.unbosque.archiveludo.model;

/**
 * Estados de conservacion de un videojuego dentro del archivo.
 * valor: texto que viaja en el JSON hacia el front.
 */
public enum EstadoConservacion {

	ACTIVO("activo"),
	EN_RIESGO("en riesgo"),
	PARCIALMENTE_PERDIDO("parcialmente perdido"),
	TOTALMENTE_PERDIDO("totalmente perdido"),
	RESTAURADO("restaurado");

	private final String valor;

	private EstadoConservacion(String valor) {
		this.valor = valor;
	}

	public String getValor() {
		return valor;
	}

	/**
	 * Busca el estado de conservacion a partir de su texto (por ejemplo "en riesgo").
	 *
	 * @param valor texto del estado
	 * @return el estado encontrado, o null si el texto no corresponde a ningun estado
	 */
	public static EstadoConservacion buscarPorValor(String valor) {
		for (EstadoConservacion estado : EstadoConservacion.values()) {
			if (estado.getValor().equals(valor)) {
				return estado;
			}
		}
		return null;
	}

}