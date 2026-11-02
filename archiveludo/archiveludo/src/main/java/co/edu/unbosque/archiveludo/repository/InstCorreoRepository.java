package co.edu.unbosque.archiveludo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.InstCorreo;

/**
 * Acceso a datos de la tabla INST_CORREO (correos de las instituciones) con SQL escrito a mano.
 */
@Repository
public class InstCorreoRepository {

	private static final String SQL_BUSCAR_POR_INSTITUCION = "SELECT id_institucion, correo FROM INST_CORREO "
			+ "WHERE id_institucion = ? ORDER BY correo ASC";

	private static final String SQL_INSERTAR = "INSERT INTO INST_CORREO (id_institucion, correo) VALUES (?, ?)";

	private static final String SQL_ELIMINAR_POR_INSTITUCION = "DELETE FROM INST_CORREO WHERE id_institucion = ?";

	private final JdbcTemplate jdbcTemplate;

	public InstCorreoRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param idInstitucion id de la institucion
	 * @return sus correos en orden alfabetico (vacio si no tiene)
	 */
	public List<InstCorreo> buscarPorIdInstitucion(long idInstitucion) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_INSTITUCION, idInstitucion);

		List<InstCorreo> correos = new ArrayList<InstCorreo>();
		for (Map<String, Object> fila : filas) {
			correos.add(convertirFilaACorreo(fila));
		}
		return correos;
	}

	/**
	 * @param instCorreo correo a guardar (con el id de su institucion)
	 */
	public void crear(InstCorreo instCorreo) {
		jdbcTemplate.update(SQL_INSERTAR, instCorreo.getIdInstitucion(), instCorreo.getCorreo());
	}

	/**
	 * Borra todos los correos de una institucion (se usa al actualizarla y al eliminarla).
	 *
	 * @param idInstitucion id de la institucion
	 */
	public void eliminarPorIdInstitucion(long idInstitucion) {
		jdbcTemplate.update(SQL_ELIMINAR_POR_INSTITUCION, idInstitucion);
	}

	private InstCorreo convertirFilaACorreo(Map<String, Object> fila) {
		Number idInstitucion = (Number) fila.get("id_institucion");
		String correo = (String) fila.get("correo");

		return new InstCorreo(idInstitucion.longValue(), correo);
	}

}