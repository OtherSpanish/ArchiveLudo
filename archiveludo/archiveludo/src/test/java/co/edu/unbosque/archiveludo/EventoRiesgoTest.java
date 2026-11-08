package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

import co.edu.unbosque.archiveludo.dto.EventoDeRiesgoDTO;
import co.edu.unbosque.archiveludo.entity.EventoDeRiesgo;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.DatoEventoRiesgoInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.EventoDeRiesgoNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.model.TipoEventoRiesgo;
import co.edu.unbosque.archiveludo.repository.EventoRiesgoRepository;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;
import co.edu.unbosque.archiveludo.service.EventoRiesgoService;

/**
 * Pruebas unitarias del CRUD de eventos de riesgo (EventoRiesgoService).
 * Los repositories son simulados con Mockito: no se toca la base de datos.
 *
 * Criterio de aceptacion: se puede crear, leer, actualizar y eliminar un evento de riesgo;
 * el tipo sale en texto legible; la fuente vacia se guarda como null;
 * se rechazan fechas futuras, descripciones vacias y videojuegos inexistentes.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EventoRiesgoTest {

	private static final long ID_EVENTO = 8L;
	private static final long ID_VIDEOJUEGO = 16L;
	private static final long ID_USUARIO = 2L;
	private static final String CORREO_USUARIO = "curador@archiveludo.co";

	@Mock
	private EventoRiesgoRepository eventoRiesgoRepository;

	@Mock
	private VideojuegoRepository videojuegoRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	private EventoRiesgoService eventoRiesgoService;

	@BeforeEach
	public void prepararService() {
		eventoRiesgoService = new EventoRiesgoService(eventoRiesgoRepository, videojuegoRepository,
				usuarioRepository, new ModelMapper());

		when(videojuegoRepository.buscarPorId(ID_VIDEOJUEGO)).thenReturn(crearVideojuego());
		when(usuarioRepository.buscarPorCorreo(CORREO_USUARIO)).thenReturn(crearUsuario());
	}


	@Test
	@DisplayName("Crear un evento de riesgo")
	public void crearEventoTest() {
		when(eventoRiesgoRepository.crear(any(EventoDeRiesgo.class))).thenReturn(crearEvento());

		EventoDeRiesgoDTO eventoDTO = crearEventoDTO(LocalDate.now().minusDays(3), "Se apagaron los servidores");
		eventoDTO.setFuente("   ");

		EventoDeRiesgoDTO eventoCreado = eventoRiesgoService.crear("16", eventoDTO, CORREO_USUARIO);

		assertEquals(ID_EVENTO, eventoCreado.getIdEvento());
		assertEquals("Cierre de servidores", eventoCreado.getTipoEvento());

		// Una fuente vacia se guarda como null
		ArgumentCaptor<EventoDeRiesgo> capturaEvento = ArgumentCaptor.forClass(EventoDeRiesgo.class);
		verify(eventoRiesgoRepository, times(1)).crear(capturaEvento.capture());
		assertNull(capturaEvento.getValue().getFuente());
		assertEquals(ID_USUARIO, capturaEvento.getValue().getIdUsuario());
	}

	@Test
	@DisplayName("No se crea un evento con fecha futura")
	public void crearEventoFechaFuturaTest() {
		try {
			eventoRiesgoService.crear("16", crearEventoDTO(LocalDate.now().plusDays(1), "Evento futuro"),
					CORREO_USUARIO);
			fail("Debio lanzar DatosEventoRiesgoInvalidosException");
		} catch (DatoEventoRiesgoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(eventoRiesgoRepository, never()).crear(any(EventoDeRiesgo.class));
	}

	@Test
	@DisplayName("No se crea un evento sin descripcion")
	public void crearEventoSinDescripcionTest() {
		try {
			eventoRiesgoService.crear("16", crearEventoDTO(LocalDate.now(), ""), CORREO_USUARIO);
			fail("Debio lanzar DatosEventoRiesgoInvalidosException");
		} catch (DatoEventoRiesgoInvalidoException e) {
			assertNotNull(e.getMessage());
		}
		verify(eventoRiesgoRepository, never()).crear(any(EventoDeRiesgo.class));
	}

	@Test
	@DisplayName("No se crea un evento para un videojuego que no existe")
	public void crearEventoVideojuegoInexistenteTest() {
		when(videojuegoRepository.buscarPorId(99L)).thenReturn(null);

		try {
			eventoRiesgoService.crear("99", crearEventoDTO(LocalDate.now(), "Descripcion"), CORREO_USUARIO);
			fail("Debio lanzar VideojuegoNoEncontradoException");
		} catch (VideojuegoNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}


	@Test
	@DisplayName("Listar los eventos de riesgo de un videojuego")
	public void buscarPorVideojuegoTest() {
		List<EventoDeRiesgo> eventos = new ArrayList<EventoDeRiesgo>();
		eventos.add(crearEvento());
		eventos.add(crearEvento());
		when(eventoRiesgoRepository.buscarPorIdVideojuego(ID_VIDEOJUEGO)).thenReturn(eventos);

		List<EventoDeRiesgoDTO> resultado = eventoRiesgoService.buscarPorVideojuego("16");

		assertEquals(2, resultado.size());
	}

	@Test
	@DisplayName("Buscar un evento de riesgo por id")
	public void buscarPorIdTest() {
		when(eventoRiesgoRepository.buscarPorId(ID_EVENTO)).thenReturn(crearEvento());

		EventoDeRiesgoDTO evento = eventoRiesgoService.buscarPorId("8");

		assertEquals("Cierre de servidores", evento.getTipoEvento());
		assertEquals("Se apagaron los servidores oficiales", evento.getDescripcion());
	}

	@Test
	@DisplayName("Buscar un evento de riesgo que no existe")
	public void buscarPorIdInexistenteTest() {
		when(eventoRiesgoRepository.buscarPorId(99L)).thenReturn(null);

		try {
			eventoRiesgoService.buscarPorId("99");
			fail("Debio lanzar EventoDeRiesgoNoEncontradoException");
		} catch (EventoDeRiesgoNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}


	@Test
	@DisplayName("Corregir la descripcion de un evento")
	public void actualizarEventoTest() {
		when(eventoRiesgoRepository.buscarPorId(ID_EVENTO)).thenReturn(crearEvento());

		EventoDeRiesgoDTO datosNuevos = crearEventoDTO(LocalDate.now().minusDays(3), "Descripcion corregida");

		EventoDeRiesgoDTO eventoActualizado = eventoRiesgoService.actualizar("8", datosNuevos);

		assertEquals("Descripcion corregida", eventoActualizado.getDescripcion());
		verify(eventoRiesgoRepository, times(1)).actualizar(any(EventoDeRiesgo.class));
	}

	@Test
	@DisplayName("Eliminar un evento de riesgo")
	public void eliminarEventoTest() {
		when(eventoRiesgoRepository.buscarPorId(ID_EVENTO)).thenReturn(crearEvento());

		eventoRiesgoService.eliminar("8");

		verify(eventoRiesgoRepository, times(1)).eliminar(ID_EVENTO);
	}

	@Test
	@DisplayName("No se elimina un evento de riesgo que no existe")
	public void eliminarEventoInexistenteTest() {
		when(eventoRiesgoRepository.buscarPorId(99L)).thenReturn(null);

		try {
			eventoRiesgoService.eliminar("99");
			fail("Debio lanzar EventoDeRiesgoNoEncontradoException");
		} catch (EventoDeRiesgoNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
		verify(eventoRiesgoRepository, never()).eliminar(99L);
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

	private EventoDeRiesgo crearEvento() {
		EventoDeRiesgo evento = new EventoDeRiesgo(ID_VIDEOJUEGO, ID_USUARIO,
				TipoEventoRiesgo.buscarPorValor("cierre de servidores"), LocalDate.now().minusDays(3),
				"Se apagaron los servidores oficiales", "Comunicado oficial");
		evento.setIdEvento(ID_EVENTO);
		return evento;
	}

	private EventoDeRiesgoDTO crearEventoDTO(LocalDate fecha, String descripcion) {
		EventoDeRiesgoDTO eventoDTO = new EventoDeRiesgoDTO();
		eventoDTO.setTipoEvento("cierre de servidores");
		eventoDTO.setFechaEvento(fecha);
		eventoDTO.setDescripcion(descripcion);
		eventoDTO.setFuente("Comunicado oficial");
		return eventoDTO;
	}

}