package co.edu.unbosque.archiveludo.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.archiveludo.dto.EstatusLegalDTO;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.EstatusLegalService;

@RestController
@RequestMapping("/api")
public class EstatusLegalController {

	private static final Logger log = LoggerFactory.getLogger(EstatusLegalController.class);

	@Autowired
	private EstatusLegalService estatusLegalService;

	/**
	 * GET /api/videojuegos/{id}/estatus-legal
	 * Respuesta: 200 con el historial de estatus legal, del mas reciente al mas antiguo.
	 *            Solo el primero trae "vigente": true.
	 * Requiere token de cualquier rol.
	 */
	@GetMapping("/videojuegos/{id}/estatus-legal")
	public ResponseEntity<?> buscarPorVideojuego(@PathVariable String id) {
		try {
			List<EstatusLegalDTO> estatusLegales = estatusLegalService.buscarPorVideojuego(id);
			return new ResponseEntity<List<EstatusLegalDTO>>(estatusLegales, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar el estatus legal del videojuego " + id, e);
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