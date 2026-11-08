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

import co.edu.unbosque.archiveludo.dto.EstudioDTO;
import co.edu.unbosque.archiveludo.entity.Estudio;
import co.edu.unbosque.archiveludo.exceptions.DatoEstudioInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.EstudioNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.model.EstadoEstudio;
import co.edu.unbosque.archiveludo.repository.EstudioRepository;
import co.edu.unbosque.archiveludo.service.EstudioService;

/**
 * Pruebas unitarias del CRUD de estudios desarrolladores (EstudioService).
 * El repository es simulado con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: se puede crear, leer, actualizar y eliminar un estudio;
 * un estudio desaparecido necesita anio de cierre y uno activo no puede tenerlo;
 * no se elimina un estudio que tiene videojuegos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EstudioTest {

	private static final long ID_ESTUDIO = 4L;

	@Mock
	private EstudioRepository estudioRepository;

	private EstudioService estudioService;

	@BeforeEach
	public void prepararService() {
		estudioService = new EstudioService(estudioRepository, new ModelMapper());
	}


	@Test
	@DisplayName("Crear un estudio activo")
	public void crearEstudioActivoTest() {
		when(estudioRepository.crear(any(Estudio.class)))
				.thenReturn(crearEstudio(EstadoEstudio.ACTIVO, 2008, null));

		EstudioDTO estudioCreado = estudioService.crear(crearEstudioDTO("activo", 2008));

		assertEquals("Supergiant Games", estudioCreado.getNombre());
		assertEquals("activo", estudioCreado.getEstadoActual());
		verify(estudioRepository, times(1)).crear(any(Estudio.class));
	}

	@Test
	@DisplayName("Crear un estudio desaparecido con su anio de cierre")
	public void crearEstudioDesaparecidoTest() {
		when(estudioRepository.crear(any(Estudio.class)))
				.thenReturn(crearEstudio(EstadoEstudio.DESAPARECIDO, 1990, 2000));

		EstudioDTO estudioDTO = crearEstudioDTO("desaparecido", 1990);
		estudioDTO.setAnioCierre(2000);

		EstudioDTO estudioCreado = estudioService.crear(estudioDTO);

		assertEquals("desaparecido", estudioCreado.getEstadoActual());
	}

	@Test
	@DisplayName("No se crea un estudio desaparecido sin anio de cierre")
	public void crearEstudioDesaparecidoSinCierreTest() {
		try {
			estudioService.crear(crearEstudioDTO("desaparecido", 1990));
			fail("Debio lanzar DatosEstudioInvalidosException");
		} catch (DatoEstudioInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(estudioRepository, never()).crear(any(Estudio.class));
	}

	@Test
	@DisplayName("No se crea un estudio activo con anio de cierre")
	public void crearEstudioActivoConCierreTest() {
		EstudioDTO estudioDTO = crearEstudioDTO("activo", 2008);
		estudioDTO.setAnioCierre(2020);

		try {
			estudioService.crear(estudioDTO);
			fail("Debio lanzar DatosEstudioInvalidosException");
		} catch (DatoEstudioInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(estudioRepository, never()).crear(any(Estudio.class));
	}


	@Test
	@DisplayName("Buscar un estudio por id")
	public void buscarPorIdTest() {
		when(estudioRepository.buscarPorId(ID_ESTUDIO)).thenReturn(crearEstudio(EstadoEstudio.ACTIVO, 2008, null));

		EstudioDTO estudio = estudioService.buscarPorId("4");

		assertEquals("Supergiant Games", estudio.getNombre());
		assertEquals("activo", estudio.getEstadoActual());
	}

	@Test
	@DisplayName("Buscar un estudio que no existe")
	public void buscarPorIdInexistenteTest() {
		when(estudioRepository.buscarPorId(99L)).thenReturn(null);

		try {
			estudioService.buscarPorId("99");
			fail("Debio lanzar EstudioNoEncontradoException");
		} catch (EstudioNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("Listar todos los estudios")
	public void buscarTodosTest() {
		List<Estudio> estudios = new ArrayList<Estudio>();
		estudios.add(crearEstudio(EstadoEstudio.ACTIVO, 2008, null));
		estudios.add(crearEstudio(EstadoEstudio.DESAPARECIDO, 1990, 2000));
		when(estudioRepository.buscarTodos()).thenReturn(estudios);

		List<EstudioDTO> resultado = estudioService.buscarTodos();

		assertEquals(2, resultado.size());
	}


	@Test
	@DisplayName("Actualizar un estudio que cierra")
	public void actualizarEstudioTest() {
		when(estudioRepository.buscarPorId(ID_ESTUDIO)).thenReturn(crearEstudio(EstadoEstudio.ACTIVO, 2008, null));

		EstudioDTO datosNuevos = crearEstudioDTO("desaparecido", 2008);
		datosNuevos.setAnioCierre(2024);

		EstudioDTO estudioActualizado = estudioService.actualizar("4", datosNuevos);

		assertEquals("desaparecido", estudioActualizado.getEstadoActual());
		verify(estudioRepository, times(1)).actualizar(any(Estudio.class));
	}

	@Test
	@DisplayName("No se actualiza un estudio que no existe")
	public void actualizarEstudioInexistenteTest() {
		when(estudioRepository.buscarPorId(99L)).thenReturn(null);

		try {
			estudioService.actualizar("99", crearEstudioDTO("activo", 2008));
			fail("Debio lanzar EstudioNoEncontradoException");
		} catch (EstudioNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
		verify(estudioRepository, never()).actualizar(any(Estudio.class));
	}


	@Test
	@DisplayName("Eliminar un estudio sin videojuegos")
	public void eliminarEstudioTest() {
		when(estudioRepository.buscarPorId(ID_ESTUDIO)).thenReturn(crearEstudio(EstadoEstudio.ACTIVO, 2008, null));
		when(estudioRepository.contarVideojuegosAsociados(ID_ESTUDIO)).thenReturn(0L);

		estudioService.eliminar("4");

		verify(estudioRepository, times(1)).eliminar(ID_ESTUDIO);
	}

	@Test
	@DisplayName("No se elimina un estudio que tiene videojuegos")
	public void eliminarEstudioConVideojuegosTest() {
		when(estudioRepository.buscarPorId(ID_ESTUDIO)).thenReturn(crearEstudio(EstadoEstudio.ACTIVO, 2008, null));
		when(estudioRepository.contarVideojuegosAsociados(ID_ESTUDIO)).thenReturn(2L);

		try {
			estudioService.eliminar("4");
			fail("Debio lanzar RegistroAsociadoException");
		} catch (RegistroAsociadoException e) {
			assertNotNull(e.getMessage());
		}
		verify(estudioRepository, never()).eliminar(anyLong());
	}


	private Estudio crearEstudio(EstadoEstudio estado, Integer anioFundacion, Integer anioCierre) {
		return new Estudio(ID_ESTUDIO, "Supergiant Games", "Estados Unidos", estado, anioFundacion, anioCierre);
	}

	/**
	 * Estudio sin anio de cierre; las pruebas que lo necesitan lo agregan con setAnioCierre.
	 */
	private EstudioDTO crearEstudioDTO(String estadoActual, int anioFundacion) {
		EstudioDTO estudioDTO = new EstudioDTO();
		estudioDTO.setNombre("Supergiant Games");
		estudioDTO.setPais("Estados Unidos");
		estudioDTO.setEstadoActual(estadoActual);
		estudioDTO.setAnioFundacion(anioFundacion);
		return estudioDTO;
	}

}