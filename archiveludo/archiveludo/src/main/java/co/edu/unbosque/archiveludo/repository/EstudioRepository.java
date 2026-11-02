package co.edu.unbosque.archiveludo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.Estudio;
import co.edu.unbosque.archiveludo.model.EstadoEstudio;

/**
 * Acceso a datos de la tabla ESTUDIO con SQL(Spring JDBC).
 */
@Repository
public class EstudioRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_estudio, nombre, pais, anio_fundacion, anio_cierre, "
			+ "estado_actual FROM ESTUDIO ";

	private static final String SQL_BUSCAR_TODOS = SQL_SELECCIONAR + "ORDER BY nombre ASC, id_estudio ASC";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_estudio = ?";

	private static final String SQL_INSERTAR = "INSERT INTO ESTUDIO (nombre, pais, anio_fundacion, anio_cierre, "
			+ "estado_actual) VALUES (?, ?, ?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	private static final String SQL_ACTUALIZAR = "UPDATE ESTUDIO SET nombre = ?, pais = ?, anio_fundacion = ?, "
			+ "anio_cierre = ?, estado_actual = ? WHERE id_estudio = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM ESTUDIO WHERE id_estudio = ?";

	private static final String SQL_CONTAR_VIDEOJUEGOS = "SELECT COUNT(*) FROM VIDEOJUEGO WHERE id_estudio = ?";

	private final JdbcTemplate jdbcTemplate;

	public EstudioRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @return todos los estudios, ordenados por nombre
	 */
	public List<Estudio> buscarTodos() {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_TODOS);

		List<Estudio> estudios = new ArrayList<Estudio>();
		for (Map<String, Object> fila : filas) {
			estudios.add(convertirFilaAEstudio(fila));
		}
		return estudios;
	}

	/**
	 * @param idEstudio id a buscar
	 * @return el estudio encontrado, o null si no existe
	 */
	public Estudio buscarPorId(long idEstudio) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idEstudio);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAEstudio(filas.get(0));
	}

	/**
	 * Inserta un estudio y le asigna el id generado.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service) para que el
	 * INSERT y el LAST_INSERT_ID() usen la misma conexion.
	 *
	 * @param estudio estudio a guardar, sin id
	 * @return el mismo estudio con su idEstudio
	 */
	public Estudio crear(Estudio estudio) {
		jdbcTemplate.update(SQL_INSERTAR,
				estudio.getNombre(),
				estudio.getPais(),
				estudio.getAnioFundacion(),
				estudio.getAnioCierre(),
				estudio.getEstadoActual().getValor());

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		estudio.setIdEstudio(idGenerado);
		return estudio;
	}

	/**
	 * Actualiza todos los datos del estudio menos su id.
	 *
	 * @param estudio estudio con los datos nuevos (y su id)
	 */
	public void actualizar(Estudio estudio) {
		jdbcTemplate.update(SQL_ACTUALIZAR,
				estudio.getNombre(),
				estudio.getPais(),
				estudio.getAnioFundacion(),
				estudio.getAnioCierre(),
				estudio.getEstadoActual().getValor(),
				estudio.getIdEstudio());
	}

	/**
	 * @param idEstudio id del estudio a eliminar
	 */
	public void eliminar(long idEstudio) {
		jdbcTemplate.update(SQL_ELIMINAR, idEstudio);
	}

	/**
	 * Cuenta cuantos videojuegos tienen este estudio como desarrollador.
	 *
	 * @param idEstudio id del estudio
	 * @return cantidad de videojuegos asociados
	 */
	public long contarVideojuegosAsociados(long idEstudio) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_VIDEOJUEGOS, Long.class, idEstudio);
		if (cantidad == null) {
			return 0;
		}
		return cantidad;
	}

	/**
	 * Convierte una fila de la tabla ESTUDIO (columna -> valor) en la entity Estudio.
	 */
	private Estudio convertirFilaAEstudio(Map<String, Object> fila) {
		Number idEstudio = (Number) fila.get("id_estudio");

		Estudio estudio = new Estudio();
		estudio.setIdEstudio(idEstudio.longValue());
		estudio.setNombre((String) fila.get("nombre"));
		estudio.setPais((String) fila.get("pais"));
		estudio.setAnioFundacion(convertirAEntero(fila.get("anio_fundacion")));
		estudio.setAnioCierre(convertirAEntero(fila.get("anio_cierre")));
		estudio.setEstadoActual(convertirEstado(fila.get("estado_actual")));
		return estudio;
	}

	/**
	 * Convierte una columna INT que puede venir en NULL (anio_cierre).
	 */
	private Integer convertirAEntero(Object valor) {
		if (valor == null) {
			return null;
		}
		Number numero = (Number) valor;
		return numero.intValue();
	}

	private EstadoEstudio convertirEstado(Object valor) {
		if (valor == null) {
			return null;
		}
		String texto = valor.toString().trim().toLowerCase();
		return EstadoEstudio.buscarPorValor(texto);
	}

}