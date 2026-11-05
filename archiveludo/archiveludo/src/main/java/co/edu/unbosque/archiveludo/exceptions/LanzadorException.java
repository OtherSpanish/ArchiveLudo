package co.edu.unbosque.archiveludo.exceptions;

import java.time.LocalDate;

import co.edu.unbosque.archiveludo.model.ResultadoInforme;
import co.edu.unbosque.archiveludo.entity.CompaniaEditora;
import co.edu.unbosque.archiveludo.entity.EstatusLegal;
import co.edu.unbosque.archiveludo.entity.Estudio;
import co.edu.unbosque.archiveludo.entity.EventoDeRiesgo;
import co.edu.unbosque.archiveludo.entity.Experto;
import co.edu.unbosque.archiveludo.entity.Institucion;
import co.edu.unbosque.archiveludo.entity.Participacion;
import co.edu.unbosque.archiveludo.entity.ProyectoDeRestauracion;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.model.EstadoConservacion;
import co.edu.unbosque.archiveludo.model.EstadoEstudio;
import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.model.EstadoProyecto;
import co.edu.unbosque.archiveludo.model.Rol;
import co.edu.unbosque.archiveludo.model.TipoEstatusLegal;
import co.edu.unbosque.archiveludo.model.TipoEventoRiesgo;
import co.edu.unbosque.archiveludo.model.TipoInstitucion;
import co.edu.unbosque.archiveludo.entity.Copia;
import co.edu.unbosque.archiveludo.entity.Custodia;
import co.edu.unbosque.archiveludo.model.TipoCopia;

/**
 * Lanzador de las excepciones propias de ArchiveLudo.
 * Los services llaman a estos metodos; si la condicion no se cumple, se lanza
 * la excepcion correspondiente y mediante try catch en el controller son convertidas en respuesta HTTP.
 */
public class LanzadorException {
	/** Anio minimo aceptado como anio de lanzamiento de un videojuego. */
	private static final int ANIO_MINIMO_LANZAMIENTO = 1950;

	public static final int LONGITUD_MAXIMA_RESUMEN_INFORME = 2000;
	/** Anio minimo aceptado como anio de fundacion de un estudio. */
	private static final int ANIO_MINIMO_FUNDACION = 1800;

	/** Tamanio maximo de una imagen de videojuego: 5 MB. */
	private static final long TAMANIO_MAXIMO_IMAGEN = 5L * 1024 * 1024;
	
	/** Cantidad de elementos por pagina cuando no se envia el parametro limite. */
	private static final int LIMITE_POR_DEFECTO = 10;

	/** Cantidad maxima de elementos que se pueden pedir en una pagina. */
	private static final int LIMITE_MAXIMO = 50;

	/** Rango de digitos de un telefono (la columna es NUMERIC(15, 0)). */
	private static final int DIGITOS_MINIMOS_TELEFONO = 7;
	private static final int DIGITOS_MAXIMOS_TELEFONO = 15;

	/** Longitudes maximas de EVENTO_DE_RIESGO (descripcion VARCHAR(500), fuente VARCHAR(200)). */
	private static final int LONGITUD_MAXIMA_DESCRIPCION_EVENTO = 500;
	private static final int LONGITUD_MAXIMA_FUENTE_EVENTO = 200;
	
	/** Longitud maxima de CUSTODIA.condicion_almacenamiento (VARCHAR(200)). */
	private static final int LONGITUD_MAXIMA_CONDICION_CUSTODIA = 200;

	/** Longitudes maximas de las columnas de EXPERTO. */
	public static final int LONGITUD_MAXIMA_NOMBRE_EXPERTO = 60;
	public static final int LONGITUD_MAXIMA_ESPECIALIDAD_EXPERTO = 100;
	public static final int LONGITUD_MAXIMA_AFILIACION_EXPERTO = 150;
	private static final int LONGITUD_MAXIMA_CORREO_EXPERTO = 150;
	
