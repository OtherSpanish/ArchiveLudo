package co.edu.unbosque.archiveludo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import co.edu.unbosque.archiveludo.dto.UsuarioDTO;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.exceptions.CampoObligatorioException;
import co.edu.unbosque.archiveludo.exceptions.CredencialInvalidaException;
import co.edu.unbosque.archiveludo.model.Rol;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.security.JwtUtil;
import co.edu.unbosque.archiveludo.service.AuthService;

/**
 * Pruebas unitarias del inicio de sesion (AuthService).
 * Spring Security, el JWT y el repository son simulados con Mockito: no se toca la base de datos.
 * Como AuthService recibe sus dependencias con @Autowired en los campos, se crea con @InjectMocks.
 *
 * Criterio de aceptacion: un usuario con credenciales correctas inicia sesion y recibe sus datos
 * sin contrasena; con credenciales incorrectas o campos vacios se rechaza; se genera el token
 * del usuario autenticado.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AutenticacionTest {

	private static final String CORREO = "curador@archiveludo.co";
	private static final String CONTRASENIA = "Clave2026";

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private UsuarioRepository usuarioRepository;

	/** El ModelMapper es el real; @Spy permite que @InjectMocks tambien lo inyecte. */
	@Spy
	private ModelMapper mapper = new ModelMapper();

	@InjectMocks
	private AuthService authService;

	@Test
	@DisplayName("Iniciar sesion con credenciales correctas devuelve el usuario sin contrasena")
	public void iniciarSesionTest() {
		when(usuarioRepository.buscarPorCorreo(CORREO)).thenReturn(crearUsuario());

		UsuarioDTO usuario = authService.iniciarSesion(crearCredenciales("  " + CORREO + "  ", CONTRASENIA));

		assertEquals(CORREO, usuario.getCorreo());
		assertEquals("curador", usuario.getRol());
		assertNull(usuario.getContrasenia());

		// El correo se envia a Spring Security sin espacios
		ArgumentCaptor<Authentication> capturaCredenciales = ArgumentCaptor.forClass(Authentication.class);
		verify(authenticationManager, times(1)).authenticate(capturaCredenciales.capture());
		assertEquals(CORREO, capturaCredenciales.getValue().getName());
	}

	@Test
	@DisplayName("Iniciar sesion con contrasena incorrecta se rechaza")
	public void iniciarSesionContraseniaIncorrectaTest() {
		when(authenticationManager.authenticate(any(Authentication.class)))
				.thenThrow(new BadCredentialsException("Credenciales incorrectas"));

		try {
			authService.iniciarSesion(crearCredenciales(CORREO, "ClaveEquivocada1"));
			fail("Debio lanzar CredencialInvalidaException");
		} catch (CredencialInvalidaException e) {
			assertNotNull(e.getMessage());
		}
		verify(usuarioRepository, never()).buscarPorCorreo(anyString());
	}

	@Test
	@DisplayName("Iniciar sesion sin correo se rechaza sin consultar a Spring Security")
	public void iniciarSesionSinCorreoTest() {
		try {
			authService.iniciarSesion(crearCredenciales("", CONTRASENIA));
			fail("Debio lanzar CampoObligatorioException");
		} catch (CampoObligatorioException e) {
			assertNotNull(e.getMessage());
		}
		verify(authenticationManager, never()).authenticate(any(Authentication.class));
	}

	@Test
	@DisplayName("Iniciar sesion sin contrasena se rechaza")
	public void iniciarSesionSinContraseniaTest() {
		try {
			authService.iniciarSesion(crearCredenciales(CORREO, null));
			fail("Debio lanzar CampoObligatorioException");
		} catch (CampoObligatorioException e) {
			assertNotNull(e.getMessage());
		}
		verify(authenticationManager, never()).authenticate(any(Authentication.class));
	}

	@Test
	@DisplayName("Generar el token del usuario autenticado")
	public void generarTokenTest() {
		UserDetails userDetails = new User(CORREO, "hash", new ArrayList<GrantedAuthority>());
		when(userDetailsService.loadUserByUsername(CORREO)).thenReturn(userDetails);
		when(jwtUtil.generarToken(userDetails)).thenReturn("token-de-prueba");

		String token = authService.generarToken(crearCredenciales(CORREO, null));

		assertEquals("token-de-prueba", token);
		verify(jwtUtil, times(1)).generarToken(userDetails);
	}


	private UsuarioDTO crearCredenciales(String correo, String contrasenia) {
		return new UsuarioDTO(null, null, null, null, correo, contrasenia, null, null);
	}

	private Usuario crearUsuario() {
		Usuario usuario = new Usuario("Camila", null, "Torres", null, CORREO, "hash-bcrypt",
				Rol.buscarPorValor("curador"), LocalDate.now());
		usuario.setIdUsuario(2L);
		return usuario;
	}

}