package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.EstudioDTO;
import co.edu.unbosque.archiveludo.entity.Estudio;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.EstadoEstudio;
import co.edu.unbosque.archiveludo.repository.EstudioRepository;

/**
 * Logica de negocio de los estudios desarrolladores (CRUD).
 */
@Service
public class EstudioService {

	private final EstudioRepository estudioRepository;
	private final ModelMapper mapper;

	public EstudioService(EstudioRepository estudioRepository, ModelMapper mapper) {
		this.estudioRepository = estudioRepository;
		this.mapper = mapper;
	}

	/**
	 * @return todos los estudios, ordenados por nombre
	 */
	public List<EstudioDTO> buscarTodos() {
		List<Estudio> estudios = estudioRepository.buscarTodos();

		List<EstudioDTO> estudiosDTO = new ArrayList<EstudioDTO>();
		for (Estudio estudio : estudios) {
			estudiosDTO.add(convertirADTO(estudio));
		}
		return estudiosDTO;
	}

	/**
	 * @param idTexto id tal como llego en la URL
	 * @return el estudio
	 * @throws IdInvalidoException          si el id no es un numero mayor que cero
	 * @throws EstudioNoEncontradoException si no existe
	 */
	public EstudioDTO buscarPorId(String idTexto) {
		long idEstudio = LanzadorException.convertirId(idTexto, "estudio");

		Estudio estudio = estudioRepository.buscarPorId(idEstudio);
		LanzadorException.verificarEstudioExiste(estudio, idEstudio);

		return convertirADTO(estudio);
	}

	/**
	 * Crea un estudio nuevo.
	 *
	 * @param estudioDTO datos del estudio
	 * @return el estudio creado, con su id
	 * @throws CampoObligatorioException      si falta el nombre o el pais
	 * @throws DatosEstudioInvalidosException si el estado o los anios no cumplen las reglas
	 */
	@Transactional
	public EstudioDTO crear(EstudioDTO estudioDTO) {
		EstadoEstudio estado = verificarDatos(estudioDTO);

		Estudio estudio = new Estudio(estudioDTO.getNombre().trim(), estudioDTO.getPais().trim(), estado,
				estudioDTO.getAnioFundacion(), estudioDTO.getAnioCierre());

		Estudio estudioGuardado = estudioRepository.crear(estudio);
		return convertirADTO(estudioGuardado);
	}

	/**
	 * Actualiza todos los datos de un estudio.
	 *
	 * @param idTexto    id tal como llego en la URL
	 * @param estudioDTO datos nuevos
	 * @return el estudio actualizado
	 * @throws IdInvalidoException            si el id no es un numero mayor que cero
	 * @throws EstudioNoEncontradoException   si no existe
	 * @throws CampoObligatorioException      si falta el nombre o el pais
	 * @throws DatosEstudioInvalidosException si el estado o los anios no cumplen las reglas
	 */
	@Transactional
	public EstudioDTO actualizar(String idTexto, EstudioDTO estudioDTO) {
		long idEstudio = LanzadorException.convertirId(idTexto, "estudio");

		Estudio estudio = estudioRepository.buscarPorId(idEstudio);
		LanzadorException.verificarEstudioExiste(estudio, idEstudio);

		EstadoEstudio estado = verificarDatos(estudioDTO);

		estudio.setNombre(estudioDTO.getNombre().trim());
		estudio.setPais(estudioDTO.getPais().trim());
		estudio.setEstadoActual(estado);
		estudio.setAnioFundacion(estudioDTO.getAnioFundacion());
		estudio.setAnioCierre(estudioDTO.getAnioCierre());

		estudioRepository.actualizar(estudio);
		return convertirADTO(estudio);
	}

	/**
	 * Elimina un estudio solo si ningun videojuego lo tiene como desarrollador.
	 *
	 * @param idTexto id tal como llego en la URL
	 * @throws IdInvalidoException          si el id no es un numero mayor que cero
	 * @throws EstudioNoEncontradoException si no existe
	 * @throws RegistrosAsociadosException  si tiene videojuegos asociados
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idEstudio = LanzadorException.convertirId(idTexto, "estudio");

		Estudio estudio = estudioRepository.buscarPorId(idEstudio);
		LanzadorException.verificarEstudioExiste(estudio, idEstudio);

		boolean tieneVideojuegos = estudioRepository.contarVideojuegosAsociados(idEstudio) > 0;
		LanzadorException.verificarSinRegistrosAsociados(tieneVideojuegos, "el estudio", idEstudio);

		estudioRepository.eliminar(idEstudio);
	}

	/**
	 * Validaciones comunes a crear y actualizar. Devuelve el estado ya convertido al enum.
	 */
	private EstadoEstudio verificarDatos(EstudioDTO estudioDTO) {
		LanzadorException.verificarCampoObligatorio(estudioDTO.getNombre(), "nombre");
		LanzadorException.verificarCampoObligatorio(estudioDTO.getPais(), "pais");

		EstadoEstudio estado = LanzadorException.convertirEstadoEstudio(estudioDTO.getEstadoActual());
		LanzadorException.verificarAniosEstudio(estado, estudioDTO.getAnioFundacion(), estudioDTO.getAnioCierre());
		return estado;
	}

	/**
	 * Convierte la entity a DTO. El estado se pasa a su texto ("activo", "desaparecido").
	 */
	private EstudioDTO convertirADTO(Estudio estudio) {
		EstudioDTO estudioDTO = mapper.map(estudio, EstudioDTO.class);
		if (estudio.getEstadoActual() != null) {
			estudioDTO.setEstadoActual(estudio.getEstadoActual().getValor());
		}
		return estudioDTO;
	}

}