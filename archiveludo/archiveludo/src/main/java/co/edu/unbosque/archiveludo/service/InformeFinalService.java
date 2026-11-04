package co.edu.unbosque.archiveludo.service;

import java.util.HashMap;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.ExpertoDTO;
import co.edu.unbosque.archiveludo.dto.InformeFinalDTO;
import co.edu.unbosque.archiveludo.dto.ParticipacionDTO;
import co.edu.unbosque.archiveludo.dto.ProyectoDeRestauracionDTO;
import co.edu.unbosque.archiveludo.dto.VideojuegoDTO;
import co.edu.unbosque.archiveludo.entity.Experto;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.EstadoProyecto;
import co.edu.unbosque.archiveludo.model.ResultadoInforme;
import co.edu.unbosque.archiveludo.repository.ExpertoRepository;
import co.edu.unbosque.archiveludo.repository.ProyectoDeRestauracionRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio del informe final de un proyecto de restauracion.
 * Un proyecto finalizado puede tener un solo informe: un PDF generado con los datos del formulario
 * (fecha de publicacion, resumen y resultado) y los del proyecto, guardado en Cloudinary.
 * En la base de datos solo queda el enlace (url_informe del proyecto).
 */
@Service
public class InformeFinalService {

	private final ProyectoDeRestauracionService proyectoService;
	private final ProyectoDeRestauracionRepository proyectoRepository;
	private final VideojuegoRepository videojuegoRepository;
	private final ExpertoRepository expertoRepository;
	private final UsuarioRepository usuarioRepository;
	private final GeneradorInformePdf generadorInformePdf;
	private final AlmacenamientoArchivosService almacenamientoArchivosService;
	private final ModelMapper mapper;

	public InformeFinalService(ProyectoDeRestauracionService proyectoService,
			ProyectoDeRestauracionRepository proyectoRepository, VideojuegoRepository videojuegoRepository,
			ExpertoRepository expertoRepository, UsuarioRepository usuarioRepository,
			GeneradorInformePdf generadorInformePdf, AlmacenamientoArchivosService almacenamientoArchivosService,
			ModelMapper mapper) {
		this.proyectoService = proyectoService;
		this.proyectoRepository = proyectoRepository;
		this.videojuegoRepository = videojuegoRepository;
		this.expertoRepository = expertoRepository;
		this.usuarioRepository = usuarioRepository;
		this.generadorInformePdf = generadorInformePdf;
		this.almacenamientoArchivosService = almacenamientoArchivosService;
		this.mapper = mapper;
	}

	/**
	 * @param idTexto id del proyecto tal como llego en la URL
	 * @return idProyecto y urlInforme del PDF
	 * @throws IdInvalidoException          si el id no es un numero mayor que cero
	 * @throws ProyectoNoEncontradoException si no existe el proyecto
	 * @throws InformeNoEncontradoException  si el proyecto no tiene informe
	 */
	public InformeFinalDTO buscar(String idTexto) {
		ProyectoDeRestauracionDTO proyecto = proyectoService.buscarPorId(idTexto);

		String urlInforme = proyectoRepository.buscarUrlInforme(proyecto.getIdProyecto());
		LanzadorException.verificarInformeExiste(urlInforme, proyecto.getIdProyecto());

		InformeFinalDTO informeDTO = new InformeFinalDTO();
		informeDTO.setIdProyecto(proyecto.getIdProyecto());
		informeDTO.setUrlInforme(urlInforme);
		return informeDTO;
	}

	/**
	 * Genera el PDF del informe final, lo sube a Cloudinary y guarda su enlace en el proyecto.
	 *
	 * @param idTexto       id del proyecto tal como llego en la URL
	 * @param informeDTO    fechaPublicacion, resumenResultados y resultado
	 * @param correoUsuario correo del usuario autenticado (aparece en el PDF)
	 * @return los datos del informe con su urlInforme
	 * @throws IdInvalidoException             si el id no es un numero mayor que cero
	 * @throws ProyectoNoEncontradoException   si no existe el proyecto
	 * @throws DatosInformeInvalidosException  si el proyecto no esta finalizado o los datos no son validos
	 * @throws InformeYaExisteException        si el proyecto ya tiene informe
	 * @throws CredencialInvalidaException     si el usuario del token ya no existe
	 * @throws GeneracionPdfException          si no se pudo construir el PDF
	 * @throws AlmacenamientoArchivosException si no se pudo subir a Cloudinary
	 */
	@Transactional
	public InformeFinalDTO crear(String idTexto, InformeFinalDTO informeDTO, String correoUsuario) {
		ProyectoDeRestauracionDTO proyecto = proyectoService.buscarPorId(idTexto);
		long idProyecto = proyecto.getIdProyecto();

		LanzadorException.verificarProyectoFinalizado(estaFinalizado(proyecto), idProyecto);
		LanzadorException.verificarInformeNoExiste(proyectoRepository.buscarUrlInforme(idProyecto), idProyecto);

		String urlInforme = generarYSubir(proyecto, informeDTO, correoUsuario);
		proyectoRepository.actualizarUrlInforme(idProyecto, urlInforme);

		return convertirARespuesta(idProyecto, informeDTO, urlInforme);
	}

