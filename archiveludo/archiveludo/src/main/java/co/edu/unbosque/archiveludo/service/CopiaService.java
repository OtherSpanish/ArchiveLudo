package co.edu.unbosque.archiveludo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.CopiaDTO;
import co.edu.unbosque.archiveludo.dto.CopiaDigitalDTO;
import co.edu.unbosque.archiveludo.dto.CopiaFisicaDTO;
import co.edu.unbosque.archiveludo.entity.Copia;
import co.edu.unbosque.archiveludo.entity.CopiaDigital;
import co.edu.unbosque.archiveludo.entity.CopiaFisica;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.repository.CopiaRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio de las copias (fisicas y digitales) de los videojuegos.
 */
@Service
public class CopiaService {

	private final CopiaRepository copiaRepository;
	private final VideojuegoRepository videojuegoRepository;
	private final UsuarioRepository usuarioRepository;
	private final ModelMapper mapper;

	public CopiaService(CopiaRepository copiaRepository, VideojuegoRepository videojuegoRepository,
			UsuarioRepository usuarioRepository, ModelMapper mapper) {
		this.copiaRepository = copiaRepository;
		this.videojuegoRepository = videojuegoRepository;
		this.usuarioRepository = usuarioRepository;
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
	 * Registra una copia nueva (fisica o digital) de un videojuego.
	 * El videojuego sale de la URL, el usuario del token y la fecha de registro es hoy;
	 * si el front los envia en el cuerpo, se ignoran.
	 * Todo va en una transaccion: si falla el INSERT del subtipo, se deshace el de COPIA.
	 *
	 * @param idTexto       id del videojuego tal como llego en la URL
	 * @param copiaDTO      datos de la copia (CopiaFisicaDTO o CopiaDigitalDTO segun "tipoCopia")
	 * @param correoUsuario correo del usuario autenticado (viene del token)
	 * @return la copia creada, con su id
	 * @throws IdInvalidoException               si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException   si no existe el videojuego
	 * @throws CredencialInvalidaException       si el usuario del token ya no existe
	 * @throws EstadoIntegridadInvalidoException si el estado de integridad no es valido
	 * @throws CampoObligatorioException         si falta un campo obligatorio del subtipo
	 * @throws TamanioInvalidoException          si el tamanio de una copia digital no es mayor que cero
	 */
	@Transactional
	public CopiaDTO crear(String idTexto, CopiaDTO copiaDTO, String correoUsuario) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		Usuario usuario = usuarioRepository.buscarPorCorreo(correoUsuario);
		LanzadorException.verificarCredencialesCorrectas(usuario != null);

		EstadoIntegridad estadoIntegridad = LanzadorException
				.convertirEstadoIntegridad(copiaDTO.getEstadoIntegridad());
		LocalDate fechaRegistro = LocalDate.now();

		Copia copiaGuardada;
		if (copiaDTO instanceof CopiaFisicaDTO) {
			CopiaFisicaDTO copiaFisicaDTO = (CopiaFisicaDTO) copiaDTO;
			CopiaFisica copiaFisica = construirCopiaFisica(copiaFisicaDTO, idVideojuego, usuario.getIdUsuario(),
					estadoIntegridad, fechaRegistro);
			copiaGuardada = copiaRepository.crearCopiaFisica(copiaFisica);
		} else {
			CopiaDigitalDTO copiaDigitalDTO = (CopiaDigitalDTO) copiaDTO;
			CopiaDigital copiaDigital = construirCopiaDigital(copiaDigitalDTO, idVideojuego,
					usuario.getIdUsuario(), estadoIntegridad, fechaRegistro);
			copiaGuardada = copiaRepository.crearCopiaDigital(copiaDigital);
		}

		return convertirADTO(copiaGuardada);
	}

	/**
	 * Valida los campos de una copia fisica y arma la entity.
	 * ubicacionFisica y tipoSoporte son obligatorios; numeroSerie es opcional.
	 */
	private CopiaFisica construirCopiaFisica(CopiaFisicaDTO copiaFisicaDTO, long idVideojuego, long idUsuario,
			EstadoIntegridad estadoIntegridad, LocalDate fechaRegistro) {
		LanzadorException.verificarCampoObligatorio(copiaFisicaDTO.getUbicacionFisica(), "ubicacionFisica");
		LanzadorException.verificarCampoObligatorio(copiaFisicaDTO.getTipoSoporte(), "tipoSoporte");

		String numeroSerie = limpiarTextoOpcional(copiaFisicaDTO.getNumeroSerie());

		return new CopiaFisica(idVideojuego, idUsuario, estadoIntegridad, fechaRegistro, numeroSerie,
				copiaFisicaDTO.getUbicacionFisica().trim(), copiaFisicaDTO.getTipoSoporte().trim());
	}

	/**
	 * Valida los campos de una copia digital y arma la entity.
	 * formatoArchivo, tamanio y hashVerificacion son obligatorios.
	 */
	private CopiaDigital construirCopiaDigital(CopiaDigitalDTO copiaDigitalDTO, long idVideojuego,
			long idUsuario, EstadoIntegridad estadoIntegridad, LocalDate fechaRegistro) {
		LanzadorException.verificarCampoObligatorio(copiaDigitalDTO.getFormatoArchivo(), "formatoArchivo");
		LanzadorException.verificarCampoObligatorio(copiaDigitalDTO.getHashVerificacion(), "hashVerificacion");
		LanzadorException.verificarTamanioValido(copiaDigitalDTO.getTamanio());

		return new CopiaDigital(idVideojuego, idUsuario, estadoIntegridad, fechaRegistro,
				copiaDigitalDTO.getFormatoArchivo().trim(), copiaDigitalDTO.getTamanio(),
				copiaDigitalDTO.getHashVerificacion().trim());
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