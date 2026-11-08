package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

import co.edu.unbosque.archiveludo.dto.PrioridadIntervencionDTO;
import co.edu.unbosque.archiveludo.dto.ResumenInicioDTO;
import co.edu.unbosque.archiveludo.entity.Copia;
import co.edu.unbosque.archiveludo.entity.CopiaFisica;
import co.edu.unbosque.archiveludo.entity.Custodia;
import co.edu.unbosque.archiveludo.entity.Estudio;
import co.edu.unbosque.archiveludo.entity.EventoDeRiesgo;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.model.EstadoConservacion;
import co.edu.unbosque.archiveludo.model.EstadoEstudio;
import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.model.TipoEventoRiesgo;
import co.edu.unbosque.archiveludo.repository.CopiaRepository;
import co.edu.unbosque.archiveludo.repository.CustodiaRepository;
import co.edu.unbosque.archiveludo.repository.EstudioRepository;
import co.edu.unbosque.archiveludo.repository.EventoRiesgoRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;
import co.edu.unbosque.archiveludo.service.InicioService;

/**
 * Pruebas unitarias del resumen de la pantalla de Inicio (InicioService).
 * Los repositories son simulados con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: el resumen trae los cuatro contadores y las tres listas cortas;
 * la prioridad de intervencion es el videojuego en riesgo con mas motivos (en empate, el primero);
 * los motivos se calculan bien; sin videojuegos en riesgo no hay prioridad.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InicioTest {

	private static final long ID_ESTUDIO_ACTIVO = 10L;
	private static final long ID_ESTUDIO_DESAPARECIDO = 20L;

	@Mock
	private VideojuegoRepository videojuegoRepository;

	@Mock
	private EstudioRepository estudioRepository;

	@Mock
	private EventoRiesgoRepository eventoRiesgoRepository;

	@Mock
	private CopiaRepository copiaRepository;

	@Mock
	private CustodiaRepository custodiaRepository;

	private InicioService inicioService;

	@BeforeEach
	public void prepararService() {
		inicioService = new InicioService(videojuegoRepository, estudioRepository, eventoRiesgoRepository,
				copiaRepository, custodiaRepository, new ModelMapper());

		when(estudioRepository.buscarPorId(ID_ESTUDIO_ACTIVO))
				.thenReturn(new Estudio(ID_ESTUDIO_ACTIVO, "Supergiant Games", "Estados Unidos", EstadoEstudio.ACTIVO,
						2009, null));
		when(estudioRepository.buscarPorId(ID_ESTUDIO_DESAPARECIDO))
				.thenReturn(new Estudio(ID_ESTUDIO_DESAPARECIDO, "Looking Glass Studios", "Estados Unidos",
						EstadoEstudio.DESAPARECIDO, 1990, 2000));
	}

	@Test
	@DisplayName("El resumen trae los contadores y listas de maximo 4 videojuegos")
	public void generarResumenContadoresTest() {
		when(videojuegoRepository.contarCatalogo(null, null)).thenReturn(16L);
		when(videojuegoRepository.contarCatalogo(EstadoConservacion.EN_RIESGO, null)).thenReturn(4L);
		when(videojuegoRepository.contarCatalogo(EstadoConservacion.TOTALMENTE_PERDIDO, null)).thenReturn(2L);
		when(videojuegoRepository.contarCatalogo(EstadoConservacion.RESTAURADO, null)).thenReturn(2L);

		List<Videojuego> enRiesgo = new ArrayList<Videojuego>();
		enRiesgo.add(crearVideojuego(1L, "Thief", ID_ESTUDIO_ACTIVO));
		enRiesgo.add(crearVideojuego(2L, "System Shock", ID_ESTUDIO_DESAPARECIDO));
		when(videojuegoRepository.buscarCatalogo(EstadoConservacion.EN_RIESGO, null, 4, 0)).thenReturn(enRiesgo);

		ResumenInicioDTO resumen = inicioService.generarResumen();

		assertEquals(16L, resumen.getTitulosCatalogados());
		assertEquals(4L, resumen.getEnRiesgo());
		assertEquals(2L, resumen.getTotalmentePerdidos());
		assertEquals(2L, resumen.getRestaurados());
		assertEquals(2, resumen.getEnRiesgoLista().size());
		assertEquals("en riesgo", resumen.getEnRiesgoLista().get(0).getEstadoConservacion());
	}

	@Test
	@DisplayName("La prioridad es el videojuego en riesgo con mas motivos")
	public void prioridadGanaElDeMasMotivosTest() {
		Videojuego conPocosMotivos = crearVideojuego(1L, "Thief", ID_ESTUDIO_ACTIVO);
		Videojuego conMasMotivos = crearVideojuego(2L, "System Shock", ID_ESTUDIO_DESAPARECIDO);

		List<Videojuego> enRiesgo = new ArrayList<Videojuego>();
		enRiesgo.add(conPocosMotivos);
		enRiesgo.add(conMasMotivos);
		when(videojuegoRepository.buscarPorEstadoConservacion(EstadoConservacion.EN_RIESGO)).thenReturn(enRiesgo);

		// Thief: estudio activo, sin eventos, con una copia custodiada -> 0 motivos
		when(copiaRepository.buscarPorIdVideojuego(1L)).thenReturn(crearListaCopias(100L));
		when(custodiaRepository.buscarVigentePorIdCopia(100L)).thenReturn(new Custodia());

		// System Shock: estudio desaparecido, 3 eventos, sin copias -> 3 motivos
		when(eventoRiesgoRepository.buscarPorIdVideojuego(2L)).thenReturn(crearListaEventos(3));

		PrioridadIntervencionDTO prioridad = inicioService.generarResumen().getPrioridadIntervencion();

		assertNotNull(prioridad);
		assertEquals("System Shock", prioridad.getVideojuego().getTitulo());
		assertEquals("Looking Glass Studios", prioridad.getNombreEstudio());
		assertEquals(3, prioridad.getMotivos().size());
		assertTrue(prioridad.getMotivos().contains("Estudio desarrollador desaparecido"));
		assertTrue(prioridad.getMotivos().contains("3 eventos de riesgo registrados"));
		assertTrue(prioridad.getMotivos().contains("No tiene copias registradas"));
	}

	@Test
	@DisplayName("En un empate de motivos gana el primero del catalogo")
	public void prioridadEmpateGanaElPrimeroTest() {
		List<Videojuego> enRiesgo = new ArrayList<Videojuego>();
		enRiesgo.add(crearVideojuego(1L, "Thief", ID_ESTUDIO_ACTIVO));
		enRiesgo.add(crearVideojuego(2L, "Ultima Underworld", ID_ESTUDIO_ACTIVO));
		when(videojuegoRepository.buscarPorEstadoConservacion(EstadoConservacion.EN_RIESGO)).thenReturn(enRiesgo);
		// Ninguno tiene copias: los dos quedan con 1 motivo

		PrioridadIntervencionDTO prioridad = inicioService.generarResumen().getPrioridadIntervencion();

		assertEquals("Thief", prioridad.getVideojuego().getTitulo());
		assertEquals(1, prioridad.getMotivos().size());
	}

	@Test
	@DisplayName("Con copias pero ninguna custodiada el motivo no es 'sin copias'")
	public void prioridadCopiasSinCustodiaTest() {
		List<Videojuego> enRiesgo = new ArrayList<Videojuego>();
		enRiesgo.add(crearVideojuego(1L, "Thief", ID_ESTUDIO_ACTIVO));
		when(videojuegoRepository.buscarPorEstadoConservacion(EstadoConservacion.EN_RIESGO)).thenReturn(enRiesgo);
		when(copiaRepository.buscarPorIdVideojuego(1L)).thenReturn(crearListaCopias(100L));
		when(custodiaRepository.buscarVigentePorIdCopia(100L)).thenReturn(null);
		when(eventoRiesgoRepository.buscarPorIdVideojuego(1L)).thenReturn(crearListaEventos(1));

		List<String> motivos = inicioService.generarResumen().getPrioridadIntervencion().getMotivos();

		assertTrue(motivos.contains("Ninguna copia bajo custodia vigente"));
		assertTrue(motivos.contains("1 evento de riesgo registrado"));
		assertFalse(motivos.contains("No tiene copias registradas"));
	}

	@Test
	@DisplayName("Sin videojuegos en riesgo no hay prioridad")
	public void sinVideojuegosEnRiesgoTest() {
		when(videojuegoRepository.buscarPorEstadoConservacion(EstadoConservacion.EN_RIESGO))
				.thenReturn(new ArrayList<Videojuego>());

		ResumenInicioDTO resumen = inicioService.generarResumen();

		assertNull(resumen.getPrioridadIntervencion());
	}

	// ──────────────────────────────────────────────
	// Datos de prueba
	// ──────────────────────────────────────────────

	private Videojuego crearVideojuego(long idVideojuego, String titulo, long idEstudio) {
		Videojuego videojuego = new Videojuego();
		videojuego.setIdVideojuego(idVideojuego);
		videojuego.setTitulo(titulo);
		videojuego.setPlataformaOriginal("MS-DOS");
		videojuego.setAnioLanzamiento(1994);
		videojuego.setEstadoConservacion(EstadoConservacion.EN_RIESGO);
		videojuego.setIdEstudio(idEstudio);
		return videojuego;
	}

	private List<Copia> crearListaCopias(long idCopia) {
		CopiaFisica copia = new CopiaFisica(1L, 2L, EstadoIntegridad.buscarPorValor("buena"), LocalDate.now(),
				"SN-001", "Bodega A", "Disquete");
		copia.setIdCopia(idCopia);

		List<Copia> copias = new ArrayList<Copia>();
		copias.add(copia);
		return copias;
	}

	private List<EventoDeRiesgo> crearListaEventos(int cantidad) {
		List<EventoDeRiesgo> eventos = new ArrayList<EventoDeRiesgo>();
		for (int i = 0; i < cantidad; i++) {
			eventos.add(new EventoDeRiesgo(2L, 2L, TipoEventoRiesgo.buscarPorValor("deterioro de medios"),
					LocalDate.now().minusDays(i + 1), "Deterioro detectado", null));
		}
		return eventos;
	}

}