package co.edu.unbosque.archiveludo.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.repository.UsuarioRepository;

/**
 * Implementacion del servicio de detalles de usuario para la autenticacion JWT.
 * Busca el usuario por su correo en la tabla USUARIO y le asigna la autoridad
 * de su rol (ROLE_CONSULTA, ROLE_CURADOR o ROLE_ADMINISTRADOR).
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	/**
	 * Carga un usuario por su correo.
	 *
	 * @param correo el correo a buscar (Spring lo llama "username")
	 * @return UserDetails listo para Spring Security
	 * @throws UsernameNotFoundException si no existe o no tiene un rol valido
	 */
	@Override
	public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.buscarPorCorreo(correo);

		if (usuario == null) {
			throw new UsernameNotFoundException("Usuario no encontrado: " + correo);
		}
		if (usuario.getRol() == null) {
			throw new UsernameNotFoundException("El usuario " + correo + " no tiene un rol valido");
		}

		List<SimpleGrantedAuthority> autoridades = new ArrayList<SimpleGrantedAuthority>();
		autoridades.add(new SimpleGrantedAuthority(usuario.getRol().getAutoridad()));

		return new User(usuario.getCorreo(), usuario.getHashContrasenia(), autoridades);
	}

}