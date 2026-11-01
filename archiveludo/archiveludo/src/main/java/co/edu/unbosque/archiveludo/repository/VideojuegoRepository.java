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
 * Todas las listas salen ordenadas por anio de lanzamiento y luego por titulo.
 */
@Repository
public class VideojuegoRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_videojuego, titulo, genero, plataforma_original, "
			+ "descripcion, anio_lanzamiento, estado_conservacion FROM VIDEOJUEGO ";

	private static final String SQL_ORDENAR = " ORDER BY anio_lanzamiento ASC, titulo ASC";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_videojuego = ?";

	private static final String SQL_BUSCAR_TODOS = SQL_SELECCIONAR + SQL_ORDENAR;

	/** Busca titulos que CONTENGAN el texto: el service le agrega los % */
	private static final String SQL_BUSCAR_POR_TITULO = SQL_SELECCIONAR + "WHERE titulo LIKE ?" + SQL_ORDENAR;

	private static final String SQL_BUSCAR_POR_GENERO = SQL_SELECCIONAR + "WHERE genero = ?" + SQL_ORDENAR;

	private static final String SQL_BUSCAR_POR_PLATAFORMA = SQL_SELECCIONAR + "WHERE plataforma_original = ?"
			+ SQL_ORDENAR;

	private static final String SQL_BUSCAR_POR_ANIO = SQL_SELECCIONAR + "WHERE anio_lanzamiento = ?"
			+ SQL_ORDENAR;

	/** Compara sin importar mayusculas y aceptando "_" o espacio ("en_riesgo" = "en riesgo") */
	private static final String SQL_BUSCAR_POR_ESTADO = SQL_SELECCIONAR
			+ "WHERE REPLACE(LOWER(estado_conservacion), '_', ' ') = ?" + SQL_ORDENAR;

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

	public List<Videojuego> buscarTodos() {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_TODOS);
		return convertirFilasAVideojuegos(filas);
	}

	/**
	 * @param patronTitulo texto con comodines, por ejemplo "%zelda%"
	 */
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