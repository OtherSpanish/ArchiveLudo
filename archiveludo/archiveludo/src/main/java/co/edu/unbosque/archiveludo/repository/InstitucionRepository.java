package co.edu.unbosque.archiveludo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.Institucion;
import co.edu.unbosque.archiveludo.model.TipoInstitucion;

/**
 * Acceso a datos de la tabla INSTITUCION con SQL escrito a mano (Spring JDBC).
 * Solo toca INSTITUCION: los telefonos y correos tienen su propio repository.
 */
@Repository
public class InstitucionRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_institucion, nombre, tipo, pais FROM INSTITUCION ";

	private static final String SQL_BUSCAR_TODAS = SQL_SELECCIONAR + "ORDER BY nombre ASC, id_institucion ASC";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_institucion = ?";

	private static final String SQL_INSERTAR = "INSERT INTO INSTITUCION (nombre, tipo, pais) VALUES (?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	private static final String SQL_ACTUALIZAR = "UPDATE INSTITUCION SET nombre = ?, tipo = ?, pais = ? "
			+ "WHERE id_institucion = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM INSTITUCION WHERE id_institucion = ?";

	private static final String SQL_CONTAR_CUSTODIAS = "SELECT COUNT(*) FROM CUSTODIA WHERE id_institucion = ?";

	private final JdbcTemplate jdbcTemplate;

	public InstitucionRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @return todas las instituciones, ordenadas por nombre
	 */
	public List<Institucion> buscarTodas() {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_TODAS);

		List<Institucion> instituciones = new ArrayList<Institucion>();
		for (Map<String, Object> fila : filas) {
			instituciones.add(convertirFilaAInstitucion(fila));
		}
		return instituciones;
	}

	/**
	 * @param idInstitucion id a buscar
	 * @return la institucion encontrada, o null si no existe
	 */
	public Institucion buscarPorId(long idInstitucion) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idInstitucion);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAInstitucion(filas.get(0));
	}

	/**
	 * Inserta una institucion y le asigna el id generado.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service).
	 *
	 * @param institucion institucion a guardar, sin id
	 * @return la misma institucion con su idInstitucion
	 */
	public Institucion crear(Institucion institucion) {
		jdbcTemplate.update(SQL_INSERTAR,
				institucion.getNombre(),
				institucion.getTipo().getValor(),
				institucion.getPais());

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		institucion.setIdInstitucion(idGenerado);
		return institucion;
	}

	/**
	 * @param institucion institucion con los datos nuevos (y su id)
	 */
	public void actualizar(Institucion institucion) {
		jdbcTemplate.update(SQL_ACTUALIZAR,
				institucion.getNombre(),
				institucion.getTipo().getValor(),
				institucion.getPais(),
				institucion.getIdInstitucion());
	}

	/**
	 * Elimina la fila de INSTITUCION. Sus telefonos y correos se deben borrar antes
	 * (lo hace el service con sus repositories).
	 *
	 * @param idInstitucion id de la institucion a eliminar
	 */
	public void eliminar(long idInstitucion) {
		jdbcTemplate.update(SQL_ELIMINAR, idInstitucion);
	}

	/**
	 * Cuenta cuantas custodias (actuales o historicas) tiene la institucion.
	 *
	 * @param idInstitucion id de la institucion
	 * @return cantidad de custodias asociadas
	 */
	public long contarCustodiasAsociadas(long idInstitucion) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_CUSTODIAS, Long.class, idInstitucion);
		if (cantidad == null) {
			return 0;
		}
		return cantidad;
	}

	/**
	 * Convierte una fila de la tabla INSTITUCION (columna -> valor) en la entity Institucion.
	 */
	private Institucion convertirFilaAInstitucion(Map<String, Object> fila) {
		Number idInstitucion = (Number) fila.get("id_institucion");

		Institucion institucion = new Institucion();
		institucion.setIdInstitucion(idInstitucion.longValue());
		institucion.setNombre((String) fila.get("nombre"));
		institucion.setTipo(convertirTipo(fila.get("tipo")));
		institucion.setPais((String) fila.get("pais"));
		return institucion;
	}

	private TipoInstitucion convertirTipo(Object valor) {
		if (valor == null) {
			return null;
		}
		String texto = valor.toString().trim().toLowerCase();
		return TipoInstitucion.buscarPorValor(texto);
	}

}