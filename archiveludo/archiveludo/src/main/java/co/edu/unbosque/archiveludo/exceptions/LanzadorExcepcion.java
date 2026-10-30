package co.edu.unbosque.archiveludo.exceptions;

/**
 * Lnazador de las excepciones propias de ArchiveLudo.
 * Los services llaman a estos metodos; si la condición no se cumple, se lanza
 * la excepción correspondiente y mediante try catch en el controller son convertidas en respuesta HTTP.
 */
public class LanzadorExcepcion {

	/**
	 * Verifica que un id recibido por la URL sea valido.
	 *
	 * @param id            id a verificar
	 * @param nombreRecurso nombre del recurso para el mensaje (por ejemplo "videojuego")
	 * @throws IdInvalidoException si el id es nulo o menor o igual a cero
	 */
	public static void verificarIdValido(Long id, String nombreDueno) {
		if (id == null || id <= 0) {
			throw new IdInvalidoException(nombreDueno);
		}
	}
	/**
	 * Verifica que el correo ingresado sea valido
	 * @param entrada
	 * @throws CorreoElectronicoException
	 */
	public static void verificarCorreoValido(String entrada) throws CorreoElectronicoException {
		if (entrada == null || entrada.trim().isEmpty()) {
			throw new CorreoElectronicoException();
		}

		entrada = entrada.trim();

		String patronDelCorreo = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(?:[A-Za-z]{2,})$";

		if (!entrada.matches(patronDelCorreo)) {
			throw new CorreoElectronicoException();
		}

		if (!(entrada.endsWith("@unbosque.edu.co") || entrada.endsWith("@gmail.com")
				|| entrada.endsWith("@hotmail.com"))) {
			throw new CorreoElectronicoException();
		}
	}
	/**
	 * Valida que la contraseña cumpla con los parametros de contener mayúscula, minúscula y número
	 * @param entrada
	 * @throws ContraseniaUsuarioException
	 */
	public static void verificarContrasenaValida(String entrada) throws ContraseniaUsuarioException {
		if (entrada == null || entrada.isEmpty()) {
			throw new ContraseniaUsuarioException();
		}

		if (entrada.length() < 8) {
			throw new ContraseniaUsuarioException();
		}

		boolean tieneMayuscula = false;
		boolean tieneMinuscula = false;
		boolean tieneNumero = false;

		for (char c : entrada.toCharArray()) {
			if (Character.isUpperCase(c)) {
				tieneMayuscula = true;
			} else if (Character.isLowerCase(c)) {
				tieneMinuscula = true;
			} else if (Character.isDigit(c)) {
				tieneNumero = true;
			}
		}

		if (!(tieneMayuscula && tieneMinuscula && tieneNumero)) {
			throw new ContraseniaUsuarioException();
		}
	}
	/**
	 * Verifica que el nombre de usuario tenga entre 4-14 caracteres y contenga una letra
	 * @param entrada
	 * @throws NombreUsuarioException
	 */
	public static void verificarNombreUsuario(String entrada) throws NombreUsuarioException {
		if (entrada == null || entrada.trim().isEmpty()) {
			throw new NombreUsuarioException();
		}

		if (entrada.length() < 4 || entrada.length() > 14) {
			throw new NombreUsuarioException();
		}

		boolean tieneLetra = false;

		for (char c : entrada.toCharArray()) {
			if (Character.isLetter(c)) {
				tieneLetra = true;
			}
		}

		if (!tieneLetra) {
			throw new NombreUsuarioException();
		}
	}

}