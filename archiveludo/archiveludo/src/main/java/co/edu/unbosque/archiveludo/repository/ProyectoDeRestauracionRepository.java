package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.ProyectoDeRestauracion;
import co.edu.unbosque.archiveludo.model.EstadoProyecto;

/**
 * Acceso a datos de la tabla PROYECTO_DE_RESTAURACION con SQL escrito a mano (Spring JDBC).
 * Sus participaciones las maneja ParticipacionRepository.
 * El enlace del informe final (url_informe) se lee y se escribe con sus propios metodos.
 */
@Repository
public class ProyectoDeRestauracionRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_proyecto, id_videojuego, id_usuario, nombre, "
			+ "descripcion, herramienta_emulador, estado, fecha_inicio, fecha_fin FROM PROYECTO_DE_RESTAURACION ";

	/** Del mas reciente al mas antiguo; si dos empiezan el mismo dia, primero el de mayor id. */
	private static final String ORDEN = "ORDER BY fecha_inicio DESC, id_proyecto DESC";

	private static final String SQL_BUSCAR_TODOS = SQL_SELECCIONAR + ORDEN;

	private static final String SQL_BUSCAR_POR_ESTADO = SQL_SELECCIONAR + "WHERE estado = ? " + ORDEN;

	private static final String SQL_BUSCAR_POR_VIDEOJUEGO = SQL_SELECCIONAR + "WHERE id_videojuego = ? " + ORDEN;

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_proyecto = ?";

	private static final String SQL_INSERTAR = "INSERT INTO PROYECTO_DE_RESTAURACION (id_videojuego, id_usuario, "
			+ "nombre, descripcion, herramienta_emulador, estado, fecha_inicio, fecha_fin) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	/** No se actualizan id_videojuego ni id_usuario: el proyecto sigue siendo del mismo juego y de quien lo creo. */
	private static final String SQL_ACTUALIZAR = "UPDATE PROYECTO_DE_RESTAURACION SET nombre = ?, descripcion = ?, "
			+ "herramienta_emulador = ?, estado = ?, fecha_inicio = ?, fecha_fin = ? WHERE id_proyecto = ?";

	private static final String SQL_BUSCAR_URL_INFORME = "SELECT url_informe FROM PROYECTO_DE_RESTAURACION "
			+ "WHERE id_proyecto = ?";

	private static final String SQL_ACTUALIZAR_URL_INFORME = "UPDATE PROYECTO_DE_RESTAURACION SET url_informe = ? "
			+ "WHERE id_proyecto = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM PROYECTO_DE_RESTAURACION WHERE id_proyecto = ?";

	private final JdbcTemplate jdbcTemplate;

	public ProyectoDeRestauracionRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	/**
	 * @return todos los proyectos, del mas reciente al mas antiguo
	 */
	public List<ProyectoDeRestauracion> buscarTodos() {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_TODOS);
		return convertirFilas(filas);
	}

	/**
	 * @param estado estado a filtrar
	 * @return los proyectos con ese estado, del mas reciente al mas antiguo (vacio si no hay)
	 */
	public List<ProyectoDeRestauracion> buscarPorEstado(EstadoProyecto estado) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ESTADO, estado.getValor());
		return convertirFilas(filas);
	}

	/**
	 * @param idVideojuego id del videojuego
	 * @return los proyectos del videojuego, del mas reciente al mas antiguo (vacio si no tiene)
	 */
	public List<ProyectoDeRestauracion> buscarPorIdVideojuego(long idVideojuego) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_VIDEOJUEGO, idVideojuego);
		return convertirFilas(filas);
	}

	/**
	 * @param idProyecto id a buscar
	 * @return el proyecto encontrado, o null si no existe
	 */
	public ProyectoDeRestauracion buscarPorId(long idProyecto) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idProyecto);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAProyecto(filas.get(0));
	}

	/**
	 * @param idProyecto id del proyecto
	 * @return el enlace del PDF del informe final, o null si el proyecto no tiene informe (o no existe)
	 */
	public String buscarUrlInforme(long idProyecto) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_URL_INFORME, idProyecto);

		if (filas.isEmpty()) {
			return null;
		}
		Object valor = filas.get(0).get("url_informe");
		if (valor == null || valor.toString().trim().isEmpty()) {
			return null;
		}
		return valor.toString().trim();
	}


	/**
	 * Inserta un proyecto y le asigna el id generado.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service).
	 *
	 * @param proyecto proyecto a guardar, sin id
	 * @return el mismo proyecto con su idProyecto
	 */
	public ProyectoDeRestauracion crear(ProyectoDeRestauracion proyecto) {
		jdbcTemplate.update(SQL_INSERTAR,
				proyecto.getIdVideojuego(),
				proyecto.getIdUsuario(),
				proyecto.getNombre(),
				proyecto.getDescripcion(),
				proyecto.getHerramientaEmulador(),
				proyecto.getEstado().getValor(),
				Date.valueOf(proyecto.getFechaInicio()),
				convertirAFechaSql(proyecto.getFechaFin()));

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		proyecto.setIdProyecto(idGenerado);
		return proyecto;
	}

	/**
	 * @param proyecto proyecto con los datos nuevos (y su id)
	 */
	public void actualizar(ProyectoDeRestauracion proyecto) {
		jdbcTemplate.update(SQL_ACTUALIZAR,
				proyecto.getNombre(),
				proyecto.getDescripcion(),
				proyecto.getHerramientaEmulador(),
				proyecto.getEstado().getValor(),
				Date.valueOf(proyecto.getFechaInicio()),
				convertirAFechaSql(proyecto.getFechaFin()),
				proyecto.getIdProyecto());
	}

	/**
	 * Guarda (o quita, con null) el enlace del PDF del informe final en Cloudinary.
	 *
	 * @param idProyecto id del proyecto
	 * @param urlInforme enlace del PDF, o null para dejar el proyecto sin informe
	 */
	public void actualizarUrlInforme(long idProyecto, String urlInforme) {
		jdbcTemplate.update(SQL_ACTUALIZAR_URL_INFORME, urlInforme, idProyecto);
	}

	/**
	 * Elimina el proyecto. Sus participaciones deben borrarse antes (FK de PARTICIPACION).
	 *
	 * @param idProyecto id del proyecto a eliminar
	 */
	public void eliminar(long idProyecto) {
		jdbcTemplate.update(SQL_ELIMINAR, idProyecto);
	}


	private List<ProyectoDeRestauracion> convertirFilas(List<Map<String, Object>> filas) {
		List<ProyectoDeRestauracion> proyectos = new ArrayList<ProyectoDeRestauracion>();
		for (Map<String, Object> fila : filas) {
			proyectos.add(convertirFilaAProyecto(fila));
		}
		return proyectos;
	}

	/**
	 * Convierte una fila de la tabla PROYECTO_DE_RESTAURACION (columna -> valor) en la entity.
	 */
	private ProyectoDeRestauracion convertirFilaAProyecto(Map<String, Object> fila) {
		Number idProyecto = (Number) fila.get("id_proyecto");
		Number idVideojuego = (Number) fila.get("id_videojuego");
		Number idUsuario = (Number) fila.get("id_usuario");

		ProyectoDeRestauracion proyecto = new ProyectoDeRestauracion();
		proyecto.setIdProyecto(idProyecto.longValue());
		proyecto.setIdVideojuego(idVideojuego.longValue());
		proyecto.setIdUsuario(idUsuario.longValue());
		proyecto.setNombre((String) fila.get("nombre"));
		proyecto.setDescripcion((String) fila.get("descripcion"));
		proyecto.setHerramientaEmulador((String) fila.get("herramienta_emulador"));
		proyecto.setEstado(convertirEstado(fila.get("estado")));
		proyecto.setFechaInicio(convertirFecha(fila.get("fecha_inicio")));
		proyecto.setFechaFin(convertirFecha(fila.get("fecha_fin")));
		return proyecto;
	}

	private EstadoProyecto convertirEstado(Object valor) {
		if (valor == null) {
			return null;
		}
		String texto = valor.toString().trim().toLowerCase();
		return EstadoProyecto.buscarPorValor(texto);
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