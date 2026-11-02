package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.ExpertoDTO;
import co.edu.unbosque.archiveludo.entity.Experto;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.repository.ExpertoRepository;

/**
 * Logica de negocio de los expertos (CRUD).
 * Un experto que participa en algun proyecto no se puede eliminar.
 */
@Service
public class ExpertoService {

	private final ExpertoRepository expertoRepository;
	private final ModelMapper mapper;

	public ExpertoService(ExpertoRepository expertoRepository, ModelMapper mapper) {
		this.expertoRepository = expertoRepository;
		this.mapper = mapper;
	}

	/**
	 * @return todos los expertos, ordenados por apellido y nombre
	 */
	public List<ExpertoDTO> buscarTodos() {
		List<Experto> expertos = expertoRepository.buscarTodos();

		List<ExpertoDTO> expertosDTO = new ArrayList<ExpertoDTO>();
		for (Experto experto : expertos) {
			expertosDTO.add(mapper.map(experto, ExpertoDTO.class));
		}
		return expertosDTO;
	}

	/**
	 * @param idTexto id tal como llego en la URL
	 * @return el experto
	 * @throws IdInvalidoException          si el id no es un numero mayor que cero
	 * @throws ExpertoNoEncontradoException si no existe
	 */
	public ExpertoDTO buscarPorId(String idTexto) {
		long idExperto = LanzadorException.convertirId(idTexto, "experto");

		Experto experto = expertoRepository.buscarPorId(idExperto);
		LanzadorException.verificarExpertoExiste(experto, idExperto);

		return mapper.map(experto, ExpertoDTO.class);
	}


	/**
	 * Registra un experto nuevo.
	 *
	 * @param expertoDTO datos del experto
	 * @return el experto creado, con su id
	 * @throws DatosExpertoInvalidosException si algun dato no es valido o el correo ya esta registrado
	 */
	@Transactional
	public ExpertoDTO crear(ExpertoDTO expertoDTO) {
		verificarDatos(expertoDTO);

		String correo = expertoDTO.getCorreo().trim();
		LanzadorException.verificarCorreoExpertoNoRegistrado(expertoRepository.existeCorreo(correo), correo);

		Experto experto = new Experto();
		llenarDatos(experto, expertoDTO);

		Experto expertoGuardado = expertoRepository.crear(experto);
		return mapper.map(expertoGuardado, ExpertoDTO.class);
	}

	/**
	 * Actualiza todos los datos de un experto.
	 *
	 * @param idTexto    id tal como llego en la URL
	 * @param expertoDTO datos nuevos
	 * @return el experto actualizado
	 * @throws IdInvalidoException            si el id no es un numero mayor que cero
	 * @throws ExpertoNoEncontradoException   si no existe
	 * @throws DatosExpertoInvalidosException si algun dato no es valido o el correo lo tiene otro experto
	 */
	@Transactional
	public ExpertoDTO actualizar(String idTexto, ExpertoDTO expertoDTO) {
		long idExperto = LanzadorException.convertirId(idTexto, "experto");

		Experto experto = expertoRepository.buscarPorId(idExperto);
		LanzadorException.verificarExpertoExiste(experto, idExperto);

		verificarDatos(expertoDTO);

		String correo = expertoDTO.getCorreo().trim();
		LanzadorException.verificarCorreoExpertoNoRegistrado(
				expertoRepository.existeCorreoEnOtroExperto(correo, idExperto), correo);

		llenarDatos(experto, expertoDTO);
		expertoRepository.actualizar(experto);
		return mapper.map(experto, ExpertoDTO.class);
	}

	/**
	 * Elimina un experto solo si no participa (ni participo) en ningun proyecto.
	 *
	 * @param idTexto id tal como llego en la URL
	 * @throws IdInvalidoException          si el id no es un numero mayor que cero
	 * @throws ExpertoNoEncontradoException si no existe
	 * @throws RegistrosAsociadosException  si tiene participaciones
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idExperto = LanzadorException.convertirId(idTexto, "experto");

		Experto experto = expertoRepository.buscarPorId(idExperto);
		LanzadorException.verificarExpertoExiste(experto, idExperto);

		boolean tieneParticipaciones = expertoRepository.contarParticipaciones(idExperto) > 0;
		LanzadorException.verificarSinRegistrosAsociados(tieneParticipaciones, "el experto", idExperto);

		expertoRepository.eliminar(idExperto);
	}


	/**
	 * Valida los datos del experto (los mismos para crear y actualizar).
	 */
	private void verificarDatos(ExpertoDTO expertoDTO) {
		int maximoNombre = LanzadorException.LONGITUD_MAXIMA_NOMBRE_EXPERTO;

		LanzadorException.verificarTextoExperto(expertoDTO.getPrimerNombreEx(), "primerNombreEx", maximoNombre, true);
		LanzadorException.verificarTextoExperto(expertoDTO.getSegundoNombreEx(), "segundoNombreEx", maximoNombre, false);
		LanzadorException.verificarTextoExperto(expertoDTO.getPrimerApellidoEx(), "primerApellidoEx", maximoNombre, true);
		LanzadorException.verificarTextoExperto(expertoDTO.getSegundoApellidoEx(), "segundoApellidoEx", maximoNombre,
				false);
		LanzadorException.verificarTextoExperto(expertoDTO.getEspecialidad(), "especialidad",
				LanzadorException.LONGITUD_MAXIMA_ESPECIALIDAD_EXPERTO, true);
		LanzadorException.verificarTextoExperto(expertoDTO.getAfiliacion(), "afiliacion",
				LanzadorException.LONGITUD_MAXIMA_AFILIACION_EXPERTO, false);
		LanzadorException.verificarCorreoExperto(expertoDTO.getCorreo());
	}

	/**
	 * Pasa los datos ya validados del DTO a la entity. Los opcionales vacios quedan en null.
	 */
	private void llenarDatos(Experto experto, ExpertoDTO expertoDTO) {
		experto.setPrimerNombreEx(expertoDTO.getPrimerNombreEx().trim());
		experto.setSegundoNombreEx(limpiarTextoOpcional(expertoDTO.getSegundoNombreEx()));
		experto.setPrimerApellidoEx(expertoDTO.getPrimerApellidoEx().trim());
		experto.setSegundoApellidoEx(limpiarTextoOpcional(expertoDTO.getSegundoApellidoEx()));
		experto.setEspecialidad(expertoDTO.getEspecialidad().trim());
		experto.setAfiliacion(limpiarTextoOpcional(expertoDTO.getAfiliacion()));
		experto.setCorreo(expertoDTO.getCorreo().trim());
	}

	/**
	 * Un texto opcional vacio ("" o "   ") se guarda como NULL en la BD.
	 */
	private String limpiarTextoOpcional(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return null;
		}
		return texto.trim();
	}

}