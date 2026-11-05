package co.edu.unbosque.archiveludo.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import co.edu.unbosque.archiveludo.dto.ReporteDTO;
import co.edu.unbosque.archiveludo.entity.Videojuego;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.model.TipoReporte;
import co.edu.unbosque.archiveludo.repository.ReporteRepository;
import co.edu.unbosque.archiveludo.repository.VideojuegoRepository;

/**
 * Arma los reportes como tablas de texto (titulo, columnas y filas) para que el front solo las pinte.
 * Las fechas salen en dd/MM/yyyy, los valores en texto legible y las celdas vacias como "".
 * Los filtros son opcionales; si un filtro no aplica a un tipo de reporte, se ignora.
 */
@Service
public class ReporteService {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final ReporteRepository reporteRepository;
	private final VideojuegoRepository videojuegoRepository;

	public ReporteService(ReporteRepository reporteRepository, VideojuegoRepository videojuegoRepository) {
		this.reporteRepository = reporteRepository;
		this.videojuegoRepository = videojuegoRepository;
	}

	/**
	 * @param tipoTexto         tipo de reporte tal como llego en la URL
	 * @param idVideojuegoTexto id del videojuego a filtrar, o null/vacio para todos
	 * @param desdeTexto        fecha minima (AAAA-MM-DD), o null/vacio
	 * @param hastaTexto        fecha maxima (AAAA-MM-DD), o null/vacio
	 * @return la tabla del reporte
	 * @throws DatosReporteInvalidosException  si el tipo no existe, una fecha esta mal escrita o desde > hasta
	 * @throws IdInvalidoException             si el id del videojuego no es un numero mayor que cero
	 * @throws VideojuegoNoEncontradoException si el videojuego no existe
	 */
	public ReporteDTO generar(String tipoTexto, String idVideojuegoTexto, String desdeTexto, String hastaTexto) {
		TipoReporte tipo = LanzadorException.convertirTipoReporte(tipoTexto);
		Long idVideojuego = convertirFiltroVideojuego(idVideojuegoTexto);
		LocalDate desde = LanzadorException.convertirFechaReporte(desdeTexto, "desde");
		LocalDate hasta = LanzadorException.convertirFechaReporte(hastaTexto, "hasta");
		LanzadorException.verificarRangoFechasReporte(desde, hasta);

		switch (tipo) {
		case ESTATUS:
			return armarReporteEstatus(reporteRepository.buscarEstatus(idVideojuego, desde, hasta));
		case EVENTOS:
			return armarReporteEventos(reporteRepository.buscarEventos(idVideojuego, desde, hasta));
		default:
			return armarReporteCopias(reporteRepository.buscarCopias(idVideojuego, desde, hasta));
		}
	}

	// ──────────────────────────────────────────────
	// Armado de cada reporte
	// ──────────────────────────────────────────────

	private ReporteDTO armarReporteEstatus(List<Map<String, Object>> filasBD) {
		List<String> columnas = Arrays.asList("Videojuego", "Estatus", "Fecha de determinación", "Observación",
				"Registrado por", "Vigente");

		List<List<String>> filas = new ArrayList<List<String>>();
		for (Map<String, Object> fila : filasBD) {
			List<String> celdas = new ArrayList<String>();
			celdas.add(texto(fila.get("titulo")));
			celdas.add(textoLegible(fila.get("estatus")));
			celdas.add(fecha(fila.get("fecha_determinacion")));
			celdas.add(texto(fila.get("observacion")));
			celdas.add(nombreUsuario(fila));
			celdas.add(siNo(fila.get("es_vigente")));
			filas.add(celdas);
		}
		return new ReporteDTO(TipoReporte.ESTATUS.getTitulo(), columnas, filas);
	}

	private ReporteDTO armarReporteEventos(List<Map<String, Object>> filasBD) {
		List<String> columnas = Arrays.asList("Fecha", "Videojuego", "Tipo", "Descripción", "Fuente",
				"Registrado por");

		List<List<String>> filas = new ArrayList<List<String>>();
		for (Map<String, Object> fila : filasBD) {
			List<String> celdas = new ArrayList<String>();
			celdas.add(fecha(fila.get("fecha_evento")));
			celdas.add(texto(fila.get("titulo")));
			celdas.add(textoLegible(fila.get("tipo_evento")));
			celdas.add(texto(fila.get("descripcion")));
			celdas.add(texto(fila.get("fuente")));
			celdas.add(nombreUsuario(fila));
			filas.add(celdas);
		}
		return new ReporteDTO(TipoReporte.EVENTOS.getTitulo(), columnas, filas);
	}

