package co.edu.unbosque.archiveludo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.archiveludo.dto.ResumenInicioDTO;
import co.edu.unbosque.archiveludo.service.InicioService;

/**
 * Pantalla de Inicio: todo su contenido en una sola consulta.
 * Leer: cualquier rol.
 */
@RestController
@RequestMapping("/api/inicio")
public class InicioController {

	private static final Logger log = LoggerFactory.getLogger(InicioController.class);

	@Autowired
	private InicioService inicioService;

	/**
	 * GET /api/inicio/resumen
	 * Respuesta: 200 con contadores, prioridad de intervencion y listas de maximo 4 videojuegos.
	 */
	@GetMapping("/resumen")
	public ResponseEntity<?> generarResumen() {
		try {
			ResumenInicioDTO resumen = inicioService.generarResumen();
			return new ResponseEntity<ResumenInicioDTO>(resumen, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error inesperado al generar el resumen de inicio", e);
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