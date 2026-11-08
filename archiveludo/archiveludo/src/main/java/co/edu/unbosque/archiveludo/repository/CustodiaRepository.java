package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.Custodia;

/**
 * Acceso a datos de la tabla CUSTODIA con SQL escrito a mano (Spring JDBC).
 * fecha_fin en NULL significa que la custodia es la vigente.
 */
@Repository
public class CustodiaRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_custodia, id_copia, id_institucion, fecha_inicio, "
			+ "fecha_fin, condicion_almacenamiento FROM CUSTODIA ";

	/** De la mas reciente a la mas antigua; si dos empiezan el mismo dia, primero la de mayor id. */
	private static final String ORDEN = "ORDER BY fecha_inicio DESC, id_custodia DESC";

	private static final String SQL_BUSCAR_POR_COPIA = SQL_SELECCIONAR + "WHERE id_copia = ? " + ORDEN;

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_custodia = ?";

	/** La vigente es la que no tiene fecha_fin. Si por datos viejos hubiera varias, se toma la mas reciente. */
	private static final String SQL_BUSCAR_VIGENTE = SQL_SELECCIONAR + "WHERE id_copia = ? AND fecha_fin IS NULL "
			+ ORDEN + " LIMIT 1";

	/** Custodias vigentes de una institucion: una por cada copia que custodia hoy. */
	private static final String SQL_BUSCAR_VIGENTES_POR_INSTITUCION = SQL_SELECCIONAR
			+ "WHERE id_institucion = ? AND fecha_fin IS NULL " + ORDEN;

	/** Copias que la institucion custodia hoy (una custodia vigente = una copia). */
	private static final String SQL_CONTAR_VIGENTES_POR_INSTITUCION = "SELECT COUNT(*) FROM CUSTODIA "
			+ "WHERE id_institucion = ? AND fecha_fin IS NULL";

	/**
	 * Por cada institucion, cuantas copias de UN videojuego custodia hoy.
	 * Solo salen las instituciones con al menos una; ordenadas por nombre.
	 */
	private static final String SQL_CONTAR_VIGENTES_POR_INSTITUCION_DE_VIDEOJUEGO = "SELECT cu.id_institucion, "
			+ "COUNT(*) AS cantidad "
			+ "FROM CUSTODIA cu "
			+ "JOIN COPIA c ON c.id_copia = cu.id_copia "
			+ "JOIN INSTITUCION i ON i.id_institucion = cu.id_institucion "
			+ "WHERE c.id_videojuego = ? AND cu.fecha_fin IS NULL "
			+ "GROUP BY cu.id_institucion, i.nombre "
			+ "ORDER BY i.nombre ASC, cu.id_institucion ASC";

	private static final String SQL_INSERTAR = "INSERT INTO CUSTODIA (id_copia, id_institucion, fecha_inicio, "
			+ "fecha_fin, condicion_almacenamiento) VALUES (?, ?, ?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	private static final String SQL_CERRAR = "UPDATE CUSTODIA SET fecha_fin = ? WHERE id_custodia = ?";

	/** No se actualiza id_copia: la custodia sigue siendo de la misma copia. */
	private static final String SQL_ACTUALIZAR = "UPDATE CUSTODIA SET id_institucion = ?, fecha_inicio = ?, "
			+ "fecha_fin = ?, condicion_almacenamiento = ? WHERE id_custodia = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM CUSTODIA WHERE id_custodia = ?";

	private final JdbcTemplate jdbcTemplate;

	public CustodiaRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	/**
	 * Busca el historial de custodias de una copia, de la mas reciente a la mas antigua.
	 *
	 * @param idCopia id de la copia
	 * @return lista de custodias (vacia si no tiene ninguna)
	 */
	public List<Custodia> buscarPorIdCopia(long idCopia) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_COPIA, idCopia);
		return convertirFilas(filas);
	}

	/**
	 * @param idCustodia id a buscar
	 * @return la custodia encontrada, o null si no existe
	 */
	public Custodia buscarPorId(long idCustodia) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idCustodia);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaACustodia(filas.get(0));
	}

	/**
	 * @param idCopia id de la copia
	 * @return la custodia vigente de la copia (sin fecha de fin), o null si no tiene
	 */
	public Custodia buscarVigentePorIdCopia(long idCopia) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_VIGENTE, idCopia);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaACustodia(filas.get(0));
	}

	/**
	 * @param idInstitucion id de la institucion
	 * @return las custodias vigentes de la institucion, de la mas reciente a la mas antigua (vacia si no tiene)
	 */
	public List<Custodia> buscarVigentesPorIdInstitucion(long idInstitucion) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_VIGENTES_POR_INSTITUCION,
				idInstitucion);
		return convertirFilas(filas);
	}

	/**
	 * Cuenta cuantas copias custodia hoy una institucion (sus custodias sin fecha de fin).
	 * Como cada copia tiene como maximo una custodia vigente, el resultado es la cantidad de copias.
	 *
	 * @param idInstitucion id de la institucion
	 * @return cantidad de custodias vigentes de la institucion
	 */
	public long contarVigentesPorInstitucion(long idInstitucion) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_VIGENTES_POR_INSTITUCION, Long.class, idInstitucion);
		if (cantidad == null) {
			return 0;
		}
		return cantidad;
	}

	/**
	 * Para un videojuego, cuenta cuantas de sus copias custodia hoy cada institucion.
	 *
	 * @param idVideojuego id del videojuego
	 * @return mapa idInstitucion -> cantidad de copias, en orden alfabetico por nombre de institucion
	 *         (vacio si ninguna copia del juego tiene custodia vigente)
	 */
	public Map<Long, Long> contarVigentesPorInstitucionDeVideojuego(long idVideojuego) {
		List<Map<String, Object>> filas = jdbcTemplate
				.queryForList(SQL_CONTAR_VIGENTES_POR_INSTITUCION_DE_VIDEOJUEGO, idVideojuego);

		// LinkedHashMap conserva el orden en que llegan las filas (por nombre de institucion)
		Map<Long, Long> cantidades = new LinkedHashMap<Long, Long>();
		for (Map<String, Object> fila : filas) {
			Number idInstitucion = (Number) fila.get("id_institucion");
			Number cantidad = (Number) fila.get("cantidad");
			cantidades.put(idInstitucion.longValue(), cantidad.longValue());
		}
		return cantidades;
	}


	/**
	 * Inserta una custodia y le asigna el id generado.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service).
	 *
	 * @param custodia custodia a guardar, sin id
	 * @return la misma custodia con su idCustodia
	 */
	public Custodia crear(Custodia custodia) {
		jdbcTemplate.update(SQL_INSERTAR,
				custodia.getIdCopia(),
				custodia.getIdInstitucion(),
				Date.valueOf(custodia.getFechaInicio()),
				convertirAFechaSql(custodia.getFechaFin()),
				custodia.getCondicionAlmacenamiento());

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		custodia.setIdCustodia(idGenerado);
		return custodia;
	}

	/**
	 * Cierra una custodia: le pone fecha de fin, con lo que deja de ser la vigente.
	 *
	 * @param idCustodia id de la custodia a cerrar
	 * @param fechaFin   fecha en que termina
	 */
	public void cerrar(long idCustodia, LocalDate fechaFin) {
		jdbcTemplate.update(SQL_CERRAR, Date.valueOf(fechaFin), idCustodia);
	}

	/**
	 * @param custodia custodia con los datos nuevos (y su id)
	 */
	public void actualizar(Custodia custodia) {
		jdbcTemplate.update(SQL_ACTUALIZAR,
				custodia.getIdInstitucion(),
				Date.valueOf(custodia.getFechaInicio()),
				convertirAFechaSql(custodia.getFechaFin()),
				custodia.getCondicionAlmacenamiento(),
				custodia.getIdCustodia());
	}

	/**
	 * @param idCustodia id de la custodia a eliminar
	 */
	public void eliminar(long idCustodia) {
		jdbcTemplate.update(SQL_ELIMINAR, idCustodia);
	}


	private List<Custodia> convertirFilas(List<Map<String, Object>> filas) {
		List<Custodia> custodias = new ArrayList<Custodia>();
		for (Map<String, Object> fila : filas) {
			custodias.add(convertirFilaACustodia(fila));
		}
		return custodias;
	}

	/**
	 * Convierte una fila de la tabla CUSTODIA (columna -> valor) en la entity Custodia.
	 */
	private Custodia convertirFilaACustodia(Map<String, Object> fila) {
		Number idCustodia = (Number) fila.get("id_custodia");
		Number idCopia = (Number) fila.get("id_copia");
		Number idInstitucion = (Number) fila.get("id_institucion");

		Custodia custodia = new Custodia();
		custodia.setIdCustodia(idCustodia.longValue());
		custodia.setIdCopia(idCopia.longValue());
		custodia.setIdInstitucion(idInstitucion.longValue());
		custodia.setFechaInicio(convertirFecha(fila.get("fecha_inicio")));
		custodia.setFechaFin(convertirFecha(fila.get("fecha_fin")));
		custodia.setCondicionAlmacenamiento((String) fila.get("condicion_almacenamiento"));
		return custodia;
	}

	/**
	 * Convierte la fecha que entrega el driver de MySQL (java.sql.Date) a LocalDate.
	 * fecha_fin puede venir NULL (custodia vigente).
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