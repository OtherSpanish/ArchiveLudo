package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.ParticipacionDTO;
import co.edu.unbosque.archiveludo.dto.ProyectoDeRestauracionDTO;
import co.edu.unbosque.archiveludo.entity.Experto;
import co.edu.unbosque.archiveludo.entity.Participacion;
import co.edu.unbosque.archiveludo.entity.ProyectoDeRestauracion;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.EstadoProyecto;
import co.edu.unbosque.archiveludo.repository.ExpertoRepository;
import co.edu.unbosque.archiveludo.repository.ParticipacionRepository;
import co.edu.unbosque.archiveludo.repository.ProyectoDeRestauracionRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio de los proyectos de restauracion.
 * Une dos tablas: PROYECTO_DE_RESTAURACION y PARTICIPACION, cada una con su repository.
 * Las participaciones viajan como lista dentro de ProyectoDeRestauracionDTO.
 * Todo proyecto debe tener al menos un experto, por eso se crea junto con sus participaciones.
 * El informe final (PDF) lo maneja InformeFinalService; aqui solo se informa si existe (tieneInforme)
 * y se borra junto con el proyecto.
 */
@Service
public class ProyectoDeRestauracionService {

	private final ProyectoDeRestauracionRepository proyectoRepository;
	private final ParticipacionRepository participacionRepository;
	private final VideojuegoRepository videojuegoRepository;
	private final UsuarioRepository usuarioRepository;
	private final ExpertoRepository expertoRepository;
	private final AlmacenamientoArchivosService almacenamientoArchivosService;
	private final ModelMapper mapper;

	public ProyectoDeRestauracionService(ProyectoDeRestauracionRepository proyectoRepository,
			ParticipacionRepository participacionRepository, VideojuegoRepository videojuegoRepository,
			UsuarioRepository usuarioRepository, ExpertoRepository expertoRepository,
			AlmacenamientoArchivosService almacenamientoArchivosService, ModelMapper mapper) {
		this.proyectoRepository = proyectoRepository;
		this.participacionRepository = participacionRepository;
		this.videojuegoRepository = videojuegoRepository;
		this.usuarioRepository = usuarioRepository;
		this.expertoRepository = expertoRepository;
		this.almacenamientoArchivosService = almacenamientoArchivosService;
		this.mapper = mapper;
	}


	/**
	 * @return todos los proyectos, del mas reciente al mas antiguo, cada uno con sus participaciones
	 */
	public List<ProyectoDeRestauracionDTO> buscarTodos() {
		return convertirListaADTO(proyectoRepository.buscarTodos());
	}

