package co.edu.unbosque.archiveludo.exceptions;

import java.time.LocalDate;

import co.edu.unbosque.archiveludo.entity.CompaniaEditora;
import co.edu.unbosque.archiveludo.entity.Estudio;
import co.edu.unbosque.archiveludo.entity.Institucion;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.model.EstadoConservacion;
import co.edu.unbosque.archiveludo.model.EstadoEstudio;
import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.model.Rol;
import co.edu.unbosque.archiveludo.model.TipoInstitucion;

/**
 * Lanzador de las excepciones propias de ArchiveLudo.
 * Los services llaman a estos metodos; si la condicion no se cumple, se lanza
 * la excepcion correspondiente y mediante try catch en el controller son convertidas en respuesta HTTP.
 */
public class LanzadorException {

	/** Anio minimo aceptado como anio de lanzamiento de un videojuego. */
	private static final int ANIO_MINIMO_LANZAMIENTO = 1950;

	/** Anio minimo aceptado como anio de fundacion de un estudio. */
	private static final int ANIO_MINIMO_FUNDACION = 1800;

	/** Cantidad de elementos por pagina cuando no se envia el parametro limite. */
	private static final int LIMITE_POR_DEFECTO = 10;

	/** Cantidad maxima de elementos que se pueden pedir en una pagina. */
	private static final int LIMITE_MAXIMO = 50;

	/** Rango de digitos de un telefono (la columna es NUMERIC(15, 0)). */
	private static final int DIGITOS_MINIMOS_TELEFONO = 7;
	private static final int DIGITOS_MAXIMOS_TELEFONO = 15;

	/**
	 * Verifica que un id recibido por la URL sea valido.
	 *
	 * @param id          id a verificar
	 * @param nombreDueno nombre del recurso para el mensaje (por ejemplo "videojuego")
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

	/**
	 * Verifica que un campo de texto obligatorio venga con contenido.
	 *
	 * @param valor       valor recibido
	 * @param nombreCampo nombre del campo para el mensaje (por ejemplo "correo")
	 * @throws CampoObligatorioException si el valor es nulo o esta vacio
	 */
	public static void verificarCampoObligatorio(String valor, String nombreCampo) {
		if (valor == null || valor.trim().isEmpty()) {
			throw new CampoObligatorioException(nombreCampo);
		}
	}

	/**
	 * Verifica el resultado de comparar las credenciales de inicio de sesion.
	 *
	 * @param credencialesCorrectas true si el correo existe y la contrasena coincide
	 * @throws CredencialInvalidaException si las credenciales no son correctas
	 */
	public static void verificarCredencialesCorrectas(boolean credencialesCorrectas) {
		if (!credencialesCorrectas) {
			throw new CredencialInvalidaException();
		}
	}

	/**
	 * Convierte el id que llega por la URL (como texto) a numero y verifica que sea valido.
	 * Se recibe como String para que un valor como "abc" llegue hasta aqui
	 * y lo podamos atrapar nosotros, en vez de que Spring lo rechace antes del controller.
	 *
	 * @param idTexto     id tal como llego en la URL
	 * @param nombreDueno nombre del recurso para el mensaje (por ejemplo "videojuego")
	 * @return el id convertido a numero
	 * @throws IdInvalidoException si el id esta vacio, no es numerico o es menor o igual a cero
	 */
	public static long convertirId(String idTexto, String nombreDueno) {
		if (idTexto == null || idTexto.trim().isEmpty()) {
			throw new IdInvalidoException(nombreDueno);
		}

		long id;
		try {
			id = Long.parseLong(idTexto.trim());
		} catch (NumberFormatException e) {
			throw new IdInvalidoException(nombreDueno);
		}

		verificarIdValido(id, nombreDueno);
		return id;
	}

	/**
	 * Verifica que el videojuego buscado exista.
	 *
	 * @param videojuego   resultado de la busqueda en el repository
	 * @param idVideojuego id que se busco (para el mensaje)
	 * @throws VideojuegoNoEncontradoException si el videojuego es null
	 */
	public static void verificarVideojuegoExiste(Videojuego videojuego, long idVideojuego) {
		if (videojuego == null) {
			throw new VideojuegoNoEncontradoException(idVideojuego);
		}
	}