	/** Longitudes maximas de PROYECTO_DE_RESTAURACION y PARTICIPACION. */
	public static final int LONGITUD_MAXIMA_NOMBRE_PROYECTO = 150;
	public static final int LONGITUD_MAXIMA_DESCRIPCION_PROYECTO = 500;
	public static final int LONGITUD_MAXIMA_HERRAMIENTA_PROYECTO = 100;
	public static final int LONGITUD_MAXIMA_ROL_EXPERTO = 80;
	
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
	public static void verificarTelefonoValido(Long telefono) {
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
	

	/**
	 * Verifica que el estatus legal buscado exista.
	 *
	 * @param estatusLegal resultado de la busqueda en el repository
	 * @param idEstatus    id que se busco (para el mensaje)
	 * @throws EstatusLegalNoEncontradoException si el estatus es null
	 */
	public static void verificarEstatusLegalExiste(EstatusLegal estatusLegal, long idEstatus) {
		if (estatusLegal == null) {
			throw new EstatusLegalNoEncontradoException(idEstatus);
		}
	}

	/**
	 * Convierte el texto del estatus legal al enum TipoEstatusLegal.
	 * Acepta el valor de la BD ("dominio publico") o el texto legible ("Dominio público").
	 *
	 * @param textoEstatus estatus tal como llego en el cuerpo de la peticion
	 * @return el tipo correspondiente
	 * @throws DatosEstatusLegalInvalidosException si viene vacio o no es uno de los 4 estatus
	 */
	public static TipoEstatusLegal convertirTipoEstatusLegal(String textoEstatus) {
		String mensaje = "El estatus legal debe ser: protegido, dominio publico, huerfano o abandonware";

		if (textoEstatus == null || textoEstatus.trim().isEmpty()) {
			throw new DatoEstatusLegalInvalidoException(mensaje);
		}

		TipoEstatusLegal tipo = TipoEstatusLegal.buscarPorValor(textoEstatus.trim().toLowerCase());
		if (tipo == null) {
			tipo = TipoEstatusLegal.buscarPorValor(textoEstatus.trim());
		}
		if (tipo == null) {
			throw new DatoEstatusLegalInvalidoException(mensaje);
		}
		return tipo;
	}

	/**
	 * Verifica la fecha de determinacion de un estatus legal: es obligatoria y no puede ser futura.
	 *
	 * @param fechaDeterminacion fecha a verificar
	 * @throws DatoEstatusLegalInvalidoException si es nula o posterior a hoy
	 */
	public static void verificarFechaDeterminacion(LocalDate fechaDeterminacion) {
		if (fechaDeterminacion == null) {
			throw new DatoEstatusLegalInvalidoException(
					"La fecha de determinacion es obligatoria (formato yyyy-MM-dd)");
		}
		if (fechaDeterminacion.isAfter(LocalDate.now())) {
			throw new DatoEstatusLegalInvalidoException("La fecha de determinacion no puede ser futura");
		}
	}
	
	/**
	 * Verifica que el evento de riesgo buscado exista.
	 *
	 * @param eventoDeRiesgo resultado de la busqueda en el repository
	 * @param idEvento       id que se busco (para el mensaje)
	 * @throws EventoDeRiesgoNoEncontradoException si el evento es null
	 */
	public static void verificarEventoDeRiesgoExiste(EventoDeRiesgo eventoDeRiesgo, long idEvento) {
		if (eventoDeRiesgo == null) {
			throw new EventoDeRiesgoNoEncontradoException(idEvento);
		}
	}

	/**
	 * Convierte el texto del tipo de evento al enum TipoEventoRiesgo.
	 * Acepta el valor de la BD ("perdida de codigo fuente") o el texto legible ("Pérdida de código fuente").
	 *
	 * @param textoTipo tipo tal como llego en el cuerpo de la peticion
	 * @return el tipo correspondiente
	 * @throws DatosEventoRiesgoInvalidosException si viene vacio o no es uno de los 5 tipos
	 */
	public static TipoEventoRiesgo convertirTipoEventoRiesgo(String textoTipo) {
		String mensaje = "El tipo de evento debe ser: cierre de servidores, perdida de codigo fuente, "
				+ "retiro de tienda digital, deterioro de medios o disputa legal";

		if (textoTipo == null || textoTipo.trim().isEmpty()) {
			throw new DatoEventoRiesgoInvalidoException(mensaje);
		}

		TipoEventoRiesgo tipo = TipoEventoRiesgo.buscarPorValor(textoTipo.trim().toLowerCase());
		if (tipo == null) {
			tipo = TipoEventoRiesgo.buscarPorTextoLegible(textoTipo.trim());
		}
		if (tipo == null) {
			throw new DatoEventoRiesgoInvalidoException(mensaje);
		}
		return tipo;
	}

	/**
	 * Verifica la fecha de un evento de riesgo: es obligatoria y no puede ser futura.
	 *
	 * @param fechaEvento fecha a verificar
	 * @throws DatosEventoRiesgoInvalidosException si es nula o posterior a hoy
	 */
	public static void verificarFechaEvento(LocalDate fechaEvento) {
		if (fechaEvento == null) {
			throw new DatoEventoRiesgoInvalidoException("La fecha del evento es obligatoria (formato yyyy-MM-dd)");
		}
		if (fechaEvento.isAfter(LocalDate.now())) {
			throw new DatoEventoRiesgoInvalidoException("La fecha del evento no puede ser futura");
		}
	}

	/**
	 * Verifica la descripcion de un evento de riesgo: es obligatoria y cabe en la columna.
	 *
	 * @param descripcion descripcion a verificar
	 * @throws DatosEventoRiesgoInvalidosException si esta vacia o supera el maximo de caracteres
	 */
	public static void verificarDescripcionEvento(String descripcion) {
		if (descripcion == null || descripcion.trim().isEmpty()) {
			throw new DatoEventoRiesgoInvalidoException("La descripcion del evento es obligatoria");
		}
		if (descripcion.trim().length() > LONGITUD_MAXIMA_DESCRIPCION_EVENTO) {
			throw new DatoEventoRiesgoInvalidoException(
					"La descripcion del evento no puede superar " + LONGITUD_MAXIMA_DESCRIPCION_EVENTO + " caracteres");
		}
	}

	/**
	 * Verifica la fuente de un evento de riesgo. Es opcional: solo se revisa que quepa en la columna.
	 *
	 * @param fuente fuente a verificar (puede ser null)
	 * @throws DatosEventoRiesgoInvalidosException si supera el maximo de caracteres
	 */
	public static void verificarFuenteEvento(String fuente) {
		if (fuente != null && fuente.trim().length() > LONGITUD_MAXIMA_FUENTE_EVENTO) {
			throw new DatoEventoRiesgoInvalidoException(
					"La fuente del evento no puede superar " + LONGITUD_MAXIMA_FUENTE_EVENTO + " caracteres");
		}
	}

	/**
	 * Verifica que la copia buscada exista.
	 *
	 * @param copia   resultado de la busqueda en el repository
	 * @param idCopia id que se busco (para el mensaje)
	 * @throws CopiaNoEncontradaException si la copia es null
	 */
	public static void verificarCopiaExiste(Copia copia, long idCopia) {
		if (copia == null) {
			throw new CopiaNoEncontradaException(idCopia);
		}
	}

	/**
	 * Verifica que al actualizar una copia no se cambie su tipo.
	 * Una copia fisica sigue siendo fisica y una digital sigue siendo digital,
	 * porque cada una vive en su propia tabla hija (COPIA_FISICA o COPIA_DIGITAL).
	 *
	 * @param tipoGuardado tipo que tiene la copia en la BD
	 * @param tipoEnviado  tipo que llego en el cuerpo de la peticion
	 * @throws DatosCopiaInvalidosException si los tipos son distintos
	 */
	public static void verificarMismoTipoCopia(TipoCopia tipoGuardado, TipoCopia tipoEnviado) {
		if (tipoGuardado != tipoEnviado) {
			throw new DatoCopiaInvalidoException("No se puede cambiar el tipo de una copia (es "
					+ tipoGuardado.getValor() + "). Para cambiarlo, elimine la copia y registre una nueva");
		}
	}
	
	/**
	 * Verifica que la custodia buscada exista.
	 *
	 * @param custodia   resultado de la busqueda en el repository
	 * @param idCustodia id que se busco (para el mensaje)
	 * @throws CustodiaNoEncontradaException si la custodia es null
	 */
	public static void verificarCustodiaExiste(Custodia custodia, long idCustodia) {
		if (custodia == null) {
			throw new CustodiaNoEncontradaException(idCustodia);
		}
	}

	/**
	 * Verifica las fechas de una custodia:
	 * - la fecha de inicio es obligatoria y no puede ser futura;
	 * - la fecha de fin es opcional (null = vigente), no puede ser anterior al inicio ni futura.
	 *
	 * @param fechaInicio fecha en que la institucion recibio la copia
	 * @param fechaFin    fecha en que la dejo de custodiar (null si sigue vigente)
	 * @throws DatosCustodiaInvalidosException si se incumple alguna regla
	 */
	public static void verificarFechasCustodia(LocalDate fechaInicio, LocalDate fechaFin) {
		LocalDate hoy = LocalDate.now();

		if (fechaInicio == null) {
			throw new DatoCustodiaInvalidoException("La fecha de inicio es obligatoria (formato yyyy-MM-dd)");
		}
		if (fechaInicio.isAfter(hoy)) {
			throw new DatoCustodiaInvalidoException("La fecha de inicio no puede ser futura");
		}

		if (fechaFin != null) {
			if (fechaFin.isBefore(fechaInicio)) {
				throw new DatoCustodiaInvalidoException("La fecha de fin no puede ser anterior a la de inicio");
			}
			if (fechaFin.isAfter(hoy)) {
				throw new DatoCustodiaInvalidoException("La fecha de fin no puede ser futura");
			}
		}
	}

	/**
	 * Verifica la condicion de almacenamiento: es obligatoria y cabe en la columna.
	 *
	 * @param condicion condicion a verificar
	 * @throws DatosCustodiaInvalidosException si esta vacia o supera el maximo de caracteres
	 */
	public static void verificarCondicionAlmacenamiento(String condicion) {
		if (condicion == null || condicion.trim().isEmpty()) {
			throw new DatoCustodiaInvalidoException("La condicion de almacenamiento es obligatoria");
		}
		if (condicion.trim().length() > LONGITUD_MAXIMA_CONDICION_CUSTODIA) {
			throw new DatoCustodiaInvalidoException("La condicion de almacenamiento no puede superar "
					+ LONGITUD_MAXIMA_CONDICION_CUSTODIA + " caracteres");
		}
	}

	/**
	 * Verifica que una custodia nueva no empiece antes que la custodia vigente de la copia.
	 * Al registrar la nueva, la vigente se cierra con fecha_fin = inicio de la nueva,
	 * y una fecha de fin no puede ser anterior a su propia fecha de inicio.
	 *
	 * @param inicioNueva  fecha de inicio de la custodia nueva
	 * @param inicioVigente fecha de inicio de la custodia vigente
	 * @throws DatosCustodiaInvalidosException si la nueva empieza antes que la vigente
	 */
	public static void verificarInicioNoAnteriorAVigente(LocalDate inicioNueva, LocalDate inicioVigente) {
		if (inicioNueva.isBefore(inicioVigente)) {
			throw new DatoCustodiaInvalidoException("La nueva custodia no puede empezar antes del "
					+ inicioVigente + ", fecha de inicio de la custodia vigente");
		}
	}

	/**
	 * Verifica que el periodo de una custodia no se cruce con el de otra custodia de la misma copia.
	 *
	 * @param seCruza            resultado de comparar los dos periodos
	 * @param idCustodiaCruzada  id de la custodia con la que se cruza (para el mensaje)
	 * @throws DatosCustodiaInvalidosException si los periodos se cruzan
	 */
	public static void verificarSinCruceDeCustodias(boolean seCruza, long idCustodiaCruzada) {
		if (seCruza) {
			throw new DatoCustodiaInvalidoException("El periodo se cruza con la custodia " + idCustodiaCruzada
					+ " de la misma copia. Una copia solo puede estar en una institucion a la vez");
		}
	}
	
	/**
	 * Verifica que el cuerpo de la peticion traiga un tipo de copia reconocido.
	 * Jackson crea CopiaFisicaDTO o CopiaDigitalDTO segun "tipoCopia"; si el campo falta
	 * o trae otro valor, crea un CopiaDTO simple y el service no sabe que subtipo es.
	 *
	 * @param tipoReconocido true si el DTO es de un subtipo (fisica o digital)
	 * @throws DatosCopiaInvalidosException si no se reconocio el tipo de copia
	 */
	public static void verificarTipoCopiaValido(boolean tipoReconocido) {
		if (!tipoReconocido) {
			throw new DatoCopiaInvalidoException("El campo 'tipoCopia' es obligatorio y debe ser fisica o digital (en minuscula)");
		}
	}
	
	/**
	 * Verifica que el experto buscado exista.
	 *
	 * @param experto   resultado de la busqueda en el repository
	 * @param idExperto id que se busco (para el mensaje)
	 * @throws ExpertoNoEncontradoException si el experto es null
	 */
	public static void verificarExpertoExiste(Experto experto, long idExperto) {
		if (experto == null) {
			throw new ExpertoNoEncontradoException(idExperto);
		}
	}

	/**
	 * Verifica un campo de texto de un experto: si es obligatorio, que venga con contenido,
	 * y en todo caso, que quepa en su columna.
	 *
	 * @param valor            texto recibido (puede ser null si el campo es opcional)
	 * @param nombreCampo      nombre del campo para el mensaje (por ejemplo "primerNombreEx")
	 * @param longitudMaxima   cantidad maxima de caracteres de la columna
	 * @param esObligatorio    true si el campo no puede venir vacio
	 * @throws DatosExpertoInvalidosException si falta un campo obligatorio o el texto es demasiado largo
	 */
	public static void verificarTextoExperto(String valor, String nombreCampo, int longitudMaxima,
			boolean esObligatorio) {
		boolean estaVacio = (valor == null || valor.trim().isEmpty());

		if (esObligatorio && estaVacio) {
			throw new DatoExpertoInvalidoException("El campo '" + nombreCampo + "' es obligatorio");
		}
		if (!estaVacio && valor.trim().length() > longitudMaxima) {
			throw new DatoExpertoInvalidoException(
					"El campo '" + nombreCampo + "' no puede superar " + longitudMaxima + " caracteres");
		}
	}

	/**
	 * Verifica el correo de un experto: obligatorio, con formato de correo y que quepa en la columna.
	 * A diferencia de los usuarios, se acepta cualquier dominio.
	 *
	 * @param correo correo a verificar
	 * @throws DatosExpertoInvalidosException si esta vacio, no tiene formato de correo o es demasiado largo
	 */
	public static void verificarCorreoExperto(String correo) {
		String patronDelCorreo = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(?:[A-Za-z]{2,})$";

		if (correo == null || correo.trim().isEmpty()) {
			throw new DatoExpertoInvalidoException("El campo 'correo' es obligatorio");
		}
		if (!correo.trim().matches(patronDelCorreo)) {
			throw new DatoExpertoInvalidoException("El correo '" + correo + "' no tiene un formato valido");
		}
		if (correo.trim().length() > LONGITUD_MAXIMA_CORREO_EXPERTO) {
			throw new DatoExpertoInvalidoException(
					"El correo no puede superar " + LONGITUD_MAXIMA_CORREO_EXPERTO + " caracteres");
		}
	}

	/**
	 * Verifica que el correo no lo tenga ya otro experto (la columna correo es UNIQUE).
	 *
	 * @param correoYaExiste resultado de consultar el correo en el repository
	 * @param correo         correo consultado (para el mensaje)
	 * @throws DatosExpertoInvalidosException si el correo ya esta registrado
	 */
	public static void verificarCorreoExpertoNoRegistrado(boolean correoYaExiste, String correo) {
		if (correoYaExiste) {
			throw new DatoExpertoInvalidoException("Ya existe un experto con el correo " + correo);
		}
	}
	/**
	 * Verifica que el proyecto buscado exista.
	 *
	 * @param proyecto   resultado de la busqueda en el repository
	 * @param idProyecto id que se busco (para el mensaje)
	 * @throws ProyectoNoEncontradoException si el proyecto es null
	 */
	public static void verificarProyectoExiste(ProyectoDeRestauracion proyecto, long idProyecto) {
		if (proyecto == null) {
			throw new ProyectoNoEncontradoException(idProyecto);
		}
	}

	/**
	 * Convierte el texto del estado de un proyecto ("activo", "pausado" o "finalizado") al enum EstadoProyecto.
	 *
	 * @param textoEstado estado tal como llego en el cuerpo de la peticion
	 * @return el estado correspondiente
	 * @throws DatosProyectoInvalidosException si viene vacio o no es uno de los 3 estados
	 */
	public static EstadoProyecto convertirEstadoProyecto(String textoEstado) {
		String mensaje = "El estado del proyecto debe ser activo, pausado o finalizado";

		if (textoEstado == null || textoEstado.trim().isEmpty()) {
			throw new DatoProyectoInvalidoException(mensaje);
		}

		EstadoProyecto estado = EstadoProyecto.buscarPorValor(textoEstado.trim().toLowerCase());
		if (estado == null) {
			throw new DatoProyectoInvalidoException(mensaje);
		}
		return estado;
	}

	/**
	 * Verifica las fechas de un proyecto. Son las mismas reglas de los CHECK de la tabla:
	 * - la fecha de inicio es obligatoria y no puede ser futura;
	 * - un proyecto activo o pausado NO tiene fecha de fin (todavia no termina);
	 * - un proyecto finalizado SI tiene fecha de fin, que no es anterior al inicio ni futura.
	 *
	 * @param estado      estado del proyecto
	 * @param fechaInicio fecha de inicio
	 * @param fechaFin    fecha de fin (null si no tiene)
	 * @throws DatosProyectoInvalidosException si se incumple alguna regla
	 */
	public static void verificarFechasProyecto(EstadoProyecto estado, LocalDate fechaInicio, LocalDate fechaFin) {
		LocalDate hoy = LocalDate.now();

		if (fechaInicio == null) {
			throw new DatoProyectoInvalidoException("La fecha de inicio es obligatoria (formato yyyy-MM-dd)");
		}
		if (fechaInicio.isAfter(hoy)) {
			throw new DatoProyectoInvalidoException("La fecha de inicio del proyecto no puede ser futura");
		}

		if (estado != EstadoProyecto.FINALIZADO && fechaFin != null) {
			throw new DatoProyectoInvalidoException(
					"Un proyecto " + estado.getValor() + " no puede tener fecha de fin");
		}
		if (estado == EstadoProyecto.FINALIZADO && fechaFin == null) {
			throw new DatoProyectoInvalidoException("Un proyecto finalizado debe tener fecha de fin");
		}

		if (fechaFin != null) {
			if (fechaFin.isBefore(fechaInicio)) {
				throw new DatoProyectoInvalidoException(
						"La fecha de fin del proyecto no puede ser anterior a la de inicio");
			}
			if (fechaFin.isAfter(hoy)) {
				throw new DatoProyectoInvalidoException("La fecha de fin del proyecto no puede ser futura");
			}
		}
	}

	/**
	 * Verifica un campo de texto de un proyecto o de una participacion: si es obligatorio,
	 * que venga con contenido, y en todo caso, que quepa en su columna.
	 *
	 * @param valor          texto recibido (puede ser null si el campo es opcional)
	 * @param nombreCampo    nombre del campo para el mensaje (por ejemplo "nombre")
	 * @param longitudMaxima cantidad maxima de caracteres de la columna
	 * @param esObligatorio  true si el campo no puede venir vacio
	 * @throws DatosProyectoInvalidosException si falta un campo obligatorio o el texto es demasiado largo
	 */
	public static void verificarTextoProyecto(String valor, String nombreCampo, int longitudMaxima,
			boolean esObligatorio) {
		boolean estaVacio = (valor == null || valor.trim().isEmpty());

		if (esObligatorio && estaVacio) {
			throw new DatoProyectoInvalidoException("El campo '" + nombreCampo + "' es obligatorio");
		}
		if (!estaVacio && valor.trim().length() > longitudMaxima) {
			throw new DatoProyectoInvalidoException(
					"El campo '" + nombreCampo + "' no puede superar " + longitudMaxima + " caracteres");
		}
	}

	

	/**
	 * Verifica que el proyecto tenga al menos un experto (regla del negocio: todo proyecto
	 * de restauracion necesita por lo menos una participacion).
	 *
	 * @param cantidadParticipaciones cantidad de participaciones que trae el proyecto
	 * @throws DatosProyectoInvalidosException si no trae ninguna
	 */
	public static void verificarProyectoConExpertos(int cantidadParticipaciones) {
		if (cantidadParticipaciones == 0) {
			throw new DatoProyectoInvalidoException("El proyecto debe tener al menos un experto en 'participaciones'");
		}
	}

	/**
	 * Verifica que el experto no este ya en el proyecto.
	 *
	 * @param yaParticipa true si el experto ya esta en el proyecto (o ya vino antes en la misma lista)
	 * @param idExperto   id del experto para el mensaje
	 * @throws ExpertoRepetidoException si el experto ya participa
	 */
	public static void verificarExpertoNoRepetido(boolean yaParticipa, long idExperto) {
		if (yaParticipa) {
			throw new ExpertoRepetidoException(idExperto);
		}
	}
	/**
	 * Verifica las fechas de una participacion:
	 * - la incorporacion es obligatoria, no puede ser futura ni anterior al inicio del proyecto;
	 * - la salida es opcional (null = sigue en el proyecto), no puede ser anterior a la incorporacion ni futura.
	 *
	 * @param fechaIncorporacion fecha en que el experto entro al proyecto
	 * @param fechaSalida        fecha en que salio (null si sigue)
	 * @param inicioProyecto     fecha de inicio del proyecto
	 * @throws DatosProyectoInvalidosException si se incumple alguna regla
	 */
	public static void verificarFechasParticipacion(LocalDate fechaIncorporacion, LocalDate fechaSalida,
			LocalDate inicioProyecto) {
		LocalDate hoy = LocalDate.now();

		if (fechaIncorporacion == null) {
			throw new DatoProyectoInvalidoException(
					"La fecha de incorporacion del experto es obligatoria (formato yyyy-MM-dd)");
		}
		if (fechaIncorporacion.isAfter(hoy)) {
			throw new DatoProyectoInvalidoException("La fecha de incorporacion no puede ser futura");
		}
		if (fechaIncorporacion.isBefore(inicioProyecto)) {
			throw new DatoProyectoInvalidoException("La fecha de incorporacion (" + fechaIncorporacion
					+ ") no puede ser anterior al inicio del proyecto (" + inicioProyecto + ")");
		}

		if (fechaSalida != null) {
			if (fechaSalida.isBefore(fechaIncorporacion)) {
				throw new DatoProyectoInvalidoException(
						"La fecha de salida no puede ser anterior a la de incorporacion");
			}
			if (fechaSalida.isAfter(hoy)) {
				throw new DatoProyectoInvalidoException("La fecha de salida no puede ser futura");
			}
		}
	}
	
	/**
	 * Verifica que la participacion buscada exista.
	 *
	 * @param participacion   resultado de la busqueda en el repository
	 * @param idParticipacion id que se busco (para el mensaje)
	 * @throws ParticipacionNoEncontradaException si la participacion es null
	 */
	public static void verificarParticipacionExiste(Participacion participacion, long idParticipacion) {
		if (participacion == null) {
			throw new ParticipacionNoEncontradaException(idParticipacion);
		}
	}

	/**
	 * Verifica que la participacion que se quiere quitar no sea la unica del proyecto.
	 *
	 * @param cantidadParticipaciones cantidad de participaciones que tiene hoy el proyecto
	 * @param idProyecto              id del proyecto (para el mensaje)
	 * @throws UltimaParticipacionException si el proyecto tiene solo una participacion
	 */
	public static void verificarNoEsUltimaParticipacion(long cantidadParticipaciones, long idProyecto) {
		if (cantidadParticipaciones <= 1) {
			throw new UltimaParticipacionException(idProyecto);
		}
	}

	/**
	 * Verifica la imagen de un videojuego antes de subirla a Cloudinary:
	 * que venga con contenido, que sea jpg, png o webp, y que no pase de 5 MB.
	 *
	 * @param estaVacia true si no llego archivo o llego sin bytes
	 * @param tipo      tipo del archivo que informa el navegador (por ejemplo "image/png")
	 * @param tamanio   tamanio del archivo en bytes
	 * @throws ArchivoInvalidoException si se incumple alguna regla
	 */
	public static void verificarImagenValida(boolean estaVacia, String tipo, long tamanio) {
		if (estaVacia) {
			throw new ArchivoInvalidoException("Debe enviar una imagen en el campo 'imagen'");
		}

		boolean esTipoPermitido = "image/jpeg".equals(tipo) || "image/png".equals(tipo)
				|| "image/webp".equals(tipo);
		if (!esTipoPermitido) {
			throw new ArchivoInvalidoException("La imagen debe ser jpg, png o webp");
		}

		if (tamanio > TAMANIO_MAXIMO_IMAGEN) {
			throw new ArchivoInvalidoException("La imagen no puede pesar mas de 5 MB");
		}
	}
	
	/**
	 * Verifica que el proyecto este finalizado antes de generar su informe.
	 *
	 * @param estaFinalizado true si el estado del proyecto es finalizado
	 * @param idProyecto     id del proyecto para el mensaje
	 * @throws DatosInformeInvalidosException si el proyecto no esta finalizado
	 */
	public static void verificarProyectoFinalizado(boolean estaFinalizado, long idProyecto) {
		if (!estaFinalizado) {
			throw new DatosInformeInvalidosException("El proyecto con id " + idProyecto
					+ " no esta finalizado: solo un proyecto finalizado puede tener informe final");
		}
	}

	/**
	 * Verifica que el proyecto todavia no tenga informe final.
	 *
	 * @param urlInforme enlace guardado en el proyecto (null si no tiene)
	 * @param idProyecto id del proyecto para el mensaje
	 * @throws InformeYaExisteException si el proyecto ya tiene informe
	 */
	public static void verificarInformeNoExiste(String urlInforme, long idProyecto) {
		if (urlInforme != null && !urlInforme.trim().isEmpty()) {
			throw new InformeYaExisteException(idProyecto);
		}
	}

	/**
	 * Verifica que el proyecto si tenga informe final.
	 *
	 * @param urlInforme enlace guardado en el proyecto (null si no tiene)
	 * @param idProyecto id del proyecto para el mensaje
	 * @throws InformeNoEncontradoException si el proyecto no tiene informe
	 */
	public static void verificarInformeExiste(String urlInforme, long idProyecto) {
		if (urlInforme == null || urlInforme.trim().isEmpty()) {
			throw new InformeNoEncontradoException(idProyecto);
		}
	}

	/**
	 * Convierte el texto del resultado ("exitoso", "parcial" o "fracaso") al enum.
	 *
	 * @param texto resultado recibido en el JSON
	 * @return el resultado convertido
	 * @throws DatosInformeInvalidosException si viene vacio o no es un resultado valido
	 */
	public static ResultadoInforme convertirResultadoInforme(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			throw new DatosInformeInvalidosException("El resultado del informe es obligatorio");
		}
		ResultadoInforme resultado = ResultadoInforme.buscarPorValor(texto.trim().toLowerCase());
		if (resultado == null) {
			throw new DatosInformeInvalidosException("El resultado del informe debe ser exitoso, parcial o fracaso");
		}
		return resultado;
	}