	/**
	 * @param idTexto id del videojuego tal como llego en la URL
	 * @return los proyectos del videojuego, cada uno con sus participaciones
	 * @throws IdInvalidoException             si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException si no existe el videojuego
	 */
	public List<ProyectoDeRestauracionDTO> buscarPorVideojuego(String idTexto) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		return convertirListaADTO(proyectoRepository.buscarPorIdVideojuego(idVideojuego));
	}

	/**
	 * @param idTexto id del proyecto tal como llego en la URL
	 * @return el proyecto con sus participaciones
	 * @throws IdInvalidoException           si el id no es un numero mayor que cero
	 * @throws ProyectoNoEncontradoException si no existe
	 */
	public ProyectoDeRestauracionDTO buscarPorId(String idTexto) {
		long idProyecto = LanzadorException.convertirId(idTexto, "proyecto");

		ProyectoDeRestauracion proyecto = proyectoRepository.buscarPorId(idProyecto);
		LanzadorException.verificarProyectoExiste(proyecto, idProyecto);

		return convertirADTO(proyecto);
	}


	/**
	 * Crea un proyecto de restauracion para un videojuego, junto con sus participaciones,
	 * en una sola transaccion: si falla alguna participacion, no queda el proyecto a medias.
	 * El videojuego sale de la URL y el usuario que lo crea, del token.
	 *
	 * @param idTexto       id del videojuego tal como llego en la URL
	 * @param proyectoDTO   datos del proyecto y su lista de participaciones (al menos una)
	 * @param correoUsuario correo del usuario autenticado
	 * @return el proyecto creado, con sus participaciones
	 * @throws IdInvalidoException             si el id del videojuego o de un experto no es valido
	 * @throws VideojuegoNoEncontradoException si no existe el videojuego
	 * @throws ExpertoNoEncontradoException    si no existe alguno de los expertos
	 * @throws DatoProyectoInvalidoException   si los datos del proyecto o de una participacion no son validos
	 * @throws CredencialInvalidaException     si el usuario del token ya no existe
	 */
	@Transactional
	public ProyectoDeRestauracionDTO crear(String idTexto, ProyectoDeRestauracionDTO proyectoDTO,
			String correoUsuario) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		Usuario usuario = usuarioRepository.buscarPorCorreo(correoUsuario);
		LanzadorException.verificarCredencialesCorrectas(usuario != null);

		EstadoProyecto estado = verificarDatosProyecto(proyectoDTO);
		List<ParticipacionDTO> participacionesDTO = verificarParticipaciones(proyectoDTO);

		ProyectoDeRestauracion proyecto = new ProyectoDeRestauracion();
		proyecto.setIdVideojuego(idVideojuego);
		proyecto.setIdUsuario(usuario.getIdUsuario());
		llenarDatosProyecto(proyecto, proyectoDTO, estado);

		ProyectoDeRestauracion proyectoGuardado = proyectoRepository.crear(proyecto);

		for (ParticipacionDTO participacionDTO : participacionesDTO) {
			Participacion participacion = new Participacion(proyectoGuardado.getIdProyecto(),
					participacionDTO.getIdExperto(), participacionDTO.getRolExperto().trim(),
					participacionDTO.getFechaIncorporacion(), participacionDTO.getFechaSalida());
			participacionRepository.crear(participacion);
		}

		return convertirADTO(proyectoGuardado);
	}

	/**
	 * Actualiza los datos del proyecto (no sus participaciones, que tienen sus propios endpoints).
	 * No cambia de videojuego ni de usuario que lo creo.
	 * Si el proyecto ya tiene informe final, debe seguir finalizado.
	 *
	 * @param idTexto     id del proyecto tal como llego en la URL
	 * @param proyectoDTO datos nuevos del proyecto; si trae "participaciones" o "tieneInforme", se ignoran
	 * @return el proyecto actualizado, con sus participaciones
	 * @throws IdInvalidoException           si el id no es un numero mayor que cero
	 * @throws ProyectoNoEncontradoException si no existe
	 * @throws DatoProyectoInvalidoException si algun dato no es valido, si la nueva fecha de inicio
	 *                                       es posterior a la incorporacion de algun experto, o si se
	 *                                       intenta reactivar o pausar un proyecto con informe
	 */
	@Transactional
	public ProyectoDeRestauracionDTO actualizar(String idTexto, ProyectoDeRestauracionDTO proyectoDTO) {
		long idProyecto = LanzadorException.convertirId(idTexto, "proyecto");

		ProyectoDeRestauracion proyecto = proyectoRepository.buscarPorId(idProyecto);
		LanzadorException.verificarProyectoExiste(proyecto, idProyecto);

		EstadoProyecto estado = verificarDatosProyecto(proyectoDTO);

		String urlInforme = proyectoRepository.buscarUrlInforme(idProyecto);
		LanzadorException.verificarCambioDeEstadoConInforme(urlInforme != null, estado, idProyecto);

		// Los expertos que ya estan no pueden quedar incorporados antes del nuevo inicio del proyecto
		for (Participacion participacion : participacionRepository.buscarPorIdProyecto(idProyecto)) {
			LanzadorException.verificarFechasParticipacion(participacion.getFechaIncorporacion(),
					participacion.getFechaSalida(), proyectoDTO.getFechaInicio());
		}

		llenarDatosProyecto(proyecto, proyectoDTO, estado);
		proyectoRepository.actualizar(proyecto);
		return convertirADTO(proyecto);
	}

	/**
	 * Elimina un proyecto con sus participaciones y, si tiene, su informe final (el PDF en Cloudinary).
	 * Las participaciones y el informe son parte del proyecto, asi que no generan 409.
	 * El PDF se borra al final: si Cloudinary falla, la transaccion se revierte y el proyecto sigue completo.
	 *
	 * @param idTexto id del proyecto tal como llego en la URL
	 * @throws IdInvalidoException            si el id no es un numero mayor que cero
	 * @throws ProyectoNoEncontradoException  si no existe
	 * @throws AlmacenamientoArchivosException si no se pudo borrar el PDF en Cloudinary
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idProyecto = LanzadorException.convertirId(idTexto, "proyecto");

		ProyectoDeRestauracion proyecto = proyectoRepository.buscarPorId(idProyecto);
		LanzadorException.verificarProyectoExiste(proyecto, idProyecto);

		String urlInforme = proyectoRepository.buscarUrlInforme(idProyecto);

		participacionRepository.eliminarPorIdProyecto(idProyecto);
		proyectoRepository.eliminar(idProyecto);

		if (urlInforme != null) {
			almacenamientoArchivosService.eliminarArchivo(urlInforme);
		}
	}


	/**
	 * Valida los datos propios del proyecto y devuelve el estado ya convertido al enum.
	 */
	private EstadoProyecto verificarDatosProyecto(ProyectoDeRestauracionDTO proyectoDTO) {
		LanzadorException.verificarTextoProyecto(proyectoDTO.getNombre(), "nombre",
				LanzadorException.LONGITUD_MAXIMA_NOMBRE_PROYECTO, true);
		LanzadorException.verificarTextoProyecto(proyectoDTO.getDescripcion(), "descripcion",
				LanzadorException.LONGITUD_MAXIMA_DESCRIPCION_PROYECTO, false);
		LanzadorException.verificarTextoProyecto(proyectoDTO.getHerramientaEmulador(), "herramientaEmulador",
				LanzadorException.LONGITUD_MAXIMA_HERRAMIENTA_PROYECTO, false);

		EstadoProyecto estado = LanzadorException.convertirEstadoProyecto(proyectoDTO.getEstado());
		LanzadorException.verificarFechasProyecto(estado, proyectoDTO.getFechaInicio(), proyectoDTO.getFechaFin());
		return estado;
	}

	/**
	 * Valida la lista de participaciones de un proyecto nuevo:
	 * al menos una, expertos existentes y sin repetir, rol obligatorio y fechas coherentes con el proyecto.
	 *
	 * @return la lista de participaciones ya validada
	 */
	private List<ParticipacionDTO> verificarParticipaciones(ProyectoDeRestauracionDTO proyectoDTO) {
		List<ParticipacionDTO> participacionesDTO = proyectoDTO.getParticipaciones();
		if (participacionesDTO == null) {
			participacionesDTO = new ArrayList<ParticipacionDTO>();
		}
		LanzadorException.verificarProyectoConExpertos(participacionesDTO.size());

		List<Long> idsExpertosVistos = new ArrayList<Long>();
		for (ParticipacionDTO participacionDTO : participacionesDTO) {
			long idExperto = participacionDTO.getIdExperto();
			LanzadorException.verificarIdValido(idExperto, "experto");

			Experto experto = expertoRepository.buscarPorId(idExperto);
			LanzadorException.verificarExpertoExiste(experto, idExperto);

			LanzadorException.verificarExpertoNoRepetido(idsExpertosVistos.contains(idExperto), idExperto);
			idsExpertosVistos.add(idExperto);

			LanzadorException.verificarTextoProyecto(participacionDTO.getRolExperto(), "rolExperto",
					LanzadorException.LONGITUD_MAXIMA_ROL_EXPERTO, true);
			LanzadorException.verificarFechasParticipacion(participacionDTO.getFechaIncorporacion(),
					participacionDTO.getFechaSalida(), proyectoDTO.getFechaInicio());
		}
		return participacionesDTO;
	}

	// ──────────────────────────────────────────────
	// Utilidades
	// ──────────────────────────────────────────────

	/**
	 * Pasa los datos ya validados del DTO a la entity. Los opcionales vacios quedan en null.
	 */
	private void llenarDatosProyecto(ProyectoDeRestauracion proyecto, ProyectoDeRestauracionDTO proyectoDTO,
			EstadoProyecto estado) {
		proyecto.setNombre(proyectoDTO.getNombre().trim());
		proyecto.setDescripcion(limpiarTextoOpcional(proyectoDTO.getDescripcion()));
		proyecto.setHerramientaEmulador(limpiarTextoOpcional(proyectoDTO.getHerramientaEmulador()));
		proyecto.setEstado(estado);
		proyecto.setFechaInicio(proyectoDTO.getFechaInicio());
		proyecto.setFechaFin(proyectoDTO.getFechaFin());
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

	private List<ProyectoDeRestauracionDTO> convertirListaADTO(List<ProyectoDeRestauracion> proyectos) {
		List<ProyectoDeRestauracionDTO> proyectosDTO = new ArrayList<ProyectoDeRestauracionDTO>();
		for (ProyectoDeRestauracion proyecto : proyectos) {
			proyectosDTO.add(convertirADTO(proyecto));
		}
		return proyectosDTO;
	}

	/**
	 * Convierte la entity a DTO: el estado pasa a su texto ("activo"), se agregan sus participaciones
	 * y se indica si tiene informe final (sin exponer el enlace).
	 */
	private ProyectoDeRestauracionDTO convertirADTO(ProyectoDeRestauracion proyecto) {
		ProyectoDeRestauracionDTO proyectoDTO = mapper.map(proyecto, ProyectoDeRestauracionDTO.class);
		if (proyecto.getEstado() != null) {
			proyectoDTO.setEstado(proyecto.getEstado().getValor());
		}

		List<ParticipacionDTO> participacionesDTO = new ArrayList<ParticipacionDTO>();
		for (Participacion participacion : participacionRepository.buscarPorIdProyecto(proyecto.getIdProyecto())) {
			participacionesDTO.add(mapper.map(participacion, ParticipacionDTO.class));
		}
		proyectoDTO.setParticipaciones(participacionesDTO);

		proyectoDTO.setTieneInforme(proyectoRepository.buscarUrlInforme(proyecto.getIdProyecto()) != null);
		return proyectoDTO;
	}

}