package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.Usuario;
import co.edu.unbosque.archiveludo.model.Rol;

/**
 * Acceso a datos de la tabla USUARIO con SQL (Spring JDBC).
 */
@Repository
public class UsuarioRepository {

	private final JdbcTemplate jdbcTemplate;
	private final SimpleJdbcInsert insercionUsuario;

	public UsuarioRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.insercionUsuario = new SimpleJdbcInsert(jdbcTemplate)
				.withTableName("USUARIO")
				.usingColumns("primer_nombre", "segundo_nombre", "primer_apellido", "segundo_apellido",
						"correo", "hash_contrasenia", "rol", "fecha_creacion")
				.usingGeneratedKeyColumns("id_usuario")
				.withoutTableColumnMetaDataAccess();
	}

	/**
	 * Inserta un usuario y le asigna el id generado por la base de datos.
	 * El usuario ya debe traer la contrasena hasheada.
	 *
	 * @param usuario usuario a guardar
	 * @return el mismo usuario con su idUsuario
	 */
	public Usuario guardar(Usuario usuario) {
		Map<String, Object> valores = new HashMap<String, Object>();
		valores.put("primer_nombre", usuario.getPrimerNombre());
		valores.put("segundo_nombre", usuario.getSegundoNombre());
		valores.put("primer_apellido", usuario.getPrimerApellido());
		valores.put("segundo_apellido", usuario.getSegundoApellido());
		valores.put("correo", usuario.getCorreo());
		valores.put("hash_contrasenia", usuario.getHashContrasenia());
		valores.put("rol", usuario.getRol().getValor());
		valores.put("fecha_creacion", Date.valueOf(usuario.getFechaCreacion()));

		Number idGenerado = insercionUsuario.executeAndReturnKey(valores);
		usuario.setIdUsuario(idGenerado.longValue());
		return usuario;
	}

	/**
	 * Busca un usuario por su correo.
	 *
	 * @param correo correo a buscar
	 * @return el usuario encontrado (con su hash de contrasena), o null si no existe
	 */
	public Usuario buscarPorCorreo(String correo) {
		String sql = "SELECT id_usuario, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, "
				+ "correo, hash_contrasenia, rol, fecha_creacion FROM USUARIO WHERE correo = ?";

		List<Map<String, Object>> filas = jdbcTemplate.queryForList(sql, correo);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAUsuario(filas.get(0));
	}

	/**
	 * Cuenta cuantos usuarios tienen el correo dado.
	 *
	 * @param correo correo a buscar
	 * @return true si ya existe un usuario con ese correo
	 */
	public boolean existeCorreo(String correo) {
		String sql = "SELECT COUNT(*) FROM USUARIO WHERE correo = ?";
		Integer cantidad = jdbcTemplate.queryForObject(sql, Integer.class, correo);
		return cantidad != null && cantidad > 0;
	}

	/**
	 * Convierte una fila de la tabla USUARIO (columna -> valor) en la entity Usuario.
	 * El rol se guarda como texto ("curador") y se convierte al enum Rol.
	 */
	private Usuario convertirFilaAUsuario(Map<String, Object> fila) {
		Number idUsuario = (Number) fila.get("id_usuario");
		String textoRol = (String) fila.get("rol");

		Usuario usuario = new Usuario();
		usuario.setIdUsuario(idUsuario.longValue());
		usuario.setPrimerNombre((String) fila.get("primer_nombre"));
		usuario.setSegundoNombre((String) fila.get("segundo_nombre"));
		usuario.setPrimerApellido((String) fila.get("primer_apellido"));
		usuario.setSegundoApellido((String) fila.get("segundo_apellido"));
		usuario.setCorreo((String) fila.get("correo"));
		usuario.setHashContrasenia((String) fila.get("hash_contrasenia"));
		usuario.setRol(Rol.buscarPorValor(textoRol));
		usuario.setFechaCreacion(convertirFecha(fila.get("fecha_creacion")));
		return usuario;
	}

	/**
	 * Convierte la fecha que entrega el driver de MySQL a LocalDate.
	 */
	private LocalDate convertirFecha(Object valor) {
		if (valor == null) {
			return null;
		}
		if (valor instanceof Date) {
			Date fechaSql = (Date) valor;
			return fechaSql.toLocalDate();
		}
		return (LocalDate) valor;
	}

}