	/**
	 * Convierte el texto del rol (por ejemplo "curador") al enum Rol.
	 *
	 * @param textoRol rol tal como llego en el cuerpo de la peticion
	 * @return el rol correspondiente
	 * @throws RolInvalidoException si el rol viene vacio o no es administrador, curador ni consulta
	 */
	public static Rol convertirRol(String textoRol) {
		if (textoRol == null || textoRol.trim().isEmpty()) {
			throw new RolInvalidoException();
		}

		Rol rol = Rol.buscarPorValor(textoRol.trim().toLowerCase());
		if (rol == null) {
			throw new RolInvalidoException();
		}
		return rol;
	}

	/**
	 * Verifica que el correo no este registrado por otro usuario.
	 *
	 * @param correoYaExiste resultado de consultar el correo en el repository
	 * @param correo         correo consultado (para el mensaje)
	 * @throws CorreoYaRegistradoException si el correo ya existe
	 */
	public static void verificarCorreoNoRegistrado(boolean correoYaExiste, String correo) {
		if (correoYaExiste) {
			throw new CorreoYaRegistradoException(correo);
		}
	}

	/**
	 * Convierte el anio que llega por la URL (como texto) a numero y verifica que este en un rango valido
	 * (entre 1950 y el anio actual).
	 *
	 * @param anioTexto anio tal como llego en la URL
	 * @return el anio convertido a numero
	 * @throws AnioInvalidoException si viene vacio, no es numerico o esta fuera del rango
	 */
	public static int convertirAnio(String anioTexto) {
		int anioMaximo = LocalDate.now().getYear();

		if (anioTexto == null || anioTexto.trim().isEmpty()) {
			throw new AnioInvalidoException(ANIO_MINIMO_LANZAMIENTO, anioMaximo);
		}

		int anio;
		try {
			anio = Integer.parseInt(anioTexto.trim());
		} catch (NumberFormatException e) {
			throw new AnioInvalidoException(ANIO_MINIMO_LANZAMIENTO, anioMaximo);
		}

		verificarAnioLanzamiento(anio);
		return anio;
	}

	/**
	 * Verifica que el anio de lanzamiento (que llega en el cuerpo de la peticion) este entre 1950
	 * y el anio actual. Si no se envio, llega en 0 y tambien es invalido.
	 *
	 * @param anioLanzamiento anio a verificar
	 * @throws AnioInvalidoException si esta fuera del rango
	 */
	public static void verificarAnioLanzamiento(int anioLanzamiento) {
		int anioMaximo = LocalDate.now().getYear();

		if (anioLanzamiento < ANIO_MINIMO_LANZAMIENTO || anioLanzamiento > anioMaximo) {
			throw new AnioInvalidoException(ANIO_MINIMO_LANZAMIENTO, anioMaximo);
		}
	}

	/**
	 * Convierte el texto del estado de conservacion al enum EstadoConservacion.
	 * Acepta "en riesgo" o "en_riesgo", sin importar mayusculas.
	 *
	 * @param textoEstado estado tal como llego en la URL
	 * @return el estado correspondiente
	 * @throws EstadoConservacionInvalidoException si viene vacio o no es uno de los 5 estados
	 */
	public static EstadoConservacion convertirEstadoConservacion(String textoEstado) {
		if (textoEstado == null || textoEstado.trim().isEmpty()) {
			throw new EstadoConservacionInvalidoException();
		}

		String textoNormalizado = textoEstado.trim().toLowerCase().replace('_', ' ');
		EstadoConservacion estado = EstadoConservacion.buscarPorValor(textoNormalizado);
		if (estado == null) {
			throw new EstadoConservacionInvalidoException();
		}
		return estado;
	}

