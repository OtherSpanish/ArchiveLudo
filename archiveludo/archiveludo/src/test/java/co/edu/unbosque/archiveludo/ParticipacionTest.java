package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import co.edu.unbosque.archiveludo.dto.ParticipacionDTO;
import co.edu.unbosque.archiveludo.entity.Experto;
import co.edu.unbosque.archiveludo.entity.Participacion;
import co.edu.unbosque.archiveludo.entity.ProyectoDeRestauracion;
import co.edu.unbosque.archiveludo.exceptions.DatoProyectoInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.ExpertoNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.ExpertoRepetidoException;
import co.edu.unbosque.archiveludo.exceptions.ParticipacionNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.UltimaParticipacionException;
import co.edu.unbosque.archiveludo.model.EstadoProyecto;
import co.edu.unbosque.archiveludo.repository.ExpertoRepository;
import co.edu.unbosque.archiveludo.repository.ParticipacionRepository;
import co.edu.unbosque.archiveludo.repository.ProyectoDeRestauracionRepository;
import co.edu.unbosque.archiveludo.service.ParticipacionService;

/**
 * Pruebas unitarias del CRUD de participaciones (ParticipacionService).
 * Los repositories son simulados con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: se puede agregar, leer, actualizar y quitar un experto de un proyecto;
 * un experto no puede estar dos veces en el mismo proyecto; las fechas deben ser coherentes
 * con el inicio del proyecto; no se quita la unica participacion de un proyecto.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParticipacionTest {

	private static final long ID_PROYECTO = 7L;
	private static final long ID_EXPERTO = 3L;
	private static final long ID_PARTICIPACION = 9L;
	private static final LocalDate INICIO_PROYECTO = LocalDate.now().minusDays(60);

	@Mock
	private ParticipacionRepository participacionRepository;

	@Mock
	private ProyectoDeRestauracionRepository proyectoRepository;

	@Mock
	private ExpertoRepository expertoRepository;

	private ParticipacionService participacionService;

	@BeforeEach
	public void prepararService() {
		participacionService = new ParticipacionService(participacionRepository, proyectoRepository,
				expertoRepository, new ModelMapper());

		when(proyectoRepository.buscarPorId(ID_PROYECTO)).thenReturn(crearProyecto());
		when(expertoRepository.buscarPorId(ID_EXPERTO)).thenReturn(crearExperto());
		when(participacionRepository.existeExpertoEnProyecto(ID_PROYECTO, ID_EXPERTO)).thenReturn(false);
	}


	@Test
	@DisplayName("Agregar un experto a un proyecto")
	public void crearParticipacionTest() {
		when(participacionRepository.crear(any(Participacion.class))).thenReturn(crearParticipacion());

		ParticipacionDTO participacionCreada = participacionService.crear("7",
				crearParticipacionDTO(INICIO_PROYECTO.plusDays(10), null));

		assertEquals(ID_EXPERTO, participacionCreada.getIdExperto());
		assertEquals("Ingeniera inversa", participacionCreada.getRolExperto());
		verify(participacionRepository, times(1)).crear(any(Participacion.class));
	}

	@Test
	@DisplayName("No se agrega un experto que ya esta en el proyecto")
	public void crearParticipacionExpertoRepetidoTest() {
		when(participacionRepository.existeExpertoEnProyecto(ID_PROYECTO, ID_EXPERTO)).thenReturn(true);

		try {
			participacionService.crear("7", crearParticipacionDTO(INICIO_PROYECTO.plusDays(10), null));
			fail("Debio lanzar ExpertoRepetidoException");
		} catch (ExpertoRepetidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(participacionRepository, never()).crear(any(Participacion.class));
	}

	@Test
	@DisplayName("No se agrega un experto que no existe")
	public void crearParticipacionExpertoInexistenteTest() {
		ParticipacionDTO participacionDTO = crearParticipacionDTO(INICIO_PROYECTO.plusDays(10), null);
		participacionDTO.setIdExperto(99L);
		when(expertoRepository.buscarPorId(99L)).thenReturn(null);

		try {
			participacionService.crear("7", participacionDTO);
			fail("Debio lanzar ExpertoNoEncontradoException");
		} catch (ExpertoNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
		verify(participacionRepository, never()).crear(any(Participacion.class));
	}

	@Test
	@DisplayName("No se incorpora un experto antes de que empiece el proyecto")
	public void crearParticipacionAntesDelProyectoTest() {
		try {
			participacionService.crear("7", crearParticipacionDTO(INICIO_PROYECTO.minusDays(5), null));
			fail("Debio lanzar DatoProyectoInvalidoException");
		} catch (DatoProyectoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(participacionRepository, never()).crear(any(Participacion.class));
	}

	@Test
	@DisplayName("La fecha de salida no puede ser anterior a la incorporacion")
	public void crearParticipacionSalidaAntesDeIncorporacionTest() {
		LocalDate incorporacion = INICIO_PROYECTO.plusDays(20);

		try {
			participacionService.crear("7", crearParticipacionDTO(incorporacion, incorporacion.minusDays(3)));
			fail("Debio lanzar DatoProyectoInvalidoException");
		} catch (DatoProyectoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(participacionRepository, never()).crear(any(Participacion.class));
	}


	@Test
	@DisplayName("Listar las participaciones de un proyecto")
	public void buscarPorProyectoTest() {
		List<Participacion> participaciones = new ArrayList<Participacion>();
		participaciones.add(crearParticipacion());
		participaciones.add(new Participacion(ID_PROYECTO, 2L, "Recuperacion de medios", INICIO_PROYECTO, null));
		when(participacionRepository.buscarPorIdProyecto(ID_PROYECTO)).thenReturn(participaciones);

		List<ParticipacionDTO> resultado = participacionService.buscarPorProyecto("7");

		assertEquals(2, resultado.size());
	}

	@Test
	@DisplayName("Buscar una participacion que no existe")
	public void buscarPorIdInexistenteTest() {
		when(participacionRepository.buscarPorId(99L)).thenReturn(null);

		try {
			participacionService.buscarPorId("99");
			fail("Debio lanzar ParticipacionNoEncontradaException");
		} catch (ParticipacionNoEncontradaException e) {
			assertNotNull(e.getMessage());
		}
	}


	@Test
	@DisplayName("Registrar la salida de un experto")
	public void actualizarParticipacionTest() {
		when(participacionRepository.buscarPorId(ID_PARTICIPACION)).thenReturn(crearParticipacion());

		LocalDate incorporacion = INICIO_PROYECTO.plusDays(10);
		ParticipacionDTO datosNuevos = crearParticipacionDTO(incorporacion, LocalDate.now().minusDays(1));
		datosNuevos.setRolExperto("Lider de restauracion");

		ParticipacionDTO participacionActualizada = participacionService.actualizar("9", datosNuevos);

		assertEquals("Lider de restauracion", participacionActualizada.getRolExperto());
		assertNotNull(participacionActualizada.getFechaSalida());
		verify(participacionRepository, times(1)).actualizar(any(Participacion.class));
	}


	@Test
	@DisplayName("Quitar un experto de un proyecto con varios expertos")
	public void eliminarParticipacionTest() {
		when(participacionRepository.buscarPorId(ID_PARTICIPACION)).thenReturn(crearParticipacion());
		when(participacionRepository.contarPorIdProyecto(ID_PROYECTO)).thenReturn(2L);

		participacionService.eliminar("9");

		verify(participacionRepository, times(1)).eliminar(ID_PARTICIPACION);
	}

	@Test
	@DisplayName("No se quita la unica participacion de un proyecto")
	public void eliminarUltimaParticipacionTest() {
		when(participacionRepository.buscarPorId(ID_PARTICIPACION)).thenReturn(crearParticipacion());
		when(participacionRepository.contarPorIdProyecto(ID_PROYECTO)).thenReturn(1L);

		try {
			participacionService.eliminar("9");
			fail("Debio lanzar UltimaParticipacionException");
		} catch (UltimaParticipacionException e) {
			assertNotNull(e.getMessage());
		}
		verify(participacionRepository, never()).eliminar(anyLong());
	}


	private ProyectoDeRestauracion crearProyecto() {
		ProyectoDeRestauracion proyecto = new ProyectoDeRestauracion();
		proyecto.setIdProyecto(ID_PROYECTO);
		proyecto.setNombre("Rescate de System Shock");
		proyecto.setEstado(EstadoProyecto.ACTIVO);
		proyecto.setFechaInicio(INICIO_PROYECTO);
		return proyecto;
	}

	private Experto crearExperto() {
		Experto experto = new Experto();
		experto.setIdExperto(ID_EXPERTO);
		experto.setPrimerNombreEx("Valentina");
		experto.setPrimerApellidoEx("Rojas");
		return experto;
	}

	private Participacion crearParticipacion() {
		return new Participacion(ID_PROYECTO, ID_EXPERTO, "Ingeniera inversa", INICIO_PROYECTO.plusDays(10), null);
	}

	private ParticipacionDTO crearParticipacionDTO(LocalDate incorporacion, LocalDate salida) {
		ParticipacionDTO participacionDTO = new ParticipacionDTO();
		participacionDTO.setIdExperto(ID_EXPERTO);
		participacionDTO.setRolExperto("Ingeniera inversa");
		participacionDTO.setFechaIncorporacion(incorporacion);
		participacionDTO.setFechaSalida(salida);
		return participacionDTO;
	}

}