	/**
	 * Vuelve a generar el informe con datos nuevos (por ejemplo, para corregir el resumen).
	 * Sube el PDF nuevo, guarda su enlace y despues borra el PDF anterior.
	 *
	 * @param idTexto       id del proyecto tal como llego en la URL
	 * @param informeDTO    datos nuevos del informe
	 * @param correoUsuario correo del usuario autenticado
	 * @return los datos del informe con su nueva urlInforme
	 * @throws InformeNoEncontradoException si el proyecto no tiene informe (ademas de las de crear)
	 */
	@Transactional
	public InformeFinalDTO actualizar(String idTexto, InformeFinalDTO informeDTO, String correoUsuario) {
		ProyectoDeRestauracionDTO proyecto = proyectoService.buscarPorId(idTexto);
		long idProyecto = proyecto.getIdProyecto();

		String urlAnterior = proyectoRepository.buscarUrlInforme(idProyecto);
		LanzadorException.verificarInformeExiste(urlAnterior, idProyecto);
		LanzadorException.verificarProyectoFinalizado(estaFinalizado(proyecto), idProyecto);

		String urlNueva = generarYSubir(proyecto, informeDTO, correoUsuario);
		proyectoRepository.actualizarUrlInforme(idProyecto, urlNueva);
		almacenamientoArchivosService.eliminarArchivo(urlAnterior);

		return convertirARespuesta(idProyecto, informeDTO, urlNueva);
	}

	/**
	 * Quita el informe del proyecto y borra el PDF de Cloudinary.
	 * Si Cloudinary falla, la transaccion se revierte y el enlace sigue guardado.
	 *
	 * @param idTexto id del proyecto tal como llego en la URL
	 * @throws InformeNoEncontradoException    si el proyecto no tiene informe
	 * @throws AlmacenamientoArchivosException si no se pudo borrar en Cloudinary
	 */
	@Transactional
	public void eliminar(String idTexto) {
		ProyectoDeRestauracionDTO proyecto = proyectoService.buscarPorId(idTexto);
		long idProyecto = proyecto.getIdProyecto();

		String urlInforme = proyectoRepository.buscarUrlInforme(idProyecto);
		LanzadorException.verificarInformeExiste(urlInforme, idProyecto);

		proyectoRepository.actualizarUrlInforme(idProyecto, null);
		almacenamientoArchivosService.eliminarArchivo(urlInforme);
	}

	// ──────────────────────────────────────────────
	// Utilidades
	// ──────────────────────────────────────────────

	/**
	 * Valida los datos del formulario, arma el PDF y lo sube a Cloudinary.
	 *
	 * @return el enlace del PDF subido
	 */
	private String generarYSubir(ProyectoDeRestauracionDTO proyecto, InformeFinalDTO informeDTO,
			String correoUsuario) {
		LanzadorException.verificarFechaPublicacion(informeDTO.getFechaPublicacion(), proyecto.getFechaFin());
		LanzadorException.verificarResumenInforme(informeDTO.getResumenResultados());
		ResultadoInforme resultado = LanzadorException.convertirResultadoInforme(informeDTO.getResultado());

		informeDTO.setResumenResultados(informeDTO.getResumenResultados().trim());
		informeDTO.setResultado(resultado.getValor());

		Usuario usuario = usuarioRepository.buscarPorCorreo(correoUsuario);
		LanzadorException.verificarCredencialesCorrectas(usuario != null);
		String generadoPor = usuario.getPrimerNombre() + " " + usuario.getPrimerApellido() + " (" + correoUsuario
				+ ")";

		VideojuegoDTO videojuego = buscarVideojuego(proyecto.getIdVideojuego());
		Map<Long, ExpertoDTO> expertos = buscarExpertos(proyecto);

		byte[] pdf = generadorInformePdf.generar(proyecto, videojuego, expertos, informeDTO, generadoPor);
		return almacenamientoArchivosService.subirInformeProyecto(proyecto.getIdProyecto(), pdf);
	}

	private boolean estaFinalizado(ProyectoDeRestauracionDTO proyecto) {
		return EstadoProyecto.FINALIZADO.getValor().equals(proyecto.getEstado());
	}

	/**
	 * El videojuego existe siempre (FK del proyecto); el estado se pasa a su texto ("en riesgo").
	 */
	private VideojuegoDTO buscarVideojuego(long idVideojuego) {
		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		VideojuegoDTO videojuegoDTO = mapper.map(videojuego, VideojuegoDTO.class);
		if (videojuego.getEstadoConservacion() != null) {
			videojuegoDTO.setEstadoConservacion(videojuego.getEstadoConservacion().getValor());
		}
		return videojuegoDTO;
	}

	/**
	 * @return los expertos del proyecto, con su idExperto como llave
	 */
	private Map<Long, ExpertoDTO> buscarExpertos(ProyectoDeRestauracionDTO proyecto) {
		Map<Long, ExpertoDTO> expertos = new HashMap<Long, ExpertoDTO>();
		for (ParticipacionDTO participacion : proyecto.getParticipaciones()) {
			Experto experto = expertoRepository.buscarPorId(participacion.getIdExperto());
			if (experto != null) {
				expertos.put(experto.getIdExperto(), mapper.map(experto, ExpertoDTO.class));
			}
		}
		return expertos;
	}

	private InformeFinalDTO convertirARespuesta(long idProyecto, InformeFinalDTO informeDTO, String urlInforme) {
		return new InformeFinalDTO(idProyecto, informeDTO.getFechaPublicacion(), informeDTO.getResumenResultados(),
				informeDTO.getResultado(), urlInforme);
	}

}