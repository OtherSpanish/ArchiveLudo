package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.model.EstadoConservacion;

/**
 * Acceso a datos de la tabla VIDEOJUEGO con SQL escrito a mano (Spring JDBC).
 * Todas las listas salen ordenadas por anio de lanzamiento, titulo e id
 * (el id al final hace que el orden sea estable para la paginacion).
 */
@Repository
public class VideojuegoRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_videojuego, titulo, genero, plataforma_original, "
			+ "descripcion, anio_lanzamiento, estado_conservacion FROM VIDEOJUEGO ";

	private static final String SQL_CONTAR = "SELECT COUNT(*) FROM VIDEOJUEGO ";

	private static final String SQL_ORDENAR = " ORDER BY anio_lanzamiento ASC, titulo ASC, id_videojuego ASC";

	private static final String SQL_PAGINAR = " LIMIT ? OFFSET ?";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_videojuego = ?";

	/** Busca titulos que CONTENGAN el texto: el service le agrega los % */
	private static final String SQL_BUSCAR_POR_TITULO = SQL_SELECCIONAR + "WHERE titulo LIKE ?" + SQL_ORDENAR;

	private static final String SQL_BUSCAR_POR_GENERO = SQL_SELECCIONAR + "WHERE genero = ?" + SQL_ORDENAR;

	private static final String SQL_BUSCAR_POR_PLATAFORMA = SQL_SELECCIONAR + "WHERE plataforma_original = ?"
			+ SQL_ORDENAR;

	private static final String SQL_BUSCAR_POR_ANIO = SQL_SELECCIONAR + "WHERE anio_lanzamiento = ?"
			+ SQL_ORDENAR;

	/** Compara sin importar mayusculas y aceptando "_" o espacio ("en_riesgo" = "en riesgo") */
	private static final String SQL_CONDICION_ESTADO = "REPLACE(LOWER(estado_conservacion), '_', ' ') = ?";

	private static final String SQL_BUSCAR_POR_ESTADO = SQL_SELECCIONAR + "WHERE " + SQL_CONDICION_ESTADO
			+ SQL_ORDENAR;

	private final JdbcTemplate jdbcTemplate;

	public VideojuegoRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Busca un videojuego por su id.
	 *
	 * @param idVideojuego id a buscar
	 * @return el videojuego encontrado, o null si no existe
	 */
	public Videojuego buscarPorId(long idVideojuego) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idVideojuego);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAVideojuego(filas.get(0));
	}

	/**
	 * Devuelve una pagina del catalogo, con filtros opcionales.
	 * Ejemplo del SQL que se arma con los dos filtros:
	 * SELECT ... FROM VIDEOJUEGO WHERE 1 = 1 AND estado = ? AND titulo LIKE ? ORDER BY ... LIMIT ? OFFSET ?
	 *
	 * @param estado         estado a filtrar, o null para todos
	 * @param patronTitulo   texto con comodines ("%zelda%"), o null para todos
	 * @param limite         cantidad de videojuegos de la pagina
	 * @param desplazamiento cuantos videojuegos saltar (pagina * limite)
	 * @return los videojuegos de esa pagina
	 */
	public List<Videojuego> buscarCatalogo(EstadoConservacion estado, String patronTitulo, int limite,
			int desplazamiento) {
		List<Object> parametros = new ArrayList<Object>();
		String condiciones = construirCondicionesCatalogo(estado, patronTitulo, parametros);

		String sql = SQL_SELECCIONAR + condiciones + SQL_ORDENAR + SQL_PAGINAR;
		parametros.add(limite);
		parametros.add(desplazamiento);

		List<Map<String, Object>> filas = jdbcTemplate.queryForList(sql, parametros.toArray());
		return convertirFilasAVideojuegos(filas);
	}

	/**
	 * Cuenta cuantos videojuegos cumplen los filtros del catalogo (en toda la base, no solo en una pagina).
	 *
	 * @param estado       estado a filtrar, o null para todos
	 * @param patronTitulo texto con comodines ("%zelda%"), o null para todos
	 * @return total de videojuegos que cumplen los filtros
	 */
	public long contarCatalogo(EstadoConservacion estado, String patronTitulo) {
		List<Object> parametros = new ArrayList<Object>();
		String condiciones = construirCondicionesCatalogo(estado, patronTitulo, parametros);

		String sql = SQL_CONTAR + condiciones;

		Long total = jdbcTemplate.queryForObject(sql, Long.class, parametros.toArray());
		if (total == null) {
			return 0;
		}
		return total;
	}

	public List<Videojuego> buscarPorTitulo(String patronTitulo) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_TITULO, patronTitulo);
		return convertirFilasAVideojuegos(filas);
	}

	public List<Videojuego> buscarPorGenero(String genero) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_GENERO, genero);
		return convertirFilasAVideojuegos(filas);
	}

	public List<Videojuego> buscarPorPlataformaOriginal(String plataformaOriginal) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_PLATAFORMA, plataformaOriginal);
		return convertirFilasAVideojuegos(filas);
	}

	public List<Videojuego> buscarPorAnioLanzamiento(int anioLanzamiento) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ANIO, anioLanzamiento);
		return convertirFilasAVideojuegos(filas);
	}

	public List<Videojuego> buscarPorEstadoConservacion(EstadoConservacion estado) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ESTADO, estado.getValor());
		return convertirFilasAVideojuegos(filas);
	}

	/**
	 * Arma el WHERE del catalogo segun los filtros que lleguen y agrega sus valores a la lista de parametros.
	 * "WHERE 1 = 1" siempre es verdadero; sirve para que cada filtro se agregue con " AND ..."
	 * sin tener que revisar si es el primero.
	 * El texto del usuario nunca se pega en el SQL: siempre viaja como parametro (?).
	 */
	private String construirCondicionesCatalogo(EstadoConservacion estado, String patronTitulo,
			List<Object> parametros) {
		StringBuilder condiciones = new StringBuilder("WHERE 1 = 1");

		if (estado != null) {
			condiciones.append(" AND ").append(SQL_CONDICION_ESTADO);
			parametros.add(estado.getValor());
		}
		if (patronTitulo != null) {
			condiciones.append(" AND titulo LIKE ?");
			parametros.add(patronTitulo);
		}
		return condiciones.toString();
	}

	/**
	 * Convierte todas las filas de una consulta en una lista de videojuegos.
	 */
	private List<Videojuego> convertirFilasAVideojuegos(List<Map<String, Object>> filas) {
		List<Videojuego> videojuegos = new ArrayList<Videojuego>();
		for (Map<String, Object> fila : filas) {
			videojuegos.add(convertirFilaAVideojuego(fila));
		}
		return videojuegos;
	}

	/**
	 * Convierte una fila de la tabla VIDEOJUEGO (columna -> valor) en la entity Videojuego.
	 */
	private Videojuego convertirFilaAVideojuego(Map<String, Object> fila) {
		Number idVideojuego = (Number) fila.get("id_videojuego");

		Videojuego videojuego = new Videojuego();
		videojuego.setIdVideojuego(idVideojuego.longValue());
		videojuego.setTitulo((String) fila.get("titulo"));
		videojuego.setGenero((String) fila.get("genero"));
		videojuego.setPlataformaOriginal((String) fila.get("plataforma_original"));
		videojuego.setDescripcion((String) fila.get("descripcion"));
		videojuego.setAnioLanzamiento(convertirAnio(fila.get("anio_lanzamiento")));
		videojuego.setEstadoConservacion(convertirEstado(fila.get("estado_conservacion")));
		return videojuego;
	}

	/**
	 * Si la columna es INT llega como numero; si es YEAR, el driver de MySQL la entrega
	 * como fecha (1 de enero de ese anio). En ambos casos se devuelve el anio.
	 */
	private int convertirAnio(Object valor) {
		if (valor == null) {
			return 0;
		}
		if (valor instanceof Date) {
			Date fecha = (Date) valor;
			return fecha.toLocalDate().getYear();
		}
		Number anio = (Number) valor;
		return anio.intValue();
	}

	/**
	 * Convierte el texto guardado en la BD al enum EstadoConservacion.
	 * Acepta "en riesgo" y tambien "en_riesgo", sin importar mayusculas.
	 */
	private EstadoConservacion convertirEstado(Object valor) {
		if (valor == null) {
			return null;
		}
		String texto = valor.toString().trim().toLowerCase().replace('_', ' ');
		return EstadoConservacion.buscarPorValor(texto);
	}

}