	/**
	 * Convierte el numero de pagina que llega por la URL. Si no se envia, es la pagina 0.
	 *
	 * @param paginaTexto pagina tal como llego en la URL
	 * @return la pagina convertida a numero (desde 0)
	 * @throws PaginacionInvalidaException si no es numerico o es negativo
	 */
	public static int convertirPagina(String paginaTexto) {
		if (paginaTexto == null || paginaTexto.trim().isEmpty()) {
			return 0;
		}

		int pagina;
		try {
			pagina = Integer.parseInt(paginaTexto.trim());
		} catch (NumberFormatException e) {
			throw new PaginacionInvalidaException("El parametro 'pagina' debe ser un numero entero desde 0");
		}

		if (pagina < 0) {
			throw new PaginacionInvalidaException("El parametro 'pagina' debe ser un numero entero desde 0");
		}
		return pagina;
	}

	/**
	 * Convierte la cantidad de elementos por pagina que llega por la URL.
	 * Si no se envia, se usan 10.
	 *
	 * @param limiteTexto limite tal como llego en la URL
	 * @return el limite convertido a numero (entre 1 y 50)
	 * @throws PaginacionInvalidaException si no es numerico o esta fuera del rango
	 */
	public static int convertirLimite(String limiteTexto) {
		if (limiteTexto == null || limiteTexto.trim().isEmpty()) {
			return LIMITE_POR_DEFECTO;
		}

		String mensaje = "El parametro 'limite' debe ser un numero entre 1 y " + LIMITE_MAXIMO;

		int limite;
		try {
			limite = Integer.parseInt(limiteTexto.trim());
		} catch (NumberFormatException e) {
			throw new PaginacionInvalidaException(mensaje);
		}

		if (limite < 1 || limite > LIMITE_MAXIMO) {
			throw new PaginacionInvalidaException(mensaje);
		}
		return limite;
	}

	/**
	 * Convierte el texto del estado de integridad (por ejemplo "dañada") al enum EstadoIntegridad.
	 *
	 * @param textoEstado estado tal como llego en el cuerpo de la peticion
	 * @return el estado correspondiente
	 * @throws EstadoIntegridadInvalidoException si viene vacio o no es uno de los 4 estados
	 */
	public static EstadoIntegridad convertirEstadoIntegridad(String textoEstado) {
		if (textoEstado == null || textoEstado.trim().isEmpty()) {
			throw new EstadoIntegridadInvalidoException();
		}

		EstadoIntegridad estado = EstadoIntegridad.buscarPorValor(textoEstado.trim().toLowerCase());
		if (estado == null) {
			throw new EstadoIntegridadInvalidoException();
		}
		return estado;
	}

	/**
	 * Verifica que el tamanio de un archivo digital sea mayor que cero.
	 *
	 * @param tamanio tamanio en bytes
	 * @throws TamanioInvalidoException si es menor o igual a cero (o si no se envio, porque llega en 0)
	 */
	public static void verificarTamanioValido(long tamanio) {
		if (tamanio <= 0) {
			throw new TamanioInvalidoException();
		}
	}

	/**
	 * Verifica que el usuario buscado exista.
	 *
	 * @param usuario   resultado de la busqueda en el repository
	 * @param idUsuario id que se busco (para el mensaje)
	 * @throws UsuarioNoEncontradoException si el usuario es null
	 */
	public static void verificarUsuarioExiste(Usuario usuario, long idUsuario) {
		if (usuario == null) {
			throw new UsuarioNoEncontradoException(idUsuario);
		}
	}

	/**
	 * Verifica que nada dependa del registro antes de eliminarlo.
	 * La usan todos los DELETE.
	 *
	 * @param tieneRegistrosAsociados resultado de contar los registros que dependen de el
	 * @param recursoConArticulo      por ejemplo "el usuario" o "la copia" (para el mensaje)
	 * @param id                      id del registro que se quiere eliminar
	 * @throws RegistroAsociadoException si tiene registros asociados
	 */
	public static void verificarSinRegistrosAsociados(boolean tieneRegistrosAsociados, String recursoConArticulo,
			long id) {
		if (tieneRegistrosAsociados) {
			throw new RegistroAsociadoException(recursoConArticulo, id);
		}
	}

