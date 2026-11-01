package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.dto.VideojuegoDTO;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.EstadoConservacion;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Logica de negocio de videojuegos: ficha, catalogo paginado y busquedas por cada atributo.
 */
@Service
public class VideojuegoService {

	private final VideojuegoRepository videojuegoRepository;
	private final ModelMapper mapper;

	public VideojuegoService(VideojuegoRepository videojuegoRepository, ModelMapper mapper) {
		this.videojuegoRepository = videojuegoRepository;
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
	 * @param estadoTexto estado de conservacion ("en riesgo"), o vacio para todos
	 * @param textoTitulo texto que debe contener el titulo, o vacio para todos
	 * @param paginaTexto numero de pagina desde 0 (por defecto 0)
	 * @param limiteTexto videojuegos por pagina, de 1 a 50 (por defecto 10)
	 * @return los videojuegos de esa pagina
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
	 * El front lo usa para saber cuantas paginas hay.
	 *
	 * @throws EstadoConservacionInvalidoException si el estado no es uno de los 5 permitidos
	 */
	public long contarCatalogo(String estadoTexto, String textoTitulo) {
		EstadoConservacion estado = convertirEstadoOpcional(estadoTexto);
		String patronTitulo = construirPatronTituloOpcional(textoTitulo);

		return videojuegoRepository.contarCatalogo(estado, patronTitulo);
	}

	/**
	 * Busca los videojuegos cuyo titulo CONTIENE el texto (por ejemplo "zelda").
	 *
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
	 * Convierte una lista de entities a una lista de DTO.
	 */
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