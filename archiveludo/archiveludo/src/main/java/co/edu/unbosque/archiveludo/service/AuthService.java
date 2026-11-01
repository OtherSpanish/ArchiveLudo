package co.edu.unbosque.archiveludo.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.dto.UsuarioDTO;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.security.JwtUtil;

/**
 * Logica de autenticacion: validacion de credenciales con Spring Security y generacion del token.
 * La creacion de usuarios esta en UsuarioService (solo administrador).
 */
@Service
public class AuthService {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ModelMapper mapper;

	public AuthService() {
	}

	/**
	 * Autentica al usuario con su correo y contrasena (en texto plano).
	 * Spring Security la compara contra el hash guardado usando BCrypt.
	 *
	 * @param credenciales DTO con correo y contrasenia
	 * @return los datos del usuario autenticado (sin contrasena)
	 * @throws CampoObligatorioException   si falta el correo o la contrasena
	 * @throws CredencialInvalidaException si el correo no existe o la contrasena no coincide
	 */
	public UsuarioDTO iniciarSesion(UsuarioDTO credenciales) {
		LanzadorException.verificarCampoObligatorio(credenciales.getCorreo(), "correo");
		LanzadorException.verificarCampoObligatorio(credenciales.getContrasenia(), "contrasenia");

		String correo = credenciales.getCorreo().trim();

		boolean credencialesCorrectas = true;
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(correo, credenciales.getContrasenia()));
		} catch (AuthenticationException e) {
			credencialesCorrectas = false;
		}
		LanzadorException.verificarCredencialesCorrectas(credencialesCorrectas);

		Usuario usuario = usuarioRepository.buscarPorCorreo(correo);
		return convertirADTO(usuario);
	}

	/**
	 * Genera el token JWT del usuario que ya inicio sesion.
	 *
	 * @param usuario usuario autenticado
	 * @return token JWT
	 */
	public String generarToken(UsuarioDTO usuario) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
		return jwtUtil.generarToken(userDetails);
	}

	/**
	 * Convierte la entity a DTO. El rol se pasa a su texto ("curador")
	 * y la contrasena se deja vacia para que el hash nunca salga de la API.
	 */
	private UsuarioDTO convertirADTO(Usuario usuario) {
		UsuarioDTO usuarioDTO = mapper.map(usuario, UsuarioDTO.class);
		usuarioDTO.setRol(usuario.getRol().getValor());
		usuarioDTO.setContrasenia(null);
		return usuarioDTO;
	}

}