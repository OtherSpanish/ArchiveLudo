package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.Participacion;

/**
 * Acceso a datos de la tabla PARTICIPACION con SQL escrito a mano (Spring JDBC).
 */
@Repository
public class ParticipacionRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_participacion, id_proyecto, id_experto, rol_experto, "
			+ "fecha_incorporacion, fecha_salida FROM PARTICIPACION ";

	/** En el orden en que entraron los expertos al proyecto. */
	private static final String SQL_BUSCAR_POR_PROYECTO = SQL_SELECCIONAR + "WHERE id_proyecto = ? "
			+ "ORDER BY fecha_incorporacion ASC, id_participacion ASC";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_participacion = ?";

	/** Para no repetir un experto en el mismo proyecto (UNIQUE (id_proyecto, id_experto)). */
	private static final String SQL_CONTAR_EXPERTO_EN_PROYECTO = "SELECT COUNT(*) FROM PARTICIPACION "
			+ "WHERE id_proyecto = ? AND id_experto = ?";

	private static final String SQL_CONTAR_POR_PROYECTO = "SELECT COUNT(*) FROM PARTICIPACION WHERE id_proyecto = ?";

	private static final String SQL_INSERTAR = "INSERT INTO PARTICIPACION (id_proyecto, id_experto, rol_experto, "
			+ "fecha_incorporacion, fecha_salida) VALUES (?, ?, ?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	/** No se actualizan id_proyecto ni id_experto: la participacion sigue siendo del mismo experto en el mismo proyecto. */
	private static final String SQL_ACTUALIZAR = "UPDATE PARTICIPACION SET rol_experto = ?, fecha_incorporacion = ?, "
			+ "fecha_salida = ? WHERE id_participacion = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM PARTICIPACION WHERE id_participacion = ?";

	private static final String SQL_ELIMINAR_POR_PROYECTO = "DELETE FROM PARTICIPACION WHERE id_proyecto = ?";

	private final JdbcTemplate jdbcTemplate;

	public ParticipacionRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	/**
	 * @param idProyecto id del proyecto
	 * @return sus participaciones, en el orden en que entraron los expertos (vacio si no tiene)
	 */
	public List<Participacion> buscarPorIdProyecto(long idProyecto) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_PROYECTO, idProyecto);

		List<Participacion> participaciones = new ArrayList<Participacion>();
		for (Map<String, Object> fila : filas) {
			participaciones.add(convertirFilaAParticipacion(fila));
		}
		return participaciones;
	}

	/**
	 * @param idParticipacion id a buscar
	 * @return la participacion encontrada, o null si no existe
	 */
	public Participacion buscarPorId(long idParticipacion) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idParticipacion);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAParticipacion(filas.get(0));
	}

	/**
	 * @param idProyecto id del proyecto
	 * @param idExperto  id del experto
	 * @return true si el experto ya participa en ese proyecto
	 */
	public boolean existeExpertoEnProyecto(long idProyecto, long idExperto) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_EXPERTO_EN_PROYECTO, Long.class, idProyecto,
				idExperto);
		return cantidad != null && cantidad > 0;
	}

	/**
	 * @param idProyecto id del proyecto
	 * @return cantidad de participaciones del proyecto
	 */
	public long contarPorIdProyecto(long idProyecto) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_POR_PROYECTO, Long.class, idProyecto);
		if (cantidad == null) {
			return 0;
		}
		return cantidad;
	}


	/**
	 * Inserta una participacion y le asigna el id generado.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service).
	 *
	 * @param participacion participacion a guardar, sin id
	 * @return la misma participacion con su idParticipacion
	 */
	public Participacion crear(Participacion participacion) {
		jdbcTemplate.update(SQL_INSERTAR,
				participacion.getIdProyecto(),
				participacion.getIdExperto(),
				participacion.getRolExperto(),
				Date.valueOf(participacion.getFechaIncorporacion()),
				convertirAFechaSql(participacion.getFechaSalida()));

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		participacion.setIdParticipacion(idGenerado);
		return participacion;
	}

	/**
	 * @param participacion participacion con los datos nuevos (y su id)
	 */
	public void actualizar(Participacion participacion) {
		jdbcTemplate.update(SQL_ACTUALIZAR,
				participacion.getRolExperto(),
				Date.valueOf(participacion.getFechaIncorporacion()),
				convertirAFechaSql(participacion.getFechaSalida()),
				participacion.getIdParticipacion());
	}

	/**
	 * @param idParticipacion id de la participacion a eliminar
	 */
	public void eliminar(long idParticipacion) {
		jdbcTemplate.update(SQL_ELIMINAR, idParticipacion);
	}

	/**
	 * Borra todas las participaciones de un proyecto (se usa al eliminar el proyecto).
	 *
	 * @param idProyecto id del proyecto
	 */
	public void eliminarPorIdProyecto(long idProyecto) {
		jdbcTemplate.update(SQL_ELIMINAR_POR_PROYECTO, idProyecto);
	}


	/**
	 * Convierte una fila de la tabla PARTICIPACION (columna -> valor) en la entity Participacion.
	 */
	private Participacion convertirFilaAParticipacion(Map<String, Object> fila) {
		Number idParticipacion = (Number) fila.get("id_participacion");
		Number idProyecto = (Number) fila.get("id_proyecto");
		Number idExperto = (Number) fila.get("id_experto");

		Participacion participacion = new Participacion();
		participacion.setIdParticipacion(idParticipacion.longValue());
		participacion.setIdProyecto(idProyecto.longValue());
		participacion.setIdExperto(idExperto.longValue());
		participacion.setRolExperto((String) fila.get("rol_experto"));
		participacion.setFechaIncorporacion(convertirFecha(fila.get("fecha_incorporacion")));
		participacion.setFechaSalida(convertirFecha(fila.get("fecha_salida")));
		return participacion;
	}

	/**
	 * Convierte la fecha que entrega el driver de MySQL (java.sql.Date) a LocalDate.
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

	/**
	 * Convierte una fecha opcional a java.sql.Date; si es null, en la BD queda NULL.
	 */
	private Date convertirAFechaSql(LocalDate fecha) {
		if (fecha == null) {
			return null;
		}
		return Date.valueOf(fecha);
	}

}