package co.edu.unbosque.archiveludo.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.archiveludo.entity.Copia;
import co.edu.unbosque.archiveludo.entity.CopiaDigital;
import co.edu.unbosque.archiveludo.entity.CopiaFisica;
import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.model.TipoCopia;

/**
 * Acceso a datos de COPIA y sus subtipos COPIA_FISICA y COPIA_DIGITAL con SQL escrito a mano.
 * Las copias se leen en una sola consulta: LEFT JOIN a las dos tablas hijas.
 * Para una copia fisica, las columnas de COPIA_DIGITAL llegan en NULL (y al reves).
 */
@Repository
public class CopiaRepository {

	private static final String SQL_BUSCAR_POR_VIDEOJUEGO = "SELECT c.id_copia, c.id_videojuego, c.id_usuario, "
			+ "c.tipo_copia, c.estado_integridad, c.fecha_registro, "
			+ "f.numero_serie, f.ubicacion_fisica, f.tipo_soporte, "
			+ "d.formato_archivo, d.tamanio, d.hash_verificacion "
			+ "FROM COPIA c "
			+ "LEFT JOIN COPIA_FISICA f ON f.id_copia = c.id_copia "
			+ "LEFT JOIN COPIA_DIGITAL d ON d.id_copia = c.id_copia "
			+ "WHERE c.id_videojuego = ? "
			+ "ORDER BY c.fecha_registro DESC, c.id_copia DESC";

	private static final String SQL_INSERTAR_COPIA = "INSERT INTO COPIA (id_videojuego, id_usuario, tipo_copia, "
			+ "estado_integridad, fecha_registro) VALUES (?, ?, ?, ?, ?)";

	/** Devuelve el id AUTO_INCREMENT que genero el ultimo INSERT en esta misma conexion. */
	private static final String SQL_ULTIMO_ID_GENERADO = "SELECT LAST_INSERT_ID()";

	private static final String SQL_INSERTAR_COPIA_FISICA = "INSERT INTO COPIA_FISICA (id_copia, numero_serie, "
			+ "ubicacion_fisica, tipo_soporte) VALUES (?, ?, ?, ?)";

	private static final String SQL_INSERTAR_COPIA_DIGITAL = "INSERT INTO COPIA_DIGITAL (id_copia, "
			+ "formato_archivo, tamanio, hash_verificacion) VALUES (?, ?, ?, ?)";

	private final JdbcTemplate jdbcTemplate;

	public CopiaRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Busca todas las copias de un videojuego (fisicas y digitales), de la mas reciente a la mas antigua.
	 *
	 * @param idVideojuego id del videojuego
	 * @return lista de copias; cada una es una CopiaFisica o una CopiaDigital
	 */
	public List<Copia> buscarPorIdVideojuego(long idVideojuego) {
		List<Map<String, Object>> filas = jdbcTemplate.queryForList(SQL_BUSCAR_POR_VIDEOJUEGO, idVideojuego);

		List<Copia> copias = new ArrayList<Copia>();
		for (Map<String, Object> fila : filas) {
			copias.add(convertirFilaACopia(fila));
		}
		return copias;
	}

	/**
	 * Inserta una copia fisica: primero en COPIA y luego en COPIA_FISICA con el mismo id.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service) para que
	 * los dos INSERT y el LAST_INSERT_ID() usen la misma conexion, y para que si uno falla
	 * se deshaga el otro.
	 *
	 * @param copiaFisica copia a guardar, sin id
	 * @return la misma copia con su idCopia
	 */
	public CopiaFisica crearCopiaFisica(CopiaFisica copiaFisica) {
		long idCopia = insertarDatosComunes(copiaFisica);

		jdbcTemplate.update(SQL_INSERTAR_COPIA_FISICA,
				idCopia,
				copiaFisica.getNumeroSerie(),
				copiaFisica.getUbicacionFisica(),
				copiaFisica.getTipoSoporte());

		copiaFisica.setIdCopia(idCopia);
		return copiaFisica;
	}

