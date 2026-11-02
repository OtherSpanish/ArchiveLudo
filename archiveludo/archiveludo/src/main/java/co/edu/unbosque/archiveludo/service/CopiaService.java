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
import co.edu.unbosque.archiveludo.model.TipoCopia;
import co.edu.unbosque.archiveludo.repository.CopiaRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio de las copias (fisicas y digitales) de los videojuegos.
 * Una copia vive en dos tablas: COPIA (datos comunes) y su tabla hija (COPIA_FISICA o COPIA_DIGITAL).
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
	 * @param idTexto id de la copia tal como llego en la URL
	 * @return la copia (CopiaFisicaDTO o CopiaDigitalDTO segun su tipo)
	 * @throws IdInvalidoException        si el id no es un numero mayor que cero
	 * @throws CopiaNoEncontradaException si no existe
	 */
	public CopiaDTO buscarPorId(String idTexto) {
		long idCopia = LanzadorException.convertirId(idTexto, "copia");

		Copia copia = copiaRepository.buscarPorId(idCopia);
		LanzadorException.verificarCopiaExiste(copia, idCopia);

		return convertirADTO(copia);
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
			verificarDatosFisicos(copiaFisicaDTO);

			CopiaFisica copiaFisica = new CopiaFisica(idVideojuego, usuario.getIdUsuario(), estadoIntegridad,
					fechaRegistro, limpiarTextoOpcional(copiaFisicaDTO.getNumeroSerie()),
					copiaFisicaDTO.getUbicacionFisica().trim(), copiaFisicaDTO.getTipoSoporte().trim());
			copiaGuardada = copiaRepository.crearCopiaFisica(copiaFisica);
		} else {
			CopiaDigitalDTO copiaDigitalDTO = (CopiaDigitalDTO) copiaDTO;
			verificarDatosDigitales(copiaDigitalDTO);

			CopiaDigital copiaDigital = new CopiaDigital(idVideojuego, usuario.getIdUsuario(), estadoIntegridad,
					fechaRegistro, copiaDigitalDTO.getFormatoArchivo().trim(), copiaDigitalDTO.getTamanio(),
					copiaDigitalDTO.getHashVerificacion().trim());
			copiaGuardada = copiaRepository.crearCopiaDigital(copiaDigital);
		}

		return convertirADTO(copiaGuardada);
	}

	/**
	 * Actualiza el estado de integridad y los datos del subtipo de una copia.
	 * No cambian el videojuego, el usuario que la registro, la fecha de registro ni el tipo de copia.
	 * Los dos UPDATE (COPIA y su tabla hija) van en la misma transaccion.
	 *
	 * @param idTexto  id de la copia tal como llego en la URL
	 * @param copiaDTO datos nuevos (con el mismo "tipoCopia" que la copia guardada)
	 * @return la copia actualizada
	 * @throws IdInvalidoException               si el id no es un numero mayor que cero
	 * @throws CopiaNoEncontradaException        si no existe
	 * @throws DatosCopiaInvalidosException      si se intenta cambiar el tipo de copia
	 * @throws EstadoIntegridadInvalidoException si el estado de integridad no es valido
	 * @throws CampoObligatorioException         si falta un campo obligatorio del subtipo
	 * @throws TamanioInvalidoException          si el tamanio de una copia digital no es mayor que cero
	 */
	@Transactional
	public CopiaDTO actualizar(String idTexto, CopiaDTO copiaDTO) {
		long idCopia = LanzadorException.convertirId(idTexto, "copia");

		Copia copia = copiaRepository.buscarPorId(idCopia);
		LanzadorException.verificarCopiaExiste(copia, idCopia);

		TipoCopia tipoEnviado = obtenerTipoEnviado(copiaDTO);
		LanzadorException.verificarMismoTipoCopia(copia.getTipoCopia(), tipoEnviado);

		EstadoIntegridad estadoIntegridad = LanzadorException
				.convertirEstadoIntegridad(copiaDTO.getEstadoIntegridad());
		copia.setEstadoIntegridad(estadoIntegridad);

		if (copia instanceof CopiaFisica) {
			CopiaFisica copiaFisica = (CopiaFisica) copia;
			CopiaFisicaDTO copiaFisicaDTO = (CopiaFisicaDTO) copiaDTO;
			verificarDatosFisicos(copiaFisicaDTO);

			copiaFisica.setNumeroSerie(limpiarTextoOpcional(copiaFisicaDTO.getNumeroSerie()));
			copiaFisica.setUbicacionFisica(copiaFisicaDTO.getUbicacionFisica().trim());
			copiaFisica.setTipoSoporte(copiaFisicaDTO.getTipoSoporte().trim());
			copiaRepository.actualizarCopiaFisica(copiaFisica);
		} else {
			CopiaDigital copiaDigital = (CopiaDigital) copia;
			CopiaDigitalDTO copiaDigitalDTO = (CopiaDigitalDTO) copiaDTO;
			verificarDatosDigitales(copiaDigitalDTO);

			copiaDigital.setFormatoArchivo(copiaDigitalDTO.getFormatoArchivo().trim());
			copiaDigital.setTamanio(copiaDigitalDTO.getTamanio());
			copiaDigital.setHashVerificacion(copiaDigitalDTO.getHashVerificacion().trim());
			copiaRepository.actualizarCopiaDigital(copiaDigital);
		}

		return convertirADTO(copia);
	}

	/**
	 * Elimina una copia solo si no tiene custodias (ni vigentes ni historicas).
	 * Se borra de su tabla hija y de COPIA en la misma transaccion.
	 *
	 * @param idTexto id de la copia tal como llego en la URL
	 * @throws IdInvalidoException         si el id no es un numero mayor que cero
	 * @throws CopiaNoEncontradaException  si no existe
	 * @throws RegistrosAsociadosException si tiene custodias
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idCopia = LanzadorException.convertirId(idTexto, "copia");

		Copia copia = copiaRepository.buscarPorId(idCopia);
		LanzadorException.verificarCopiaExiste(copia, idCopia);

		boolean tieneCustodias = copiaRepository.contarCustodiasAsociadas(idCopia) > 0;
		LanzadorException.verificarSinRegistrosAsociados(tieneCustodias, "la copia", idCopia);

		copiaRepository.eliminar(idCopia);
	}


	/**
	 * Campos de una copia fisica: ubicacionFisica y tipoSoporte son obligatorios; numeroSerie es opcional.
	 */
	private void verificarDatosFisicos(CopiaFisicaDTO copiaFisicaDTO) {
		LanzadorException.verificarCampoObligatorio(copiaFisicaDTO.getUbicacionFisica(), "ubicacionFisica");
		LanzadorException.verificarCampoObligatorio(copiaFisicaDTO.getTipoSoporte(), "tipoSoporte");
	}

	/**
	 * Campos de una copia digital: formatoArchivo, hashVerificacion y tamanio (mayor que cero) son obligatorios.
	 */
	private void verificarDatosDigitales(CopiaDigitalDTO copiaDigitalDTO) {
		LanzadorException.verificarCampoObligatorio(copiaDigitalDTO.getFormatoArchivo(), "formatoArchivo");
		LanzadorException.verificarCampoObligatorio(copiaDigitalDTO.getHashVerificacion(), "hashVerificacion");
		LanzadorException.verificarTamanioValido(copiaDigitalDTO.getTamanio());
	}


	/**
	 * Jackson ya decidio el subtipo del DTO con el campo "tipoCopia";
	 * aqui se traduce esa clase al enum para compararlo con el de la copia guardada.
	 */
	private TipoCopia obtenerTipoEnviado(CopiaDTO copiaDTO) {
		if (copiaDTO instanceof CopiaFisicaDTO) {
			return TipoCopia.FISICA;
		}
		return TipoCopia.DIGITAL;
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