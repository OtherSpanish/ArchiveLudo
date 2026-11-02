package co.edu.unbosque.archiveludo.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.archiveludo.dto.EventoDeRiesgoDTO;
import co.edu.unbosque.archiveludo.exceptions.CredencialInvalidaException;
import co.edu.unbosque.archiveludo.exceptions.DatoEventoRiesgoInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.EventoDeRiesgoNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.EventoRiesgoService;

/**
 * Eventos de riesgo de los videojuegos.
 * La bitacora y el registro van por el videojuego (/api/videojuegos/{id}/eventos-riesgo);
 * ver, editar y eliminar un evento, por su propio id (/api/eventos-riesgo/{id}).
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 */
@RestController
@RequestMapping("/api")
public class EventoRiesgoController {

	private static final Logger log = LoggerFactory.getLogger(EventoRiesgoController.class);

	@Autowired
	private EventoRiesgoService eventoDeRiesgoService;

	/**
	 * GET /api/videojuegos/{id}/eventos-riesgo
	 * Respuesta: 200 con los eventos de riesgo del videojuego, del mas reciente al mas antiguo.
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
	 * POST /api/videojuegos/{id}/eventos-riesgo
	 * Cuerpo:    { "tipoEvento": "cierre de servidores", "fechaEvento": "2024-02-01",
	 *              "descripcion": "La editora apago los servidores en linea", "fuente": "Comunicado oficial" }
	 * Respuesta: 201 con el evento creado. El usuario que lo registra sale del token.
	 */
	@PostMapping("/videojuegos/{id}/eventos-riesgo")
	public ResponseEntity<?> crear(@PathVariable String id, @RequestBody EventoDeRiesgoDTO eventoDTO,
			@AuthenticationPrincipal UserDetails usuarioAutenticado) {
		try {
			EventoDeRiesgoDTO eventoCreado = eventoDeRiesgoService.crear(id, eventoDTO,
					usuarioAutenticado.getUsername());
			return new ResponseEntity<EventoDeRiesgoDTO>(eventoCreado, HttpStatus.CREATED);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoEventoRiesgoInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CredencialInvalidaException e) {
			return crearRespuestaError(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al registrar un evento de riesgo del videojuego " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/eventos-riesgo/{id}
	 * Respuesta: 200 con el evento de riesgo.
	 */
	@GetMapping("/eventos-riesgo/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			EventoDeRiesgoDTO evento = eventoDeRiesgoService.buscarPorId(id);
			return new ResponseEntity<EventoDeRiesgoDTO>(evento, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EventoDeRiesgoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar el evento de riesgo " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/eventos-riesgo/{id}
	 * Cuerpo:    los mismos campos que el POST.
	 * Respuesta: 200 con el evento actualizado.
	 */
	@PutMapping("/eventos-riesgo/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody EventoDeRiesgoDTO eventoDTO) {
		try {
			EventoDeRiesgoDTO eventoActualizado = eventoDeRiesgoService.actualizar(id, eventoDTO);
			return new ResponseEntity<EventoDeRiesgoDTO>(eventoActualizado, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EventoDeRiesgoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoEventoRiesgoInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar el evento de riesgo " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/eventos-riesgo/{id}
	 * Respuesta: 204 sin cuerpo si se elimino.
	 */
	@DeleteMapping("/eventos-riesgo/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			eventoDeRiesgoService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EventoDeRiesgoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar el evento de riesgo " + id, e);
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