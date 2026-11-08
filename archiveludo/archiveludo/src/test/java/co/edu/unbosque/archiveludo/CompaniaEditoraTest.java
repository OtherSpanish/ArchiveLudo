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

import co.edu.unbosque.archiveludo.dto.CompaniaEditoraDTO;
import co.edu.unbosque.archiveludo.entity.CompaniaEditora;
import co.edu.unbosque.archiveludo.exceptions.CampoObligatorioException;
import co.edu.unbosque.archiveludo.exceptions.CompaniaEditoraNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.repository.CompaniaEditoraRepository;
import co.edu.unbosque.archiveludo.service.CompaniaEditoraService;

/**
 * Pruebas unitarias del CRUD de companias editoras (CompaniaEditoraService).
 * El repository es simulado con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: se puede crear, leer, actualizar y eliminar una compania editora;
 * el nombre y el pais son obligatorios; no se elimina una editora que publica videojuegos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CompaniaEditoraTest {

	private static final long ID_EDITORA = 3L;

	@Mock
	private CompaniaEditoraRepository companiaEditoraRepository;

	private CompaniaEditoraService companiaEditoraService;

	@BeforeEach
	public void prepararService() {
		companiaEditoraService = new CompaniaEditoraService(companiaEditoraRepository, new ModelMapper());
	}


	@Test
	@DisplayName("Crear una compania editora")
	public void crearCompaniaEditoraTest() {
		when(companiaEditoraRepository.crear(any(CompaniaEditora.class)))
				.thenReturn(new CompaniaEditora("Electronic Arts", "Estados Unidos"));

		CompaniaEditoraDTO editoraCreada = companiaEditoraService.crear(crearEditoraDTO("  Electronic Arts  "));

		assertEquals("Electronic Arts", editoraCreada.getNombre());
		assertEquals("Estados Unidos", editoraCreada.getPais());
		verify(companiaEditoraRepository, times(1)).crear(any(CompaniaEditora.class));
	}

	@Test
	@DisplayName("No se crea una compania editora sin nombre")
	public void crearCompaniaEditoraSinNombreTest() {
		try {
			companiaEditoraService.crear(crearEditoraDTO("   "));
			fail("Debio lanzar CampoObligatorioException");
		} catch (CampoObligatorioException e) {
			assertNotNull(e.getMessage());
		}
		verify(companiaEditoraRepository, never()).crear(any(CompaniaEditora.class));
	}


	@Test
	@DisplayName("Buscar una compania editora por id")
	public void buscarPorIdTest() {
		when(companiaEditoraRepository.buscarPorId(ID_EDITORA))
				.thenReturn(new CompaniaEditora("Electronic Arts", "Estados Unidos"));

		CompaniaEditoraDTO editora = companiaEditoraService.buscarPorId("3");

		assertEquals("Electronic Arts", editora.getNombre());
	}

	@Test
	@DisplayName("Buscar una compania editora que no existe")
	public void buscarPorIdInexistenteTest() {
		when(companiaEditoraRepository.buscarPorId(99L)).thenReturn(null);

		try {
			companiaEditoraService.buscarPorId("99");
			fail("Debio lanzar CompaniaEditoraNoEncontradaException");
		} catch (CompaniaEditoraNoEncontradaException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("Listar todas las companias editoras")
	public void buscarTodasTest() {
		List<CompaniaEditora> editoras = new ArrayList<CompaniaEditora>();
		editoras.add(new CompaniaEditora("Electronic Arts", "Estados Unidos"));
		editoras.add(new CompaniaEditora("Nintendo", "Japon"));
		when(companiaEditoraRepository.buscarTodas()).thenReturn(editoras);

		List<CompaniaEditoraDTO> resultado = companiaEditoraService.buscarTodas();

		assertEquals(2, resultado.size());
	}


	@Test
	@DisplayName("Actualizar el nombre de una compania editora")
	public void actualizarCompaniaEditoraTest() {
		when(companiaEditoraRepository.buscarPorId(ID_EDITORA))
				.thenReturn(new CompaniaEditora("Electronic Arts", "Estados Unidos"));

		CompaniaEditoraDTO editoraActualizada = companiaEditoraService.actualizar("3", crearEditoraDTO("EA Games"));

		assertEquals("EA Games", editoraActualizada.getNombre());
		verify(companiaEditoraRepository, times(1)).actualizar(any(CompaniaEditora.class));
	}


	@Test
	@DisplayName("Eliminar una compania editora sin videojuegos")
	public void eliminarCompaniaEditoraTest() {
		when(companiaEditoraRepository.buscarPorId(ID_EDITORA))
				.thenReturn(new CompaniaEditora("Electronic Arts", "Estados Unidos"));
		when(companiaEditoraRepository.contarVideojuegosAsociados(ID_EDITORA)).thenReturn(0L);

		companiaEditoraService.eliminar("3");

		verify(companiaEditoraRepository, times(1)).eliminar(ID_EDITORA);
	}

	@Test
	@DisplayName("No se elimina una compania editora que publica videojuegos")
	public void eliminarCompaniaEditoraConVideojuegosTest() {
		when(companiaEditoraRepository.buscarPorId(ID_EDITORA))
				.thenReturn(new CompaniaEditora("Electronic Arts", "Estados Unidos"));
		when(companiaEditoraRepository.contarVideojuegosAsociados(ID_EDITORA)).thenReturn(3L);

		try {
			companiaEditoraService.eliminar("3");
			fail("Debio lanzar RegistroAsociadoException");
		} catch (RegistroAsociadoException e) {
			assertNotNull(e.getMessage());
		}
		verify(companiaEditoraRepository, never()).eliminar(anyLong());
	}


	private CompaniaEditoraDTO crearEditoraDTO(String nombre) {
		CompaniaEditoraDTO editoraDTO = new CompaniaEditoraDTO();
		editoraDTO.setNombre(nombre);
		editoraDTO.setPais("Estados Unidos");
		return editoraDTO;
	}

}