	/**
	 * Inserta una copia digital: primero en COPIA y luego en COPIA_DIGITAL con el mismo id.
	 * Debe llamarse dentro de una transaccion (@Transactional en el service).
	 *
	 * @param copiaDigital copia a guardar, sin id
	 * @return la misma copia con su idCopia
	 */
	public CopiaDigital crearCopiaDigital(CopiaDigital copiaDigital) {
		long idCopia = insertarDatosComunes(copiaDigital);

		jdbcTemplate.update(SQL_INSERTAR_COPIA_DIGITAL,
				idCopia,
				copiaDigital.getFormatoArchivo(),
				copiaDigital.getTamanio(),
				copiaDigital.getHashVerificacion());

		copiaDigital.setIdCopia(idCopia);
		return copiaDigital;
	}

	/**
	 * Inserta la fila en COPIA (los datos comunes) y devuelve el id que genero MySQL.
	 */
	private long insertarDatosComunes(Copia copia) {
		jdbcTemplate.update(SQL_INSERTAR_COPIA,
				copia.getIdVideojuego(),
				copia.getIdUsuario(),
				copia.getTipoCopia().getValor(),
				copia.getEstadoIntegridad().getValor(),
				Date.valueOf(copia.getFechaRegistro()));

		Long idGenerado = jdbcTemplate.queryForObject(SQL_ULTIMO_ID_GENERADO, Long.class);
		return idGenerado;
	}

	/**
	 * Segun la columna tipo_copia, crea una CopiaFisica o una CopiaDigital
	 * y le llena los datos comunes y los de su subtipo.
	 */
	private Copia convertirFilaACopia(Map<String, Object> fila) {
		TipoCopia tipoCopia = convertirTipoCopia(fila.get("tipo_copia"));

		Copia copia;
		if (tipoCopia == TipoCopia.FISICA) {
			copia = convertirFilaACopiaFisica(fila);
		} else {
			copia = convertirFilaACopiaDigital(fila);
		}

		llenarDatosComunes(copia, fila);
		return copia;
	}

	private CopiaFisica convertirFilaACopiaFisica(Map<String, Object> fila) {
		CopiaFisica copiaFisica = new CopiaFisica();
		copiaFisica.setNumeroSerie((String) fila.get("numero_serie"));
		copiaFisica.setUbicacionFisica((String) fila.get("ubicacion_fisica"));
		copiaFisica.setTipoSoporte((String) fila.get("tipo_soporte"));
		return copiaFisica;
	}

	private CopiaDigital convertirFilaACopiaDigital(Map<String, Object> fila) {
		CopiaDigital copiaDigital = new CopiaDigital();
		copiaDigital.setFormatoArchivo((String) fila.get("formato_archivo"));
		copiaDigital.setTamanio(convertirALong(fila.get("tamanio")));
		copiaDigital.setHashVerificacion((String) fila.get("hash_verificacion"));
		return copiaDigital;
	}

	/**
	 * Llena las columnas de COPIA, que son iguales para los dos subtipos.
	 */
	private void llenarDatosComunes(Copia copia, Map<String, Object> fila) {
		copia.setIdCopia(convertirALong(fila.get("id_copia")));
		copia.setIdVideojuego(convertirALong(fila.get("id_videojuego")));
		copia.setIdUsuario(convertirALong(fila.get("id_usuario")));
		copia.setEstadoIntegridad(convertirEstadoIntegridad(fila.get("estado_integridad")));
		copia.setFechaRegistro(convertirFecha(fila.get("fecha_registro")));
	}

	/**
	 * MySQL devuelve INT como Integer y BIGINT como Long; aqui ambos pasan a long.
	 */
	private long convertirALong(Object valor) {
		if (valor == null) {
			return 0;
		}
		Number numero = (Number) valor;
		return numero.longValue();
	}

	private TipoCopia convertirTipoCopia(Object valor) {
		if (valor == null) {
			return null;
		}
		String texto = valor.toString().trim().toLowerCase();
		return TipoCopia.buscarPorValor(texto);
	}

	private EstadoIntegridad convertirEstadoIntegridad(Object valor) {
		if (valor == null) {
			return null;
		}
		String texto = valor.toString().trim().toLowerCase();
		return EstadoIntegridad.buscarPorValor(texto);
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