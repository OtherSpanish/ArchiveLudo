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
 * Acceso a datos de VIDEOJUEGO y de su relacion N:M con las editoras (COMPED_VID), con SQL a mano.
 *
 * Las editoras de cada juego se traen en la MISMA consulta:
 * LEFT JOIN a COMPED_VID + GROUP_CONCAT(id_editora) devuelve un texto como "2,5"
 * que se convierte a la lista [2, 5]. Asi no se hace una consulta por cada juego.
 *
 * Todas las listas salen ordenadas por anio de lanzamiento, titulo e id
 * (el id al final hace que el orden sea estable para la paginacion).
 */
@Repository
public class VideojuegoRepository {

	private static final String SQL_SELECCIONAR = "SELECT v.id_videojuego, v.titulo, v.genero, "
			+ "v.plataforma_original, v.descripcion, v.anio_lanzamiento, v.estado_conservacion, "
			+ "v.id_estudio, v.id_usuario, v.url_imagen, "
			+ "GROUP_CONCAT(cv.id_editora ORDER BY cv.id_editora) AS ids_editoras "
			+ "FROM VIDEOJUEGO v "
			+ "LEFT JOIN COMPED_VID cv ON cv.id_videojuego = v.id_videojuego ";

	/** Una fila por videojuego (las editoras quedan juntas en ids_editoras). */
	private static final String SQL_AGRUPAR = " GROUP BY v.id_videojuego";

	private static final String SQL_ORDENAR = " ORDER BY v.anio_lanzamiento ASC, v.titulo ASC, v.id_videojuego ASC";

	private static final String SQL_PAGINAR = " LIMIT ? OFFSET ?";

	private static final String SQL_CONTAR = "SELECT COUNT(*) FROM VIDEOJUEGO v ";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE v.id_videojuego = ?" + SQL_AGRUPAR;

	/** Busca titulos que CONTENGAN el texto: el service le agrega los % */
	private static final String SQL_BUSCAR_POR_TITULO = SQL_SELECCIONAR + "WHERE v.titulo LIKE ?" + SQL_AGRUPAR
			+ SQL_ORDENAR;

	private static final String SQL_BUSCAR_POR_GENERO = SQL_SELECCIONAR + "WHERE v.genero = ?" + SQL_AGRUPAR
			+ SQL_ORDENAR;

	private static final String SQL_BUSCAR_POR_PLATAFORMA = SQL_SELECCIONAR + "WHERE v.plataforma_original = ?"
			+ SQL_AGRUPAR + SQL_ORDENAR;

	private static final String SQL_BUSCAR_POR_ANIO = SQL_SELECCIONAR + "WHERE v.anio_lanzamiento = ?"
			+ SQL_AGRUPAR + SQL_ORDENAR;

	/** Compara sin importar mayusculas y aceptando "_" o espacio ("en_riesgo" = "en riesgo") */
	private static final String SQL_CONDICION_ESTADO = "REPLACE(LOWER(v.estado_conservacion), '_', ' ') = ?";

	private static final String SQL_BUSCAR_POR_ESTADO = SQL_SELECCIONAR + "WHERE " + SQL_CONDICION_ESTADO
			+ SQL_AGRUPAR + SQL_ORDENAR;

	/** url_imagen no va aqui: un videojuego nuevo nace sin imagen y se le sube despues. */
	private static final String SQL_INSERTAR = "INSERT INTO VIDEOJUEGO (titulo, genero, plataforma_original, "
			+ "descripcion, anio_lanzamiento, estado_conservacion, id_estudio, id_usuario) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	private static final String SQL_INSERTAR_EDITORA = "INSERT INTO COMPED_VID (id_videojuego, id_editora) "
			+ "VALUES (?, ?)";

	/** No se actualizan id_usuario (se conserva quien lo registro) ni url_imagen (tiene su propio metodo). */
	private static final String SQL_ACTUALIZAR = "UPDATE VIDEOJUEGO SET titulo = ?, genero = ?, "
			+ "plataforma_original = ?, descripcion = ?, anio_lanzamiento = ?, estado_conservacion = ?, "
			+ "id_estudio = ? WHERE id_videojuego = ?";

