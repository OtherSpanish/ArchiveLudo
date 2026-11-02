package co.edu.unbosque.archiveludo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.Experto;

/**
 * Acceso a datos de la tabla EXPERTO con SQL(Spring JDBC).
 */
@Repository
public class ExpertoRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_experto, primer_nombre_ex, segundo_nombre_ex, "
			+ "primer_apellido_ex, segundo_apellido_ex, especialidad, afiliacion, correo FROM EXPERTO ";

	/** En orden alfabetico por apellido y nombre, como una lista de personas. */
	private static final String SQL_BUSCAR_TODOS = SQL_SELECCIONAR
			+ "ORDER BY primer_apellido_ex ASC, primer_nombre_ex ASC, id_experto ASC";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_experto = ?";

	private static final String SQL_CONTAR_POR_CORREO = "SELECT COUNT(*) FROM EXPERTO WHERE correo = ?";

	/** Igual que el anterior, pero sin contar al propio experto (se usa al actualizar). */
	private static final String SQL_CONTAR_POR_CORREO_EN_OTRO = "SELECT COUNT(*) FROM EXPERTO "
			+ "WHERE correo = ? AND id_experto <> ?";

	/** Participaciones del experto en proyectos: si tiene alguna, no se puede eliminar. */
	private static final String SQL_CONTAR_PARTICIPACIONES = "SELECT COUNT(*) FROM PARTICIPACION WHERE id_experto = ?";

	private static final String SQL_INSERTAR = "INSERT INTO EXPERTO (primer_nombre_ex, segundo_nombre_ex, "
			+ "primer_apellido_ex, segundo_apellido_ex, especialidad, afiliacion, correo) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	private static final String SQL_ACTUALIZAR = "UPDATE EXPERTO SET primer_nombre_ex = ?, segundo_nombre_ex = ?, "
			+ "primer_apellido_ex = ?, segundo_apellido_ex = ?, especialidad = ?, afiliacion = ?, correo = ? "
			+ "WHERE id_experto = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM EXPERTO WHERE id_experto = ?";

	private final JdbcTemplate jdbcTemplate;

	public ExpertoRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	/**
	 * @return todos los expertos, ordenados por apellido y nombre
	 */
	public List<Experto> buscarTodos() {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_TODOS);

		List<Experto> expertos = new ArrayList<Experto>();
		for (Map<String, Object> fila : filas) {
			expertos.add(convertirFilaAExperto(fila));
		}
		return expertos;
	}

	/**
	 * @param idExperto id a buscar
	 * @return el experto encontrado, o null si no existe
	 */
	public Experto buscarPorId(long idExperto) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idExperto);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAExperto(filas.get(0));
	}

	/**
	 * @param correo correo a buscar
	 * @return true si algun experto ya tiene ese correo
	 */
	public boolean existeCorreo(String correo) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_POR_CORREO, Long.class, correo);
		return cantidad != null && cantidad > 0;
	}

	/**
	 * @param correo    correo a buscar
	 * @param idExperto experto que se esta actualizando (no se cuenta)
	 * @return true si OTRO experto ya tiene ese correo
	 */
	public boolean existeCorreoEnOtroExperto(String correo, long idExperto) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_POR_CORREO_EN_OTRO, Long.class, correo, idExperto);
		return cantidad != null && cantidad > 0;
	}

	/**
	 * @param idExperto id del experto
	 * @return cantidad de participaciones del experto en proyectos
	 */
	public long contarParticipaciones(long idExperto) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_PARTICIPACIONES, Long.class, idExperto);
		if (cantidad == null) {
			return 0;
		}
		return cantidad;
	}


	/**
	 * Inserta un experto y le asigna el id generado.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service).
	 *
	 * @param experto experto a guardar, sin id
	 * @return el mismo experto con su idExperto
	 */
	public Experto crear(Experto experto) {
		jdbcTemplate.update(SQL_INSERTAR,
				experto.getPrimerNombreEx(),
				experto.getSegundoNombreEx(),
				experto.getPrimerApellidoEx(),
				experto.getSegundoApellidoEx(),
				experto.getEspecialidad(),
				experto.getAfiliacion(),
				experto.getCorreo());

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		experto.setIdExperto(idGenerado);
		return experto;
	}

	/**
	 * @param experto experto con los datos nuevos (y su id)
	 */
	public void actualizar(Experto experto) {
		jdbcTemplate.update(SQL_ACTUALIZAR,
				experto.getPrimerNombreEx(),
				experto.getSegundoNombreEx(),
				experto.getPrimerApellidoEx(),
				experto.getSegundoApellidoEx(),
				experto.getEspecialidad(),
				experto.getAfiliacion(),
				experto.getCorreo(),
				experto.getIdExperto());
	}

	/**
	 * @param idExperto id del experto a eliminar
	 */
	public void eliminar(long idExperto) {
		jdbcTemplate.update(SQL_ELIMINAR, idExperto);
	}

	/**
	 * Convierte una fila de la tabla EXPERTO (columna -> valor) en la entity Experto.
	 */
	private Experto convertirFilaAExperto(Map<String, Object> fila) {
		Number idExperto = (Number) fila.get("id_experto");

		Experto experto = new Experto();
		experto.setIdExperto(idExperto.longValue());
		experto.setPrimerNombreEx((String) fila.get("primer_nombre_ex"));
		experto.setSegundoNombreEx((String) fila.get("segundo_nombre_ex"));
		experto.setPrimerApellidoEx((String) fila.get("primer_apellido_ex"));
		experto.setSegundoApellidoEx((String) fila.get("segundo_apellido_ex"));
		experto.setEspecialidad((String) fila.get("especialidad"));
		experto.setAfiliacion((String) fila.get("afiliacion"));
		experto.setCorreo((String) fila.get("correo"));
		return experto;
	}

}