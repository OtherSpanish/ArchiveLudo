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

	/** Custodias vigentes e historicas, con la copia, su videojuego y la institucion. */
	private static final String SQL_CUSTODIAS = "SELECT cu.id_custodia, cu.id_copia, v.titulo, "
			+ "i.nombre AS institucion, cu.fecha_inicio, cu.fecha_fin, cu.condicion_almacenamiento "
			+ "FROM CUSTODIA cu "
			+ "JOIN COPIA c ON c.id_copia = cu.id_copia "
			+ "JOIN VIDEOJUEGO v ON v.id_videojuego = c.id_videojuego "
			+ "JOIN INSTITUCION i ON i.id_institucion = cu.id_institucion "
			+ "WHERE 1 = 1";

	private static final String ORDEN_CUSTODIAS = " ORDER BY cu.fecha_inicio DESC, cu.id_custodia DESC";

	/** cantidad_expertos cuenta las participaciones del proyecto con una subconsulta. */
	private static final String SQL_PROYECTOS = "SELECT p.id_proyecto, p.nombre, v.titulo, p.estado, "
			+ "p.fecha_inicio, p.fecha_fin, p.herramienta_emulador, p.url_informe, "
			+ "(SELECT COUNT(*) FROM PARTICIPACION pa WHERE pa.id_proyecto = p.id_proyecto) AS cantidad_expertos "
			+ "FROM PROYECTO_DE_RESTAURACION p "
			+ "JOIN VIDEOJUEGO v ON v.id_videojuego = p.id_videojuego "
			+ "WHERE 1 = 1";

	private static final String ORDEN_PROYECTOS = " ORDER BY p.fecha_inicio DESC, p.id_proyecto DESC";

	/** Una fila por participacion: el experto, el proyecto en que participo y el videojuego de ese proyecto. */
	private static final String SQL_EXPERTOS = "SELECT pa.id_participacion, ex.primer_nombre_ex, "
			+ "ex.segundo_nombre_ex, ex.primer_apellido_ex, ex.segundo_apellido_ex, ex.especialidad, "
			+ "p.nombre AS nombre_proyecto, v.titulo, pa.rol_experto, pa.fecha_incorporacion, pa.fecha_salida "
			+ "FROM PARTICIPACION pa "
			+ "JOIN EXPERTO ex ON ex.id_experto = pa.id_experto "
			+ "JOIN PROYECTO_DE_RESTAURACION p ON p.id_proyecto = pa.id_proyecto "
			+ "JOIN VIDEOJUEGO v ON v.id_videojuego = p.id_videojuego "
			+ "WHERE 1 = 1";

	private static final String ORDEN_EXPERTOS = " ORDER BY ex.primer_apellido_ex ASC, ex.primer_nombre_ex ASC, "
			+ "pa.fecha_incorporacion ASC, pa.id_participacion ASC";

	private final JdbcTemplate jdbcTemplate;

	public ReporteRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @return filas del reporte de estatus legal, filtradas por fecha de determinacion
	 */
	public List<Map<String, Object>> buscarEstatus(Long idVideojuego, LocalDate desde, LocalDate hasta) {
		return ejecutarConRango(SQL_ESTATUS, ORDEN_ESTATUS, "e.id_videojuego", "e.fecha_determinacion",
				idVideojuego, desde, hasta);
	}

	/**
	 * @return filas del reporte de eventos de riesgo, filtradas por fecha del evento
	 */
	public List<Map<String, Object>> buscarEventos(Long idVideojuego, LocalDate desde, LocalDate hasta) {
		return ejecutarConRango(SQL_EVENTOS, ORDEN_EVENTOS, "ev.id_videojuego", "ev.fecha_evento", idVideojuego,
				desde, hasta);
	}

	/**
	 * @return filas del reporte de copias, filtradas por fecha de registro
	 */
	public List<Map<String, Object>> buscarCopias(Long idVideojuego, LocalDate desde, LocalDate hasta) {
		return ejecutarConRango(SQL_COPIAS, ORDEN_COPIAS, "c.id_videojuego", "c.fecha_registro", idVideojuego,
				desde, hasta);
	}

	/**
	 * Custodias que estuvieron vigentes en algun momento del rango [desde, hasta]:
	 * empezaron a mas tardar en "hasta" y terminaron desde "desde" en adelante (o siguen vigentes).
	 *
	 * @return filas del reporte de custodias
	 */
	public List<Map<String, Object>> buscarCustodias(Long idVideojuego, LocalDate desde, LocalDate hasta) {
		StringBuilder sql = new StringBuilder(SQL_CUSTODIAS);
		List<Object> parametros = new ArrayList<Object>();

		agregarFiltroVideojuego(sql, parametros, "c.id_videojuego", idVideojuego);
		agregarFiltroVigenciaEnRango(sql, parametros, desde, hasta);
		sql.append(ORDEN_CUSTODIAS);

		return jdbcTemplate.queryForList(sql.toString(), parametros.toArray());
	}

	/**
	 * @return filas del reporte de proyectos, filtradas por fecha de inicio
	 */
	public List<Map<String, Object>> buscarProyectos(Long idVideojuego, LocalDate desde, LocalDate hasta) {
		return ejecutarConRango(SQL_PROYECTOS, ORDEN_PROYECTOS, "p.id_videojuego", "p.fecha_inicio", idVideojuego,
				desde, hasta);
	}

	/**
	 * @return filas del reporte de participacion de expertos, filtradas por fecha de incorporacion
	 */
	public List<Map<String, Object>> buscarParticipacionesExpertos(Long idVideojuego, LocalDate desde,
			LocalDate hasta) {
		return ejecutarConRango(SQL_EXPERTOS, ORDEN_EXPERTOS, "p.id_videojuego", "pa.fecha_incorporacion",
				idVideojuego, desde, hasta);
	}

	// ──────────────────────────────────────────────
	// Armado de filtros
	// ──────────────────────────────────────────────

	/**
	 * Para los reportes cuyo rango de fechas se aplica sobre una sola columna.
	 */
	private List<Map<String, Object>> ejecutarConRango(String sqlBase, String orden, String columnaVideojuego,
			String columnaFecha, Long idVideojuego, LocalDate desde, LocalDate hasta) {
		StringBuilder sql = new StringBuilder(sqlBase);
		List<Object> parametros = new ArrayList<Object>();

		agregarFiltroVideojuego(sql, parametros, columnaVideojuego, idVideojuego);
		agregarFiltroRango(sql, parametros, columnaFecha, desde, hasta);
		sql.append(orden);

		return jdbcTemplate.queryForList(sql.toString(), parametros.toArray());
	}

	private void agregarFiltroVideojuego(StringBuilder sql, List<Object> parametros, String columna,
			Long idVideojuego) {
		if (idVideojuego != null) {
			sql.append(" AND ").append(columna).append(" = ?");
			parametros.add(idVideojuego);
		}
	}

	/**
	 * columna >= desde y columna <= hasta (cada uno solo si llega).
	 */
	private void agregarFiltroRango(StringBuilder sql, List<Object> parametros, String columnaFecha,
			LocalDate desde, LocalDate hasta) {
		if (desde != null) {
			sql.append(" AND ").append(columnaFecha).append(" >= ?");
			parametros.add(Date.valueOf(desde));
		}
		if (hasta != null) {
			sql.append(" AND ").append(columnaFecha).append(" <= ?");
			parametros.add(Date.valueOf(hasta));
		}
	}

	/**
	 * Una custodia [inicio, fin] se cruza con el rango [desde, hasta] si:
	 * inicio <= hasta, y (fin es NULL o fin >= desde). Una custodia vigente no tiene fin.
	 */
	private void agregarFiltroVigenciaEnRango(StringBuilder sql, List<Object> parametros, LocalDate desde,
			LocalDate hasta) {
		if (hasta != null) {
			sql.append(" AND cu.fecha_inicio <= ?");
			parametros.add(Date.valueOf(hasta));
		}
		if (desde != null) {
			sql.append(" AND (cu.fecha_fin IS NULL OR cu.fecha_fin >= ?)");
			parametros.add(Date.valueOf(desde));
		}
	}

}