	private ReporteDTO armarReporteCopias(List<Map<String, Object>> filasBD) {
		List<String> columnas = Arrays.asList("Copia", "Videojuego", "Tipo", "Estado de integridad",
				"Fecha de registro", "Detalle", "Institución actual");

		List<List<String>> filas = new ArrayList<List<String>>();
		for (Map<String, Object> fila : filasBD) {
			List<String> celdas = new ArrayList<String>();
			celdas.add(texto(fila.get("id_copia")));
			celdas.add(texto(fila.get("titulo")));
			celdas.add(tipoCopiaLegible(fila.get("tipo_copia")));
			celdas.add(textoLegible(fila.get("estado_integridad")));
			celdas.add(fecha(fila.get("fecha_registro")));
			celdas.add(detalleCopia(fila));
			celdas.add(textoOPorDefecto(fila.get("institucion_actual"), "Sin custodia"));
			filas.add(celdas);
		}
		return new ReporteDTO(TipoReporte.COPIAS.getTitulo(), columnas, filas);
	}

	// ──────────────────────────────────────────────
	// Filtros
	// ──────────────────────────────────────────────

	/**
	 * El filtro de videojuego es opcional; si llega, debe ser un id valido de un videojuego existente.
	 */
	private Long convertirFiltroVideojuego(String idVideojuegoTexto) {
		if (idVideojuegoTexto == null || idVideojuegoTexto.trim().isEmpty()) {
			return null;
		}
		long idVideojuego = LanzadorException.convertirId(idVideojuegoTexto, "videojuego");

		Videojuego videojuego = videojuegoRepository.buscarPorId(idVideojuego);
		LanzadorException.verificarVideojuegoExiste(videojuego, idVideojuego);
		return idVideojuego;
	}

	// ──────────────────────────────────────────────
	// Formato de las celdas
	// ──────────────────────────────────────────────

	private String texto(Object valor) {
		if (valor == null) {
			return "";
		}
		return valor.toString().trim();
	}

	private String textoOPorDefecto(Object valor, String porDefecto) {
		String texto = texto(valor);
		if (texto.isEmpty()) {
			return porDefecto;
		}
		return texto;
	}

	/**
	 * Valor guardado en la BD a texto para mostrar: "_" pasa a espacio y la primera letra a mayuscula
	 * ("en_riesgo" -> "En riesgo", "dañada" -> "Dañada").
	 */
	private String textoLegible(Object valor) {
		String texto = texto(valor).replace('_', ' ');
		if (texto.isEmpty()) {
			return "";
		}
		return texto.substring(0, 1).toUpperCase() + texto.substring(1);
	}

	private String tipoCopiaLegible(Object valor) {
		String texto = texto(valor).toLowerCase();
		if (texto.equals("fisica")) {
			return "Física";
		}
		if (texto.equals("digital")) {
			return "Digital";
		}
		return textoLegible(valor);
	}

	/**
	 * Fisica: "soporte · ubicacion". Digital: "formato · tamanio".
	 */
	private String detalleCopia(Map<String, Object> fila) {
		String tipo = texto(fila.get("tipo_copia")).toLowerCase();
		if (tipo.equals("fisica")) {
			return unirConPunto(texto(fila.get("tipo_soporte")), texto(fila.get("ubicacion_fisica")));
		}
		return unirConPunto(texto(fila.get("formato_archivo")), texto(fila.get("tamanio")));
	}

	private String unirConPunto(String primero, String segundo) {
		if (primero.isEmpty()) {
			return segundo;
		}
		if (segundo.isEmpty()) {
			return primero;
		}
		return primero + " · " + segundo;
	}

	/**
	 * Fecha del driver de MySQL (java.sql.Date o LocalDate) a dd/MM/yyyy. Sin fecha, "".
	 */
	private String fecha(Object valor) {
		if (valor == null) {
			return "";
		}
		LocalDate fecha;
		if (valor instanceof Date) {
			fecha = ((Date) valor).toLocalDate();
		} else {
			fecha = (LocalDate) valor;
		}
		return fecha.format(FORMATO_FECHA);
	}

	/**
	 * MySQL devuelve el resultado de una comparacion como 1 o 0.
	 */
	private String siNo(Object valor) {
		if (valor instanceof Number && ((Number) valor).intValue() == 1) {
			return "Sí";
		}
		if (valor instanceof Boolean && ((Boolean) valor)) {
			return "Sí";
		}
		return "No";
	}

	private String nombreUsuario(Map<String, Object> fila) {
		return unirConEspacio(texto(fila.get("primer_nombre")), texto(fila.get("primer_apellido")));
	}

	private String unirConEspacio(String primero, String segundo) {
		if (primero.isEmpty()) {
			return segundo;
		}
		if (segundo.isEmpty()) {
			return primero;
		}
		return primero + " " + segundo;
	}

}