package co.edu.unbosque.archiveludo.service;

import java.time.LocalDate;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.dto.UsuarioDTO;
import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.exceptions.ContraseniaUsuarioException;
import co.edu.unbosque.archiveludo.exceptions.CorreoElectronicoException;
import co.edu.unbosque.archiveludo.exceptions.LanzadorExcepcion;
import co.edu.unbosque.archiveludo.model.Rol;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;

/**
 * Logica de negocio de usuarios.
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
	 * Crea un usuario nuevo.
	 *
	 * @param usuarioDTO datos del usuario con la contrasena en texto plano
	 * @return el usuario creado, sin contrasena
	 * @throws CorreoElectronicoException  si el correo no es valido
	 * @throws ContraseniaUsuarioException si la contrasena no cumple las reglas
	 * @throws IllegalArgumentException    si el rol no existe
	 * @throws DuplicateKeyException       si el correo ya esta registrado
	 */
	public UsuarioDTO crear(UsuarioDTO usuarioDTO) throws CorreoElectronicoException, ContraseniaUsuarioException {
		LanzadorExcepcion.verificarCorreoValido(usuarioDTO.getCorreo());
		LanzadorExcepcion.verificarContrasenaValida(usuarioDTO.getContrasenia());

		Rol rol = Rol.buscarPorValor(usuarioDTO.getRol());
		if (rol == null) {
			throw new IllegalArgumentException("El rol debe ser administrador, curador o consulta");
		}

		String correo = usuarioDTO.getCorreo().trim();
		if (usuarioRepository.existeCorreo(correo)) {
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