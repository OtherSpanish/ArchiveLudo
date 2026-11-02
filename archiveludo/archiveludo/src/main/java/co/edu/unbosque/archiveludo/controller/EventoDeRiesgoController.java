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

import co.edu.unbosque.archiveludo.dto.EventoDeRiesgoDTO;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.EventoDeRiesgoService;

@RestController
@RequestMapping("/api")
public class EventoDeRiesgoController {

	private static final Logger log = LoggerFactory.getLogger(EventoDeRiesgoController.class);

	@Autowired
	private EventoDeRiesgoService eventoDeRiesgoService;

	/**
	 * GET /api/videojuegos/{id}/eventos-riesgo
	 * Respuesta: 200 con los eventos de riesgo del videojuego, del mas reciente al mas antiguo.
	 * Requiere token de cualquier rol.
	 */
	@GetMapping("/videojuegos/{id}/eventos-riesgo")
	public ResponseEntity<?> buscarPorVideojuego(@PathVariable String id) {
		try {
			List<EventoDeRiesgoDTO> eventos = eventoDeRiesgoService.buscarPorVideojuego(id);
			return new ResponseEntity<List<EventoDeRiesgoDTO>>(eventos, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar los eventos de riesgo del videojuego " + id, e);
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