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

import co.edu.unbosque.archiveludo.dto.InstitucionDTO;
import co.edu.unbosque.archiveludo.entity.InstCorreo;
import co.edu.unbosque.archiveludo.entity.InstTelefono;
import co.edu.unbosque.archiveludo.entity.Institucion;
import co.edu.unbosque.archiveludo.exceptions.DatoInstitucionInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.InstitucionNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.model.TipoInstitucion;
import co.edu.unbosque.archiveludo.repository.CustodiaRepository;
import co.edu.unbosque.archiveludo.repository.InstCorreoRepository;
import co.edu.unbosque.archiveludo.repository.InstTelefonoRepository;
import co.edu.unbosque.archiveludo.repository.InstitucionRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;
import co.edu.unbosque.archiveludo.service.InstitucionService;

/**
 * Pruebas unitarias del CRUD de instituciones (InstitucionService).
 * Los repositories son simulados con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: se puede crear, leer, actualizar y eliminar una institucion
 * con sus telefonos y correos; no se crea una institucion sin contacto y no se elimina
 * una institucion que tiene custodias.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InstitucionTest {

	private static final long ID_INSTITUCION = 1L;
	private static final long TELEFONO = 6015551234L;
	private static final String CORREO = "contacto@museovideojuego.org";

	@Mock
	private InstitucionRepository institucionRepository;

	@Mock
	private InstTelefonoRepository instTelefonoRepository;

	@Mock
	private InstCorreoRepository instCorreoRepository;

	@Mock
	private CustodiaRepository custodiaRepository;

	@Mock
	private VideojuegoRepository videojuegoRepository;

	private InstitucionService institucionService;

	@BeforeEach
	public void prepararService() {
		institucionService = new InstitucionService(institucionRepository, instTelefonoRepository,
				instCorreoRepository, custodiaRepository, videojuegoRepository);

		// Lo que devuelven los repositories cuando el service arma el DTO de cualquier institucion
		when(instTelefonoRepository.buscarPorIdInstitucion(anyLong())).thenReturn(crearListaTelefonos());
		when(instCorreoRepository.buscarPorIdInstitucion(anyLong())).thenReturn(crearListaCorreos());
		when(custodiaRepository.contarVigentesPorInstitucion(anyLong())).thenReturn(0L);
	}

	
	@Test
	@DisplayName("Crear una institucion con telefono y correo")
	public void crearInstitucionTest() {
		when(institucionRepository.crear(any(Institucion.class))).thenReturn(crearInstitucion(ID_INSTITUCION));

		InstitucionDTO institucionCreada = institucionService.crear(crearInstitucionDTO());

		assertEquals(ID_INSTITUCION, institucionCreada.getIdInstitucion());
		assertEquals("Museo del Videojuego", institucionCreada.getNombre());
		assertEquals(1, institucionCreada.getTelefonos().size());
		assertEquals(1, institucionCreada.getCorreos().size());
		verify(institucionRepository, times(1)).crear(any(Institucion.class));
		verify(instTelefonoRepository, times(1)).crear(any(InstTelefono.class));
		verify(instCorreoRepository, times(1)).crear(any(InstCorreo.class));
	}

	@Test
	@DisplayName("No se crea una institucion sin telefonos")
	public void crearInstitucionSinTelefonosTest() {
		InstitucionDTO institucionDTO = crearInstitucionDTO();
		institucionDTO.setTelefonos(new ArrayList<Long>());

		try {
			institucionService.crear(institucionDTO);
			fail("Debio lanzar DatoInstitucionInvalidoException");
		} catch (DatoInstitucionInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(institucionRepository, never()).crear(any(Institucion.class));
	}


	@Test
	@DisplayName("Buscar una institucion existente por su id")
	public void buscarPorIdTest() {
		when(institucionRepository.buscarPorId(ID_INSTITUCION)).thenReturn(crearInstitucion(ID_INSTITUCION));

		InstitucionDTO institucion = institucionService.buscarPorId("1");

		assertEquals(ID_INSTITUCION, institucion.getIdInstitucion());
		assertEquals("Museo del Videojuego", institucion.getNombre());
		assertNotNull(institucion.getTipo());
		assertEquals(TELEFONO, institucion.getTelefonos().get(0).longValue());
	}

	@Test
	@DisplayName("Buscar una institucion que no existe")
	public void buscarPorIdInexistenteTest() {
		when(institucionRepository.buscarPorId(99L)).thenReturn(null);

		try {
			institucionService.buscarPorId("99");
			fail("Debio lanzar InstitucionNoEncontradaException");
		} catch (InstitucionNoEncontradaException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("Buscar con un id que no es un numero")
	public void buscarPorIdInvalidoTest() {
		try {
			institucionService.buscarPorId("abc");
			fail("Debio lanzar IdInvalidoException");
		} catch (IdInvalidoException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("Listar todas las instituciones")
	public void buscarTodasTest() {
		List<Institucion> instituciones = new ArrayList<Institucion>();
		instituciones.add(crearInstitucion(1L));
		instituciones.add(crearInstitucion(2L));
		when(institucionRepository.buscarTodas()).thenReturn(instituciones);

		List<InstitucionDTO> resultado = institucionService.buscarTodas();

		assertEquals(2, resultado.size());
	}


	@Test
	@DisplayName("Actualizar una institucion reemplaza sus datos, telefonos y correos")
	public void actualizarInstitucionTest() {
		when(institucionRepository.buscarPorId(ID_INSTITUCION)).thenReturn(crearInstitucion(ID_INSTITUCION));

		InstitucionDTO datosNuevos = crearInstitucionDTO();
		datosNuevos.setNombre("Museo Nacional del Videojuego");

		InstitucionDTO institucionActualizada = institucionService.actualizar("1", datosNuevos);

		assertEquals("Museo Nacional del Videojuego", institucionActualizada.getNombre());
		verify(institucionRepository, times(1)).actualizar(any(Institucion.class));
		verify(instTelefonoRepository, times(1)).eliminarPorIdInstitucion(ID_INSTITUCION);
		verify(instCorreoRepository, times(1)).eliminarPorIdInstitucion(ID_INSTITUCION);
		verify(instTelefonoRepository, times(1)).crear(any(InstTelefono.class));
	}

	@Test
	@DisplayName("No se actualiza una institucion que no existe")
	public void actualizarInstitucionInexistenteTest() {
		when(institucionRepository.buscarPorId(99L)).thenReturn(null);

		try {
			institucionService.actualizar("99", crearInstitucionDTO());
			fail("Debio lanzar InstitucionNoEncontradaException");
		} catch (InstitucionNoEncontradaException e) {
			assertNotNull(e.getMessage());
		}
		verify(institucionRepository, never()).actualizar(any(Institucion.class));
	}


	@Test
	@DisplayName("Eliminar una institucion sin custodias")
	public void eliminarInstitucionTest() {
		when(institucionRepository.buscarPorId(ID_INSTITUCION)).thenReturn(crearInstitucion(ID_INSTITUCION));
		when(institucionRepository.contarCustodiasAsociadas(ID_INSTITUCION)).thenReturn(0L);

		institucionService.eliminar("1");

		verify(instTelefonoRepository, times(1)).eliminarPorIdInstitucion(ID_INSTITUCION);
		verify(instCorreoRepository, times(1)).eliminarPorIdInstitucion(ID_INSTITUCION);
		verify(institucionRepository, times(1)).eliminar(ID_INSTITUCION);
	}

	@Test
	@DisplayName("No se elimina una institucion que tiene custodias")
	public void eliminarInstitucionConCustodiasTest() {
		when(institucionRepository.buscarPorId(ID_INSTITUCION)).thenReturn(crearInstitucion(ID_INSTITUCION));
		when(institucionRepository.contarCustodiasAsociadas(ID_INSTITUCION)).thenReturn(3L);

		try {
			institucionService.eliminar("1");
			fail("Debio lanzar RegistroAsociadoException");
		} catch (RegistroAsociadoException e) {
			assertNotNull(e.getMessage());
		}
		verify(institucionRepository, never()).eliminar(anyLong());
	}


	private Institucion crearInstitucion(long idInstitucion) {
		Institucion institucion = new Institucion();
		institucion.setIdInstitucion(idInstitucion);
		institucion.setNombre("Museo del Videojuego");
		institucion.setTipo(TipoInstitucion.buscarPorValor("museo"));
		institucion.setPais("Colombia");
		return institucion;
	}

	private InstitucionDTO crearInstitucionDTO() {
		List<Long> telefonos = new ArrayList<Long>();
		telefonos.add(TELEFONO);

		List<String> correos = new ArrayList<String>();
		correos.add(CORREO);

		InstitucionDTO institucionDTO = new InstitucionDTO();
		institucionDTO.setNombre("Museo del Videojuego");
		institucionDTO.setTipo("museo");
		institucionDTO.setPais("Colombia");
		institucionDTO.setTelefonos(telefonos);
		institucionDTO.setCorreos(correos);
		return institucionDTO;
	}

	private List<InstTelefono> crearListaTelefonos() {
		List<InstTelefono> telefonos = new ArrayList<InstTelefono>();
		telefonos.add(new InstTelefono(ID_INSTITUCION, TELEFONO));
		return telefonos;
	}

	private List<InstCorreo> crearListaCorreos() {
		List<InstCorreo> correos = new ArrayList<InstCorreo>();
		correos.add(new InstCorreo(ID_INSTITUCION, CORREO));
		return correos;
	}

}