package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.InstitucionDTO;
import co.edu.unbosque.archiveludo.entity.InstCorreo;
import co.edu.unbosque.archiveludo.entity.InstTelefono;
import co.edu.unbosque.archiveludo.entity.Institucion;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.TipoInstitucion;
import co.edu.unbosque.archiveludo.repository.CustodiaRepository;
import co.edu.unbosque.archiveludo.repository.InstCorreoRepository;
import co.edu.unbosque.archiveludo.repository.InstTelefonoRepository;
import co.edu.unbosque.archiveludo.repository.InstitucionRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio de las instituciones custodias (CRUD).
 * Une tres tablas: INSTITUCION, INST_TELEFONO e INST_CORREO, cada una con su propio repository.
 * Los telefonos y correos viajan como listas dentro de InstitucionDTO.
 * Toda institucion debe tener al menos un telefono y al menos un correo.
 * Cada institucion que sale hacia el front lleva cuantas copias custodia hoy.
 */
@Service
public class InstitucionService {

	private final InstitucionRepository institucionRepository;
	private final InstTelefonoRepository instTelefonoRepository;
	private final InstCorreoRepository instCorreoRepository;
	private final CustodiaRepository custodiaRepository;
	private final VideojuegoRepository videojuegoRepository;

	public InstitucionService(InstitucionRepository institucionRepository,
			InstTelefonoRepository instTelefonoRepository, InstCorreoRepository instCorreoRepository,
			CustodiaRepository custodiaRepository, VideojuegoRepository videojuegoRepository) {
		this.institucionRepository = institucionRepository;
		this.instTelefonoRepository = instTelefonoRepository;
		this.instCorreoRepository = instCorreoRepository;
		this.custodiaRepository = custodiaRepository;
		this.videojuegoRepository = videojuegoRepository;
	}


	/**
	 * @return todas las instituciones, cada una con sus telefonos, correos y copias custodiadas
	 */
	public List<InstitucionDTO> buscarTodas() {
		List<Institucion> instituciones = institucionRepository.buscarTodas();

		List<InstitucionDTO> institucionesDTO = new ArrayList<InstitucionDTO>();
		for (Institucion institucion : instituciones) {
			institucionesDTO.add(convertirADTO(institucion));
		}
		return institucionesDTO;
	}

	/**
	 * @param idTexto id tal como llego en la URL
	 * @return la institucion con sus telefonos, correos y copias custodiadas
	 * @throws IdInvalidoException              si el id no es un numero mayor que cero
	 * @throws InstitucionNoEncontradaException si no existe
	 */
	public InstitucionDTO buscarPorId(String idTexto) {
		long idInstitucion = LanzadorException.convertirId(idTexto, "institucion");

		Institucion institucion = institucionRepository.buscarPorId(idInstitucion);
		LanzadorException.verificarInstitucionExiste(institucion, idInstitucion);

		return convertirADTO(institucion);
	}

	/**
	 * Instituciones que hoy custodian al menos una copia del videojuego, en orden alfabetico.
	 * Cada una trae cantidadCopiasDelVideojuego (copias de ESE juego que custodia hoy).
	 *
	 * @param idTexto id del videojuego tal como llego en la URL
	 * @return las instituciones custodias del videojuego (vacio si ninguna copia tiene custodia vigente)
	 * @throws IdInvalidoException             si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException si no existe el videojuego
	 */
	public List<InstitucionDTO> buscarPorVideojuego(String idTexto) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		Map<Long, Long> cantidades = custodiaRepository.contarVigentesPorInstitucionDeVideojuego(idVideojuego);

