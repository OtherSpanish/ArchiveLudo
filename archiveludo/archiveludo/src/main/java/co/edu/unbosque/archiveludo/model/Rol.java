package co.edu.unbosque.archiveludo.model;

/**
 * Roles del sistema. Cada rol corresponde a una especializacion de USUARIO
 * (tablas ADMINISTRADOR, CURADOR y CONSULTA).
 * valor: texto que viaja en el JSON hacia el front.
 * autoridad: nombre que usa Spring Security en hasRole(...).
 */
public enum Rol {

	CONSULTA("consulta", "ROLE_CONSULTA"),
	CURADOR("curador", "ROLE_CURADOR"),
	ADMINISTRADOR("administrador", "ROLE_ADMINISTRADOR");

	private final String valor;
	private final String autoridad;

	private Rol(String valor, String autoridad) {
		this.valor = valor;
		this.autoridad = autoridad;
	}

	public String getValor() {
		return valor;
	}

	public String getAutoridad() {
		return autoridad;
	}

	/**
	 * Busca el rol a partir de su texto (por ejemplo "curador").
	 *
	 * @param valor texto del rol
	 * @return el rol encontrado, o null si el texto no corresponde a ningun rol
	 */
	public static Rol buscarPorValor(String valor) {
		for (Rol rol : Rol.values()) {
			if (rol.getValor().equals(valor)) {
				return rol;
			}
		}
		return null;
	}

}