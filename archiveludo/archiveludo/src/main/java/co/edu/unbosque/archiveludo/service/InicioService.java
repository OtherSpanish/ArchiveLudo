package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.dto.PrioridadIntervencionDTO;
import co.edu.unbosque.archiveludo.dto.ResumenInicioDTO;
import co.edu.unbosque.archiveludo.dto.VideojuegoDTO;
import co.edu.unbosque.archiveludo.entity.Copia;
import co.edu.unbosque.archiveludo.entity.Estudio;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.model.EstadoConservacion;
import co.edu.unbosque.archiveludo.model.EstadoEstudio;
import co.edu.unbosque.archiveludo.repository.CopiaRepository;
import co.edu.unbosque.archiveludo.repository.CustodiaRepository;
import co.edu.unbosque.archiveludo.repository.EstudioRepository;
import co.edu.unbosque.archiveludo.repository.EventoRiesgoRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Arma el resumen de la pantalla de Inicio: contadores, listas cortas y el videojuego prioritario.
 *
 * Regla de prioridad de intervencion:
 * 1. Solo compiten los videojuegos en riesgo.
 * 2. Cada uno acumula motivos: estudio desaparecido, eventos de riesgo registrados,
 *    sin copias, o con copias pero ninguna bajo custodia vigente.
 * 3. Gana el de mas motivos; en empate, el primero en el orden del catalogo (el mas antiguo).
 * 4. Si no hay videojuegos en riesgo, no hay prioridad (null).
 */
@Service
public class InicioService {

	/** Cuantos videojuegos trae cada lista corta de Inicio. */
	private static final int TAMANIO_LISTA = 4;

	private final VideojuegoRepository videojuegoRepository;
	private final EstudioRepository estudioRepository;
	private final EventoRiesgoRepository eventoRiesgoRepository;
	private final CopiaRepository copiaRepository;
	private final CustodiaRepository custodiaRepository;
	private final ModelMapper mapper;

	public InicioService(VideojuegoRepository videojuegoRepository, EstudioRepository estudioRepository,
			EventoRiesgoRepository eventoRiesgoRepository, CopiaRepository copiaRepository,
			CustodiaRepository custodiaRepository, ModelMapper mapper) {
		this.videojuegoRepository = videojuegoRepository;
		this.estudioRepository = estudioRepository;
		this.eventoRiesgoRepository = eventoRiesgoRepository;
		this.copiaRepository = copiaRepository;
		this.custodiaRepository = custodiaRepository;
		this.mapper = mapper;
	}

	/**
	 * @return el resumen completo de Inicio
	 */
	public ResumenInicioDTO generarResumen() {
		ResumenInicioDTO resumen = new ResumenInicioDTO();

		resumen.setTitulosCatalogados(videojuegoRepository.contarCatalogo(null, null));
		resumen.setEnRiesgo(videojuegoRepository.contarCatalogo(EstadoConservacion.EN_RIESGO, null));
		resumen.setTotalmentePerdidos(videojuegoRepository.contarCatalogo(EstadoConservacion.TOTALMENTE_PERDIDO, null));
		resumen.setRestaurados(videojuegoRepository.contarCatalogo(EstadoConservacion.RESTAURADO, null));

		resumen.setEnRiesgoLista(buscarListaCorta(EstadoConservacion.EN_RIESGO));
		resumen.setPerdidosLista(buscarListaCorta(EstadoConservacion.TOTALMENTE_PERDIDO));
		resumen.setRestauradosLista(buscarListaCorta(EstadoConservacion.RESTAURADO));

		resumen.setPrioridadIntervencion(calcularPrioridad());
		return resumen;
	}

	/**
	 * Los primeros videojuegos de un estado, en el mismo orden del catalogo.
	 */
	private List<VideojuegoDTO> buscarListaCorta(EstadoConservacion estado) {
		List<VideojuegoDTO> lista = new ArrayList<VideojuegoDTO>();
		for (Videojuego videojuego : videojuegoRepository.buscarCatalogo(estado, null, TAMANIO_LISTA, 0)) {
			lista.add(convertirADTO(videojuego));
		}
		return lista;
	}

	/**
	 * Recorre los videojuegos en riesgo y se queda con el de mas motivos.
	 * Como vienen en el orden del catalogo y solo se reemplaza con "mas" motivos (no "igual"),
	 * en un empate gana el que aparece primero.
	 *
	 * @return la prioridad, o null si no hay videojuegos en riesgo
	 */
	private PrioridadIntervencionDTO calcularPrioridad() {
		Videojuego elegido = null;
		List<String> motivosElegido = null;

		for (Videojuego videojuego : videojuegoRepository.buscarPorEstadoConservacion(EstadoConservacion.EN_RIESGO)) {
			List<String> motivos = calcularMotivos(videojuego);
			if (elegido == null || motivos.size() > motivosElegido.size()) {
				elegido = videojuego;
				motivosElegido = motivos;
			}
		}

		if (elegido == null) {
			return null;
		}

		String nombreEstudio = null;
		Estudio estudio = buscarEstudio(elegido);
		if (estudio != null) {
			nombreEstudio = estudio.getNombre();
		}
		return new PrioridadIntervencionDTO(convertirADTO(elegido), nombreEstudio, motivosElegido);
	}

	/**
	 * Arma la lista de motivos de un videojuego. "Sin copias" y "ninguna copia custodiada"
	 * nunca salen juntos: el segundo solo aplica si tiene copias.
	 */
	private List<String> calcularMotivos(Videojuego videojuego) {
		List<String> motivos = new ArrayList<String>();
		long idVideojuego = videojuego.getIdVideojuego();

		Estudio estudio = buscarEstudio(videojuego);
		if (estudio != null && estudio.getEstadoActual() == EstadoEstudio.DESAPARECIDO) {
			motivos.add("Estudio desarrollador desaparecido");
		}

		int cantidadEventos = eventoRiesgoRepository.buscarPorIdVideojuego(idVideojuego).size();
		if (cantidadEventos == 1) {
			motivos.add("1 evento de riesgo registrado");
		} else if (cantidadEventos > 1) {
			motivos.add(cantidadEventos + " eventos de riesgo registrados");
		}

		List<Copia> copias = copiaRepository.buscarPorIdVideojuego(idVideojuego);
		if (copias.isEmpty()) {
			motivos.add("No tiene copias registradas");
		} else if (!tieneCopiaCustodiada(copias)) {
			motivos.add("Ninguna copia bajo custodia vigente");
		}

		return motivos;
	}

	/**
	 * @return true si al menos una de las copias tiene custodia vigente
	 */
	private boolean tieneCopiaCustodiada(List<Copia> copias) {
		for (Copia copia : copias) {
			if (custodiaRepository.buscarVigentePorIdCopia(copia.getIdCopia()) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return el estudio del videojuego, o null si no tiene (id_estudio en NULL llega como 0)
	 */
	private Estudio buscarEstudio(Videojuego videojuego) {
		if (videojuego.getIdEstudio() <= 0) {
			return null;
		}
		return estudioRepository.buscarPorId(videojuego.getIdEstudio());
	}

	/**
	 * Convierte la entity a DTO; el estado de conservacion pasa a su texto ("en riesgo").
	 */
	private VideojuegoDTO convertirADTO(Videojuego videojuego) {
		VideojuegoDTO videojuegoDTO = mapper.map(videojuego, VideojuegoDTO.class);
		if (videojuego.getEstadoConservacion() != null) {
			videojuegoDTO.setEstadoConservacion(videojuego.getEstadoConservacion().getValor());
		}
		return videojuegoDTO;
	}

}