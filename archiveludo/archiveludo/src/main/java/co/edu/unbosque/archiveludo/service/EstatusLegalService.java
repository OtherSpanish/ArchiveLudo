package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.dto.EstatusLegalDTO;
import co.edu.unbosque.archiveludo.entity.EstatusLegal;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.repository.EstatusLegalRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio del estatus legal de los videojuegos.
 */
@Service
public class EstatusLegalService {

	private final EstatusLegalRepository estatusLegalRepository;
	private final VideojuegoRepository videojuegoRepository;
	private final ModelMapper mapper;

	public EstatusLegalService(EstatusLegalRepository estatusLegalRepository,
			VideojuegoRepository videojuegoRepository, ModelMapper mapper) {
		this.estatusLegalRepository = estatusLegalRepository;
		this.videojuegoRepository = videojuegoRepository;
		this.mapper = mapper;
	}

	/**
	 * Devuelve el historial de estatus legal de un videojuego, del mas reciente al mas antiguo.
	 * El primero de la lista es el vigente (vigente = true); los demas son historicos.
	 *
	 * @param idTexto id del videojuego tal como llego en la URL
	 * @return historial de estatus (vacio si el videojuego no tiene ninguno)
	 * @throws IdInvalidoException             si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException si no existe el videojuego
	 */
	public List<EstatusLegalDTO> buscarPorVideojuego(String idTexto) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		List<EstatusLegal> estatusLegales = estatusLegalRepository.buscarPorIdVideojuego(idVideojuego);

		List<EstatusLegalDTO> estatusLegalesDTO = new ArrayList<EstatusLegalDTO>();
		for (int i = 0; i < estatusLegales.size(); i++) {
			EstatusLegalDTO estatusLegalDTO = convertirADTO(estatusLegales.get(i));
			boolean esElMasReciente = (i == 0);
			estatusLegalDTO.setVigente(esElMasReciente);
			estatusLegalesDTO.add(estatusLegalDTO);
		}
		return estatusLegalesDTO;
	}

	/**
	 * Convierte la entity a DTO. El estatus se pasa a su texto legible ("Dominio público").
	 */
	private EstatusLegalDTO convertirADTO(EstatusLegal estatusLegal) {
		EstatusLegalDTO estatusLegalDTO = mapper.map(estatusLegal, EstatusLegalDTO.class);
		if (estatusLegal.getEstatus() != null) {
			estatusLegalDTO.setEstatus(estatusLegal.getEstatus().getTextoLegible());
		}
		return estatusLegalDTO;
	}

}