	private static final String SQL_ACTUALIZAR_URL_IMAGEN = "UPDATE VIDEOJUEGO SET url_imagen = ? "
			+ "WHERE id_videojuego = ?";

	private static final String SQL_ELIMINAR_EDITORAS = "DELETE FROM COMPED_VID WHERE id_videojuego = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM VIDEOJUEGO WHERE id_videojuego = ?";

	/**
	 * Suma lo que depende del videojuego: copias, estatus legales, eventos de riesgo
	 * y proyectos de restauracion. Si da mas de cero, el videojuego no se puede eliminar.
	 */
	private static final String SQL_CONTAR_REGISTROS_ASOCIADOS = "SELECT "
			+ "(SELECT COUNT(*) FROM COPIA WHERE id_videojuego = ?) + "
			+ "(SELECT COUNT(*) FROM ESTATUS_LEGAL WHERE id_videojuego = ?) + "
			+ "(SELECT COUNT(*) FROM EVENTO_DE_RIESGO WHERE id_videojuego = ?) + "
			+ "(SELECT COUNT(*) FROM PROYECTO_DE_RESTAURACION WHERE id_videojuego = ?)";

	private final JdbcTemplate jdbcTemplate;

	public VideojuegoRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// ──────────────────────────────────────────────
	// Lectura
	// ──────────────────────────────────────────────

	/**
	 * @param idVideojuego id a buscar
	 * @return el videojuego encontrado (con sus editoras), o null si no existe
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

		String sql = SQL_SELECCIONAR + condiciones + SQL_AGRUPAR + SQL_ORDENAR + SQL_PAGINAR;
		parametros.add(limite);
		parametros.add(desplazamiento);

		List<Map<String, Object>> filas = jdbcTemplate.queryForList(sql, parametros.toArray());
		return convertirFilasAVideojuegos(filas);
	}

	/**
	 * Cuenta cuantos videojuegos cumplen los filtros del catalogo (en toda la base, no solo en una pagina).
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
	 * Cuenta cuantas copias, estatus legales, eventos de riesgo y proyectos de restauracion
	 * tiene el videojuego. El mismo id va en los 4 parametros de la consulta.
	 */
	public long contarRegistrosAsociados(long idVideojuego) {
		Long cantidad = jdbcTemplate.queryForObject(SQL_CONTAR_REGISTROS_ASOCIADOS, Long.class, idVideojuego,
				idVideojuego, idVideojuego, idVideojuego);
		if (cantidad == null) {
			return 0;
		}
		return cantidad;
	}

	// ──────────────────────────────────────────────
	// Escritura (llamar siempre dentro de @Transactional en el service)
	// ──────────────────────────────────────────────

	/**
	 * Inserta el videojuego en VIDEOJUEGO y una fila en COMPED_VID por cada editora.
	 * Debe ir en una transaccion para que el LAST_INSERT_ID() use la misma conexion
	 * y para que, si falla una editora, no quede el videojuego a medias.
	 *
	 * @param videojuego videojuego a guardar, sin id
	 * @return el mismo videojuego con su idVideojuego
	 */
	public Videojuego crear(Videojuego videojuego) {
		jdbcTemplate.update(SQL_INSERTAR,
				videojuego.getTitulo(),
				videojuego.getGenero(),
				videojuego.getPlataformaOriginal(),
				videojuego.getDescripcion(),
				videojuego.getAnioLanzamiento(),
				videojuego.getEstadoConservacion().getValor(),
				videojuego.getIdEstudio(),
				videojuego.getIdUsuario());

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		videojuego.setIdVideojuego(idGenerado);

		insertarEditoras(idGenerado, videojuego.getIdsEditoras());
		return videojuego;
	}

	/**
	 * Actualiza los datos del videojuego y REEMPLAZA sus editoras:
	 * borra las filas actuales de COMPED_VID y vuelve a insertar las de la lista nueva.
	 * La imagen no cambia aqui (ver actualizarUrlImagen).
	 *
	 * @param videojuego videojuego con los datos nuevos (y su id)
	 */
	public void actualizar(Videojuego videojuego) {
		jdbcTemplate.update(SQL_ACTUALIZAR,
				videojuego.getTitulo(),
				videojuego.getGenero(),
				videojuego.getPlataformaOriginal(),
				videojuego.getDescripcion(),
				videojuego.getAnioLanzamiento(),
				videojuego.getEstadoConservacion().getValor(),
				videojuego.getIdEstudio(),
				videojuego.getIdVideojuego());

		jdbcTemplate.update(SQL_ELIMINAR_EDITORAS, videojuego.getIdVideojuego());
		insertarEditoras(videojuego.getIdVideojuego(), videojuego.getIdsEditoras());
	}

