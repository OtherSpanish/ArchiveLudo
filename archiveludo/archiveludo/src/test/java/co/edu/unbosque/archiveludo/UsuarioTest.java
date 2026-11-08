package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.archiveludo.dto.UsuarioDTO;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.exceptions.ContraseniaUsuarioException;
import co.edu.unbosque.archiveludo.exceptions.CorreoElectronicoException;
import co.edu.unbosque.archiveludo.exceptions.CorreoYaRegistradoException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.exceptions.UsuarioNoEncontradoException;
import co.edu.unbosque.archiveludo.model.Rol;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.service.UsuarioService;

/**
 * Pruebas unitarias del CRUD de usuarios (UsuarioService).
 * El repository es simulado con Mockito: no se toca la base de datos.
 * El PasswordEncoder es el BCrypt real, para comprobar que la contrasena se guarda hasheada.
 *
 * Criterio de aceptacion: se puede crear, leer, actualizar y eliminar un usuario;
 * la contrasena se guarda hasheada y nunca sale en la respuesta; se rechazan correos
 * y contrasenas invalidos, correos repetidos y la eliminacion de un usuario con registros.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UsuarioTest {

	private static final long ID_USUARIO = 2L;
	private static final String CORREO = "camila.torres@gmail.com";
	private static final String CONTRASENIA = "Clave2026";

	@Mock
	private UsuarioRepository usuarioRepository;

	private PasswordEncoder passwordEncoder;

	private UsuarioService usuarioService;

	@BeforeEach
	public void prepararService() {
		passwordEncoder = new BCryptPasswordEncoder();
		usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
	}


	@Test
	@DisplayName("Crear un usuario guarda la contrasena hasheada y no la devuelve")
	public void crearUsuarioTest() throws Exception {
		when(usuarioRepository.existeCorreo(CORREO)).thenReturn(false);
		when(usuarioRepository.guardar(any(Usuario.class))).thenReturn(crearUsuario("hash"));

		UsuarioDTO usuarioCreado = usuarioService.crear(crearUsuarioDTO(CORREO, CONTRASENIA));

		assertEquals(ID_USUARIO, usuarioCreado.getIdUsuario());
		assertEquals("curador", usuarioCreado.getRol());
		assertNull(usuarioCreado.getContrasenia());

		ArgumentCaptor<Usuario> capturaUsuario = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository, times(1)).guardar(capturaUsuario.capture());
		String hashGuardado = capturaUsuario.getValue().getHashContrasenia();
		assertNotEquals(CONTRASENIA, hashGuardado);
		assertTrue(passwordEncoder.matches(CONTRASENIA, hashGuardado));
	}

	@Test
	@DisplayName("No se crea un usuario con un correo de dominio no permitido")
	public void crearUsuarioCorreoInvalidoTest() throws Exception {
		try {
			usuarioService.crear(crearUsuarioDTO("camila@yahoo.com", CONTRASENIA));
			fail("Debio lanzar CorreoElectronicoException");
		} catch (CorreoElectronicoException e) {
			assertNotNull(e.getMessage());
		}
		verify(usuarioRepository, never()).guardar(any(Usuario.class));
	}

	@Test
	@DisplayName("No se crea un usuario con una contrasena sin mayuscula ni numero")
	public void crearUsuarioContraseniaInvalidaTest() throws Exception {
		try {
			usuarioService.crear(crearUsuarioDTO(CORREO, "clavesimple"));
			fail("Debio lanzar ContraseniaUsuarioException");
		} catch (ContraseniaUsuarioException e) {
			assertNotNull(e.getMessage());
		}
		verify(usuarioRepository, never()).guardar(any(Usuario.class));
	}

	@Test
	@DisplayName("No se crea un usuario con un correo ya registrado")
	public void crearUsuarioCorreoRegistradoTest() throws Exception {
		when(usuarioRepository.existeCorreo(CORREO)).thenReturn(true);

		try {
			usuarioService.crear(crearUsuarioDTO(CORREO, CONTRASENIA));
			fail("Debio lanzar CorreoYaRegistradoException");
		} catch (CorreoYaRegistradoException e) {
			assertNotNull(e.getMessage());
		}
		verify(usuarioRepository, never()).guardar(any(Usuario.class));
	}


	@Test
	@DisplayName("Buscar un usuario por id no devuelve la contrasena")
	public void buscarPorIdTest() {
		when(usuarioRepository.buscarPorId(ID_USUARIO)).thenReturn(crearUsuario("hash"));

		UsuarioDTO usuario = usuarioService.buscarPorId("2");

		assertEquals(CORREO, usuario.getCorreo());
		assertEquals("Camila", usuario.getPrimerNombre());
		assertNull(usuario.getContrasenia());
	}

	@Test
	@DisplayName("Buscar un usuario que no existe")
	public void buscarPorIdInexistenteTest() {
		when(usuarioRepository.buscarPorId(99L)).thenReturn(null);

		try {
			usuarioService.buscarPorId("99");
			fail("Debio lanzar UsuarioNoEncontradoException");
		} catch (UsuarioNoEncontradoException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("Listar todos los usuarios")
	public void buscarTodosTest() {
		List<Usuario> usuarios = new ArrayList<Usuario>();
		usuarios.add(crearUsuario("hash1"));
		usuarios.add(crearUsuario("hash2"));
		when(usuarioRepository.buscarTodos()).thenReturn(usuarios);

		List<UsuarioDTO> resultado = usuarioService.buscarTodos();

		assertEquals(2, resultado.size());
		assertNull(resultado.get(0).getContrasenia());
	}


	@Test
	@DisplayName("Actualizar sin contrasena conserva la contrasena actual")
	public void actualizarUsuarioSinContraseniaTest() throws Exception {
		when(usuarioRepository.buscarPorId(ID_USUARIO)).thenReturn(crearUsuario("hash-original"));
		when(usuarioRepository.existeCorreoEnOtroUsuario(CORREO, ID_USUARIO)).thenReturn(false);

		UsuarioDTO datosNuevos = crearUsuarioDTO(CORREO, null);
		datosNuevos.setPrimerNombre("Camila Andrea");
		datosNuevos.setRol("administrador");

		UsuarioDTO usuarioActualizado = usuarioService.actualizar("2", datosNuevos);

		assertEquals("Camila Andrea", usuarioActualizado.getPrimerNombre());
		assertEquals("administrador", usuarioActualizado.getRol());

		ArgumentCaptor<Usuario> capturaUsuario = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository, times(1)).actualizar(capturaUsuario.capture());
		assertEquals("hash-original", capturaUsuario.getValue().getHashContrasenia());
	}

	@Test
	@DisplayName("Actualizar con contrasena nueva la guarda hasheada")
	public void actualizarUsuarioConContraseniaTest() throws Exception {
		when(usuarioRepository.buscarPorId(ID_USUARIO)).thenReturn(crearUsuario("hash-original"));
		when(usuarioRepository.existeCorreoEnOtroUsuario(CORREO, ID_USUARIO)).thenReturn(false);

		usuarioService.actualizar("2", crearUsuarioDTO(CORREO, "NuevaClave99"));

		ArgumentCaptor<Usuario> capturaUsuario = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository, times(1)).actualizar(capturaUsuario.capture());
		assertTrue(passwordEncoder.matches("NuevaClave99", capturaUsuario.getValue().getHashContrasenia()));
	}

	@Test
	@DisplayName("No se actualiza un usuario con el correo de otro usuario")
	public void actualizarUsuarioCorreoDeOtroTest() throws Exception {
		when(usuarioRepository.buscarPorId(ID_USUARIO)).thenReturn(crearUsuario("hash"));
		when(usuarioRepository.existeCorreoEnOtroUsuario(CORREO, ID_USUARIO)).thenReturn(true);

		try {
			usuarioService.actualizar("2", crearUsuarioDTO(CORREO, null));
			fail("Debio lanzar CorreoYaRegistradoException");
		} catch (CorreoYaRegistradoException e) {
			assertNotNull(e.getMessage());
		}
		verify(usuarioRepository, never()).actualizar(any(Usuario.class));
	}


	@Test
	@DisplayName("Eliminar un usuario sin registros asociados")
	public void eliminarUsuarioTest() {
		when(usuarioRepository.buscarPorId(ID_USUARIO)).thenReturn(crearUsuario("hash"));
		when(usuarioRepository.contarRegistrosAsociados(ID_USUARIO)).thenReturn(0L);

		usuarioService.eliminar("2");

		verify(usuarioRepository, times(1)).eliminar(ID_USUARIO);
	}

	@Test
	@DisplayName("No se elimina un usuario que registro datos")
	public void eliminarUsuarioConRegistrosTest() {
		when(usuarioRepository.buscarPorId(ID_USUARIO)).thenReturn(crearUsuario("hash"));
		when(usuarioRepository.contarRegistrosAsociados(ID_USUARIO)).thenReturn(4L);

		try {
			usuarioService.eliminar("2");
			fail("Debio lanzar RegistroAsociadoException");
		} catch (RegistroAsociadoException e) {
			assertNotNull(e.getMessage());
		}
		verify(usuarioRepository, never()).eliminar(anyLong());
	}


	private Usuario crearUsuario(String hashContrasenia) {
		Usuario usuario = new Usuario("Camila", null, "Torres", null, CORREO, hashContrasenia,
				Rol.buscarPorValor("curador"), LocalDate.now());
		usuario.setIdUsuario(ID_USUARIO);
		return usuario;
	}

	private UsuarioDTO crearUsuarioDTO(String correo, String contrasenia) {
		return new UsuarioDTO("Camila", null, "Torres", null, correo, contrasenia, "curador", null);
	}

}