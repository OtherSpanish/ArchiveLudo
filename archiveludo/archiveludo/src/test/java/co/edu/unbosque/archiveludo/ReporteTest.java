package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import co.edu.unbosque.archiveludo.dto.ReporteDTO;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.DatosReporteInvalidosException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.repository.ReporteRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;
import co.edu.unbosque.archiveludo.service.ReporteService;

/**
 * Pruebas unitarias de los reportes (ReporteService).
 * Los repositories son simulados con Mockito: las filas que "devuelve MySQL" se arman a mano.
 *
 * Criterio de aceptacion: cada reporte sale con su titulo, columnas y filas en texto legible
 * (fechas dd/MM/yyyy, "Vigente", "Sí"/"No"); los filtros llegan al repository convertidos;
 * se rechazan tipos inexistentes, fechas mal escritas, rangos invertidos y videojuegos inexistentes.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReporteTest {

	private static final long ID_VIDEOJUEGO = 16L;

	@Mock
	private ReporteRepository reporteRepository;

	@Mock
	private VideojuegoRepository videojuegoRepository;

	private ReporteService reporteService;

	@BeforeEach
	public void prepararService() {
		reporteService = new ReporteService(reporteRepository, videojuegoRepository);

		Videojuego videojuego = new Videojuego();
		videojuego.setIdVideojuego(ID_VIDEOJUEGO);
		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(videojuego);
	}

	@Test
	@DisplayName("El reporte de eventos sale con fechas y textos legibles")
	public void generarReporteEventosTest() {
		Map<String, Object> fila = new HashMap<String, Object>();
		fila.put("fecha_evento", Date.valueOf(LocalDate.of(2026, 10, 15)));
		fila.put("titulo", "System Shock");
		fila.put("tipo_evento", "cierre de servidores");
		fila.put("descripcion", "Se apagaron los servidores");
		fila.put("fuente", null);
		fila.put("primer_nombre", "Camila");
		fila.put("primer_apellido", "Torres");
		when(reporteRepository.buscarEventos(null, null, null)).thenReturn(crearFilas(fila));

		ReporteDTO reporte = reporteService.generar("eventos", null, null, null);

		assertEquals("Reporte de eventos de riesgo", reporte.getTitulo());
		assertEquals(6, reporte.getColumnas().size());
		List<String> celdas = reporte.getFilas().get(0);
		assertEquals("15/10/2026", celdas.get(0));
		assertEquals("Cierre de servidores", celdas.get(2));
		assertEquals("", celdas.get(4));
		assertEquals("Camila Torres", celdas.get(5));
	}

	@Test
	@DisplayName("En el reporte de custodias una custodia sin fin dice Vigente")
	public void generarReporteCustodiasTest() {
		Map<String, Object> fila = new HashMap<String, Object>();
		fila.put("id_copia", 4L);
		fila.put("titulo", "System Shock");
		fila.put("institucion", "Archivo Digital Universitario");
		fila.put("fecha_inicio", Date.valueOf(LocalDate.of(2026, 11, 1)));
		fila.put("fecha_fin", null);
		fila.put("condicion_almacenamiento", "Servidor con respaldo diario");
		when(reporteRepository.buscarCustodias(null, null, null)).thenReturn(crearFilas(fila));

		ReporteDTO reporte = reporteService.generar("custodias", null, null, null);

		List<String> celdas = reporte.getFilas().get(0);
		assertEquals("4", celdas.get(0));
		assertEquals("01/11/2026", celdas.get(3));
		assertEquals("Vigente", celdas.get(4));
	}

	@Test
	@DisplayName("En el reporte de proyectos el informe sale como Si o No")
	public void generarReporteProyectosTest() {
		Map<String, Object> conInforme = new HashMap<String, Object>();
		conInforme.put("nombre", "Rescate de System Shock");
		conInforme.put("estado", "finalizado");
		conInforme.put("cantidad_expertos", 2L);
		conInforme.put("url_informe", "https://res.cloudinary.com/demo/informe.pdf");

		Map<String, Object> sinInforme = new HashMap<String, Object>();
		sinInforme.put("nombre", "Rescate de Thief");
		sinInforme.put("estado", "activo");
		sinInforme.put("cantidad_expertos", 1L);
		sinInforme.put("url_informe", null);

		List<Map<String, Object>> filas = crearFilas(conInforme);
		filas.add(sinInforme);
		when(reporteRepository.buscarProyectos(null, null, null)).thenReturn(filas);

		ReporteDTO reporte = reporteService.generar("proyectos", null, null, null);

		assertEquals("Finalizado", reporte.getFilas().get(0).get(2));
		assertEquals("2", reporte.getFilas().get(0).get(6));
		assertEquals("Sí", reporte.getFilas().get(0).get(7));
		assertEquals("No", reporte.getFilas().get(1).get(7));
	}

	@Test
	@DisplayName("Los filtros llegan convertidos al repository")
	public void generarReporteConFiltrosTest() {
		LocalDate desde = LocalDate.of(2026, 1, 1);
		LocalDate hasta = LocalDate.of(2026, 12, 31);
		when(reporteRepository.buscarCopias(ID_VIDEOJUEGO, desde, hasta))
				.thenReturn(new ArrayList<Map<String, Object>>());

		ReporteDTO reporte = reporteService.generar("copias", "16", "2026-01-01", "2026-12-31");

		assertEquals(0, reporte.getFilas().size());
		verify(reporteRepository, times(1)).buscarCopias(ID_VIDEOJUEGO, desde, hasta);
	}

	@Test
	@DisplayName("No se genera un reporte de un tipo que no existe")
	public void generarReporteTipoInvalidoTest() {
		try {
			reporteService.generar("ventas", null, null, null);
			fail("Debio lanzar DatosReporteInvalidosException");
		} catch (DatosReporteInvalidosException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("No se genera un reporte con una fecha mal escrita")
	public void generarReporteFechaInvalidaTest() {
		try {
			reporteService.generar("eventos", null, "2026-13-01", null);
			fail("Debio lanzar DatosReporteInvalidosException");
		} catch (DatosReporteInvalidosException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("No se genera un reporte con desde posterior a hasta")
	public void generarReporteRangoInvertidoTest() {
		try {
			reporteService.generar("eventos", null, "2026-12-01", "2026-01-01");
			fail("Debio lanzar DatosReporteInvalidosException");
		} catch (DatosReporteInvalidosException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("No se genera un reporte de un videojuego que no existe")
	public void generarReporteVideojuegoInexistenteTest() {
		when(videojuegoRepository.buscarPorId(999L)).thenReturn(null);

		try {
			reporteService.generar("copias", "999", null, null);
			fail("Debio lanzar VideojuegoNoEncontradoException");
		} catch (VideojuegoNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}


	private List<Map<String, Object>> crearFilas(Map<String, Object> fila) {
		List<Map<String, Object>> filas = new ArrayList<Map<String, Object>>();
		filas.add(fila);
		return filas;
	}

}