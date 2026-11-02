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
 * Acceso a datos de la tabla EVENTO_DE_RIESGO con SQL(Spring JDBC).
 */
@Repository
public class EventoDeRiesgoRepository {

	/** Del mas reciente al mas antiguo; si dos tienen la misma fecha, primero el de mayor id. */
	private static final String SQL_BUSCAR_POR_VIDEOJUEGO = "SELECT id_evento, id_videojuego, id_usuario, "
			+ "tipo_evento, fecha_evento, descripcion, fuente FROM EVENTO_DE_RIESGO WHERE id_videojuego = ? "
			+ "ORDER BY fecha_evento DESC, id_evento DESC";

	private final JdbcTemplate jdbcTemplate;

	public EventoDeRiesgoRepository(JdbcTemplate jdbcTemplate) {
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