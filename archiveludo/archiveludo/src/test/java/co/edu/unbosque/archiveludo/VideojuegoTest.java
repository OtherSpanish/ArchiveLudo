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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;

import co.edu.unbosque.archiveludo.dto.VideojuegoDTO;
import co.edu.unbosque.archiveludo.entity.CompaniaEditora;
import co.edu.unbosque.archiveludo.entity.Estudio;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.ArchivoInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.EstudioNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.model.EstadoConservacion;
import co.edu.unbosque.archiveludo.model.EstadoEstudio;
import co.edu.unbosque.archiveludo.repository.CompaniaEditoraRepository;
import co.edu.unbosque.archiveludo.repository.EstudioRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;
import co.edu.unbosque.archiveludo.service.AlmacenamientoArchivosService;
import co.edu.unbosque.archiveludo.service.VideojuegoService;

/**
 * Pruebas unitarias del CRUD de videojuegos, sus busquedas y su imagen (VideojuegoService).
 * Los repositories y Cloudinary son simulados con Mockito: no se toca la base de datos ni la nube.
 *
 * Criterio de aceptacion: se puede crear, leer (por id, catalogo y busquedas), actualizar y eliminar
 * un videojuego; se puede subir y quitar su imagen; no se crea con un estudio inexistente,
 * no se elimina con registros asociados y no se acepta una imagen que no sea jpg, png o webp.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VideojuegoTest {

	private static final long ID_VIDEOJUEGO = 16L;
	private static final long ID_ESTUDIO = 4L;
	private static final long ID_EDITORA = 3L;
	private static final long ID_USUARIO = 2L;
	private static final String CORREO_USUARIO = "curador@archiveludo.co";
	private static final String URL_IMAGEN = "https://res.cloudinary.com/demo/image/upload/videojuego-16.png";

	@Mock
	private VideojuegoRepository videojuegoRepository;

	@Mock
	private EstudioRepository estudioRepository;

	@Mock
	private CompaniaEditoraRepository companiaEditoraRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private AlmacenamientoArchivosService almacenamientoArchivosService;

	private VideojuegoService videojuegoService;

	@BeforeEach
	public void prepararService() {
		videojuegoService = new VideojuegoService(videojuegoRepository, estudioRepository, companiaEditoraRepository,
				usuarioRepository, almacenamientoArchivosService, new ModelMapper());

		when(usuarioRepository.buscarPorCorreo(CORREO_USUARIO)).thenReturn(crearUsuario());
		when(estudioRepository.buscarPorId(ID_ESTUDIO)).thenReturn(crearEstudio());
		when(companiaEditoraRepository.buscarPorId(ID_EDITORA)).thenReturn(new CompaniaEditora("Origin Systems", "Estados Unidos"));
	}

	@Test
	@DisplayName("Crear un videojuego con su estudio y sin editoras repetidas")
	public void crearVideojuegoTest() {
		when(videojuegoRepository.crear(any(Videojuego.class))).thenReturn(crearVideojuego(null));

		VideojuegoDTO videojuegoDTO = crearVideojuegoDTO();
		videojuegoDTO.getIdsEditoras().add(ID_EDITORA);

		VideojuegoDTO videojuegoCreado = videojuegoService.crear(videojuegoDTO, CORREO_USUARIO);

		assertEquals(ID_VIDEOJUEGO, videojuegoCreado.getIdVideojuego());
		assertEquals("en riesgo", videojuegoCreado.getEstadoConservacion());

		// La editora venia dos veces: se debe guardar una sola
		ArgumentCaptor<Videojuego> capturaVideojuego = ArgumentCaptor.forClass(Videojuego.class);
		verify(videojuegoRepository, times(1)).crear(capturaVideojuego.capture());
		assertEquals(1, capturaVideojuego.getValue().getIdsEditoras().size());
		assertEquals(ID_USUARIO, capturaVideojuego.getValue().getIdUsuario());
	}

	@Test
	@DisplayName("No se crea un videojuego con un estudio que no existe")
	public void crearVideojuegoEstudioInexistenteTest() {
		VideojuegoDTO videojuegoDTO = crearVideojuegoDTO();
		videojuegoDTO.setIdEstudio(99L);
		when(estudioRepository.buscarPorId(99L)).thenReturn(null);

		try {
			videojuegoService.crear(videojuegoDTO, CORREO_USUARIO);
			fail("Debio lanzar EstudioNoEncontradoException");
		} catch (EstudioNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
		verify(videojuegoRepository, never()).crear(any(Videojuego.class));
	}


	@Test
	@DisplayName("Buscar un videojuego por id")
	public void buscarPorIdTest() {
		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego(URL_IMAGEN));

		VideojuegoDTO videojuego = videojuegoService.buscarPorId("16");

		assertEquals("System Shock", videojuego.getTitulo());
		assertEquals("en riesgo", videojuego.getEstadoConservacion());
		assertEquals(URL_IMAGEN, videojuego.getUrlImagen());
	}

	@Test
	@DisplayName("Buscar un videojuego que no existe")
	public void buscarPorIdInexistenteTest() {
		when(videojuegoRepository.buscarPorId(99L)).thenReturn(null);

		try {
			videojuegoService.buscarPorId("99");
			fail("Debio lanzar VideojuegoNoEncontradoException");
		} catch (VideojuegoNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("El catalogo filtra por estado y calcula el desplazamiento de la pagina")
	public void buscarCatalogoTest() {
		List<Videojuego> pagina = new ArrayList<Videojuego>();
		pagina.add(crearVideojuego(null));
		when(videojuegoRepository.buscarCatalogo(EstadoConservacion.EN_RIESGO, null, 10, 20)).thenReturn(pagina);

		List<VideojuegoDTO> resultado = videojuegoService.buscarCatalogo("en riesgo", null, "2", "10");

		assertEquals(1, resultado.size());
		// pagina 2 con limite 10 = saltar 20 videojuegos
		verify(videojuegoRepository, times(1)).buscarCatalogo(EstadoConservacion.EN_RIESGO, null, 10, 20);
	}

	@Test
	@DisplayName("La busqueda por titulo busca titulos que contengan el texto")
	public void buscarPorTituloTest() {
		List<Videojuego> encontrados = new ArrayList<Videojuego>();
		encontrados.add(crearVideojuego(null));
		when(videojuegoRepository.buscarPorTitulo("%shock%")).thenReturn(encontrados);

		List<VideojuegoDTO> resultado = videojuegoService.buscarPorTitulo("  shock ");

		assertEquals(1, resultado.size());
		verify(videojuegoRepository, times(1)).buscarPorTitulo("%shock%");
	}


	@Test
	@DisplayName("Actualizar los datos de un videojuego")
	public void actualizarVideojuegoTest() {
		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego(null));

		VideojuegoDTO datosNuevos = crearVideojuegoDTO();
		datosNuevos.setEstadoConservacion("restaurado");

		VideojuegoDTO videojuegoActualizado = videojuegoService.actualizar("16", datosNuevos);

		assertEquals("restaurado", videojuegoActualizado.getEstadoConservacion());
		verify(videojuegoRepository, times(1)).actualizar(any(Videojuego.class));
	}


	@Test
	@DisplayName("Eliminar un videojuego borra tambien su imagen")
	public void eliminarVideojuegoTest() {
		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego(URL_IMAGEN));
		when(videojuegoRepository.contarRegistrosAsociados(ID_VIDEOJUEGO)).thenReturn(0L);

		videojuegoService.eliminar("16");

		verify(videojuegoRepository, times(1)).eliminar(ID_VIDEOJUEGO);
		verify(almacenamientoArchivosService, times(1)).eliminarArchivo(URL_IMAGEN);
	}

	@Test
	@DisplayName("No se elimina un videojuego con copias, estatus, eventos o proyectos")
	public void eliminarVideojuegoConRegistrosTest() {
		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego(null));
		when(videojuegoRepository.contarRegistrosAsociados(ID_VIDEOJUEGO)).thenReturn(5L);

		try {
			videojuegoService.eliminar("16");
			fail("Debio lanzar RegistroAsociadoException");
		} catch (RegistroAsociadoException e) {
			assertNotNull(e.getMessage());
		}
		verify(videojuegoRepository, never()).eliminar(anyLong());
	}


	@Test
	@DisplayName("Subir una imagen PNG guarda su URL")
	public void subirImagenTest() {
		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego(null));
		when(almacenamientoArchivosService.subirImagenVideojuego(any(Long.class), any(byte[].class)))
				.thenReturn(URL_IMAGEN);

		MockMultipartFile imagen = new MockMultipartFile("imagen", "portada.png", "image/png",
				new byte[] { 1, 2, 3, 4 });

		VideojuegoDTO videojuego = videojuegoService.subirImagen("16", imagen);

		assertEquals(URL_IMAGEN, videojuego.getUrlImagen());
		verify(videojuegoRepository, times(1)).actualizarUrlImagen(ID_VIDEOJUEGO, URL_IMAGEN);
	}

	@Test
	@DisplayName("No se acepta una imagen que no sea jpg, png o webp")
	public void subirImagenFormatoInvalidoTest() {
		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego(null));

		MockMultipartFile archivo = new MockMultipartFile("imagen", "notas.txt", "text/plain",
				new byte[] { 1, 2, 3 });

		try {
			videojuegoService.subirImagen("16", archivo);
			fail("Debio lanzar ArchivoInvalidoException");
		} catch (ArchivoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(almacenamientoArchivosService, never()).subirImagenVideojuego(any(Long.class), any(byte[].class));
	}

	@Test
	@DisplayName("Quitar la imagen la borra de Cloudinary y deja la URL vacia")
	public void eliminarImagenTest() {
		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego(URL_IMAGEN));

		videojuegoService.eliminarImagen("16");

		verify(videojuegoRepository, times(1)).actualizarUrlImagen(ID_VIDEOJUEGO, null);
		verify(almacenamientoArchivosService, times(1)).eliminarArchivo(URL_IMAGEN);
	}


	private Usuario crearUsuario() {
		Usuario usuario = new Usuario();
		usuario.setIdUsuario(ID_USUARIO);
		usuario.setCorreo(CORREO_USUARIO);
		return usuario;
	}

	private Estudio crearEstudio() {
		Estudio estudio = new Estudio("Looking Glass Studios", "Estados Unidos", EstadoEstudio.DESAPARECIDO, 1990, 2000);
		estudio.setIdEstudio(ID_ESTUDIO);
		return estudio;
	}

	private Videojuego crearVideojuego(String urlImagen) {
		List<Long> idsEditoras = new ArrayList<Long>();
		idsEditoras.add(ID_EDITORA);

		Videojuego videojuego = new Videojuego();
		videojuego.setIdVideojuego(ID_VIDEOJUEGO);
		videojuego.setTitulo("System Shock");
		videojuego.setGenero("Inmersivo");
		videojuego.setPlataformaOriginal("MS-DOS");
		videojuego.setAnioLanzamiento(1994);
		videojuego.setEstadoConservacion(EstadoConservacion.EN_RIESGO);
		videojuego.setIdEstudio(ID_ESTUDIO);
		videojuego.setIdUsuario(ID_USUARIO);
		videojuego.setIdsEditoras(idsEditoras);
		videojuego.setUrlImagen(urlImagen);
		return videojuego;
	}

	private VideojuegoDTO crearVideojuegoDTO() {
		List<Long> idsEditoras = new ArrayList<Long>();
		idsEditoras.add(ID_EDITORA);

		VideojuegoDTO videojuegoDTO = new VideojuegoDTO();
		videojuegoDTO.setTitulo("System Shock");
		videojuegoDTO.setGenero("Inmersivo");
		videojuegoDTO.setPlataformaOriginal("MS-DOS");
		videojuegoDTO.setDescripcion("Clasico de ciencia ficcion");
		videojuegoDTO.setAnioLanzamiento(1994);
		videojuegoDTO.setEstadoConservacion("en riesgo");
		videojuegoDTO.setIdEstudio(ID_ESTUDIO);
		videojuegoDTO.setIdsEditoras(idsEditoras);
		return videojuegoDTO;
	}

}