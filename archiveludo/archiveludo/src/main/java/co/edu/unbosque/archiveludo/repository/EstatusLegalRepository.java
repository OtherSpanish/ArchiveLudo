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

	/**
	 * Del mas reciente al mas antiguo. Si dos tienen la misma fecha, va primero el de mayor id
	 * (el que se registro despues). Asi el primero de la lista siempre es el vigente.
	 */
	private static final String SQL_BUSCAR_POR_VIDEOJUEGO = "SELECT id_estatus, id_videojuego, id_usuario, "
			+ "estatus, fecha_determinacion, observacion FROM ESTATUS_LEGAL WHERE id_videojuego = ? "
			+ "ORDER BY fecha_determinacion DESC, id_estatus DESC";

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