package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import co.edu.unbosque.archiveludo.dto.CopiaDTO;
import co.edu.unbosque.archiveludo.dto.CopiaDigitalDTO;
import co.edu.unbosque.archiveludo.dto.CopiaFisicaDTO;
import co.edu.unbosque.archiveludo.entity.Copia;
import co.edu.unbosque.archiveludo.entity.CopiaDigital;
import co.edu.unbosque.archiveludo.entity.CopiaFisica;
import co.edu.unbosque.archiveludo.entity.Custodia;
import co.edu.unbosque.archiveludo.entity.Institucion;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.CopiaNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.DatoCopiaInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.repository.CopiaRepository;
import co.edu.unbosque.archiveludo.repository.CustodiaRepository;
import co.edu.unbosque.archiveludo.repository.InstitucionRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;
import co.edu.unbosque.archiveludo.service.CopiaService;

/**
 * Pruebas unitarias del CRUD de copias fisicas y digitales (CopiaService).
 * Los repositories son simulados con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: se puede crear, leer, actualizar y eliminar una copia;
 * cada copia trae los datos de su custodia vigente; no se cambia el tipo de una copia
 * y no se elimina una copia con custodias.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CopiaTest {

	private static final long ID_COPIA = 1L;
	private static final long ID_VIDEOJUEGO = 16L;
	private static final long ID_USUARIO = 2L;
	private static final long ID_INSTITUCION = 7L;
	private static final String CORREO_USUARIO = "curador@archiveludo.co";

	@Mock
	private CopiaRepository copiaRepository;

	@Mock
	private VideojuegoRepository videojuegoRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private CustodiaRepository custodiaRepository;

	@Mock
	private InstitucionRepository institucionRepository;

	private CopiaService copiaService;

	@BeforeEach
	public void prepararService() {
		copiaService = new CopiaService(copiaRepository, videojuegoRepository, usuarioRepository,
				custodiaRepository, institucionRepository, new ModelMapper());

		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego());
		when(usuarioRepository.buscarPorCorreo(CORREO_USUARIO)).thenReturn(crearUsuario());
		// Por defecto ninguna copia tiene custodia vigente
		when(custodiaRepository.buscarVigentePorIdCopia(anyLong())).thenReturn(null);
	}


	@Test
	@DisplayName("Crear una copia fisica")
	public void crearCopiaFisicaTest() {
		when(copiaRepository.crearCopiaFisica(any(CopiaFisica.class))).thenReturn(crearCopiaFisica());

		CopiaDTO copiaCreada = copiaService.crear("16", crearCopiaFisicaDTO("buena"), CORREO_USUARIO);

		assertTrue(copiaCreada instanceof CopiaFisicaDTO);
		assertEquals(ID_COPIA, copiaCreada.getIdCopia());
		assertEquals("fisica", copiaCreada.getTipoCopia());
		assertEquals("buena", copiaCreada.getEstadoIntegridad());
		verify(copiaRepository, times(1)).crearCopiaFisica(any(CopiaFisica.class));
		verify(copiaRepository, never()).crearCopiaDigital(any(CopiaDigital.class));
	}

	@Test
	@DisplayName("Crear una copia digital")
	public void crearCopiaDigitalTest() {
		when(copiaRepository.crearCopiaDigital(any(CopiaDigital.class))).thenReturn(crearCopiaDigital());

		CopiaDTO copiaCreada = copiaService.crear("16", crearCopiaDigitalDTO(), CORREO_USUARIO);

		assertTrue(copiaCreada instanceof CopiaDigitalDTO);
		assertEquals("digital", copiaCreada.getTipoCopia());
		verify(copiaRepository, times(1)).crearCopiaDigital(any(CopiaDigital.class));
	}

	@Test
	@DisplayName("No se crea una copia sin tipo de copia")
	public void crearCopiaSinTipoTest() {
		CopiaDTO copiaSinTipo = new CopiaDTO();
		copiaSinTipo.setEstadoIntegridad("buena");

		try {
			copiaService.crear("16", copiaSinTipo, CORREO_USUARIO);
			fail("Debio lanzar DatosCopiaInvalidosException");
		} catch (DatoCopiaInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(copiaRepository, never()).crearCopiaFisica(any(CopiaFisica.class));
		verify(copiaRepository, never()).crearCopiaDigital(any(CopiaDigital.class));
	}

	@Test
	@DisplayName("No se crea una copia de un videojuego que no existe")
	public void crearCopiaVideojuegoInexistenteTest() {
		when(videojuegoRepository.buscarPorId(999L)).thenReturn(null);

		try {
			copiaService.crear("999", crearCopiaFisicaDTO("buena"), CORREO_USUARIO);
			fail("Debio lanzar VideojuegoNoEncontradoException");
		} catch (VideojuegoNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}


	@Test
	@DisplayName("Listar las copias de un videojuego")
	public void buscarPorVideojuegoTest() {
		List<Copia> copias = new ArrayList<Copia>();
		copias.add(crearCopiaFisica());
		copias.add(crearCopiaDigital());
		when(copiaRepository.buscarPorIdVideojuego(ID_VIDEOJUEGO)).thenReturn(copias);

		List<CopiaDTO> resultado = copiaService.buscarPorVideojuego("16");

		assertEquals(2, resultado.size());
	}

	@Test
	@DisplayName("Buscar una copia trae los datos de su custodia vigente")
	public void buscarPorIdConCustodiaVigenteTest() {
		when(copiaRepository.buscarPorId(ID_COPIA)).thenReturn(crearCopiaFisica());
		when(custodiaRepository.buscarVigentePorIdCopia(ID_COPIA)).thenReturn(crearCustodiaVigente());
		when(institucionRepository.buscarPorId(ID_INSTITUCION)).thenReturn(crearInstitucion());

		CopiaDTO copia = copiaService.buscarPorId("1");

		assertEquals(ID_INSTITUCION, copia.getIdInstitucionActual().longValue());
		assertEquals("Archivo Digital Universitario", copia.getNombreInstitucionActual());
		assertEquals("Bodega climatizada", copia.getCondicionAlmacenamientoActual());
		assertNotNull(copia.getFechaInicioCustodiaActual());
	}

	@Test
	@DisplayName("Una copia sin custodia vigente no trae institucion actual")
	public void buscarPorIdSinCustodiaTest() {
		when(copiaRepository.buscarPorId(ID_COPIA)).thenReturn(crearCopiaFisica());

		CopiaDTO copia = copiaService.buscarPorId("1");

		assertNull(copia.getIdInstitucionActual());
		assertNull(copia.getNombreInstitucionActual());
	}

	@Test
	@DisplayName("Buscar una copia que no existe")
	public void buscarPorIdInexistenteTest() {
		when(copiaRepository.buscarPorId(99L)).thenReturn(null);

		try {
			copiaService.buscarPorId("99");
			fail("Debio lanzar CopiaNoEncontradaException");
		} catch (CopiaNoEncontradaException e) {
			assertNotNull(e.getMessage());
		}
	}


	@Test
	@DisplayName("Actualizar el estado de integridad de una copia fisica")
	public void actualizarCopiaTest() {
		when(copiaRepository.buscarPorId(ID_COPIA)).thenReturn(crearCopiaFisica());

		CopiaDTO copiaActualizada = copiaService.actualizar("1", crearCopiaFisicaDTO("deteriorada"));

		assertEquals("deteriorada", copiaActualizada.getEstadoIntegridad());
		verify(copiaRepository, times(1)).actualizarCopiaFisica(any(CopiaFisica.class));
	}

	@Test
	@DisplayName("No se puede cambiar una copia fisica a digital")
	public void actualizarCambiandoTipoTest() {
		when(copiaRepository.buscarPorId(ID_COPIA)).thenReturn(crearCopiaFisica());

		try {
			copiaService.actualizar("1", crearCopiaDigitalDTO());
			fail("Debio lanzar DatosCopiaInvalidosException");
		} catch (DatoCopiaInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(copiaRepository, never()).actualizarCopiaFisica(any(CopiaFisica.class));
		verify(copiaRepository, never()).actualizarCopiaDigital(any(CopiaDigital.class));
	}


	@Test
	@DisplayName("Eliminar una copia sin custodias")
	public void eliminarCopiaTest() {
		when(copiaRepository.buscarPorId(ID_COPIA)).thenReturn(crearCopiaFisica());
		when(copiaRepository.contarCustodiasAsociadas(ID_COPIA)).thenReturn(0L);

		copiaService.eliminar("1");

		verify(copiaRepository, times(1)).eliminar(ID_COPIA);
	}

	@Test
	@DisplayName("No se elimina una copia que tiene custodias")
	public void eliminarCopiaConCustodiasTest() {
		when(copiaRepository.buscarPorId(ID_COPIA)).thenReturn(crearCopiaFisica());
		when(copiaRepository.contarCustodiasAsociadas(ID_COPIA)).thenReturn(2L);

		try {
			copiaService.eliminar("1");
			fail("Debio lanzar RegistroAsociadoException");
		} catch (RegistroAsociadoException e) {
			assertNotNull(e.getMessage());
		}
		verify(copiaRepository, never()).eliminar(anyLong());
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

	private CopiaFisica crearCopiaFisica() {
		CopiaFisica copiaFisica = new CopiaFisica(ID_VIDEOJUEGO, ID_USUARIO, EstadoIntegridad.buscarPorValor("buena"),
				LocalDate.now(), "SN-001", "Bodega A", "Cartucho");
		copiaFisica.setIdCopia(ID_COPIA);
		return copiaFisica;
	}

	private CopiaDigital crearCopiaDigital() {
		CopiaDigital copiaDigital = new CopiaDigital(ID_VIDEOJUEGO, ID_USUARIO,
				EstadoIntegridad.buscarPorValor("buena"), LocalDate.now(), "ISO", 650L, "a1b2c3d4");
		copiaDigital.setIdCopia(2L);
		return copiaDigital;
	}

	private CopiaFisicaDTO crearCopiaFisicaDTO(String estadoIntegridad) {
		CopiaFisicaDTO copiaFisicaDTO = new CopiaFisicaDTO();
		copiaFisicaDTO.setTipoCopia("fisica");
		copiaFisicaDTO.setEstadoIntegridad(estadoIntegridad);
		copiaFisicaDTO.setNumeroSerie("SN-001");
		copiaFisicaDTO.setUbicacionFisica("Bodega A");
		copiaFisicaDTO.setTipoSoporte("Cartucho");
		return copiaFisicaDTO;
	}

	private CopiaDigitalDTO crearCopiaDigitalDTO() {
		CopiaDigitalDTO copiaDigitalDTO = new CopiaDigitalDTO();
		copiaDigitalDTO.setTipoCopia("digital");
		copiaDigitalDTO.setEstadoIntegridad("buena");
		copiaDigitalDTO.setFormatoArchivo("ISO");
		copiaDigitalDTO.setTamanio(650L);
		copiaDigitalDTO.setHashVerificacion("a1b2c3d4");
		return copiaDigitalDTO;
	}

	private Custodia crearCustodiaVigente() {
		Custodia custodia = new Custodia();
		custodia.setIdCustodia(10L);
		custodia.setIdCopia(ID_COPIA);
		custodia.setIdInstitucion(ID_INSTITUCION);
		custodia.setFechaInicio(LocalDate.now().minusDays(10));
		custodia.setCondicionAlmacenamiento("Bodega climatizada");
		return custodia;
	}

	private Institucion crearInstitucion() {
		Institucion institucion = new Institucion();
		institucion.setIdInstitucion(ID_INSTITUCION);
		institucion.setNombre("Archivo Digital Universitario");
		return institucion;
	}

}