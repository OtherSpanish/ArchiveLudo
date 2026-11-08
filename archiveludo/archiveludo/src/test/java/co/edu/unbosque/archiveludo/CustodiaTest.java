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

import co.edu.unbosque.archiveludo.dto.CustodiaDTO;
import co.edu.unbosque.archiveludo.entity.CopiaFisica;
import co.edu.unbosque.archiveludo.entity.Custodia;
import co.edu.unbosque.archiveludo.entity.Institucion;
import co.edu.unbosque.archiveludo.exceptions.CustodiaNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.DatoCustodiaInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.InstitucionNoEncontradaException;
import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.repository.CopiaRepository;
import co.edu.unbosque.archiveludo.repository.CustodiaRepository;
import co.edu.unbosque.archiveludo.repository.InstitucionRepository;
import co.edu.unbosque.archiveludo.service.CustodiaService;

/**
 * Pruebas unitarias del CRUD de custodias (CustodiaService).
 * Los repositories son simulados con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: se puede crear, leer, actualizar y eliminar una custodia;
 * al registrar una custodia nueva se cierra la vigente; la nueva no puede empezar antes
 * que la vigente ni en el futuro; los periodos de una misma copia no se cruzan.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CustodiaTest {

	private static final long ID_COPIA = 1L;
	private static final long ID_CUSTODIA = 10L;
	private static final long ID_INSTITUCION = 7L;

	@Mock
	private CustodiaRepository custodiaRepository;

	@Mock
	private CopiaRepository copiaRepository;

	@Mock
	private InstitucionRepository institucionRepository;

	private CustodiaService custodiaService;

	@BeforeEach
	public void prepararService() {
		custodiaService = new CustodiaService(custodiaRepository, copiaRepository, institucionRepository,
				new ModelMapper());

		when(copiaRepository.buscarPorId(ID_COPIA)).thenReturn(crearCopia());
		when(institucionRepository.buscarPorId(ID_INSTITUCION)).thenReturn(crearInstitucion());
		when(custodiaRepository.buscarVigentePorIdCopia(ID_COPIA)).thenReturn(null);
		when(custodiaRepository.buscarPorIdCopia(ID_COPIA)).thenReturn(new ArrayList<Custodia>());
	}


	@Test
	@DisplayName("Crear la primera custodia de una copia")
	public void crearCustodiaTest() {
		LocalDate inicio = LocalDate.now().minusDays(5);
		when(custodiaRepository.crear(any(Custodia.class))).thenReturn(crearCustodia(ID_CUSTODIA, inicio, null));

		CustodiaDTO custodiaCreada = custodiaService.crear("1", crearCustodiaDTO(inicio, null));

		assertEquals("Archivo Digital Universitario", custodiaCreada.getNombreInstitucion());
		assertNull(custodiaCreada.getFechaFin());
		verify(custodiaRepository, times(1)).crear(any(Custodia.class));
		verify(custodiaRepository, never()).cerrar(anyLong(), any(LocalDate.class));
	}

	@Test
	@DisplayName("Crear una custodia nueva cierra la vigente el dia en que empieza la nueva")
	public void crearCustodiaCierraVigenteTest() {
		LocalDate inicioVigente = LocalDate.now().minusDays(30);
		LocalDate inicioNueva = LocalDate.now().minusDays(5);
		Custodia vigente = crearCustodia(9L, inicioVigente, null);

		List<Custodia> custodiasDeLaCopia = new ArrayList<Custodia>();
		custodiasDeLaCopia.add(vigente);
		when(custodiaRepository.buscarVigentePorIdCopia(ID_COPIA)).thenReturn(vigente);
		when(custodiaRepository.buscarPorIdCopia(ID_COPIA)).thenReturn(custodiasDeLaCopia);
		when(custodiaRepository.crear(any(Custodia.class))).thenReturn(crearCustodia(ID_CUSTODIA, inicioNueva, null));

		custodiaService.crear("1", crearCustodiaDTO(inicioNueva, null));

		verify(custodiaRepository, times(1)).cerrar(9L, inicioNueva);
		verify(custodiaRepository, times(1)).crear(any(Custodia.class));
	}

	@Test
	@DisplayName("No se crea una custodia que empieza antes que la vigente")
	public void crearCustodiaAnteriorAVigenteTest() {
		Custodia vigente = crearCustodia(9L, LocalDate.now().minusDays(10), null);
		when(custodiaRepository.buscarVigentePorIdCopia(ID_COPIA)).thenReturn(vigente);

		try {
			custodiaService.crear("1", crearCustodiaDTO(LocalDate.now().minusDays(20), null));
			fail("Debio lanzar DatosCustodiaInvalidosException");
		} catch (DatoCustodiaInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(custodiaRepository, never()).cerrar(anyLong(), any(LocalDate.class));
		verify(custodiaRepository, never()).crear(any(Custodia.class));
	}

	@Test
	@DisplayName("No se crea una custodia con fecha de inicio futura")
	public void crearCustodiaFechaFuturaTest() {
		try {
			custodiaService.crear("1", crearCustodiaDTO(LocalDate.now().plusDays(3), null));
			fail("Debio lanzar DatosCustodiaInvalidosException");
		} catch (DatoCustodiaInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(custodiaRepository, never()).crear(any(Custodia.class));
	}

	@Test
	@DisplayName("No se crea una custodia en una institucion que no existe")
	public void crearCustodiaInstitucionInexistenteTest() {
		CustodiaDTO custodiaDTO = crearCustodiaDTO(LocalDate.now().minusDays(1), null);
		custodiaDTO.setIdInstitucion(99L);
		when(institucionRepository.buscarPorId(99L)).thenReturn(null);

		try {
			custodiaService.crear("1", custodiaDTO);
			fail("Debio lanzar InstitucionNoEncontradaException");
		} catch (InstitucionNoEncontradaException e) {
			assertNotNull(e.getMessage());
		}
		verify(custodiaRepository, never()).crear(any(Custodia.class));
	}


	@Test
	@DisplayName("El historial de una copia trae el nombre de cada institucion")
	public void buscarPorCopiaTest() {
		List<Custodia> historial = new ArrayList<Custodia>();
		historial.add(crearCustodia(ID_CUSTODIA, LocalDate.now().minusDays(5), null));
		historial.add(crearCustodia(9L, LocalDate.now().minusDays(30), LocalDate.now().minusDays(5)));
		when(custodiaRepository.buscarPorIdCopia(ID_COPIA)).thenReturn(historial);

		List<CustodiaDTO> resultado = custodiaService.buscarPorCopia("1");

		assertEquals(2, resultado.size());
		assertEquals("Archivo Digital Universitario", resultado.get(0).getNombreInstitucion());
	}

	@Test
	@DisplayName("Buscar una custodia que no existe")
	public void buscarPorIdInexistenteTest() {
		when(custodiaRepository.buscarPorId(99L)).thenReturn(null);

		try {
			custodiaService.buscarPorId("99");
			fail("Debio lanzar CustodiaNoEncontradaException");
		} catch (CustodiaNoEncontradaException e) {
			assertNotNull(e.getMessage());
		}
	}


	@Test
	@DisplayName("Corregir la condicion de almacenamiento de una custodia historica")
	public void actualizarCustodiaTest() {
		LocalDate inicio = LocalDate.now().minusDays(60);
		LocalDate fin = LocalDate.now().minusDays(30);
		Custodia historica = crearCustodia(ID_CUSTODIA, inicio, fin);

		List<Custodia> custodiasDeLaCopia = new ArrayList<Custodia>();
		custodiasDeLaCopia.add(historica);
		when(custodiaRepository.buscarPorId(ID_CUSTODIA)).thenReturn(historica);
		when(custodiaRepository.buscarPorIdCopia(ID_COPIA)).thenReturn(custodiasDeLaCopia);

		CustodiaDTO datosNuevos = crearCustodiaDTO(inicio, fin);
		datosNuevos.setCondicionAlmacenamiento("Caja sellada con control de humedad");

		CustodiaDTO custodiaActualizada = custodiaService.actualizar("10", datosNuevos);

		assertEquals("Caja sellada con control de humedad", custodiaActualizada.getCondicionAlmacenamiento());
		verify(custodiaRepository, times(1)).actualizar(any(Custodia.class));
	}

	@Test
	@DisplayName("No se actualiza una custodia si su periodo se cruza con otra de la misma copia")
	public void actualizarCustodiaConCruceTest() {
		LocalDate inicio = LocalDate.now().minusDays(60);
		LocalDate fin = LocalDate.now().minusDays(30);
		Custodia editada = crearCustodia(ID_CUSTODIA, inicio, fin);
		Custodia otra = crearCustodia(11L, LocalDate.now().minusDays(40), LocalDate.now().minusDays(20));

		List<Custodia> custodiasDeLaCopia = new ArrayList<Custodia>();
		custodiasDeLaCopia.add(editada);
		custodiasDeLaCopia.add(otra);
		when(custodiaRepository.buscarPorId(ID_CUSTODIA)).thenReturn(editada);
		when(custodiaRepository.buscarPorIdCopia(ID_COPIA)).thenReturn(custodiasDeLaCopia);

		try {
			custodiaService.actualizar("10", crearCustodiaDTO(inicio, fin));
			fail("Debio lanzar DatosCustodiaInvalidosException");
		} catch (DatoCustodiaInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(custodiaRepository, never()).actualizar(any(Custodia.class));
	}


	@Test
	@DisplayName("Eliminar una custodia")
	public void eliminarCustodiaTest() {
		when(custodiaRepository.buscarPorId(ID_CUSTODIA))
				.thenReturn(crearCustodia(ID_CUSTODIA, LocalDate.now().minusDays(5), null));

		custodiaService.eliminar("10");

		verify(custodiaRepository, times(1)).eliminar(ID_CUSTODIA);
	}


	private CopiaFisica crearCopia() {
		CopiaFisica copia = new CopiaFisica(16L, 2L, EstadoIntegridad.buscarPorValor("buena"), LocalDate.now(),
				"SN-001", "Bodega A", "Cartucho");
		copia.setIdCopia(ID_COPIA);
		return copia;
	}

	private Institucion crearInstitucion() {
		Institucion institucion = new Institucion();
		institucion.setIdInstitucion(ID_INSTITUCION);
		institucion.setNombre("Archivo Digital Universitario");
		return institucion;
	}

	private Custodia crearCustodia(long idCustodia, LocalDate inicio, LocalDate fin) {
		Custodia custodia = new Custodia(ID_COPIA, ID_INSTITUCION, inicio, fin, "Bodega climatizada");
		custodia.setIdCustodia(idCustodia);
		return custodia;
	}

	private CustodiaDTO crearCustodiaDTO(LocalDate inicio, LocalDate fin) {
		CustodiaDTO custodiaDTO = new CustodiaDTO();
		custodiaDTO.setIdInstitucion(ID_INSTITUCION);
		custodiaDTO.setFechaInicio(inicio);
		custodiaDTO.setFechaFin(fin);
		custodiaDTO.setCondicionAlmacenamiento("Bodega climatizada");
		return custodiaDTO;
	}

}