package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.ParticipacionDTO;
import co.edu.unbosque.archiveludo.entity.Experto;
import co.edu.unbosque.archiveludo.entity.Participacion;
import co.edu.unbosque.archiveludo.entity.ProyectoDeRestauracion;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.repository.ExpertoRepository;
import co.edu.unbosque.archiveludo.repository.ParticipacionRepository;
import co.edu.unbosque.archiveludo.repository.ProyectoDeRestauracionRepository;

/**
 * Logica de negocio de las participaciones de los expertos en los proyectos de restauracion.
 * Un experto puede estar en varios proyectos a la vez, pero solo una vez en cada proyecto.
 * Todo proyecto debe conservar al menos una participacion.
 */
@Service
public class ParticipacionService {

	private final ParticipacionRepository participacionRepository;
	private final ProyectoDeRestauracionRepository proyectoRepository;
	private final ExpertoRepository expertoRepository;
	private final ModelMapper mapper;

	public ParticipacionService(ParticipacionRepository participacionRepository,
			ProyectoDeRestauracionRepository proyectoRepository, ExpertoRepository expertoRepository,
			ModelMapper mapper) {
		this.participacionRepository = participacionRepository;
		this.proyectoRepository = proyectoRepository;
		this.expertoRepository = expertoRepository;
		this.mapper = mapper;
	}


	/**
	 * @param idTexto id del proyecto tal como llego en la URL
	 * @return las participaciones del proyecto, en el orden en que entraron los expertos
	 * @throws IdInvalidoException           si el id no es un numero mayor que cero
	 * @throws ProyectoNoEncontradoException si no existe el proyecto
	 */
	public List<ParticipacionDTO> buscarPorProyecto(String idTexto) {
		long idProyecto = LanzadorException.convertirId(idTexto, "proyecto");

		ProyectoDeRestauracion proyecto = proyectoRepository.buscarPorId(idProyecto);
		LanzadorException.verificarProyectoExiste(proyecto, idProyecto);

		List<ParticipacionDTO> participacionesDTO = new ArrayList<ParticipacionDTO>();
		for (Participacion participacion : participacionRepository.buscarPorIdProyecto(idProyecto)) {
			participacionesDTO.add(mapper.map(participacion, ParticipacionDTO.class));
		}
		return participacionesDTO;
	}

	/**
	 * @param idTexto id de la participacion tal como llego en la URL
	 * @return la participacion
	 * @throws IdInvalidoException                si el id no es un numero mayor que cero
	 * @throws ParticipacionNoEncontradaException si no existe
	 */
	public ParticipacionDTO buscarPorId(String idTexto) {
		long idParticipacion = LanzadorException.convertirId(idTexto, "participacion");

		Participacion participacion = participacionRepository.buscarPorId(idParticipacion);
		LanzadorException.verificarParticipacionExiste(participacion, idParticipacion);

		return mapper.map(participacion, ParticipacionDTO.class);
	}


	/**
	 * Agrega un experto a un proyecto que ya existe. El proyecto sale de la URL.
	 *
	 * @param idTexto          id del proyecto tal como llego en la URL
	 * @param participacionDTO experto, rol y fechas
	 * @return la participacion creada
	 * @throws IdInvalidoException             si el id del proyecto o del experto no es valido
	 * @throws ProyectoNoEncontradoException   si no existe el proyecto
	 * @throws ExpertoNoEncontradoException    si no existe el experto
	 * @throws DatosProyectoInvalidosException si el experto ya esta en el proyecto, o el rol o las fechas no son validos
	 */
	@Transactional
	public ParticipacionDTO crear(String idTexto, ParticipacionDTO participacionDTO) {
		long idProyecto = LanzadorException.convertirId(idTexto, "proyecto");

		ProyectoDeRestauracion proyecto = proyectoRepository.buscarPorId(idProyecto);
		LanzadorException.verificarProyectoExiste(proyecto, idProyecto);

		long idExperto = participacionDTO.getIdExperto();
		LanzadorException.verificarIdValido(idExperto, "experto");

		Experto experto = expertoRepository.buscarPorId(idExperto);
		LanzadorException.verificarExpertoExiste(experto, idExperto);

		boolean yaParticipa = participacionRepository.existeExpertoEnProyecto(idProyecto, idExperto);
		LanzadorException.verificarExpertoNoRepetido(yaParticipa, idExperto);

		verificarDatos(participacionDTO, proyecto);

		Participacion participacion = new Participacion(idProyecto, idExperto,
				participacionDTO.getRolExperto().trim(), participacionDTO.getFechaIncorporacion(),
				participacionDTO.getFechaSalida());

		Participacion participacionGuardada = participacionRepository.crear(participacion);
		return mapper.map(participacionGuardada, ParticipacionDTO.class);
	}

	/**
	 * Corrige el rol y las fechas de una participacion. El experto y el proyecto no cambian.
	 *
	 * @param idTexto          id de la participacion tal como llego en la URL
	 * @param participacionDTO rol y fechas nuevos
	 * @return la participacion actualizada
	 * @throws IdInvalidoException                si el id no es un numero mayor que cero
	 * @throws ParticipacionNoEncontradaException si no existe
	 * @throws DatosProyectoInvalidosException    si el rol o las fechas no son validos
	 */
	@Transactional
	public ParticipacionDTO actualizar(String idTexto, ParticipacionDTO participacionDTO) {
		long idParticipacion = LanzadorException.convertirId(idTexto, "participacion");

		Participacion participacion = participacionRepository.buscarPorId(idParticipacion);
		LanzadorException.verificarParticipacionExiste(participacion, idParticipacion);

		ProyectoDeRestauracion proyecto = proyectoRepository.buscarPorId(participacion.getIdProyecto());
		verificarDatos(participacionDTO, proyecto);

		participacion.setRolExperto(participacionDTO.getRolExperto().trim());
		participacion.setFechaIncorporacion(participacionDTO.getFechaIncorporacion());
		participacion.setFechaSalida(participacionDTO.getFechaSalida());

		participacionRepository.actualizar(participacion);
		return mapper.map(participacion, ParticipacionDTO.class);
	}

	/**
	 * Quita un experto de un proyecto. Si es la unica participacion del proyecto, no se permite.
	 *
	 * @param idTexto id de la participacion tal como llego en la URL
	 * @throws IdInvalidoException                si el id no es un numero mayor que cero
	 * @throws ParticipacionNoEncontradaException si no existe
	 * @throws UltimaParticipacionException       si es la unica participacion del proyecto
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idParticipacion = LanzadorException.convertirId(idTexto, "participacion");

		Participacion participacion = participacionRepository.buscarPorId(idParticipacion);
		LanzadorException.verificarParticipacionExiste(participacion, idParticipacion);

		long cantidad = participacionRepository.contarPorIdProyecto(participacion.getIdProyecto());
		LanzadorException.verificarNoEsUltimaParticipacion(cantidad, participacion.getIdProyecto());

		participacionRepository.eliminar(idParticipacion);
	}


	/**
	 * Valida el rol y las fechas de la participacion contra el inicio del proyecto
	 * (las mismas reglas que al crear el proyecto con sus participaciones).
	 */
	private void verificarDatos(ParticipacionDTO participacionDTO, ProyectoDeRestauracion proyecto) {
		LanzadorException.verificarTextoProyecto(participacionDTO.getRolExperto(), "rolExperto",
				LanzadorException.LONGITUD_MAXIMA_ROL_EXPERTO, true);
		LanzadorException.verificarFechasParticipacion(participacionDTO.getFechaIncorporacion(),
				participacionDTO.getFechaSalida(), proyecto.getFechaInicio());
	}

}