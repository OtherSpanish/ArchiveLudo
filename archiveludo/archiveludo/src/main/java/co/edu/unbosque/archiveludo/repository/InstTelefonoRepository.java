package co.edu.unbosque.archiveludo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.InstTelefono;

/**
 * Acceso a datos de la tabla INST_TELEFONO (telefonos de las instituciones) con SQL escrito a mano.
 * La columna se llama numero_telefono; en Java el atributo de InstTelefono sigue siendo "telefono".
 */
@Repository
public class InstTelefonoRepository {

	private static final String SQL_BUSCAR_POR_INSTITUCION = "SELECT id_institucion, numero_telefono "
			+ "FROM INST_TELEFONO WHERE id_institucion = ? ORDER BY numero_telefono ASC";

	private static final String SQL_INSERTAR = "INSERT INTO INST_TELEFONO (id_institucion, numero_telefono) "
			+ "VALUES (?, ?)";

	private static final String SQL_ELIMINAR_POR_INSTITUCION = "DELETE FROM INST_TELEFONO WHERE id_institucion = ?";

	private final JdbcTemplate jdbcTemplate;

	public InstTelefonoRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param idInstitucion id de la institucion
	 * @return sus telefonos, de menor a mayor (vacio si no tiene)
	 */
	public List<InstTelefono> buscarPorIdInstitucion(long idInstitucion) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_INSTITUCION, idInstitucion);

		List<InstTelefono> telefonos = new ArrayList<InstTelefono>();
		for (Map<String, Object> fila : filas) {
			telefonos.add(convertirFilaATelefono(fila));
		}
		return telefonos;
	}

	/**
	 * @param instTelefono telefono a guardar (con el id de su institucion)
	 */
	public void crear(InstTelefono instTelefono) {
		jdbcTemplate.update(SQL_INSERTAR, instTelefono.getIdInstitucion(), instTelefono.getTelefono());
	}

	/**
	 * Borra todos los telefonos de una institucion (se usa al actualizarla y al eliminarla).
	 *
	 * @param idInstitucion id de la institucion
	 */
	public void eliminarPorIdInstitucion(long idInstitucion) {
		jdbcTemplate.update(SQL_ELIMINAR_POR_INSTITUCION, idInstitucion);
	}

	/**
	 * La columna numero_telefono es DECIMAL(15, 0): el driver la entrega como BigDecimal,
	 * que tambien es un Number, asi que se convierte a long igual que los ids.
	 */
	private InstTelefono convertirFilaATelefono(Map<String, Object> fila) {
		Number idInstitucion = (Number) fila.get("id_institucion");
		Number telefono = (Number) fila.get("numero_telefono");

		return new InstTelefono(idInstitucion.longValue(), telefono.longValue());
	}

}