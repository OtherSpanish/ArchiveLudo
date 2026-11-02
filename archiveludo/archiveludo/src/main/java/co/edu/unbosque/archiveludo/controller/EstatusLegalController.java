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

import co.edu.unbosque.archiveludo.dto.EstatusLegalDTO;
import co.edu.unbosque.archiveludo.exceptions.CredencialInvalidaException;
import co.edu.unbosque.archiveludo.exceptions.DatoEstatusLegalInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.EstatusLegalNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.EstatusLegalService;

/**
 * Estatus legal de los videojuegos.
 * El historial y el registro van por el videojuego (/api/videojuegos/{id}/estatus-legal);
 * ver, editar y eliminar un estatus, por su propio id (/api/estatus-legal/{id}).
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 */
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
	 * POST /api/videojuegos/{id}/estatus-legal
	 * Cuerpo:    { "estatus": "dominio publico", "fechaDeterminacion": "2024-01-15",
	 *              "observacion": "Liberado por los herederos" }
	 * Respuesta: 201 con el estatus creado. El usuario que lo registra sale del token.
	 */
	@PostMapping("/videojuegos/{id}/estatus-legal")
	public ResponseEntity<?> crear(@PathVariable String id, @RequestBody EstatusLegalDTO estatusLegalDTO,
			@AuthenticationPrincipal UserDetails usuarioAutenticado) {
		try {
			EstatusLegalDTO estatusCreado = estatusLegalService.crear(id, estatusLegalDTO,
					usuarioAutenticado.getUsername());
			return new ResponseEntity<EstatusLegalDTO>(estatusCreado, HttpStatus.CREATED);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoEstatusLegalInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CredencialInvalidaException e) {
			return crearRespuestaError(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al registrar el estatus legal del videojuego " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/estatus-legal/{id}
	 * Respuesta: 200 con el estatus, indicando si es el vigente de su videojuego.
	 */
	@GetMapping("/estatus-legal/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			EstatusLegalDTO estatusLegal = estatusLegalService.buscarPorId(id);
			return new ResponseEntity<EstatusLegalDTO>(estatusLegal, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EstatusLegalNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar el estatus legal " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/estatus-legal/{id}
	 * Cuerpo:    los mismos campos que el POST.
	 * Respuesta: 200 con el estatus actualizado.
	 */
	@PutMapping("/estatus-legal/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody EstatusLegalDTO estatusLegalDTO) {
		try {
			EstatusLegalDTO estatusActualizado = estatusLegalService.actualizar(id, estatusLegalDTO);
			return new ResponseEntity<EstatusLegalDTO>(estatusActualizado, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EstatusLegalNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoEstatusLegalInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar el estatus legal " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/estatus-legal/{id}
	 * Respuesta: 204 sin cuerpo si se elimino.
	 */
	@DeleteMapping("/estatus-legal/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			estatusLegalService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EstatusLegalNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar el estatus legal " + id, e);
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