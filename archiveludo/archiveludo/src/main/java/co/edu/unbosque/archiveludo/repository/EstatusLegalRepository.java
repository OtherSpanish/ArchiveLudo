package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.EstatusLegal;
import co.edu.unbosque.archiveludo.model.TipoEstatusLegal;

/**
 * Acceso a datos de la tabla ESTATUS_LEGAL con SQL escrito a mano (Spring JDBC).
 */
@Repository
public class EstatusLegalRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_estatus, id_videojuego, id_usuario, estatus, "
			+ "fecha_determinacion, observacion FROM ESTATUS_LEGAL ";

	/**
	 * Del mas reciente al mas antiguo. Si dos tienen la misma fecha, va primero el de mayor id
	 * (el que se registro despues). Asi el primero de la lista siempre es el vigente.
	 */
	private static final String SQL_BUSCAR_POR_VIDEOJUEGO = SQL_SELECCIONAR + "WHERE id_videojuego = ? "
			+ "ORDER BY fecha_determinacion DESC, id_estatus DESC";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_estatus = ?";

	/** El vigente es el primero con el mismo orden de arriba: LIMIT 1 se queda solo con ese. */
	private static final String SQL_BUSCAR_ID_VIGENTE = "SELECT id_estatus FROM ESTATUS_LEGAL "
			+ "WHERE id_videojuego = ? ORDER BY fecha_determinacion DESC, id_estatus DESC LIMIT 1";

	private static final String SQL_INSERTAR = "INSERT INTO ESTATUS_LEGAL (id_videojuego, id_usuario, estatus, "
			+ "fecha_determinacion, observacion) VALUES (?, ?, ?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	/** No se actualizan id_videojuego ni id_usuario: el estatus sigue siendo del mismo juego y de quien lo registro. */
	private static final String SQL_ACTUALIZAR = "UPDATE ESTATUS_LEGAL SET estatus = ?, fecha_determinacion = ?, "
			+ "observacion = ? WHERE id_estatus = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM ESTATUS_LEGAL WHERE id_estatus = ?";

	private final JdbcTemplate jdbcTemplate;

	public EstatusLegalRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Busca todos los estatus legales de un videojuego, del mas reciente al mas antiguo.
	 *
	 * @param idVideojuego id del videojuego
	 * @return lista de estatus (vacia si no tiene ninguno)
	 */
	public List<EstatusLegal> buscarPorIdVideojuego(long idVideojuego) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_VIDEOJUEGO, idVideojuego);

		List<EstatusLegal> estatusLegales = new ArrayList<EstatusLegal>();
		for (Map<String, Object> fila : filas) {
			estatusLegales.add(convertirFilaAEstatusLegal(fila));
		}
		return estatusLegales;
	}

	/**
	 * @param idEstatus id a buscar
	 * @return el estatus encontrado, o null si no existe
	 */
	public EstatusLegal buscarPorId(long idEstatus) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idEstatus);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAEstatusLegal(filas.get(0));
	}

	/**
	 * Devuelve el id del estatus vigente de un videojuego (el de fecha mas reciente).
	 *
	 * @param idVideojuego id del videojuego
	 * @return id del estatus vigente, o 0 si el videojuego no tiene estatus
	 */
	public long buscarIdVigente(long idVideojuego) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_ID_VIGENTE, idVideojuego);

		if (filas.isEmpty()) {
			return 0;
		}
		Number idEstatus = (Number) filas.get(0).get("id_estatus");
		return idEstatus.longValue();
	}

	/**
	 * Inserta un estatus legal y le asigna el id generado.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service).
	 *
	 * @param estatusLegal estatus a guardar, sin id
	 * @return el mismo estatus con su idEstatus
	 */
	public EstatusLegal crear(EstatusLegal estatusLegal) {
		jdbcTemplate.update(SQL_INSERTAR,
				estatusLegal.getIdVideojuego(),
				estatusLegal.getIdUsuario(),
				estatusLegal.getEstatus().getValor(),
				Date.valueOf(estatusLegal.getFechaDeterminacion()),
				estatusLegal.getObservacion());

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		estatusLegal.setIdEstatus(idGenerado);
		return estatusLegal;
	}

	/**
	 * @param estatusLegal estatus con los datos nuevos (y su id)
	 */
	public void actualizar(EstatusLegal estatusLegal) {
		jdbcTemplate.update(SQL_ACTUALIZAR,
				estatusLegal.getEstatus().getValor(),
				Date.valueOf(estatusLegal.getFechaDeterminacion()),
				estatusLegal.getObservacion(),
				estatusLegal.getIdEstatus());
	}

	/**
	 * @param idEstatus id del estatus a eliminar
	 */
	public void eliminar(long idEstatus) {
		jdbcTemplate.update(SQL_ELIMINAR, idEstatus);
	}

	/**
	 * Convierte una fila de la tabla ESTATUS_LEGAL (columna -> valor) en la entity EstatusLegal.
	 */
	private EstatusLegal convertirFilaAEstatusLegal(Map<String, Object> fila) {
		Number idEstatus = (Number) fila.get("id_estatus");
		Number idVideojuego = (Number) fila.get("id_videojuego");
		Number idUsuario = (Number) fila.get("id_usuario");

		EstatusLegal estatusLegal = new EstatusLegal();
		estatusLegal.setIdEstatus(idEstatus.longValue());
		estatusLegal.setIdVideojuego(idVideojuego.longValue());
		estatusLegal.setIdUsuario(idUsuario.longValue());
		estatusLegal.setEstatus(convertirEstatus(fila.get("estatus")));
		estatusLegal.setFechaDeterminacion(convertirFecha(fila.get("fecha_determinacion")));
		estatusLegal.setObservacion((String) fila.get("observacion"));
		return estatusLegal;
	}

	/**
	 * Convierte el texto de la columna estatus al enum, sin importar mayusculas.
	 */
	private TipoEstatusLegal convertirEstatus(Object valor) {
		if (valor == null) {
			return null;
		}
		String texto = valor.toString().trim().toLowerCase();
		return TipoEstatusLegal.buscarPorValor(texto);
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

}