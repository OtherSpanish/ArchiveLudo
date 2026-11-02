package co.edu.unbosque.archiveludo.model;

/**
 * Estados de un proyecto de restauracion.
 * valor: texto que se guarda en la columna estado de PROYECTO_DE_RESTAURACION.
 * activo y pausado no tienen fecha de fin; finalizado si.
 */
public enum EstadoProyecto {

	ACTIVO("activo"),
	PAUSADO("pausado"),
	FINALIZADO("finalizado");

	private final String valor;

	private EstadoProyecto(String valor) {
		this.valor = valor;
	}

	public String getValor() {
		return valor;
	}

	/**
	 * Busca el estado a partir del texto guardado en la BD ("activo", "pausado" o "finalizado").
	 *
	 * @param valor texto del estado
	 * @return el estado encontrado, o null si el texto no corresponde a ningun estado
	 */
	public static EstadoProyecto buscarPorValor(String valor) {
		for (EstadoProyecto estado : EstadoProyecto.values()) {
			if (estado.getValor().equals(valor)) {
				return estado;
			}
		}
		return null;
	}

}