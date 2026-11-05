package co.edu.unbosque.archiveludo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.archiveludo.dto.ReporteDTO;
import co.edu.unbosque.archiveludo.exceptions.DatosReporteInvalidosException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.ReporteService;

/**
 * Reportes del archivo como tablas listas para mostrar o imprimir.
 * Leer: cualquier rol.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

	private static final Logger log = LoggerFactory.getLogger(ReporteController.class);

	@Autowired
	private ReporteService reporteService;

	/**
	 * GET /api/reportes/{tipo}?idVideojuego=&desde=AAAA-MM-DD&hasta=AAAA-MM-DD
	 * Todos los filtros son opcionales.
	 * Respuesta: 200 con { titulo, columnas, filas }.
	 *            400 si el tipo no existe, una fecha esta mal escrita o desde es posterior a hasta.
	 *            404 si el videojuego del filtro no existe.
	 */
	@GetMapping("/{tipo}")
	public ResponseEntity<?> generar(@PathVariable String tipo,
			@RequestParam(name = "idVideojuego", required = false) String idVideojuego,
			@RequestParam(name = "desde", required = false) String desde,
			@RequestParam(name = "hasta", required = false) String hasta) {
		try {
			ReporteDTO reporte = reporteService.generar(tipo, idVideojuego, desde, hasta);
			return new ResponseEntity<ReporteDTO>(reporte, HttpStatus.OK);
		} catch (DatosReporteInvalidosException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al generar el reporte " + tipo, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * Arma el cuerpo { estado, mensaje } que espera el front.
	 */
	private ResponseEntity<Map<String, Object>> crearRespuestaError(HttpStatus estado, String mensaje) {
		Map<String, Object> cuerpo = new LinkedHashMap<String, Object>();
		cuerpo.put("estado", estado.value());
		cuerpo.put("mensaje", mensaje);
		return new ResponseEntity<Map<String, Object>>(cuerpo, estado);
	}

}