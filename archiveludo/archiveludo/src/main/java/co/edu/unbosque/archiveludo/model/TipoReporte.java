package co.edu.unbosque.archiveludo.model;

/**
 * Tipos de reporte que ofrece GET /api/reportes/{tipo}.
 * valor: texto que llega en la URL. titulo: titulo de la tabla que recibe el front.
 */
public enum TipoReporte {

	ESTATUS("estatus", "Reporte de estatus legal"),
	EVENTOS("eventos", "Reporte de eventos de riesgo"),
	COPIAS("copias", "Reporte de copias");

	private final String valor;
	private final String titulo;

	private TipoReporte(String valor, String titulo) {
		this.valor = valor;
		this.titulo = titulo;
	}

	public String getValor() {
		return valor;
	}

	public String getTitulo() {
		return titulo;
	}

	/**
	 * @param valor texto del tipo (por ejemplo "eventos")
	 * @return el tipo encontrado, o null si el texto no corresponde a ninguno
	 */
	public static TipoReporte buscarPorValor(String valor) {
		for (TipoReporte tipo : TipoReporte.values()) {
			if (tipo.getValor().equals(valor)) {
				return tipo;
			}
		}
		return null;
	}

}