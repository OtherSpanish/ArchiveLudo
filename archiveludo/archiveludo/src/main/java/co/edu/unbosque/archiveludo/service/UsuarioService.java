package co.edu.unbosque.archiveludo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
 * Lógica de negocio de usuarios. Solo el administrador gestiona usuarios (lo controla SecurityConfig).
 * La contraseña se recibe en texto plano, se valida y se guarda hasheada con BCrypt.
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
	 * @throws CampoObligatorioException   si falta primer nombre, primer apellido, correo o contrasena
	 * @throws CorreoElectronicoException  si el correo no es valido
	 * @throws ContraseniaUsuarioException si la contrasena no cumple las reglas
	 * @throws RolInvalidoException        si el rol viene vacio o no existe
	 * @throws CorreoYaRegistradoException si el correo ya esta registrado
	 */
	@Transactional
	public UsuarioDTO crear(UsuarioDTO usuarioDTO) throws CorreoElectronicoException, ContraseniaUsuarioException {
		verificarDatosBasicos(usuarioDTO);
		LanzadorException.verificarCampoObligatorio(usuarioDTO.getContrasenia(), "contrasenia");
		LanzadorException.verificarContrasenaValida(usuarioDTO.getContrasenia());

		Rol rol = LanzadorException.convertirRol(usuarioDTO.getRol());

		String correo = usuarioDTO.getCorreo().trim();
		LanzadorException.verificarCorreoNoRegistrado(usuarioRepository.existeCorreo(correo), correo);

		String hashContrasenia = passwordEncoder.encode(usuarioDTO.getContrasenia());

		Usuario usuario = new Usuario(usuarioDTO.getPrimerNombre().trim(),
				limpiarTextoOpcional(usuarioDTO.getSegundoNombre()), usuarioDTO.getPrimerApellido().trim(),
				limpiarTextoOpcional(usuarioDTO.getSegundoApellido()), correo, hashContrasenia, rol,
				LocalDate.now());

		Usuario usuarioGuardado = usuarioRepository.guardar(usuario);
		return convertirADTO(usuarioGuardado);
	}

	/**
	 * @return todos los usuarios, sin contrasena
	 */
	public List<UsuarioDTO> buscarTodos() {
		List<Usuario> usuarios = usuarioRepository.buscarTodos();

		List<UsuarioDTO> usuariosDTO = new ArrayList<UsuarioDTO>();
		for (Usuario usuario : usuarios) {
			usuariosDTO.add(convertirADTO(usuario));
		}
		return usuariosDTO;
	}

	/**
	 * @param idTexto id tal como llego en la URL
	 * @return el usuario, sin contrasena
	 * @throws IdInvalidoException          si el id no es un numero mayor que cero
	 * @throws UsuarioNoEncontradoException si no existe
	 */
	public UsuarioDTO buscarPorId(String idTexto) {
		long idUsuario = LanzadorException.convertirId(idTexto, "usuario");

		Usuario usuario = usuarioRepository.buscarPorId(idUsuario);
		LanzadorException.verificarUsuarioExiste(usuario, idUsuario);

		return convertirADTO(usuario);
	}

	/**
	 * Actualiza nombres, correo y rol. La contrasena es opcional:
	 * si viene, se valida y se reemplaza; si no viene, se conserva la actual.
	 *
	 * @param idTexto    id tal como llego en la URL
	 * @param usuarioDTO datos nuevos
	 * @return el usuario actualizado, sin contrasena
	 * @throws IdInvalidoException          si el id no es un numero mayor que cero
	 * @throws UsuarioNoEncontradoException si no existe
	 * @throws CampoObligatorioException    si falta primer nombre, primer apellido o correo
	 * @throws CorreoElectronicoException   si el correo no es valido
	 * @throws ContraseniaUsuarioException  si se envio una contrasena que no cumple las reglas
	 * @throws RolInvalidoException         si el rol viene vacio o no existe
	 * @throws CorreoYaRegistradoException  si el correo ya lo usa otro usuario
	 */
	@Transactional
	public UsuarioDTO actualizar(String idTexto, UsuarioDTO usuarioDTO)
			throws CorreoElectronicoException, ContraseniaUsuarioException {
		long idUsuario = LanzadorException.convertirId(idTexto, "usuario");

		Usuario usuario = usuarioRepository.buscarPorId(idUsuario);
		LanzadorException.verificarUsuarioExiste(usuario, idUsuario);

		verificarDatosBasicos(usuarioDTO);
		Rol rol = LanzadorException.convertirRol(usuarioDTO.getRol());

		String correo = usuarioDTO.getCorreo().trim();
		LanzadorException.verificarCorreoNoRegistrado(usuarioRepository.existeCorreoEnOtroUsuario(correo, idUsuario),
				correo);

		boolean cambiaContrasenia = usuarioDTO.getContrasenia() != null
				&& !usuarioDTO.getContrasenia().trim().isEmpty();
		if (cambiaContrasenia) {
			LanzadorException.verificarContrasenaValida(usuarioDTO.getContrasenia());
			usuario.setHashContrasenia(passwordEncoder.encode(usuarioDTO.getContrasenia()));
		}

		usuario.setPrimerNombre(usuarioDTO.getPrimerNombre().trim());
		usuario.setSegundoNombre(limpiarTextoOpcional(usuarioDTO.getSegundoNombre()));
		usuario.setPrimerApellido(usuarioDTO.getPrimerApellido().trim());
		usuario.setSegundoApellido(limpiarTextoOpcional(usuarioDTO.getSegundoApellido()));
		usuario.setCorreo(correo);
		usuario.setRol(rol);

		usuarioRepository.actualizar(usuario);
		return convertirADTO(usuario);
	}

	/**
	 * Elimina un usuario solo si no registro copias, estatus legales ni eventos de riesgo.
	 *
	 * @param idTexto id tal como llego en la URL
	 * @throws IdInvalidoException          si el id no es un numero mayor que cero
	 * @throws UsuarioNoEncontradoException si no existe
	 * @throws RegistrosAsociadosException  si tiene registros asociados
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idUsuario = LanzadorException.convertirId(idTexto, "usuario");

		Usuario usuario = usuarioRepository.buscarPorId(idUsuario);
		LanzadorException.verificarUsuarioExiste(usuario, idUsuario);

		boolean tieneRegistrosAsociados = usuarioRepository.contarRegistrosAsociados(idUsuario) > 0;
		LanzadorException.verificarSinRegistrosAsociados(tieneRegistrosAsociados, "el usuario", idUsuario);

		usuarioRepository.eliminar(idUsuario);
	}

	/**
	 * Validaciones comunes a crear y actualizar.
	 */
	private void verificarDatosBasicos(UsuarioDTO usuarioDTO) throws CorreoElectronicoException {
		LanzadorException.verificarCampoObligatorio(usuarioDTO.getPrimerNombre(), "primerNombre");
		LanzadorException.verificarCampoObligatorio(usuarioDTO.getPrimerApellido(), "primerApellido");
		LanzadorException.verificarCampoObligatorio(usuarioDTO.getCorreo(), "correo");
		LanzadorException.verificarCorreoValido(usuarioDTO.getCorreo());
	}

	/**
	 * Un texto opcional vacio ("" o "   ") se guarda como NULL en la BD.
	 */
	private String limpiarTextoOpcional(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return null;
		}
		return texto.trim();
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