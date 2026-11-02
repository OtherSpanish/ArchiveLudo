package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.VideojuegoDTO;
import co.edu.unbosque.archiveludo.entity.CompaniaEditora;
import co.edu.unbosque.archiveludo.entity.Estudio;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.EstadoConservacion;
import co.edu.unbosque.archiveludo.repository.CompaniaEditoraRepository;
import co.edu.unbosque.archiveludo.repository.EstudioRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio de videojuegos: ficha, catalogo paginado, busquedas y CRUD.
 */
@Service
public class VideojuegoService {

	private final VideojuegoRepository videojuegoRepository;
	private final EstudioRepository estudioRepository;
	private final CompaniaEditoraRepository companiaEditoraRepository;
	private final UsuarioRepository usuarioRepository;
	private final ModelMapper mapper;

	public VideojuegoService(VideojuegoRepository videojuegoRepository, EstudioRepository estudioRepository,
			CompaniaEditoraRepository companiaEditoraRepository, UsuarioRepository usuarioRepository,
			ModelMapper mapper) {
		this.videojuegoRepository = videojuegoRepository;
		this.estudioRepository = estudioRepository;
		this.companiaEditoraRepository = companiaEditoraRepository;
		this.usuarioRepository = usuarioRepository;
		this.mapper = mapper;
	}

	
	/**
	 * Busca un videojuego por el id que llega en la URL.
	 *
	 * @throws IdInvalidoException             si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException si no existe un videojuego con ese id
	 */
	public VideojuegoDTO buscarPorId(String idTexto) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		return convertirADTO(videojuego);
	}

	/**
	 * Devuelve una pagina del catalogo. Todos los parametros son opcionales.
	 *
	 * @throws EstadoConservacionInvalidoException si el estado no es uno de los 5 permitidos
	 * @throws PaginacionInvalidaException         si la pagina o el limite no son validos
	 */
	public List<VideojuegoDTO> buscarCatalogo(String estadoTexto, String textoTitulo, String paginaTexto,
			String limiteTexto) {
		EstadoConservacion estado = convertirEstadoOpcional(estadoTexto);
		String patronTitulo = construirPatronTituloOpcional(textoTitulo);
		int pagina = LanzadorException.convertirPagina(paginaTexto);
		int limite = LanzadorException.convertirLimite(limiteTexto);

		int desplazamiento = pagina * limite;

		List<Videojuego> videojuegos = videojuegoRepository.buscarCatalogo(estado, patronTitulo, limite,
				desplazamiento);
		return convertirListaADTO(videojuegos);
	}

	/**
	 * Cuenta cuantos videojuegos cumplen los filtros del catalogo, en toda la base.
	 *
	 * @throws EstadoConservacionInvalidoException si el estado no es uno de los 5 permitidos
	 */
	public long contarCatalogo(String estadoTexto, String textoTitulo) {
		EstadoConservacion estado = convertirEstadoOpcional(estadoTexto);
		String patronTitulo = construirPatronTituloOpcional(textoTitulo);

		return videojuegoRepository.contarCatalogo(estado, patronTitulo);
	}

	/**
	 * @throws CampoObligatorioException si el titulo viene vacio
	 */
	public List<VideojuegoDTO> buscarPorTitulo(String titulo) {
		LanzadorException.verificarCampoObligatorio(titulo, "titulo");

		String patronTitulo = "%" + titulo.trim() + "%";
		List<Videojuego> videojuegos = videojuegoRepository.buscarPorTitulo(patronTitulo);
		return convertirListaADTO(videojuegos);
	}

	/**
	 * @throws CampoObligatorioException si el genero viene vacio
	 */
	public List<VideojuegoDTO> buscarPorGenero(String genero) {
		LanzadorException.verificarCampoObligatorio(genero, "genero");

		List<Videojuego> videojuegos = videojuegoRepository.buscarPorGenero(genero.trim());
		return convertirListaADTO(videojuegos);
	}

	/**
	 * @throws CampoObligatorioException si la plataforma viene vacia
	 */
	public List<VideojuegoDTO> buscarPorPlataformaOriginal(String plataformaOriginal) {
		LanzadorException.verificarCampoObligatorio(plataformaOriginal, "plataformaOriginal");

		List<Videojuego> videojuegos = videojuegoRepository.buscarPorPlataformaOriginal(plataformaOriginal.trim());
		return convertirListaADTO(videojuegos);
	}

	/**
	 * @throws AnioInvalidoException si el anio no es numerico o esta fuera de rango
	 */
	public List<VideojuegoDTO> buscarPorAnioLanzamiento(String anioTexto) {
		int anioLanzamiento = LanzadorException.convertirAnio(anioTexto);

		List<Videojuego> videojuegos = videojuegoRepository.buscarPorAnioLanzamiento(anioLanzamiento);
		return convertirListaADTO(videojuegos);
	}

	/**
	 * @throws EstadoConservacionInvalidoException si el estado no es uno de los 5 permitidos
	 */
	public List<VideojuegoDTO> buscarPorEstadoConservacion(String textoEstado) {
		EstadoConservacion estado = LanzadorException.convertirEstadoConservacion(textoEstado);

		List<Videojuego> videojuegos = videojuegoRepository.buscarPorEstadoConservacion(estado);
		return convertirListaADTO(videojuegos);
	}

	/**
	 * Crea un videojuego con su estudio y sus editoras.
	 * El usuario que lo registra sale del token, no del cuerpo.
	 *
	 * @param videojuegoDTO datos del videojuego
	 * @param correoUsuario correo del usuario autenticado
	 * @return el videojuego creado, con su id
	 * @throws CampoObligatorioException            si falta titulo, genero o plataforma
	 * @throws AnioInvalidoException                si el anio de lanzamiento esta fuera de rango
	 * @throws EstadoConservacionInvalidoException  si el estado no es valido
	 * @throws EstudioNoEncontradoException         si el estudio no existe
	 * @throws CompaniaEditoraNoEncontradaException si alguna editora no existe
	 * @throws CredencialInvalidaException          si el usuario del token ya no existe
	 */
	@Transactional
	public VideojuegoDTO crear(VideojuegoDTO videojuegoDTO, String correoUsuario) {
		Usuario usuario = usuarioRepository.buscarPorCorreo(correoUsuario);
		LanzadorException.verificarCredencialesCorrectas(usuario != null);

		Videojuego videojuego = new Videojuego();
		llenarDatosValidados(videojuego, videojuegoDTO);
		videojuego.setIdUsuario(usuario.getIdUsuario());

		Videojuego videojuegoGuardado = videojuegoRepository.crear(videojuego);
		return convertirADTO(videojuegoGuardado);
	}

	/**
	 * Actualiza un videojuego. La lista de editoras enviada REEMPLAZA a la anterior.
	 * El usuario que lo registro no cambia.
	 *
	 * @param idTexto       id tal como llego en la URL
	 * @param videojuegoDTO datos nuevos
	 * @return el videojuego actualizado
	 * @throws IdInvalidoException                  si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException      si el videojuego no existe
	 * @throws CampoObligatorioException            si falta titulo, genero o plataforma
	 * @throws AnioInvalidoException                si el anio de lanzamiento esta fuera de rango
	 * @throws EstadoConservacionInvalidoException  si el estado no es valido
	 * @throws EstudioNoEncontradoException         si el estudio no existe
	 * @throws CompaniaEditoraNoEncontradaException si alguna editora no existe
	 */
	@Transactional
	public VideojuegoDTO actualizar(String idTexto, VideojuegoDTO videojuegoDTO) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		llenarDatosValidados(videojuego, videojuegoDTO);

		videojuegoRepository.actualizar(videojuego);
		return convertirADTO(videojuego);
	}

	/**
	 * Elimina un videojuego solo si no tiene copias, estatus legales ni eventos de riesgo.
	 * Su relacion con las editoras se borra junto con el (es parte del propio videojuego).
	 *
	 * @param idTexto id tal como llego en la URL
	 * @throws IdInvalidoException             si el id no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException si no existe
	 * @throws RegistrosAsociadosException     si tiene registros asociados
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idVideojuego = LanzadorException.convertirId(idTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);

		boolean tieneRegistrosAsociados = videojuegoRepository.contarRegistrosAsociados(idVideojuego) > 0;
		LanzadorException.verificarSinRegistrosAsociados(tieneRegistrosAsociados, "el videojuego", idVideojuego);

		videojuegoRepository.eliminar(idVideojuego);
	}

	/**
	 * Valida todos los datos que llegan del front (comunes a crear y actualizar)
	 * y los copia a la entity.
	 */
	private void llenarDatosValidados(Videojuego videojuego, VideojuegoDTO videojuegoDTO) {
		LanzadorException.verificarCampoObligatorio(videojuegoDTO.getTitulo(), "titulo");
		LanzadorException.verificarCampoObligatorio(videojuegoDTO.getGenero(), "genero");
		LanzadorException.verificarCampoObligatorio(videojuegoDTO.getPlataformaOriginal(), "plataformaOriginal");
		LanzadorException.verificarAnioLanzamiento(videojuegoDTO.getAnioLanzamiento());

		EstadoConservacion estado = LanzadorException
				.convertirEstadoConservacion(videojuegoDTO.getEstadoConservacion());

		long idEstudio = videojuegoDTO.getIdEstudio();
		LanzadorException.verificarIdValido(idEstudio, "estudio");
		Estudio estudio = estudioRepository.buscarPorId(idEstudio);
		LanzadorException.verificarEstudioExiste(estudio, idEstudio);

		List<Long> idsEditoras = verificarEditoras(videojuegoDTO.getIdsEditoras());

		videojuego.setTitulo(videojuegoDTO.getTitulo().trim());
		videojuego.setGenero(videojuegoDTO.getGenero().trim());
		videojuego.setPlataformaOriginal(videojuegoDTO.getPlataformaOriginal().trim());
		videojuego.setDescripcion(limpiarTextoOpcional(videojuegoDTO.getDescripcion()));
		videojuego.setAnioLanzamiento(videojuegoDTO.getAnioLanzamiento());
		videojuego.setEstadoConservacion(estado);
		videojuego.setIdEstudio(idEstudio);
		videojuego.setIdsEditoras(idsEditoras);
	}

	/**
	 * Verifica que cada editora exista y quita los ids repetidos.
	 * Una lista vacia (o que no se envio) es valida: un videojuego puede no tener editora registrada.
	 */
	private List<Long> verificarEditoras(List<Long> idsEnviados) {
		List<Long> idsSinRepetir = new ArrayList<Long>();
		if (idsEnviados == null) {
			return idsSinRepetir;
		}

		for (Long idEditora : idsEnviados) {
			LanzadorException.verificarIdValido(idEditora, "compañia editora");

			if (!idsSinRepetir.contains(idEditora)) {
				CompaniaEditora editora = companiaEditoraRepository.buscarPorId(idEditora);
				LanzadorException.verificarCompaniaEditoraExiste(editora, idEditora);
				idsSinRepetir.add(idEditora);
			}
		}
		return idsSinRepetir;
	}

	/**
	 * En el catalogo el estado es opcional: si viene vacio se devuelven todos los estados.
	 */
	private EstadoConservacion convertirEstadoOpcional(String estadoTexto) {
		if (estadoTexto == null || estadoTexto.trim().isEmpty()) {
			return null;
		}
		return LanzadorException.convertirEstadoConservacion(estadoTexto);
	}

	/**
	 * En el catalogo el texto del titulo es opcional: si viene vacio no se filtra por titulo.
	 */
	private String construirPatronTituloOpcional(String textoTitulo) {
		if (textoTitulo == null || textoTitulo.trim().isEmpty()) {
			return null;
		}
		return "%" + textoTitulo.trim() + "%";
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

	private List<VideojuegoDTO> convertirListaADTO(List<Videojuego> videojuegos) {
		List<VideojuegoDTO> videojuegosDTO = new ArrayList<VideojuegoDTO>();
		for (Videojuego videojuego : videojuegos) {
			videojuegosDTO.add(convertirADTO(videojuego));
		}
		return videojuegosDTO;
	}

	/**
	 * Convierte la entity a DTO. El estado de conservacion se pasa a su texto ("en riesgo").
	 */
	private VideojuegoDTO convertirADTO(Videojuego videojuego) {
		VideojuegoDTO videojuegoDTO = mapper.map(videojuego, VideojuegoDTO.class);
		if (videojuego.getEstadoConservacion() != null) {
			videojuegoDTO.setEstadoConservacion(videojuego.getEstadoConservacion().getValor());
		}
		return videojuegoDTO;
	}

}