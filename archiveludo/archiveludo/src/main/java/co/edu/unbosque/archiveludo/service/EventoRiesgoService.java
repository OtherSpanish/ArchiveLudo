package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.EventoDeRiesgoDTO;
import co.edu.unbosque.archiveludo.entity.EventoDeRiesgo;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.TipoEventoRiesgo;
import co.edu.unbosque.archiveludo.repository.EventoRiesgoRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio de los eventos de riesgo de los videojuegos (bitacora y CRUD).
 */
@Service
public class EventoRiesgoService {

	private final EventoRiesgoRepository eventoDeRiesgoRepository;
	private final VideojuegoRepository videojuegoRepository;
	private final UsuarioRepository usuarioRepository;
	private final ModelMapper mapper;

	public EventoRiesgoService(EventoRiesgoRepository eventoDeRiesgoRepository,
			VideojuegoRepository videojuegoRepository, UsuarioRepository usuarioRepository, ModelMapper mapper) {
		this.eventoDeRiesgoRepository = eventoDeRiesgoRepository;
		this.videojuegoRepository = videojuegoRepository;
		this.usuarioRepository = usuarioRepository;
		this.mapper = mapper;
	}


	/**
	 * Devuelve los eventos de riesgo de un videojuego, del mas reciente al mas antiguo.
	 *
	 * @param idTexto id del videojuego tal como llego en la URL
	 * @return eventos de riesgo (vacio si el videojuego no tiene ninguno)
	 * @throws IdInvalidoException             si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException si no existe el videojuego
	 */
	public List<EventoDeRiesgoDTO> buscarPorVideojuego(String idTexto) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		List<EventoDeRiesgo> eventos = eventoDeRiesgoRepository.buscarPorIdVideojuego(idVideojuego);

		List<EventoDeRiesgoDTO> eventosDTO = new ArrayList<EventoDeRiesgoDTO>();
		for (EventoDeRiesgo evento : eventos) {
			eventosDTO.add(convertirADTO(evento));
		}
		return eventosDTO;
	}

	/**
	 * @param idTexto id del evento tal como llego en la URL
	 * @return el evento de riesgo
	 * @throws IdInvalidoException                 si el id no es un numero mayor que cero
	 * @throws EventoDeRiesgoNoEncontradoException si no existe
	 */
	public EventoDeRiesgoDTO buscarPorId(String idTexto) {
		long idEvento = LanzadorException.convertirId(idTexto, "evento de riesgo");

		EventoDeRiesgo evento = eventoDeRiesgoRepository.buscarPorId(idEvento);
		LanzadorException.verificarEventoDeRiesgoExiste(evento, idEvento);

		return convertirADTO(evento);
	}

	

	/**
	 * Registra un evento de riesgo para un videojuego.
	 * El videojuego sale de la URL y el usuario del token.
	 *
	 * @param idTexto       id del videojuego tal como llego en la URL
	 * @param eventoDTO     tipo, fecha, descripcion y fuente del evento
	 * @param correoUsuario correo del usuario autenticado
	 * @return el evento creado
	 * @throws IdInvalidoException                 si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException     si no existe el videojuego
	 * @throws DatosEventoRiesgoInvalidosException si algun dato del evento no es valido
	 * @throws CredencialInvalidaException         si el usuario del token ya no existe
	 */
	@Transactional
	public EventoDeRiesgoDTO crear(String idTexto, EventoDeRiesgoDTO eventoDTO, String correoUsuario) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		Usuario usuario = usuarioRepository.buscarPorCorreo(correoUsuario);
		LanzadorException.verificarCredencialesCorrectas(usuario != null);

		TipoEventoRiesgo tipo = verificarDatos(eventoDTO);

		EventoDeRiesgo evento = new EventoDeRiesgo(idVideojuego, usuario.getIdUsuario(), tipo,
				eventoDTO.getFechaEvento(), eventoDTO.getDescripcion().trim(),
				limpiarTextoOpcional(eventoDTO.getFuente()));

		EventoDeRiesgo eventoGuardado = eventoDeRiesgoRepository.crear(evento);
		return convertirADTO(eventoGuardado);
	}

	/**
	 * Corrige el tipo, la fecha, la descripcion o la fuente de un evento de riesgo.
	 * No cambia de videojuego ni de usuario que lo registro.
	 *
	 * @param idTexto   id del evento tal como llego en la URL
	 * @param eventoDTO datos nuevos
	 * @return el evento actualizado
	 * @throws IdInvalidoException                 si el id no es un numero mayor que cero
	 * @throws EventoDeRiesgoNoEncontradoException si no existe
	 * @throws DatosEventoRiesgoInvalidosException si algun dato del evento no es valido
	 */
	@Transactional
	public EventoDeRiesgoDTO actualizar(String idTexto, EventoDeRiesgoDTO eventoDTO) {
		long idEvento = LanzadorException.convertirId(idTexto, "evento de riesgo");

		EventoDeRiesgo evento = eventoDeRiesgoRepository.buscarPorId(idEvento);
		LanzadorException.verificarEventoDeRiesgoExiste(evento, idEvento);

		TipoEventoRiesgo tipo = verificarDatos(eventoDTO);

		evento.setTipoEvento(tipo);
		evento.setFechaEvento(eventoDTO.getFechaEvento());
		evento.setDescripcion(eventoDTO.getDescripcion().trim());
		evento.setFuente(limpiarTextoOpcional(eventoDTO.getFuente()));

		eventoDeRiesgoRepository.actualizar(evento);
		return convertirADTO(evento);
	}

	/**
	 * Elimina un evento de riesgo. Ninguna tabla depende de EVENTO_DE_RIESGO, asi que no hay 409.
	 *
	 * @param idTexto id del evento tal como llego en la URL
	 * @throws IdInvalidoException                 si el id no es un numero mayor que cero
	 * @throws EventoDeRiesgoNoEncontradoException si no existe
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idEvento = LanzadorException.convertirId(idTexto, "evento de riesgo");

		EventoDeRiesgo evento = eventoDeRiesgoRepository.buscarPorId(idEvento);
		LanzadorException.verificarEventoDeRiesgoExiste(evento, idEvento);

		eventoDeRiesgoRepository.eliminar(idEvento);
	}

	
	/**
	 * Valida los datos del evento (los mismos para crear y actualizar)
	 * y devuelve el tipo ya convertido al enum.
	 */
	private TipoEventoRiesgo verificarDatos(EventoDeRiesgoDTO eventoDTO) {
		TipoEventoRiesgo tipo = LanzadorException.convertirTipoEventoRiesgo(eventoDTO.getTipoEvento());
		LanzadorException.verificarFechaEvento(eventoDTO.getFechaEvento());
		LanzadorException.verificarDescripcionEvento(eventoDTO.getDescripcion());
		LanzadorException.verificarFuenteEvento(eventoDTO.getFuente());
		return tipo;
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

	/**
	 * Convierte la entity a DTO. El tipo de evento se pasa a su texto legible ("Cierre de servidores").
	 */
	private EventoDeRiesgoDTO convertirADTO(EventoDeRiesgo evento) {
		EventoDeRiesgoDTO eventoDTO = mapper.map(evento, EventoDeRiesgoDTO.class);
		if (evento.getTipoEvento() != null) {
			eventoDTO.setTipoEvento(evento.getTipoEvento().getTextoLegible());
		}
		return eventoDTO;
	}

}