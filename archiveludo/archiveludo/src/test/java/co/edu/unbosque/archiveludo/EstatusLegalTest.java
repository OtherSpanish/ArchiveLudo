package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
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

import co.edu.unbosque.archiveludo.dto.EstatusLegalDTO;
import co.edu.unbosque.archiveludo.entity.EstatusLegal;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.DatoEstatusLegalInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.EstatusLegalNoEncontradoException;
import co.edu.unbosque.archiveludo.model.TipoEstatusLegal;
import co.edu.unbosque.archiveludo.repository.EstatusLegalRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;
import co.edu.unbosque.archiveludo.service.EstatusLegalService;

/**
 * Pruebas unitarias del CRUD de estatus legal (EstatusLegalService).
 * Los repositories son simulados con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: se puede crear, leer, actualizar y eliminar un estatus legal;
 * el historial marca como vigente solo al mas reciente; el estatus sale en texto legible;
 * se rechazan fechas futuras y estatus que no existen.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EstatusLegalTest {

	private static final long ID_ESTATUS = 5L;
	private static final long ID_VIDEOJUEGO = 16L;
	private static final long ID_USUARIO = 2L;
	private static final String CORREO_USUARIO = "curador@archiveludo.co";

	@Mock
	private EstatusLegalRepository estatusLegalRepository;

	@Mock
	private VideojuegoRepository videojuegoRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	private EstatusLegalService estatusLegalService;

	@BeforeEach
	public void prepararService() {
		estatusLegalService = new EstatusLegalService(estatusLegalRepository, videojuegoRepository,
				usuarioRepository, new ModelMapper());

		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego());
		when(usuarioRepository.buscarPorCorreo(CORREO_USUARIO)).thenReturn(crearUsuario());
		// Por defecto, el estatus de prueba es el vigente de su videojuego
		when(estatusLegalRepository.buscarIdVigente(ID_VIDEOJUEGO)).thenReturn(ID_ESTATUS);
	}


	@Test
	@DisplayName("Crear un estatus legal lo deja como vigente y en texto legible")
	public void crearEstatusLegalTest() {
		when(estatusLegalRepository.crear(any(EstatusLegal.class)))
				.thenReturn(crearEstatus(ID_ESTATUS, "dominio publico", LocalDate.now().minusDays(1)));

		EstatusLegalDTO estatusCreado = estatusLegalService.crear("16",
				crearEstatusDTO("dominio publico", LocalDate.now().minusDays(1)), CORREO_USUARIO);

		assertEquals("Dominio público", estatusCreado.getEstatus());
		assertTrue(estatusCreado.getVigente());
		verify(estatusLegalRepository, times(1)).crear(any(EstatusLegal.class));
	}

	@Test
	@DisplayName("No se crea un estatus legal con fecha futura")
	public void crearEstatusLegalFechaFuturaTest() {
		try {
			estatusLegalService.crear("16", crearEstatusDTO("protegido", LocalDate.now().plusDays(5)),
					CORREO_USUARIO);
			fail("Debio lanzar DatosEstatusLegalInvalidosException");
		} catch (DatoEstatusLegalInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(estatusLegalRepository, never()).crear(any(EstatusLegal.class));
	}

	@Test
	@DisplayName("No se crea un estatus legal que no existe")
	public void crearEstatusLegalTipoInvalidoTest() {
		try {
			estatusLegalService.crear("16", crearEstatusDTO("prestado", LocalDate.now()), CORREO_USUARIO);
			fail("Debio lanzar DatosEstatusLegalInvalidosException");
		} catch (DatoEstatusLegalInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(estatusLegalRepository, never()).crear(any(EstatusLegal.class));
	}


	@Test
	@DisplayName("En el historial solo el mas reciente es vigente")
	public void buscarPorVideojuegoTest() {
		List<EstatusLegal> historial = new ArrayList<EstatusLegal>();
		historial.add(crearEstatus(ID_ESTATUS, "abandonware", LocalDate.now().minusDays(10)));
		historial.add(crearEstatus(4L, "protegido", LocalDate.now().minusYears(5)));
		when(estatusLegalRepository.buscarPorIdVideojuego(ID_VIDEOJUEGO)).thenReturn(historial);

		List<EstatusLegalDTO> resultado = estatusLegalService.buscarPorVideojuego("16");

		assertEquals(2, resultado.size());
		assertTrue(resultado.get(0).getVigente());
		assertFalse(resultado.get(1).getVigente());
	}

	@Test
	@DisplayName("Buscar un estatus legal por id")
	public void buscarPorIdTest() {
		when(estatusLegalRepository.buscarPorId(ID_ESTATUS))
				.thenReturn(crearEstatus(ID_ESTATUS, "protegido", LocalDate.now().minusDays(1)));

		EstatusLegalDTO estatus = estatusLegalService.buscarPorId("5");

		assertEquals("Protegido", estatus.getEstatus());
		assertTrue(estatus.getVigente());
	}

	@Test
	@DisplayName("Buscar un estatus legal que no existe")
	public void buscarPorIdInexistenteTest() {
		when(estatusLegalRepository.buscarPorId(99L)).thenReturn(null);

		try {
			estatusLegalService.buscarPorId("99");
			fail("Debio lanzar EstatusLegalNoEncontradoException");
		} catch (EstatusLegalNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}


	@Test
	@DisplayName("Corregir el estatus y la observacion")
	public void actualizarEstatusLegalTest() {
		when(estatusLegalRepository.buscarPorId(ID_ESTATUS))
				.thenReturn(crearEstatus(ID_ESTATUS, "protegido", LocalDate.now().minusDays(1)));

		EstatusLegalDTO datosNuevos = crearEstatusDTO("huerfano", LocalDate.now().minusDays(1));
		datosNuevos.setObservacion("El titular de los derechos ya no existe");

		EstatusLegalDTO estatusActualizado = estatusLegalService.actualizar("5", datosNuevos);

		assertEquals("Huérfano", estatusActualizado.getEstatus());
		verify(estatusLegalRepository, times(1)).actualizar(any(EstatusLegal.class));
	}


	@Test
	@DisplayName("Eliminar un estatus legal")
	public void eliminarEstatusLegalTest() {
		when(estatusLegalRepository.buscarPorId(ID_ESTATUS))
				.thenReturn(crearEstatus(ID_ESTATUS, "protegido", LocalDate.now().minusDays(1)));

		estatusLegalService.eliminar("5");

		verify(estatusLegalRepository, times(1)).eliminar(ID_ESTATUS);
	}

	@Test
	@DisplayName("No se elimina un estatus legal que no existe")
	public void eliminarEstatusLegalInexistenteTest() {
		when(estatusLegalRepository.buscarPorId(99L)).thenReturn(null);

		try {
			estatusLegalService.eliminar("99");
			fail("Debio lanzar EstatusLegalNoEncontradoException");
		} catch (EstatusLegalNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
		verify(estatusLegalRepository, never()).eliminar(99L);
	}


	private Videojuego crearVideojuego() {
		Videojuego videojuego = new Videojuego();
		videojuego.setIdVideojuego(ID_VIDEOJUEGO);
		videojuego.setTitulo("System Shock");
		return videojuego;
	}

	private Usuario crearUsuario() {
		Usuario usuario = new Usuario();
		usuario.setIdUsuario(ID_USUARIO);
		usuario.setCorreo(CORREO_USUARIO);
		return usuario;
	}

	private EstatusLegal crearEstatus(long idEstatus, String valorEstatus, LocalDate fecha) {
		return new EstatusLegal(idEstatus, ID_VIDEOJUEGO, ID_USUARIO, TipoEstatusLegal.buscarPorValor(valorEstatus),
				fecha, "Verificado en registro de derechos");
	}

	private EstatusLegalDTO crearEstatusDTO(String estatus, LocalDate fecha) {
		EstatusLegalDTO estatusDTO = new EstatusLegalDTO();
		estatusDTO.setEstatus(estatus);
		estatusDTO.setFechaDeterminacion(fecha);
		estatusDTO.setObservacion("Verificado en registro de derechos");
		return estatusDTO;
	}

}