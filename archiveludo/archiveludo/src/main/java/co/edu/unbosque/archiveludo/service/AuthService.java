package co.edu.unbosque.archiveludo.service;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.dto.UsuarioDTO;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.exceptions.ContraseniaUsuarioException;
import co.edu.unbosque.archiveludo.exceptions.CorreoElectronicoException;
import co.edu.unbosque.archiveludo.exceptions.LanzadorExcepcion;
import co.edu.unbosque.archiveludo.model.Rol;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;
import co.edu.unbosque.archiveludo.security.JwtUtil;

/**
 * Logica de autenticación: registro de usuarios (con la contrasena hasheada),
 * validación de credenciales con Spring Security y generación del token.
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
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ModelMapper mapper;

	public AuthService() {
	}

	/**
	 * Registra un usuario nuevo. La contrasena llega en texto plano
	 * y se guarda en la base de datos hasheada con BCrypt.
	 * Si no se envia rol, el usuario queda como "consulta".
	 *
	 * @param usuarioDTO datos del usuario con la contrasena en texto plano
	 * @return el usuario creado (sin contrasena)
	 * @throws CampoObligatorioException   si falta primer nombre, primer apellido, correo o contrasena
	 * @throws CorreoElectronicoException  si el correo no es valido
	 * @throws ContraseniaUsuarioException si la contrasena no cumple las reglas
	 * @throws IllegalArgumentException    si el rol enviado no existe
	 * @throws DuplicateKeyException       si el correo ya esta registrado
	 */
	public UsuarioDTO registrar(UsuarioDTO usuarioDTO) throws CorreoElectronicoException, ContraseniaUsuarioException {
		LanzadorExcepcion.verificarCampoObligatorio(usuarioDTO.getPrimerNombre(), "primerNombre");
		LanzadorExcepcion.verificarCampoObligatorio(usuarioDTO.getPrimerApellido(), "primerApellido");
		LanzadorExcepcion.verificarCampoObligatorio(usuarioDTO.getCorreo(), "correo");
		LanzadorExcepcion.verificarCampoObligatorio(usuarioDTO.getContrasenia(), "contrasenia");

		LanzadorExcepcion.verificarCorreoValido(usuarioDTO.getCorreo());
		LanzadorExcepcion.verificarContrasenaValida(usuarioDTO.getContrasenia());

		Rol rol = Rol.CONSULTA;
		if (usuarioDTO.getRol() != null) {
			rol = Rol.buscarPorValor(usuarioDTO.getRol());
			if (rol == null) {
				throw new IllegalArgumentException("El rol debe ser administrador, curador o consulta");
			}
		}

		String correo = usuarioDTO.getCorreo().trim();
		if (usuarioRepository.buscarPorCorreo(correo) != null) {
			throw new DuplicateKeyException("Ya existe un usuario con el correo " + correo);
		}

		String hashContrasenia = passwordEncoder.encode(usuarioDTO.getContrasenia());

		Usuario usuario = new Usuario(usuarioDTO.getPrimerNombre(), usuarioDTO.getSegundoNombre(),
				usuarioDTO.getPrimerApellido(), usuarioDTO.getSegundoApellido(), correo, hashContrasenia, rol,
				LocalDate.now());

		Usuario usuarioGuardado = usuarioRepository.guardar(usuario);
		return convertirADTO(usuarioGuardado);
	}

	/**
	 * Autentica al usuario con su correo y contrasena (en texto plano).
	 * Spring Security la compara contra el hash guardado usando BCrypt.
	 *
	 * @param credenciales DTO con correo y contrasenia
	 * @return los datos del usuario autenticado (sin contrasena)
	 * @throws CampoObligatorioException    si falta el correo o la contrasena
	 * @throws CredencialInvalidaException  si el correo no existe o la contrasena no coincide
	 */
	public UsuarioDTO iniciarSesion(UsuarioDTO credenciales) {
		LanzadorExcepcion.verificarCampoObligatorio(credenciales.getCorreo(), "correo");
		LanzadorExcepcion.verificarCampoObligatorio(credenciales.getContrasenia(), "contrasenia");

		String correo = credenciales.getCorreo().trim();

		boolean credencialesCorrectas = true;
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(correo, credenciales.getContrasenia()));
		} catch (AuthenticationException e) {
			credencialesCorrectas = false;
		}
		LanzadorExcepcion.verificarCredencialesCorrectas(credencialesCorrectas);

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