	/**
	 * Guarda (o quita, con null) la URL de la imagen del videojuego en Cloudinary.
	 *
	 * @param idVideojuego id del videojuego
	 * @param urlImagen    URL de la imagen, o null para dejarlo sin imagen
	 */
	public void actualizarUrlImagen(long idVideojuego, String urlImagen) {
		jdbcTemplate.update(SQL_ACTUALIZAR_URL_IMAGEN, urlImagen, idVideojuego);
	}

	/**
	 * Elimina el videojuego. Primero borra su relacion con las editoras (COMPED_VID)
	 * y despues la fila de VIDEOJUEGO.
	 *
	 * @param idVideojuego id del videojuego a eliminar
	 */
	public void eliminar(long idVideojuego) {
		jdbcTemplate.update(SQL_ELIMINAR_EDITORAS, idVideojuego);
		jdbcTemplate.update(SQL_ELIMINAR, idVideojuego);
	}

	private void insertarEditoras(long idVideojuego, List<Long> idsEditoras) {
		for (Long idEditora : idsEditoras) {
			jdbcTemplate.update(SQL_INSERTAR_EDITORA, idVideojuego, idEditora);
		}
	}

	// ──────────────────────────────────────────────
	// Conversion de filas
	// ──────────────────────────────────────────────

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
			condiciones.append(" AND v.titulo LIKE ?");
			parametros.add(patronTitulo);
		}
		return condiciones.toString();
	}

	private List<Videojuego> convertirFilasAVideojuegos(List<Map<String, Object>> filas) {
		List<Videojuego> videojuegos = new ArrayList<Videojuego>();
		for (Map<String, Object> fila : filas) {
			videojuegos.add(convertirFilaAVideojuego(fila));
		}
		return videojuegos;
	}

	/**
	 * Convierte una fila (VIDEOJUEGO + editoras agrupadas) en la entity Videojuego.
	 */
	private Videojuego convertirFilaAVideojuego(Map<String, Object> fila) {
		Videojuego videojuego = new Videojuego();
		videojuego.setIdVideojuego(convertirALong(fila.get("id_videojuego")));
		videojuego.setTitulo((String) fila.get("titulo"));
		videojuego.setGenero((String) fila.get("genero"));
		videojuego.setPlataformaOriginal((String) fila.get("plataforma_original"));
		videojuego.setDescripcion((String) fila.get("descripcion"));
		videojuego.setAnioLanzamiento(convertirAnio(fila.get("anio_lanzamiento")));
		videojuego.setEstadoConservacion(convertirEstado(fila.get("estado_conservacion")));
		videojuego.setIdEstudio(convertirALong(fila.get("id_estudio")));
		videojuego.setIdUsuario(convertirALong(fila.get("id_usuario")));
		videojuego.setUrlImagen((String) fila.get("url_imagen"));
		videojuego.setIdsEditoras(convertirIdsEditoras(fila.get("ids_editoras")));
		return videojuego;
	}

	/**
	 * Convierte el texto de GROUP_CONCAT ("2,5") en la lista [2, 5].
	 * Si el juego no tiene editoras, GROUP_CONCAT devuelve NULL y la lista queda vacia.
	 */
	private List<Long> convertirIdsEditoras(Object valor) {
		List<Long> idsEditoras = new ArrayList<Long>();
		if (valor == null) {
			return idsEditoras;
		}

		String[] partes = valor.toString().split(",");
		for (String parte : partes) {
			if (!parte.trim().isEmpty()) {
				idsEditoras.add(Long.valueOf(parte.trim()));
			}
		}
		return idsEditoras;
	}

	private long convertirALong(Object valor) {
		if (valor == null) {
			return 0;
		}
		Number numero = (Number) valor;
		return numero.longValue();
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