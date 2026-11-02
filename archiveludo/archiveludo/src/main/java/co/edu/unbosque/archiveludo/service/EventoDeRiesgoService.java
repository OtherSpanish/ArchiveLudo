package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.dto.EventoDeRiesgoDTO;
import co.edu.unbosque.archiveludo.entity.EventoDeRiesgo;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.repository.EventoDeRiesgoRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio de los eventos de riesgo de los videojuegos.
 */
@Service
public class EventoDeRiesgoService {

	private final EventoDeRiesgoRepository eventoDeRiesgoRepository;
	private final VideojuegoRepository videojuegoRepository;
	private final ModelMapper mapper;

	public EventoDeRiesgoService(EventoDeRiesgoRepository eventoDeRiesgoRepository,
			VideojuegoRepository videojuegoRepository, ModelMapper mapper) {
		this.eventoDeRiesgoRepository = eventoDeRiesgoRepository;
		this.videojuegoRepository = videojuegoRepository;
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