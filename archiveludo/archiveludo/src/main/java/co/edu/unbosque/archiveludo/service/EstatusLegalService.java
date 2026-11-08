package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.EstatusLegalDTO;
import co.edu.unbosque.archiveludo.entity.EstatusLegal;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.TipoEstatusLegal;
import co.edu.unbosque.archiveludo.repository.EstatusLegalRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio del estatus legal de los videojuegos (historial y CRUD).
 * El estatus vigente no se guarda: es el de fecha_determinacion mas reciente del videojuego.
 */
@Service
public class EstatusLegalService {

	private final EstatusLegalRepository estatusLegalRepository;
	private final VideojuegoRepository videojuegoRepository;
	private final UsuarioRepository usuarioRepository;
	private final ModelMapper mapper;

	public EstatusLegalService(EstatusLegalRepository estatusLegalRepository,
			VideojuegoRepository videojuegoRepository, UsuarioRepository usuarioRepository, ModelMapper mapper) {
		this.estatusLegalRepository = estatusLegalRepository;
		this.videojuegoRepository = videojuegoRepository;
		this.usuarioRepository = usuarioRepository;
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
	 * @param idTexto id del estatus tal como llego en la URL
	 * @return el estatus, indicando si es el vigente de su videojuego
	 * @throws IdInvalidoException               si el id no es un numero mayor que cero
	 * @throws EstatusLegalNoEncontradoException si no existe
	 */
	public EstatusLegalDTO buscarPorId(String idTexto) {
		long idEstatus = LanzadorException.convertirId(idTexto, "estatus legal");

		EstatusLegal estatusLegal = estatusLegalRepository.buscarPorId(idEstatus);
		LanzadorException.verificarEstatusLegalExiste(estatusLegal, idEstatus);

		return convertirADTOConVigencia(estatusLegal);
	}


	/**
	 * Registra un estatus legal nuevo para un videojuego. Registrar uno nuevo NO modifica los anteriores:
	 * si su fecha es la mas reciente, pasa a ser el vigente.
	 * El videojuego sale de la URL y el usuario del token.
	 *
	 * @param idTexto         id del videojuego tal como llego en la URL
	 * @param estatusLegalDTO estatus, fecha de determinacion y observacion
	 * @param correoUsuario   correo del usuario autenticado
	 * @return el estatus creado
	 * @throws IdInvalidoException                 si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException     si no existe el videojuego
	 * @throws DatosEstatusLegalInvalidosException si el estatus o la fecha no son validos
	 * @throws CredencialInvalidaException         si el usuario del token ya no existe
	 */
	@Transactional
	public EstatusLegalDTO crear(String idTexto, EstatusLegalDTO estatusLegalDTO, String correoUsuario) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		Usuario usuario = usuarioRepository.buscarPorCorreo(correoUsuario);
		LanzadorException.verificarCredencialesCorrectas(usuario != null);

		TipoEstatusLegal tipo = LanzadorException.convertirTipoEstatusLegal(estatusLegalDTO.getEstatus());
		LanzadorException.verificarFechaDeterminacion(estatusLegalDTO.getFechaDeterminacion());

		EstatusLegal estatusLegal = new EstatusLegal(idVideojuego, usuario.getIdUsuario(), tipo,
				estatusLegalDTO.getFechaDeterminacion(), limpiarTextoOpcional(estatusLegalDTO.getObservacion()));

		EstatusLegal estatusGuardado = estatusLegalRepository.crear(estatusLegal);
		return convertirADTOConVigencia(estatusGuardado);
	}

	/**
	 * Corrige el estatus, la fecha o la observacion de un estatus legal.
	 * No cambia de videojuego ni de usuario que lo registro.
	 *
	 * @param idTexto         id del estatus tal como llego en la URL
	 * @param estatusLegalDTO datos nuevos
	 * @return el estatus actualizado
	 * @throws IdInvalidoException                 si el id no es un numero mayor que cero
	 * @throws EstatusLegalNoEncontradoException   si no existe
	 * @throws DatosEstatusLegalInvalidosException si el estatus o la fecha no son validos
	 */
	@Transactional
	public EstatusLegalDTO actualizar(String idTexto, EstatusLegalDTO estatusLegalDTO) {
		long idEstatus = LanzadorException.convertirId(idTexto, "estatus legal");

		EstatusLegal estatusLegal = estatusLegalRepository.buscarPorId(idEstatus);
		LanzadorException.verificarEstatusLegalExiste(estatusLegal, idEstatus);

		TipoEstatusLegal tipo = LanzadorException.convertirTipoEstatusLegal(estatusLegalDTO.getEstatus());
		LanzadorException.verificarFechaDeterminacion(estatusLegalDTO.getFechaDeterminacion());

		estatusLegal.setEstatus(tipo);
		estatusLegal.setFechaDeterminacion(estatusLegalDTO.getFechaDeterminacion());
		estatusLegal.setObservacion(limpiarTextoOpcional(estatusLegalDTO.getObservacion()));

		estatusLegalRepository.actualizar(estatusLegal);
		return convertirADTOConVigencia(estatusLegal);
	}

	/**
	 * Elimina un estatus legal. Ninguna tabla depende de ESTATUS_LEGAL, asi que no hay 409.
	 * Si era el vigente, pasa a ser vigente el siguiente mas reciente.
	 *
	 * @param idTexto id del estatus tal como llego en la URL
	 * @throws IdInvalidoException               si el id no es un numero mayor que cero
	 * @throws EstatusLegalNoEncontradoException si no existe
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idEstatus = LanzadorException.convertirId(idTexto, "estatus legal");

		EstatusLegal estatusLegal = estatusLegalRepository.buscarPorId(idEstatus);
		LanzadorException.verificarEstatusLegalExiste(estatusLegal, idEstatus);

		estatusLegalRepository.eliminar(idEstatus);
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
	 * Convierte a DTO y calcula si es el estatus vigente de su videojuego
	 * (comparando su id con el id del mas reciente).
	 */
	private EstatusLegalDTO convertirADTOConVigencia(EstatusLegal estatusLegal) {
		EstatusLegalDTO estatusLegalDTO = convertirADTO(estatusLegal);

		long idVigente = estatusLegalRepository.buscarIdVigente(estatusLegal.getIdVideojuego());
		boolean esVigente = (idVigente == estatusLegal.getIdEstatus());
		estatusLegalDTO.setVigente(esVigente);
		return estatusLegalDTO;
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