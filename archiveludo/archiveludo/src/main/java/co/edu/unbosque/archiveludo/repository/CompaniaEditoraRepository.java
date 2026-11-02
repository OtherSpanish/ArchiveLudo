package co.edu.unbosque.archiveludo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.CompaniaEditora;

/**
 * Acceso a datos de la tabla COMPANIA_EDITORA con SQL  (Spring JDBC).
 */
@Repository
public class CompaniaEditoraRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_editora, nombre, pais FROM COMPANIA_EDITORA ";

	private static final String SQL_BUSCAR_TODAS = SQL_SELECCIONAR + "ORDER BY nombre ASC, id_editora ASC";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_editora = ?";

	private static final String SQL_INSERTAR = "INSERT INTO COMPANIA_EDITORA (nombre, pais) VALUES (?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	private static final String SQL_ACTUALIZAR = "UPDATE COMPANIA_EDITORA SET nombre = ?, pais = ? "
			+ "WHERE id_editora = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM COMPANIA_EDITORA WHERE id_editora = ?";

	/** Cuantos videojuegos publica la editora (relacion N:M en COMPED_VID). */
	private static final String SQL_CONTAR_VIDEOJUEGOS = "SELECT COUNT(*) FROM COMPED_VID WHERE id_editora = ?";

	private final JdbcTemplate jdbcTemplate;

	public CompaniaEditoraRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @return todas las companias editoras, ordenadas por nombre
	 */
	public List<CompaniaEditora> buscarTodas() {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_TODAS);

		List<CompaniaEditora> editoras = new ArrayList<CompaniaEditora>();
		for (Map<String, Object> fila : filas) {
			editoras.add(convertirFilaAEditora(fila));
		}
		return editoras;
	}

	/**
	 * @param idEditora id a buscar
	 * @return la editora encontrada, o null si no existe
	 */
	public CompaniaEditora buscarPorId(long idEditora) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idEditora);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAEditora(filas.get(0));
	}

	/**
	 * Inserta una editora y le asigna el id generado.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service) para que el
	 * INSERT y el LAST_INSERT_ID() usen la misma conexion.
	 *
	 * @param companiaEditora editora a guardar, sin id
	 * @return la misma editora con su idEditora
	 */
	public CompaniaEditora crear(CompaniaEditora companiaEditora) {
		jdbcTemplate.update(SQL_INSERTAR, companiaEditora.getNombre(), companiaEditora.getPais());

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		companiaEditora.setIdEditora(idGenerado);
		return companiaEditora;
	}

	/**
	 * Actualiza el nombre y el pais de la editora.
	 *
	 * @param companiaEditora editora con los datos nuevos (y su id)
	 */
	public void actualizar(CompaniaEditora companiaEditora) {
		jdbcTemplate.update(SQL_ACTUALIZAR, companiaEditora.getNombre(), companiaEditora.getPais(),
				companiaEditora.getIdEditora());
	}

	/**
	 * @param idEditora id de la editora a eliminar
	 */
	public void eliminar(long idEditora) {
		jdbcTemplate.update(SQL_ELIMINAR, idEditora);
	}

	/**
	 * Cuenta cuantos videojuegos publica esta editora.
	 *
	 * @param idEditora id de la editora
	 * @return cantidad de videojuegos asociados en COMPED_VID
	 */
	public long contarVideojuegosAsociados(long idEditora) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_VIDEOJUEGOS, Long.class, idEditora);
		if (cantidad == null) {
			return 0;
		}
		return cantidad;
	}

	/**
	 * Convierte una fila de la tabla COMPANIA_EDITORA (columna -> valor) en la entity CompaniaEditora.
	 */
	private CompaniaEditora convertirFilaAEditora(Map<String, Object> fila) {
		Number idEditora = (Number) fila.get("id_editora");

		CompaniaEditora companiaEditora = new CompaniaEditora();
		companiaEditora.setIdEditora(idEditora.longValue());
		companiaEditora.setNombre((String) fila.get("nombre"));
		companiaEditora.setPais((String) fila.get("pais"));
		return companiaEditora;
	}

}