package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.model.Rol;

/**
 * Acceso a datos de la tabla USUARIO con SQL escrito a mano (Spring JDBC).
 */
@Repository
public class UsuarioRepository {

	private static final String SQL_INSERTAR = "INSERT INTO USUARIO (primer_nombre, segundo_nombre, "
			+ "primer_apellido, segundo_apellido, correo, hash_contrasenia, rol, fecha_creacion) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String SQL_BUSCAR_ID_POR_CORREO = "SELECT id_usuario FROM USUARIO WHERE correo = ?";

	private static final String SQL_BUSCAR_POR_CORREO = "SELECT id_usuario, primer_nombre, segundo_nombre, "
			+ "primer_apellido, segundo_apellido, correo, hash_contrasenia, rol, fecha_creacion "
			+ "FROM USUARIO WHERE correo = ?";

	private static final String SQL_CONTAR_POR_CORREO = "SELECT COUNT(*) FROM USUARIO WHERE correo = ?";

	private final JdbcTemplate jdbcTemplate;

	public UsuarioRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Inserta un usuario y le asigna el id generado por la base de datos (AUTO_INCREMENT).
	 * Como el correo es unico, el id se recupera buscandolo por correo justo despues del INSERT.
	 * El usuario ya debe traer la contrasena hasheada.
	 *
	 * @param usuario usuario a guardar
	 * @return el mismo usuario con su idUsuario
	 */
	public Usuario guardar(Usuario usuario) {
		jdbcTemplate.update(SQL_INSERTAR,
				usuario.getPrimerNombre(),
				usuario.getSegundoNombre(),
				usuario.getPrimerApellido(),
				usuario.getSegundoApellido(),
				usuario.getCorreo(),
				usuario.getHashContrasenia(),
				usuario.getRol().getValor(),
				Date.valueOf(usuario.getFechaCreacion()));

		Long idGenerado = jdbcTemplate.queryForObject(SQL_BUSCAR_ID_POR_CORREO, Long.class, usuario.getCorreo());
		usuario.setIdUsuario(idGenerado);
		return usuario;
	}

	/**
	 * Busca un usuario por su correo.
	 *
	 * @param correo correo a buscar
	 * @return el usuario encontrado (con su hash de contrasena), o null si no existe
	 */
	public Usuario buscarPorCorreo(String correo) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_CORREO, correo);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAUsuario(filas.get(0));
	}

	/**
	 * Indica si ya existe un usuario con el correo dado.
	 *
	 * @param correo correo a buscar
	 * @return true si ya existe un usuario con ese correo
	 */
	public boolean existeCorreo(String correo) {
		Integer cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_POR_CORREO, Integer.class, correo);
		return cantidad != null && cantidad > 0;
	}

	/**
	 * Convierte una fila de la tabla USUARIO (columna -> valor) en la entity Usuario.
	 * El rol se guarda como texto ("curador") y se convierte al enum Rol.
	 */
	private Usuario convertirFilaAUsuario(Map<String, Object> fila) {
		Number idUsuario = (Number) fila.get("id_usuario");

		Usuario usuario = new Usuario();
		usuario.setIdUsuario(idUsuario.longValue());
		usuario.setPrimerNombre((String) fila.get("primer_nombre"));
		usuario.setSegundoNombre((String) fila.get("segundo_nombre"));
		usuario.setPrimerApellido((String) fila.get("primer_apellido"));
		usuario.setSegundoApellido((String) fila.get("segundo_apellido"));
		usuario.setCorreo((String) fila.get("correo"));
		usuario.setHashContrasenia((String) fila.get("hash_contrasenia"));
		usuario.setRol(convertirRol(fila.get("rol")));
		usuario.setFechaCreacion(convertirFecha(fila.get("fecha_creacion")));
		return usuario;
	}

	/**
	 * Convierte el texto de la columna rol al enum Rol, sin importar mayusculas.
	 */
	private Rol convertirRol(Object valor) {
		if (valor == null) {
			return null;
		}
		String textoRol = valor.toString().trim().toLowerCase();
		return Rol.buscarPorValor(textoRol);
	}

	/**
	 * Convierte la fecha que entrega el driver de MySQL a LocalDate.
	 * Si la columna es DATE llega como java.sql.Date; si es DATETIME, como fecha y hora.
	 */
	private LocalDate convertirFecha(Object valor) {
		if (valor == null) {
			return null;
		}
		if (valor instanceof Date) {
			Date fechaSql = (Date) valor;
			return fechaSql.toLocalDate();
		}
		if (valor instanceof Timestamp) {
			Timestamp fechaHora = (Timestamp) valor;
			return fechaHora.toLocalDateTime().toLocalDate();
		}
		if (valor instanceof LocalDateTime) {
			LocalDateTime fechaHora = (LocalDateTime) valor;
			return fechaHora.toLocalDate();
		}
		return (LocalDate) valor;
	}

}