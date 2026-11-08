package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import co.edu.unbosque.archiveludo.dto.ProyectoDeRestauracionDTO;
import co.edu.unbosque.archiveludo.entity.Experto;
import co.edu.unbosque.archiveludo.entity.Participacion;
import co.edu.unbosque.archiveludo.entity.ProyectoDeRestauracion;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.DatoProyectoInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.ExpertoRepetidoException;
import co.edu.unbosque.archiveludo.exceptions.ProyectoNoEncontradoException;
import co.edu.unbosque.archiveludo.model.EstadoProyecto;
import co.edu.unbosque.archiveludo.repository.ExpertoRepository;
import co.edu.unbosque.archiveludo.repository.ParticipacionRepository;
import co.edu.unbosque.archiveludo.repository.ProyectoDeRestauracionRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;
import co.edu.unbosque.archiveludo.service.AlmacenamientoArchivosService;
import co.edu.unbosque.archiveludo.service.ProyectoDeRestauracionService;

/**
 * Pruebas unitarias del CRUD de proyectos de restauracion (ProyectoDeRestauracionService).
 * Los repositories y Cloudinary son simulados con Mockito: no se toca la base de datos ni la nube.
 *
 * Criterio de aceptacion: se puede crear (con al menos un experto), leer, filtrar por estado,
 * actualizar y eliminar un proyecto; no se crea sin expertos ni con expertos repetidos;
 * un proyecto con informe no vuelve a activo; al eliminarlo se borran sus participaciones y su informe.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProyectoDeRestauracionTest {

	private static final long ID_PROYECTO = 7L;
	private static final long ID_VIDEOJUEGO = 16L;
	private static final long ID_USUARIO = 2L;
	private static final long ID_EXPERTO = 3L;
	private static final String CORREO_USUARIO = "curador@archiveludo.co";
	private static final String URL_INFORME = "https://res.cloudinary.com/demo/image/upload/informe-proyecto-7.pdf";
	private static final LocalDate FECHA_INICIO = LocalDate.now().minusDays(60);
	private static final LocalDate FECHA_FIN = LocalDate.now().minusDays(1);

	@Mock
	private ProyectoDeRestauracionRepository proyectoRepository;

	@Mock
	private ParticipacionRepository participacionRepository;

	@Mock
	private VideojuegoRepository videojuegoRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private ExpertoRepository expertoRepository;

	@Mock
	private AlmacenamientoArchivosService almacenamientoArchivosService;

	private ProyectoDeRestauracionService proyectoService;

	@BeforeEach
	public void prepararService() {
		proyectoService = new ProyectoDeRestauracionService(proyectoRepository, participacionRepository,
				videojuegoRepository, usuarioRepository, expertoRepository, almacenamientoArchivosService,
				new ModelMapper());

		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego());
		when(usuarioRepository.buscarPorCorreo(CORREO_USUARIO)).thenReturn(crearUsuario());
		when(expertoRepository.buscarPorId(ID_EXPERTO)).thenReturn(crearExperto());
		when(participacionRepository.buscarPorIdProyecto(anyLong())).thenReturn(crearListaParticipaciones());
		// Por defecto los proyectos no tienen informe
		when(proyectoRepository.buscarUrlInforme(anyLong())).thenReturn(null);
	}


	@Test
	@DisplayName("Crear un proyecto con un experto")
	public void crearProyectoTest() {
		when(proyectoRepository.crear(any(ProyectoDeRestauracion.class)))
				.thenReturn(crearProyecto(EstadoProyecto.ACTIVO, null));

		ProyectoDeRestauracionDTO proyectoCreado = proyectoService.crear("16", crearProyectoDTO(), CORREO_USUARIO);

		assertEquals(ID_PROYECTO, proyectoCreado.getIdProyecto());
		assertEquals("activo", proyectoCreado.getEstado());
		assertEquals("System Shock", proyectoCreado.getTituloVideojuego());
		assertEquals(1, proyectoCreado.getParticipaciones().size());
		verify(proyectoRepository, times(1)).crear(any(ProyectoDeRestauracion.class));
		verify(participacionRepository, times(1)).crear(any(Participacion.class));
	}

	@Test
	@DisplayName("No se crea un proyecto sin expertos")
	public void crearProyectoSinExpertosTest() {
		ProyectoDeRestauracionDTO proyectoDTO = crearProyectoDTO();
		proyectoDTO.setParticipaciones(new ArrayList<ParticipacionDTO>());

		try {
			proyectoService.crear("16", proyectoDTO, CORREO_USUARIO);
			fail("Debio lanzar DatoProyectoInvalidoException");
		} catch (DatoProyectoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(proyectoRepository, never()).crear(any(ProyectoDeRestauracion.class));
	}

	@Test
	@DisplayName("No se crea un proyecto con el mismo experto dos veces")
	public void crearProyectoExpertoRepetidoTest() {
		ProyectoDeRestauracionDTO proyectoDTO = crearProyectoDTO();
		proyectoDTO.getParticipaciones().add(crearParticipacionDTO());

		try {
			proyectoService.crear("16", proyectoDTO, CORREO_USUARIO);
			fail("Debio lanzar ExpertoRepetidoException");
		} catch (ExpertoRepetidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(proyectoRepository, never()).crear(any(ProyectoDeRestauracion.class));
	}


	@Test
	@DisplayName("Buscar un proyecto por id trae el videojuego y sus participaciones")
	public void buscarPorIdTest() {
		when(proyectoRepository.buscarPorId(ID_PROYECTO)).thenReturn(crearProyecto(EstadoProyecto.ACTIVO, null));

		ProyectoDeRestauracionDTO proyecto = proyectoService.buscarPorId("7");

		assertEquals("Rescate de System Shock", proyecto.getNombre());
		assertEquals("MS-DOS", proyecto.getPlataformaVideojuego());
		assertEquals(1, proyecto.getParticipaciones().size());
	}

	@Test
	@DisplayName("Buscar un proyecto que no existe")
	public void buscarPorIdInexistenteTest() {
		when(proyectoRepository.buscarPorId(99L)).thenReturn(null);

		try {
			proyectoService.buscarPorId("99");
			fail("Debio lanzar ProyectoNoEncontradoException");
		} catch (ProyectoNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("Listar proyectos filtrando por estado")
	public void buscarTodosPorEstadoTest() {
		List<ProyectoDeRestauracion> finalizados = new ArrayList<ProyectoDeRestauracion>();
		finalizados.add(crearProyecto(EstadoProyecto.FINALIZADO, FECHA_FIN));
		when(proyectoRepository.buscarPorEstado(EstadoProyecto.FINALIZADO)).thenReturn(finalizados);

		List<ProyectoDeRestauracionDTO> resultado = proyectoService.buscarTodos("finalizado");

		assertEquals(1, resultado.size());
		assertEquals("finalizado", resultado.get(0).getEstado());
		verify(proyectoRepository, never()).buscarTodos();
	}

	@Test
	@DisplayName("El proyecto indica si tiene informe final")
	public void proyectoIndicaInformeTest() {
		when(proyectoRepository.buscarPorId(ID_PROYECTO))
				.thenReturn(crearProyecto(EstadoProyecto.FINALIZADO, FECHA_FIN));
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(URL_INFORME);

		ProyectoDeRestauracionDTO proyecto = proyectoService.buscarPorId("7");

		assertTrue(proyecto.isTieneInforme());
	}


	@Test
	@DisplayName("Finalizar un proyecto")
	public void actualizarProyectoTest() {
		when(proyectoRepository.buscarPorId(ID_PROYECTO)).thenReturn(crearProyecto(EstadoProyecto.ACTIVO, null));

		ProyectoDeRestauracionDTO datosNuevos = crearProyectoDTO();
		datosNuevos.setEstado("finalizado");
		datosNuevos.setFechaFin(FECHA_FIN);

		ProyectoDeRestauracionDTO proyectoActualizado = proyectoService.actualizar("7", datosNuevos);

		assertEquals("finalizado", proyectoActualizado.getEstado());
		verify(proyectoRepository, times(1)).actualizar(any(ProyectoDeRestauracion.class));
	}

	@Test
	@DisplayName("Un proyecto con informe final no puede volver a activo")
	public void actualizarReactivarProyectoConInformeTest() {
		when(proyectoRepository.buscarPorId(ID_PROYECTO))
				.thenReturn(crearProyecto(EstadoProyecto.FINALIZADO, FECHA_FIN));
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(URL_INFORME);

		try {
			proyectoService.actualizar("7", crearProyectoDTO());
			fail("Debio lanzar DatoProyectoInvalidoException");
		} catch (DatoProyectoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(proyectoRepository, never()).actualizar(any(ProyectoDeRestauracion.class));
	}


	@Test
	@DisplayName("Eliminar un proyecto borra sus participaciones y su informe")
	public void eliminarProyectoConInformeTest() {
		when(proyectoRepository.buscarPorId(ID_PROYECTO))
				.thenReturn(crearProyecto(EstadoProyecto.FINALIZADO, FECHA_FIN));
		when(proyectoRepository.buscarUrlInforme(ID_PROYECTO)).thenReturn(URL_INFORME);

		proyectoService.eliminar("7");

		verify(participacionRepository, times(1)).eliminarPorIdProyecto(ID_PROYECTO);
		verify(proyectoRepository, times(1)).eliminar(ID_PROYECTO);
		verify(almacenamientoArchivosService, times(1)).eliminarArchivo(URL_INFORME);
	}

	@Test
	@DisplayName("Eliminar un proyecto sin informe no llama a Cloudinary")
	public void eliminarProyectoSinInformeTest() {
		when(proyectoRepository.buscarPorId(ID_PROYECTO)).thenReturn(crearProyecto(EstadoProyecto.ACTIVO, null));

		proyectoService.eliminar("7");

		verify(proyectoRepository, times(1)).eliminar(ID_PROYECTO);
		verify(almacenamientoArchivosService, never()).eliminarArchivo(any(String.class));
	}


	private Videojuego crearVideojuego() {
		Videojuego videojuego = new Videojuego();
		videojuego.setIdVideojuego(ID_VIDEOJUEGO);
		videojuego.setTitulo("System Shock");
		videojuego.setPlataformaOriginal("MS-DOS");
		return videojuego;
	}

	private Usuario crearUsuario() {
		Usuario usuario = new Usuario();
		usuario.setIdUsuario(ID_USUARIO);
		usuario.setCorreo(CORREO_USUARIO);
		return usuario;
	}

	private Experto crearExperto() {
		Experto experto = new Experto();
		experto.setIdExperto(ID_EXPERTO);
		experto.setPrimerNombreEx("Valentina");
		experto.setPrimerApellidoEx("Rojas");
		experto.setEspecialidad("Ingenieria inversa");
		return experto;
	}

	private ProyectoDeRestauracion crearProyecto(EstadoProyecto estado, LocalDate fechaFin) {
		ProyectoDeRestauracion proyecto = new ProyectoDeRestauracion();
		proyecto.setIdProyecto(ID_PROYECTO);
		proyecto.setIdVideojuego(ID_VIDEOJUEGO);
		proyecto.setIdUsuario(ID_USUARIO);
		proyecto.setNombre("Rescate de System Shock");
		proyecto.setDescripcion("Reconstruccion del codigo perdido");
		proyecto.setHerramientaEmulador("DOSBox");
		proyecto.setEstado(estado);
		proyecto.setFechaInicio(FECHA_INICIO);
		proyecto.setFechaFin(fechaFin);
		return proyecto;
	}

	private ProyectoDeRestauracionDTO crearProyectoDTO() {
		List<ParticipacionDTO> participaciones = new ArrayList<ParticipacionDTO>();
		participaciones.add(crearParticipacionDTO());

		ProyectoDeRestauracionDTO proyectoDTO = new ProyectoDeRestauracionDTO();
		proyectoDTO.setNombre("Rescate de System Shock");
		proyectoDTO.setDescripcion("Reconstruccion del codigo perdido");
		proyectoDTO.setHerramientaEmulador("DOSBox");
		proyectoDTO.setEstado("activo");
		proyectoDTO.setFechaInicio(FECHA_INICIO);
		proyectoDTO.setParticipaciones(participaciones);
		return proyectoDTO;
	}

	private ParticipacionDTO crearParticipacionDTO() {
		ParticipacionDTO participacionDTO = new ParticipacionDTO();
		participacionDTO.setIdExperto(ID_EXPERTO);
		participacionDTO.setRolExperto("Ingeniera inversa");
		participacionDTO.setFechaIncorporacion(FECHA_INICIO);
		return participacionDTO;
	}

	private List<Participacion> crearListaParticipaciones() {
		List<Participacion> participaciones = new ArrayList<Participacion>();
		participaciones.add(new Participacion(ID_PROYECTO, ID_EXPERTO, "Ingeniera inversa", FECHA_INICIO, null));
		return participaciones;
	}

}