		List<InstitucionDTO> institucionesDTO = new ArrayList<InstitucionDTO>();
		for (Map.Entry<Long, Long> entrada : cantidades.entrySet()) {
			Institucion institucion = institucionRepository.buscarPorId(entrada.getKey());
			if (institucion != null) {
				InstitucionDTO institucionDTO = convertirADTO(institucion);
				institucionDTO.setCantidadCopiasDelVideojuego(entrada.getValue());
				institucionesDTO.add(institucionDTO);
			}
		}
		return institucionesDTO;
	}

	/**
	 * Crea una institucion con sus telefonos y correos, en una sola transaccion.
	 *
	 * @param institucionDTO datos de la institucion
	 * @return la institucion creada, con su id
	 * @throws CampoObligatorioException          si falta el nombre o el pais
	 * @throws DatosInstitucionInvalidosException si el tipo, un telefono o un correo no son validos,
	 *                                            o si no trae al menos un telefono y un correo
	 */
	@Transactional
	public InstitucionDTO crear(InstitucionDTO institucionDTO) {
		TipoInstitucion tipo = verificarDatos(institucionDTO);
		List<Long> telefonos = verificarTelefonos(institucionDTO.getTelefonos());
		List<String> correos = verificarCorreos(institucionDTO.getCorreos());
		LanzadorException.verificarInstitucionConContacto(telefonos.size(), correos.size());

		Institucion institucion = new Institucion();
		institucion.setNombre(institucionDTO.getNombre().trim());
		institucion.setTipo(tipo);
		institucion.setPais(institucionDTO.getPais().trim());

		Institucion institucionGuardada = institucionRepository.crear(institucion);
		guardarTelefonosYCorreos(institucionGuardada.getIdInstitucion(), telefonos, correos);

		return convertirADTO(institucionGuardada);
	}

	/**
	 * Actualiza una institucion. Las listas de telefonos y correos enviadas REEMPLAZAN a las anteriores.
	 *
	 * @param idTexto        id tal como llego en la URL
	 * @param institucionDTO datos nuevos
	 * @return la institucion actualizada
	 * @throws IdInvalidoException                si el id no es un numero mayor que cero
	 * @throws InstitucionNoEncontradaException   si no existe
	 * @throws CampoObligatorioException          si falta el nombre o el pais
	 * @throws DatosInstitucionInvalidosException si el tipo, un telefono o un correo no son validos,
	 *                                            o si no trae al menos un telefono y un correo
	 */
	@Transactional
	public InstitucionDTO actualizar(String idTexto, InstitucionDTO institucionDTO) {
		long idInstitucion = LanzadorException.convertirId(idTexto, "institucion");

		Institucion institucion = institucionRepository.buscarPorId(idInstitucion);
		LanzadorException.verificarInstitucionExiste(institucion, idInstitucion);

		TipoInstitucion tipo = verificarDatos(institucionDTO);
		List<Long> telefonos = verificarTelefonos(institucionDTO.getTelefonos());
		List<String> correos = verificarCorreos(institucionDTO.getCorreos());
		LanzadorException.verificarInstitucionConContacto(telefonos.size(), correos.size());

		institucion.setNombre(institucionDTO.getNombre().trim());
		institucion.setTipo(tipo);
		institucion.setPais(institucionDTO.getPais().trim());
		institucionRepository.actualizar(institucion);

		instTelefonoRepository.eliminarPorIdInstitucion(idInstitucion);
		instCorreoRepository.eliminarPorIdInstitucion(idInstitucion);
		guardarTelefonosYCorreos(idInstitucion, telefonos, correos);

		return convertirADTO(institucion);
	}

	/**
	 * Elimina una institucion solo si no tiene custodias (ni vigentes ni historicas).
	 * Sus telefonos y correos se borran con ella: son parte de la propia institucion.
	 *
	 * @param idTexto id tal como llego en la URL
	 * @throws IdInvalidoException              si el id no es un numero mayor que cero
	 * @throws InstitucionNoEncontradaException si no existe
	 * @throws RegistrosAsociadosException      si tiene custodias
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idInstitucion = LanzadorException.convertirId(idTexto, "institucion");

		Institucion institucion = institucionRepository.buscarPorId(idInstitucion);
		LanzadorException.verificarInstitucionExiste(institucion, idInstitucion);

		boolean tieneCustodias = institucionRepository.contarCustodiasAsociadas(idInstitucion) > 0;
		LanzadorException.verificarSinRegistrosAsociados(tieneCustodias, "la institucion", idInstitucion);

		instTelefonoRepository.eliminarPorIdInstitucion(idInstitucion);
		instCorreoRepository.eliminarPorIdInstitucion(idInstitucion);
		institucionRepository.eliminar(idInstitucion);
	}


	/**
	 * Valida los datos propios de la institucion y devuelve el tipo ya convertido al enum.
	 */
	private TipoInstitucion verificarDatos(InstitucionDTO institucionDTO) {
		LanzadorException.verificarCampoObligatorio(institucionDTO.getNombre(), "nombre");
		LanzadorException.verificarCampoObligatorio(institucionDTO.getPais(), "pais");
		return LanzadorException.convertirTipoInstitucion(institucionDTO.getTipo());
	}

	/**
	 * Valida cada telefono y quita los repetidos.
	 * Que haya al menos uno lo verifica despues verificarInstitucionConContacto.
	 */
	private List<Long> verificarTelefonos(List<Long> telefonosEnviados) {
		List<Long> telefonosSinRepetir = new ArrayList<Long>();
		if (telefonosEnviados == null) {
			return telefonosSinRepetir;
		}

		for (Long telefono : telefonosEnviados) {
			LanzadorException.verificarTelefonoValido(telefono);
			if (!telefonosSinRepetir.contains(telefono)) {
				telefonosSinRepetir.add(telefono);
			}
		}
		return telefonosSinRepetir;
	}

	/**
	 * Valida cada correo y quita los repetidos sin distinguir mayusculas
	 * (MySQL compara asi la PK de INST_CORREO).
	 * Que haya al menos uno lo verifica despues verificarInstitucionConContacto.
	 */
	private List<String> verificarCorreos(List<String> correosEnviados) {
		List<String> correosSinRepetir = new ArrayList<String>();
		if (correosEnviados == null) {
			return correosSinRepetir;
		}

		for (String correo : correosEnviados) {
			LanzadorException.verificarCorreoInstitucionValido(correo);
			String correoLimpio = correo.trim();
			if (!contieneSinDistinguirMayusculas(correosSinRepetir, correoLimpio)) {
				correosSinRepetir.add(correoLimpio);
			}
		}
		return correosSinRepetir;
	}

	private boolean contieneSinDistinguirMayusculas(List<String> lista, String texto) {
		for (String elemento : lista) {
			if (elemento.equalsIgnoreCase(texto)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Inserta los telefonos y correos de la institucion, cada uno en su tabla.
	 */
	private void guardarTelefonosYCorreos(long idInstitucion, List<Long> telefonos, List<String> correos) {
		for (Long telefono : telefonos) {
			instTelefonoRepository.crear(new InstTelefono(idInstitucion, telefono));
		}
		for (String correo : correos) {
			instCorreoRepository.crear(new InstCorreo(idInstitucion, correo));
		}
	}

	/**
	 * Arma el DTO con los datos de la institucion, sus listas de telefonos y correos
	 * y la cantidad de copias que custodia hoy. El tipo se pasa a su texto legible ("Colección privada").
	 */
	private InstitucionDTO convertirADTO(Institucion institucion) {
		long idInstitucion = institucion.getIdInstitucion();

		List<Long> telefonos = new ArrayList<Long>();
		for (InstTelefono instTelefono : instTelefonoRepository.buscarPorIdInstitucion(idInstitucion)) {
			telefonos.add(instTelefono.getTelefono());
		}

		List<String> correos = new ArrayList<String>();
		for (InstCorreo instCorreo : instCorreoRepository.buscarPorIdInstitucion(idInstitucion)) {
			correos.add(instCorreo.getCorreo());
		}

		InstitucionDTO institucionDTO = new InstitucionDTO();
		institucionDTO.setIdInstitucion(idInstitucion);
		institucionDTO.setNombre(institucion.getNombre());
		if (institucion.getTipo() != null) {
			institucionDTO.setTipo(institucion.getTipo().getTextoLegible());
		}
		institucionDTO.setPais(institucion.getPais());
		institucionDTO.setTelefonos(telefonos);
		institucionDTO.setCorreos(correos);
		institucionDTO.setCantidadCopiasCustodiadas(custodiaRepository.contarVigentesPorInstitucion(idInstitucion));
		return institucionDTO;
	}

}