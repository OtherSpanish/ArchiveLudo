package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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

import co.edu.unbosque.archiveludo.dto.InformeFinalDTO;
import co.edu.unbosque.archiveludo.dto.ParticipacionDTO;
import co.edu.unbosque.archiveludo.dto.ProyectoDeRestauracionDTO;
import co.edu.unbosque.archiveludo.entity.Experto;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.DatosInformeInvalidosException;
import co.edu.unbosque.archiveludo.exceptions.InformeNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.InformeYaExisteException;
import co.edu.unbosque.archiveludo.model.EstadoConservacion;
import co.edu.unbosque.archiveludo.repository.ExpertoRepository;
import co.edu.unbosque.archiveludo.repository.ProyectoDeRestauracionRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;
import co.edu.unbosque.archiveludo.service.AlmacenamientoArchivosService;
import co.edu.unbosque.archiveludo.service.GeneradorInformePdfService;
import co.edu.unbosque.archiveludo.service.InformeFinalService;
import co.edu.unbosque.archiveludo.service.ProyectoDeRestauracionService;

/**
 * Pruebas unitarias del informe final en PDF (InformeFinalService).
 * Los repositories y Cloudinary son simulados con Mockito; el PDF se genera de verdad, en memoria.
 *
 * Criterio de aceptacion: se puede crear, consultar, regenerar y eliminar el informe final
 * de un proyecto finalizado; el archivo generado es un PDF; no se genera para un proyecto
 * sin finalizar ni se crea un segundo informe.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InformeFinalTest {

	private static final long ID_PROYECTO = 7L;
	private static final long ID_VIDEOJUEGO = 16L;
	private static final long ID_EXPERTO = 3L;
	private static final String CORREO_USUARIO = "curador@archiveludo.co";
	private static final String URL_INFORME = "https://res.cloudinary.com/demo/image/upload/informe-proyecto-7-a.pdf";
	private static final String URL_INFORME_NUEVO = "https://res.cloudinary.com/demo/image/upload/informe-proyecto-7-b.pdf";

	@Mock
	private ProyectoDeRestauracionService proyectoService;

	@Mock
	private ProyectoDeRestauracionRepository proyectoRepository;

	@Mock
	private VideojuegoRepository videojuegoRepository;

	@Mock
	private ExpertoRepository expertoRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private AlmacenamientoArchivosService almacenamientoArchivosService;

	private InformeFinalService informeService;

	@BeforeEach
	public void prepararService() {
		informeService = new InformeFinalService(proyectoService, proyectoRepository, videojuegoRepository,
				expertoRepository, usuarioRepository, new GeneradorInformePdfService(), almacenamientoArchivosService,
				new ModelMapper());

		when(proyectoService.buscarPorId("7")).thenReturn(crearProyectoDTO("finalizado", LocalDate.now().minusDays(1)));
		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego());
		when(expertoRepository.buscarPorId(ID_EXPERTO)).thenReturn(crearExperto());
		when(usuarioRepository.buscarPorCorreo(CORREO_USUARIO)).thenReturn(crearUsuario());
		when(almacenamientoArchivosService.subirInformeProyecto(anyLong(), any(byte[].class)))
				.thenReturn(URL_INFORME);
	}

	// ──────────────────────────────────────────────
	// Crear
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("Crear el informe final genera un PDF y guarda su enlace")
	public void crearInformeTest() {
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(null);

		InformeFinalDTO informe = informeService.crear("7", crearInformeDTO(), CORREO_USUARIO);

		assertEquals(URL_INFORME, informe.getUrlInforme());
		assertEquals("exitoso", informe.getResultado());
		verify(proyectoRepository, times(1)).actualizarUrlInforme(ID_PROYECTO, URL_INFORME);

		// El archivo que se envio a Cloudinary debe ser un PDF de verdad
		ArgumentCaptor<byte[]> capturaPdf = ArgumentCaptor.forClass(byte[].class);
		verify(almacenamientoArchivosService, times(1)).subirInformeProyecto(eq(ID_PROYECTO), capturaPdf.capture());
		byte[] pdf = capturaPdf.getValue();
		assertTrue(pdf.length > 0);
		assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
	}

	@Test
	@DisplayName("No se genera informe para un proyecto sin finalizar")
	public void crearInformeProyectoNoFinalizadoTest() {
		when(proyectoService.buscarPorId("7")).thenReturn(crearProyectoDTO("activo", null));

		try {
			informeService.crear("7", crearInformeDTO(), CORREO_USUARIO);
			fail("Debio lanzar DatosInformeInvalidosException");
		} catch (DatosInformeInvalidosException e) {
			assertNotNull(e.getMessage());
		}
		verify(almacenamientoArchivosService, never()).subirInformeProyecto(anyLong(), any(byte[].class));
	}

	@Test
	@DisplayName("No se crea un segundo informe para el mismo proyecto")
	public void crearInformeDuplicadoTest() {
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(URL_INFORME);

		try {
			informeService.crear("7", crearInformeDTO(), CORREO_USUARIO);
			fail("Debio lanzar InformeYaExisteException");
		} catch (InformeYaExisteException e) {
			assertNotNull(e.getMessage());
		}
		verify(almacenamientoArchivosService, never()).subirInformeProyecto(anyLong(), any(byte[].class));
	}

	@Test
	@DisplayName("No se crea un informe con un resultado invalido")
	public void crearInformeResultadoInvalidoTest() {
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(null);
		InformeFinalDTO informeDTO = crearInformeDTO();
		informeDTO.setResultado("regular");

		try {
			informeService.crear("7", informeDTO, CORREO_USUARIO);
			fail("Debio lanzar DatosInformeInvalidosException");
		} catch (DatosInformeInvalidosException e) {
			assertNotNull(e.getMessage());
		}
	}

	// ──────────────────────────────────────────────
	// Leer
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("Consultar el informe devuelve solo su enlace")
	public void buscarInformeTest() {
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(URL_INFORME);

		InformeFinalDTO informe = informeService.buscar("7");

		assertEquals(ID_PROYECTO, informe.getIdProyecto());
		assertEquals(URL_INFORME, informe.getUrlInforme());
		assertNull(informe.getResumenResultados());
	}

	@Test
	@DisplayName("Consultar el informe de un proyecto que no tiene")
	public void buscarInformeInexistenteTest() {
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(null);

		try {
			informeService.buscar("7");
			fail("Debio lanzar InformeNoEncontradoException");
		} catch (InformeNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}

	// ──────────────────────────────────────────────
	// Actualizar (regenerar)
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("Regenerar el informe guarda el enlace nuevo y borra el PDF anterior")
	public void actualizarInformeTest() {
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(URL_INFORME);
		when(almacenamientoArchivosService.subirInformeProyecto(anyLong(), any(byte[].class)))
				.thenReturn(URL_INFORME_NUEVO);

		InformeFinalDTO informe = informeService.actualizar("7", crearInformeDTO(), CORREO_USUARIO);

		assertEquals(URL_INFORME_NUEVO, informe.getUrlInforme());
		verify(proyectoRepository, times(1)).actualizarUrlInforme(ID_PROYECTO, URL_INFORME_NUEVO);
		verify(almacenamientoArchivosService, times(1)).eliminarArchivo(URL_INFORME);
	}


	@Test
	@DisplayName("Eliminar el informe quita el enlace y borra el PDF")
	public void eliminarInformeTest() {
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(URL_INFORME);

		informeService.eliminar("7");

		verify(proyectoRepository, times(1)).actualizarUrlInforme(ID_PROYECTO, null);
		verify(almacenamientoArchivosService, times(1)).eliminarArchivo(URL_INFORME);
	}

	@Test
	@DisplayName("No se elimina un informe que no existe")
	public void eliminarInformeInexistenteTest() {
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(null);

		try {
			informeService.eliminar("7");
			fail("Debio lanzar InformeNoEncontradoException");
		} catch (InformeNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
		verify(almacenamientoArchivosService, never()).eliminarArchivo(any(String.class));
	}


	private ProyectoDeRestauracionDTO crearProyectoDTO(String estado, LocalDate fechaFin) {
		ParticipacionDTO participacion = new ParticipacionDTO();
		participacion.setIdExperto(ID_EXPERTO);
		participacion.setRolExperto("Ingeniera inversa");
		participacion.setFechaIncorporacion(LocalDate.now().minusDays(60));

		List<ParticipacionDTO> participaciones = new ArrayList<ParticipacionDTO>();
		participaciones.add(participacion);

		ProyectoDeRestauracionDTO proyectoDTO = new ProyectoDeRestauracionDTO();
		proyectoDTO.setIdProyecto(ID_PROYECTO);
		proyectoDTO.setIdVideojuego(ID_VIDEOJUEGO);
		proyectoDTO.setNombre("Rescate de System Shock");
		proyectoDTO.setDescripcion("Reconstruccion del codigo perdido");
		proyectoDTO.setHerramientaEmulador("DOSBox");
		proyectoDTO.setEstado(estado);
		proyectoDTO.setFechaInicio(LocalDate.now().minusDays(60));
		proyectoDTO.setFechaFin(fechaFin);
		proyectoDTO.setParticipaciones(participaciones);
		return proyectoDTO;
	}

	private InformeFinalDTO crearInformeDTO() {
		return new InformeFinalDTO(LocalDate.now(), "Se recupero el 90% del codigo fuente.", "exitoso");
	}

	private Videojuego crearVideojuego() {
		Videojuego videojuego = new Videojuego();
		videojuego.setIdVideojuego(ID_VIDEOJUEGO);
		videojuego.setTitulo("System Shock");
		videojuego.setPlataformaOriginal("MS-DOS");
		videojuego.setAnioLanzamiento(1994);
		videojuego.setEstadoConservacion(EstadoConservacion.EN_RIESGO);
		return videojuego;
	}

	private Experto crearExperto() {
		Experto experto = new Experto();
		experto.setIdExperto(ID_EXPERTO);
		experto.setPrimerNombreEx("Valentina");
		experto.setPrimerApellidoEx("Rojas");
		experto.setEspecialidad("Ingenieria inversa");
		return experto;
	}

	private Usuario crearUsuario() {
		Usuario usuario = new Usuario();
		usuario.setIdUsuario(2L);
		usuario.setPrimerNombre("Camila");
		usuario.setPrimerApellido("Torres");
		usuario.setCorreo(CORREO_USUARIO);
		return usuario;
	}

}