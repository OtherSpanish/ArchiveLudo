package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consultas de los reportes, con SQL escrito a mano.
 * Cada consulta une varias tablas (JOIN) para traer en una sola ida a la BD todo lo que la tabla necesita.
 * Devuelve las filas tal como salen de MySQL (columna -> valor): una fila de reporte junta datos
 * de varias tablas y no corresponde a ninguna entity. ReporteService las convierte a texto.
 *
 * Los filtros son opcionales: se agregan al WHERE solo si llegan, y su valor siempre viaja como parametro (?).
 */
@Repository
public class ReporteRepository {

	/**
	 * es_vigente vale 1 si el estatus es el mas reciente de su videojuego
	 * (mayor fecha_determinacion; en empate, el de mayor id).
	 */
	private static final String SQL_ESTATUS = "SELECT e.id_estatus, v.titulo, e.estatus, e.fecha_determinacion, "
			+ "e.observacion, u.primer_nombre, u.primer_apellido, "
			+ "(e.id_estatus = (SELECT e2.id_estatus FROM ESTATUS_LEGAL e2 "
			+ "WHERE e2.id_videojuego = e.id_videojuego "
			+ "ORDER BY e2.fecha_determinacion DESC, e2.id_estatus DESC LIMIT 1)) AS es_vigente "
			+ "FROM ESTATUS_LEGAL e "
			+ "JOIN VIDEOJUEGO v ON v.id_videojuego = e.id_videojuego "
			+ "JOIN USUARIO u ON u.id_usuario = e.id_usuario "
			+ "WHERE 1 = 1";

	private static final String ORDEN_ESTATUS = " ORDER BY e.fecha_determinacion DESC, e.id_estatus DESC";

	private static final String SQL_EVENTOS = "SELECT ev.id_evento, ev.fecha_evento, v.titulo, ev.tipo_evento, "
			+ "ev.descripcion, ev.fuente, u.primer_nombre, u.primer_apellido "
			+ "FROM EVENTO_DE_RIESGO ev "
			+ "JOIN VIDEOJUEGO v ON v.id_videojuego = ev.id_videojuego "
			+ "JOIN USUARIO u ON u.id_usuario = ev.id_usuario "
			+ "WHERE 1 = 1";

	private static final String ORDEN_EVENTOS = " ORDER BY ev.fecha_evento DESC, ev.id_evento DESC";

	/**
	 * Las dos tablas hijas van con LEFT JOIN (una copia esta solo en una de ellas)
	 * y la custodia vigente tambien (una copia puede no tener custodia).
	 */
	private static final String SQL_COPIAS = "SELECT c.id_copia, v.titulo, c.tipo_copia, c.estado_integridad, "
			+ "c.fecha_registro, f.tipo_soporte, f.ubicacion_fisica, d.formato_archivo, d.tamanio, "
			+ "i.nombre AS institucion_actual "
			+ "FROM COPIA c "
			+ "JOIN VIDEOJUEGO v ON v.id_videojuego = c.id_videojuego "
			+ "LEFT JOIN COPIA_FISICA f ON f.id_copia = c.id_copia "
			+ "LEFT JOIN COPIA_DIGITAL d ON d.id_copia = c.id_copia "
			+ "LEFT JOIN CUSTODIA cu ON cu.id_copia = c.id_copia AND cu.fecha_fin IS NULL "
			+ "LEFT JOIN INSTITUCION i ON i.id_institucion = cu.id_institucion "
			+ "WHERE 1 = 1";

	private static final String ORDEN_COPIAS = " ORDER BY c.fecha_registro DESC, c.id_copia DESC";

	private final JdbcTemplate jdbcTemplate;

	public ReporteRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param idVideojuego videojuego a filtrar, o null para todos
	 * @param desde        fecha de determinacion minima, o null
	 * @param hasta        fecha de determinacion maxima, o null
	 * @return filas del reporte de estatus legal, del mas reciente al mas antiguo
	 */
	public List<Map<String, Object>> buscarEstatus(Long idVideojuego, LocalDate desde, LocalDate hasta) {
		return ejecutar(SQL_ESTATUS, ORDEN_ESTATUS, "e.id_videojuego", "e.fecha_determinacion", idVideojuego, desde,
				hasta);
	}

	/**
	 * @param idVideojuego videojuego a filtrar, o null para todos
	 * @param desde        fecha minima del evento, o null
	 * @param hasta        fecha maxima del evento, o null
	 * @return filas del reporte de eventos de riesgo, del mas reciente al mas antiguo
	 */
	public List<Map<String, Object>> buscarEventos(Long idVideojuego, LocalDate desde, LocalDate hasta) {
		return ejecutar(SQL_EVENTOS, ORDEN_EVENTOS, "ev.id_videojuego", "ev.fecha_evento", idVideojuego, desde,
				hasta);
	}

	/**
	 * @param idVideojuego videojuego a filtrar, o null para todos
	 * @param desde        fecha de registro minima, o null
	 * @param hasta        fecha de registro maxima, o null
	 * @return filas del reporte de copias, de la mas reciente a la mas antigua
	 */
	public List<Map<String, Object>> buscarCopias(Long idVideojuego, LocalDate desde, LocalDate hasta) {
		return ejecutar(SQL_COPIAS, ORDEN_COPIAS, "c.id_videojuego", "c.fecha_registro", idVideojuego, desde, hasta);
	}

	/**
	 * Agrega al SQL base los filtros que lleguen, le pone el orden y ejecuta la consulta.
	 *
	 * @param sqlBase           consulta con "WHERE 1 = 1" al final
	 * @param orden             ORDER BY de la consulta
	 * @param columnaVideojuego columna con la que se filtra el videojuego (ej. "c.id_videojuego")
	 * @param columnaFecha      columna con la que se filtra el rango (ej. "c.fecha_registro")
	 */
	private List<Map<String, Object>> ejecutar(String sqlBase, String orden, String columnaVideojuego,
			String columnaFecha, Long idVideojuego, LocalDate desde, LocalDate hasta) {
		StringBuilder sql = new StringBuilder(sqlBase);
		List<Object> parametros = new ArrayList<Object>();

		if (idVideojuego != null) {
			sql.append(" AND ").append(columnaVideojuego).append(" = ?");
			parametros.add(idVideojuego);
		}
		if (desde != null) {
			sql.append(" AND ").append(columnaFecha).append(" >= ?");
			parametros.add(Date.valueOf(desde));
		}
		if (hasta != null) {
			sql.append(" AND ").append(columnaFecha).append(" <= ?");
			parametros.add(Date.valueOf(hasta));
		}
		sql.append(orden);

		return jdbcTemplate.queryForList(sql.toString(), parametros.toArray());
	}

}