	/**
	 * Verifica el resumen de resultados del informe.
	 *
	 * @param resumen texto recibido
	 * @throws DatosInformeInvalidosException si viene vacio o supera el maximo
	 */
	public static void verificarResumenInforme(String resumen) {
		if (resumen == null || resumen.trim().isEmpty()) {
			throw new DatosInformeInvalidosException("El resumen de resultados es obligatorio");
		}
		if (resumen.trim().length() > LONGITUD_MAXIMA_RESUMEN_INFORME) {
			throw new DatosInformeInvalidosException("El resumen de resultados no puede superar "
					+ LONGITUD_MAXIMA_RESUMEN_INFORME + " caracteres");
		}
	}

	/**
	 * Verifica la fecha de publicacion del informe: obligatoria, no futura
	 * y no anterior a la fecha de fin del proyecto.
	 *
	 * @param fechaPublicacion fecha recibida
	 * @param fechaFin         fecha de fin del proyecto
	 * @throws DatosInformeInvalidosException si la fecha no cumple las reglas
	 */
	public static void verificarFechaPublicacion(LocalDate fechaPublicacion, LocalDate fechaFin) {
		if (fechaPublicacion == null) {
			throw new DatosInformeInvalidosException("La fecha de publicacion del informe es obligatoria");
		}
		if (fechaPublicacion.isAfter(LocalDate.now())) {
			throw new DatosInformeInvalidosException("La fecha de publicacion del informe no puede ser futura");
		}
		if (fechaFin != null && fechaPublicacion.isBefore(fechaFin)) {
			throw new DatosInformeInvalidosException(
					"La fecha de publicacion no puede ser anterior a la fecha de fin del proyecto (" + fechaFin + ")");
		}
	}

	/**
	 * Un proyecto con informe final debe seguir finalizado: no puede volver a activo ni a pausado.
	 *
	 * @param tieneInforme true si el proyecto ya tiene informe
	 * @param estadoNuevo  estado que llega en el PUT
	 * @param idProyecto   id del proyecto para el mensaje
	 * @throws DatoProyectoInvalidoException si se intenta sacar de finalizado un proyecto con informe
	 */
	public static void verificarCambioDeEstadoConInforme(boolean tieneInforme, EstadoProyecto estadoNuevo,
			long idProyecto) {
		if (tieneInforme && estadoNuevo != EstadoProyecto.FINALIZADO) {
			throw new DatoProyectoInvalidoException("El proyecto con id " + idProyecto
					+ " ya tiene informe final: no puede pasar a activo o pausado. Elimina primero el informe");
		}
	}
}