package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.dto.CopiaDTO;
import co.edu.unbosque.archiveludo.dto.CopiaDigitalDTO;
import co.edu.unbosque.archiveludo.dto.CopiaFisicaDTO;
import co.edu.unbosque.archiveludo.entity.Copia;
import co.edu.unbosque.archiveludo.entity.CopiaFisica;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.repository.CopiaRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio de las copias (fisicas y digitales) de los videojuegos.
 */
@Service
public class CopiaService {

	private final CopiaRepository copiaRepository;
	private final VideojuegoRepository videojuegoRepository;
	private final ModelMapper mapper;

	public CopiaService(CopiaRepository copiaRepository, VideojuegoRepository videojuegoRepository,
			ModelMapper mapper) {
		this.copiaRepository = copiaRepository;
		this.videojuegoRepository = videojuegoRepository;
		this.mapper = mapper;
	}

	/**
	 * Devuelve las copias de un videojuego, de la mas reciente a la mas antigua.
	 *
	 * @param idTexto id del videojuego tal como llego en la URL
	 * @return copias del videojuego (vacio si no tiene ninguna)
	 * @throws IdInvalidoException             si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException si no existe el videojuego
	 */
	public List<CopiaDTO> buscarPorVideojuego(String idTexto) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		List<Copia> copias = copiaRepository.buscarPorIdVideojuego(idVideojuego);

		List<CopiaDTO> copiasDTO = new ArrayList<CopiaDTO>();
		for (Copia copia : copias) {
			copiasDTO.add(convertirADTO(copia));
		}
		return copiasDTO;
	}

	/**
	 * Convierte la entity al DTO de su mismo subtipo.
	 * El tipo de copia y el estado de integridad se pasan a su texto ("fisica", "dañada").
	 */
	private CopiaDTO convertirADTO(Copia copia) {
		CopiaDTO copiaDTO;
		if (copia instanceof CopiaFisica) {
			copiaDTO = mapper.map(copia, CopiaFisicaDTO.class);
		} else {
			copiaDTO = mapper.map(copia, CopiaDigitalDTO.class);
		}

		if (copia.getTipoCopia() != null) {
			copiaDTO.setTipoCopia(copia.getTipoCopia().getValor());
		}
		if (copia.getEstadoIntegridad() != null) {
			copiaDTO.setEstadoIntegridad(copia.getEstadoIntegridad().getValor());
		}
		return copiaDTO;
	}

}