	/**
	 * Verifica que el estudio buscado exista.
	 *
	 * @param estudio   resultado de la busqueda en el repository
	 * @param idEstudio id que se busco (para el mensaje)
	 * @throws EstudioNoEncontradoException si el estudio es null
	 */
	public static void verificarEstudioExiste(Estudio estudio, long idEstudio) {
		if (estudio == null) {
			throw new EstudioNoEncontradoException(idEstudio);
		}
	}

	/**
	 * Convierte el texto del estado de un estudio ("activo" o "desaparecido") al enum EstadoEstudio.
	 *
	 * @param textoEstado estado tal como llego en el cuerpo de la peticion
	 * @return el estado correspondiente
	 * @throws DatosEstudioInvalidosException si viene vacio o no es activo ni desaparecido
	 */
	public static EstadoEstudio convertirEstadoEstudio(String textoEstado) {
		String mensaje = "El estado actual del estudio debe ser activo o desaparecido";

		if (textoEstado == null || textoEstado.trim().isEmpty()) {
			throw new DatoEstudioInvalidoException(mensaje);
		}

		EstadoEstudio estado = EstadoEstudio.buscarPorValor(textoEstado.trim().toLowerCase());
		if (estado == null) {
			throw new DatoEstudioInvalidoException(mensaje);
		}
		return estado;
	}

	/**
	 * Verifica los anios de un estudio. Son las mismas reglas de los CHECK de la tabla ESTUDIO:
	 * - el anio de fundacion es obligatorio y esta entre 1800 y el anio actual;
	 * - un estudio activo NO tiene anio de cierre;
	 * - un estudio desaparecido SI tiene anio de cierre;
	 * - el anio de cierre no es anterior al de fundacion ni posterior al anio actual.
	 *
	 * @param estado        estado actual del estudio
	 * @param anioFundacion anio de fundacion
	 * @param anioCierre    anio de cierre (null si no tiene)
	 * @throws DatoEstudioInvalidoException si se incumple alguna regla
	 */
	public static void verificarAniosEstudio(EstadoEstudio estado, Integer anioFundacion, Integer anioCierre) {
		int anioActual = LocalDate.now().getYear();

		if (anioFundacion == null) {
			throw new DatoEstudioInvalidoException("El año de fundacion es obligatorio");
		}
		if (anioFundacion < ANIO_MINIMO_FUNDACION || anioFundacion > anioActual) {
			throw new DatoEstudioInvalidoException(
					"El año de fundacion debe estar entre " + ANIO_MINIMO_FUNDACION + " y " + anioActual);
		}

		if (estado == EstadoEstudio.ACTIVO && anioCierre != null) {
			throw new DatoEstudioInvalidoException("Un estudio activo no puede tener año de cierre");
		}
		if (estado == EstadoEstudio.DESAPARECIDO && anioCierre == null) {
			throw new DatoEstudioInvalidoException("Un estudio desaparecido debe tener año de cierre");
		}

		if (anioCierre != null) {
			if (anioCierre < anioFundacion) {
				throw new DatoEstudioInvalidoException(
						"El año de cierre no puede ser anterior al año de fundacion");
			}
			if (anioCierre > anioActual) {
				throw new DatoEstudioInvalidoException("El año de cierre no puede ser posterior a " + anioActual);
			}
		}
	}

	/**
	 * Verifica que la compania editora buscada exista.
	 *
	 * @param companiaEditora resultado de la busqueda en el repository
	 * @param idEditora       id que se busco (para el mensaje)
	 * @throws CompaniaEditoraNoEncontradaException si la editora es null
	 */
	public static void verificarCompaniaEditoraExiste(CompaniaEditora companiaEditora, long idEditora) {
		if (companiaEditora == null) {
			throw new CompaniaEditoraNoEncontradaException(idEditora);
		}
	}

