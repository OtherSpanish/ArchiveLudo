package co.edu.unbosque.archiveludo.service;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.UsuarioDTO;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.exceptions.ContraseniaUsuarioException;
import co.edu.unbosque.archiveludo.exceptions.CorreoElectronicoException;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.Rol;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;

/**
 * Logica de negocio de usuarios. Solo el administrador crea usuarios (lo controla SecurityConfig).
 * Recibe la contrasena en texto plano, la valida y la guarda hasheada con BCrypt.
 */
@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Crea un usuario nuevo con el rol que elige el administrador.
	 *
	 * @param usuarioDTO datos del usuario con la contrasena en texto plano
	 * @return el usuario creado, sin contrasena
	 * @throws CampoObligatorioException    si falta primer nombre, primer apellido, correo o contrasena
	 * @throws CorreoElectronicoException   si el correo no es valido
	 * @throws ContraseniaUsuarioException  si la contrasena no cumple las reglas
	 * @throws RolInvalidoException         si el rol viene vacio o no existe
	 * @throws CorreoYaRegistradoException  si el correo ya esta registrado
	 */
	@Transactional
	public UsuarioDTO crear(UsuarioDTO usuarioDTO) throws CorreoElectronicoException, ContraseniaUsuarioException {
		LanzadorException.verificarCampoObligatorio(usuarioDTO.getPrimerNombre(), "primerNombre");
		LanzadorException.verificarCampoObligatorio(usuarioDTO.getPrimerApellido(), "primerApellido");
		LanzadorException.verificarCampoObligatorio(usuarioDTO.getCorreo(), "correo");
		LanzadorException.verificarCampoObligatorio(usuarioDTO.getContrasenia(), "contrasenia");

		LanzadorException.verificarCorreoValido(usuarioDTO.getCorreo());
		LanzadorException.verificarContrasenaValida(usuarioDTO.getContrasenia());

		Rol rol = LanzadorException.convertirRol(usuarioDTO.getRol());

		String correo = usuarioDTO.getCorreo().trim();
		LanzadorException.verificarCorreoNoRegistrado(usuarioRepository.existeCorreo(correo), correo);

		String hashContrasenia = passwordEncoder.encode(usuarioDTO.getContrasenia());

		Usuario usuario = new Usuario(usuarioDTO.getPrimerNombre(), usuarioDTO.getSegundoNombre(),
				usuarioDTO.getPrimerApellido(), usuarioDTO.getSegundoApellido(), correo, hashContrasenia, rol,
				LocalDate.now());

		Usuario usuarioGuardado = usuarioRepository.guardar(usuario);
		return convertirADTO(usuarioGuardado);
	}

	/**
	 * Convierte la entity en DTO. La contrasena nunca se devuelve al front.
	 */
	private UsuarioDTO convertirADTO(Usuario usuario) {
		UsuarioDTO usuarioDTO = new UsuarioDTO(usuario.getPrimerNombre(), usuario.getSegundoNombre(),
				usuario.getPrimerApellido(), usuario.getSegundoApellido(), usuario.getCorreo(), null,
				usuario.getRol().getValor(), usuario.getFechaCreacion());
		usuarioDTO.setIdUsuario(usuario.getIdUsuario());
		return usuarioDTO;
	}

}