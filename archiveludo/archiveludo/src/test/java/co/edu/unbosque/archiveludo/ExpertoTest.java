package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

import co.edu.unbosque.archiveludo.dto.ExpertoDTO;
import co.edu.unbosque.archiveludo.entity.Experto;
import co.edu.unbosque.archiveludo.exceptions.DatoExpertoInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.ExpertoNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.repository.ExpertoRepository;
import co.edu.unbosque.archiveludo.service.ExpertoService;

/**
 * Pruebas unitarias del CRUD de expertos (ExpertoService).
 * El repository es simulado con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: se puede crear, leer, actualizar y eliminar un experto;
 * el correo debe tener formato valido y no repetirse; los opcionales vacios se guardan como null;
 * no se elimina un experto que participa en proyectos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExpertoTest {

	private static final long ID_EXPERTO = 3L;
	private static final String CORREO = "valentina.rojas@expertos-ludo.org";

	@Mock
	private ExpertoRepository expertoRepository;

	private ExpertoService expertoService;

	@BeforeEach
	public void prepararService() {
		expertoService = new ExpertoService(expertoRepository, new ModelMapper());
	}


	@Test
	@DisplayName("Crear un experto guarda los opcionales vacios como null")
	public void crearExpertoTest() {
		when(expertoRepository.existeCorreo(CORREO)).thenReturn(false);
		when(expertoRepository.crear(any(Experto.class))).thenReturn(crearExperto());

		ExpertoDTO expertoDTO = crearExpertoDTO(CORREO);
		expertoDTO.setSegundoNombreEx("   ");

		ExpertoDTO expertoCreado = expertoService.crear(expertoDTO);

		assertEquals(ID_EXPERTO, expertoCreado.getIdExperto());
		assertEquals("Valentina", expertoCreado.getPrimerNombreEx());

		ArgumentCaptor<Experto> capturaExperto = ArgumentCaptor.forClass(Experto.class);
		verify(expertoRepository, times(1)).crear(capturaExperto.capture());
		assertNull(capturaExperto.getValue().getSegundoNombreEx());
	}

	@Test
	@DisplayName("No se crea un experto con un correo sin formato valido")
	public void crearExpertoCorreoInvalidoTest() {
		try {
			expertoService.crear(crearExpertoDTO("correo-sin-arroba"));
			fail("Debio lanzar DatosExpertoInvalidosException");
		} catch (DatoExpertoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(expertoRepository, never()).crear(any(Experto.class));
	}

	@Test
	@DisplayName("No se crea un experto con un correo ya registrado")
	public void crearExpertoCorreoRegistradoTest() {
		when(expertoRepository.existeCorreo(CORREO)).thenReturn(true);

		try {
			expertoService.crear(crearExpertoDTO(CORREO));
			fail("Debio lanzar DatosExpertoInvalidosException");
		} catch (DatoExpertoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(expertoRepository, never()).crear(any(Experto.class));
	}

	@Test
	@DisplayName("No se crea un experto sin especialidad")
	public void crearExpertoSinEspecialidadTest() {
		ExpertoDTO expertoDTO = crearExpertoDTO(CORREO);
		expertoDTO.setEspecialidad("");

		try {
			expertoService.crear(expertoDTO);
			fail("Debio lanzar DatosExpertoInvalidosException");
		} catch (DatoExpertoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(expertoRepository, never()).crear(any(Experto.class));
	}


	@Test
	@DisplayName("Buscar un experto por id")
	public void buscarPorIdTest() {
		when(expertoRepository.buscarPorId(ID_EXPERTO)).thenReturn(crearExperto());

		ExpertoDTO experto = expertoService.buscarPorId("3");

		assertEquals("Rojas", experto.getPrimerApellidoEx());
		assertEquals(CORREO, experto.getCorreo());
	}

	@Test
	@DisplayName("Buscar un experto que no existe")
	public void buscarPorIdInexistenteTest() {
		when(expertoRepository.buscarPorId(99L)).thenReturn(null);

		try {
			expertoService.buscarPorId("99");
			fail("Debio lanzar ExpertoNoEncontradoException");
		} catch (ExpertoNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("Listar todos los expertos")
	public void buscarTodosTest() {
		List<Experto> expertos = new ArrayList<Experto>();
		expertos.add(crearExperto());
		expertos.add(crearExperto());
		when(expertoRepository.buscarTodos()).thenReturn(expertos);

		List<ExpertoDTO> resultado = expertoService.buscarTodos();

		assertEquals(2, resultado.size());
	}


	@Test
	@DisplayName("Actualizar la afiliacion de un experto")
	public void actualizarExpertoTest() {
		when(expertoRepository.buscarPorId(ID_EXPERTO)).thenReturn(crearExperto());
		when(expertoRepository.existeCorreoEnOtroExperto(CORREO, ID_EXPERTO)).thenReturn(false);

		ExpertoDTO datosNuevos = crearExpertoDTO(CORREO);
		datosNuevos.setAfiliacion("Archivo Digital Universitario");

		ExpertoDTO expertoActualizado = expertoService.actualizar("3", datosNuevos);

		assertEquals("Archivo Digital Universitario", expertoActualizado.getAfiliacion());
		verify(expertoRepository, times(1)).actualizar(any(Experto.class));
	}

	@Test
	@DisplayName("No se actualiza un experto con el correo de otro experto")
	public void actualizarExpertoCorreoDeOtroTest() {
		when(expertoRepository.buscarPorId(ID_EXPERTO)).thenReturn(crearExperto());
		when(expertoRepository.existeCorreoEnOtroExperto(CORREO, ID_EXPERTO)).thenReturn(true);

		try {
			expertoService.actualizar("3", crearExpertoDTO(CORREO));
			fail("Debio lanzar DatosExpertoInvalidosException");
		} catch (DatoExpertoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(expertoRepository, never()).actualizar(any(Experto.class));
	}


	@Test
	@DisplayName("Eliminar un experto sin participaciones")
	public void eliminarExpertoTest() {
		when(expertoRepository.buscarPorId(ID_EXPERTO)).thenReturn(crearExperto());
		when(expertoRepository.contarParticipaciones(ID_EXPERTO)).thenReturn(0L);

		expertoService.eliminar("3");

		verify(expertoRepository, times(1)).eliminar(ID_EXPERTO);
	}

	@Test
	@DisplayName("No se elimina un experto que participa en proyectos")
	public void eliminarExpertoConParticipacionesTest() {
		when(expertoRepository.buscarPorId(ID_EXPERTO)).thenReturn(crearExperto());
		when(expertoRepository.contarParticipaciones(ID_EXPERTO)).thenReturn(2L);

		try {
			expertoService.eliminar("3");
			fail("Debio lanzar RegistroAsociadoException");
		} catch (RegistroAsociadoException e) {
			assertNotNull(e.getMessage());
		}
		verify(expertoRepository, never()).eliminar(anyLong());
	}


	private Experto crearExperto() {
		Experto experto = new Experto();
		experto.setIdExperto(ID_EXPERTO);
		experto.setPrimerNombreEx("Valentina");
		experto.setPrimerApellidoEx("Rojas");
		experto.setEspecialidad("Ingenieria inversa");
		experto.setAfiliacion("Museo Nacional del Videojuego");
		experto.setCorreo(CORREO);
		return experto;
	}

	private ExpertoDTO crearExpertoDTO(String correo) {
		ExpertoDTO expertoDTO = new ExpertoDTO();
		expertoDTO.setPrimerNombreEx("Valentina");
		expertoDTO.setPrimerApellidoEx("Rojas");
		expertoDTO.setEspecialidad("Ingenieria inversa");
		expertoDTO.setAfiliacion("Museo Nacional del Videojuego");
		expertoDTO.setCorreo(correo);
		return expertoDTO;
	}

}