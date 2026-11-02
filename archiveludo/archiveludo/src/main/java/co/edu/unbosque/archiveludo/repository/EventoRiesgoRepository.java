package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.EventoDeRiesgo;
import co.edu.unbosque.archiveludo.model.TipoEventoRiesgo;

/**
 * Acceso a datos de la tabla EVENTO_DE_RIESGO con SQL escrito a mano (Spring JDBC).
 */
@Repository
public class EventoRiesgoRepository {

	private static final String SQL_SELECCIONAR = "SELECT id_evento, id_videojuego, id_usuario, tipo_evento, "
			+ "fecha_evento, descripcion, fuente FROM EVENTO_DE_RIESGO ";

	/** Del mas reciente al mas antiguo; si dos tienen la misma fecha, primero el de mayor id. */
	private static final String SQL_BUSCAR_POR_VIDEOJUEGO = SQL_SELECCIONAR + "WHERE id_videojuego = ? "
			+ "ORDER BY fecha_evento DESC, id_evento DESC";

	private static final String SQL_BUSCAR_POR_ID = SQL_SELECCIONAR + "WHERE id_evento = ?";

	private static final String SQL_INSERTAR = "INSERT INTO EVENTO_DE_RIESGO (id_videojuego, id_usuario, "
			+ "tipo_evento, fecha_evento, descripcion, fuente) VALUES (?, ?, ?, ?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	/** No se actualizan id_videojuego ni id_usuario: el evento sigue siendo del mismo juego y de quien lo registro. */
	private static final String SQL_ACTUALIZAR = "UPDATE EVENTO_DE_RIESGO SET tipo_evento = ?, fecha_evento = ?, "
			+ "descripcion = ?, fuente = ? WHERE id_evento = ?";

	private static final String SQL_ELIMINAR = "DELETE FROM EVENTO_DE_RIESGO WHERE id_evento = ?";

	private final JdbcTemplate jdbcTemplate;

	public EventoRiesgoRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Busca todos los eventos de riesgo de un videojuego, del mas reciente al mas antiguo.
	 *
	 * @param idVideojuego id del videojuego
	 * @return lista de eventos (vacia si no tiene ninguno)
	 */
	public List<EventoDeRiesgo> buscarPorIdVideojuego(long idVideojuego) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_VIDEOJUEGO, idVideojuego);

		List<EventoDeRiesgo> eventos = new ArrayList<EventoDeRiesgo>();
		for (Map<String, Object> fila : filas) {
			eventos.add(convertirFilaAEvento(fila));
		}
		return eventos;
	}

	/**
	 * @param idEvento id a buscar
	 * @return el evento encontrado, o null si no existe
	 */
	public EventoDeRiesgo buscarPorId(long idEvento) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_ID, idEvento);

		if (filas.isEmpty()) {
			return null;
		}
		return convertirFilaAEvento(filas.get(0));
	}

	/**
	 * Inserta un evento de riesgo y le asigna el id generado.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service).
	 *
	 * @param evento evento a guardar, sin id
	 * @return el mismo evento con su idEvento
	 */
	public EventoDeRiesgo crear(EventoDeRiesgo evento) {
		jdbcTemplate.update(SQL_INSERTAR,
				evento.getIdVideojuego(),
				evento.getIdUsuario(),
				evento.getTipoEvento().getValor(),
				Date.valueOf(evento.getFechaEvento()),
				evento.getDescripcion(),
				evento.getFuente());

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		evento.setIdEvento(idGenerado);
		return evento;
	}

	/**
	 * @param evento evento con los datos nuevos (y su id)
	 */
	public void actualizar(EventoDeRiesgo evento) {
		jdbcTemplate.update(SQL_ACTUALIZAR,
				evento.getTipoEvento().getValor(),
				Date.valueOf(evento.getFechaEvento()),
				evento.getDescripcion(),
				evento.getFuente(),
				evento.getIdEvento());
	}

	/**
	 * @param idEvento id del evento a eliminar
	 */
	public void eliminar(long idEvento) {
		jdbcTemplate.update(SQL_ELIMINAR, idEvento);
	}

	/**
	 * Convierte una fila de la tabla EVENTO_DE_RIESGO (columna -> valor) en la entity EventoDeRiesgo.
	 */
	private EventoDeRiesgo convertirFilaAEvento(Map<String, Object> fila) {
		Number idEvento = (Number) fila.get("id_evento");
		Number idVideojuego = (Number) fila.get("id_videojuego");
		Number idUsuario = (Number) fila.get("id_usuario");

		EventoDeRiesgo evento = new EventoDeRiesgo();
		evento.setIdEvento(idEvento.longValue());
		evento.setIdVideojuego(idVideojuego.longValue());
		evento.setIdUsuario(idUsuario.longValue());
		evento.setTipoEvento(convertirTipoEvento(fila.get("tipo_evento")));
		evento.setFechaEvento(convertirFecha(fila.get("fecha_evento")));
		evento.setDescripcion((String) fila.get("descripcion"));
		evento.setFuente((String) fila.get("fuente"));
		return evento;
	}

	/**
	 * Convierte el texto de la columna tipo_evento al enum, sin importar mayusculas.
	 */
	private TipoEventoRiesgo convertirTipoEvento(Object valor) {
		if (valor == null) {
			return null;
		}
		String texto = valor.toString().trim().toLowerCase();
		return TipoEventoRiesgo.buscarPorValor(texto);
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