	/**
	 * Verifica que la institucion buscada exista.
	 *
	 * @param institucion   resultado de la busqueda en el repository
	 * @param idInstitucion id que se busco (para el mensaje)
	 * @throws InstitucionNoEncontradaException si la institucion es null
	 */
	public static void verificarInstitucionExiste(Institucion institucion, long idInstitucion) {
		if (institucion == null) {
			throw new InstitucionNoEncontradaException(idInstitucion);
		}
	}

	/**
	 * Convierte el tipo de institucion al enum TipoInstitucion.
	 * Acepta el valor de la BD ("coleccion privada") o el texto legible ("Colección privada").
	 *
	 * @param textoTipo tipo tal como llego en el cuerpo de la peticion
	 * @return el tipo correspondiente
	 * @throws DatoInstitucionInvalidoException si viene vacio o no es uno de los 4 tipos
	 */
	public static TipoInstitucion convertirTipoInstitucion(String textoTipo) {
		String mensaje = "El tipo de institucion debe ser: museo, universidad, archivo o coleccion privada";

		if (textoTipo == null || textoTipo.trim().isEmpty()) {
			throw new DatoInstitucionInvalidoException(mensaje);
		}

		TipoInstitucion tipo = TipoInstitucion.buscarPorValor(textoTipo.trim().toLowerCase());
		if (tipo == null) {
			tipo = TipoInstitucion.buscarPorTextoLegible(textoTipo.trim());
		}
		if (tipo == null) {
			throw new DatoInstitucionInvalidoException(mensaje);
		}
		return tipo;
	}

	/**
	 * Verifica que un telefono tenga entre 7 y 15 digitos (la columna es NUMERIC(15, 0)).
	 *
	 * @param telefono telefono a verificar
	 * @throws DatoInstitucionInvalidoException si es nulo, negativo o no tiene entre 7 y 15 digitos
	 */
	public static void verificarTelefonoValido(Integer telefono) {
		String mensaje = "Cada telefono debe ser un numero de entre " + DIGITOS_MINIMOS_TELEFONO + " y "
				+ DIGITOS_MAXIMOS_TELEFONO + " digitos, sin el signo +";

		if (telefono == null || telefono <= 0) {
			throw new DatoInstitucionInvalidoException(mensaje);
		}

		int cantidadDigitos = String.valueOf(telefono).length();
		if (cantidadDigitos < DIGITOS_MINIMOS_TELEFONO || cantidadDigitos > DIGITOS_MAXIMOS_TELEFONO) {
			throw new DatoInstitucionInvalidoException(mensaje);
		}
	}

	/**
	 * Verifica el formato de un correo de institucion. A diferencia de verificarCorreoValido
	 * (usuarios), aqui se acepta cualquier dominio: contacto@museo.org es valido.
	 *
	 * @param correo correo a verificar
	 * @throws DatoInstitucionInvalidoException si esta vacio o no tiene formato de correo
	 */
	public static void verificarCorreoInstitucionValido(String correo) {
		String patronDelCorreo = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(?:[A-Za-z]{2,})$";

		if (correo == null || !correo.trim().matches(patronDelCorreo)) {
			throw new DatoInstitucionInvalidoException("El correo '" + correo + "' no tiene un formato valido");
		}
	}

	/**
	 * Verifica que la institucion tenga al menos un telefono y al menos un correo
	 * (atributos multivaluados obligatorios). La BD no puede exigirlo con una restriccion,
	 * por eso se valida aqui.
	 *
	 * @param cantidadTelefonos cantidad de telefonos validos (sin repetidos)
	 * @param cantidadCorreos   cantidad de correos validos (sin repetidos)
	 * @throws DatoInstitucionInvalidoException si no tiene telefonos o no tiene correos
	 */
	public static void verificarInstitucionConContacto(int cantidadTelefonos, int cantidadCorreos) {
		if (cantidadTelefonos == 0) {
			throw new DatoInstitucionInvalidoException("La institucion debe tener al menos un telefono");
		}
		if (cantidadCorreos == 0) {
			throw new DatoInstitucionInvalidoException("La institucion debe tener al menos un correo");
		}
	}

}