package co.edu.unbosque.archiveludo.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.archiveludo.dto.ExpertoDTO;
import co.edu.unbosque.archiveludo.exceptions.DatoExpertoInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.ExpertoNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.service.ExpertoService;

/**
 * Expertos que participan en los proyectos de restauracion.
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 */
@RestController
@RequestMapping("/api/expertos")
public class ExpertoController {

	private static final Logger log = LoggerFactory.getLogger(ExpertoController.class);

	@Autowired
	private ExpertoService expertoService;

	/**
	 * GET /api/expertos
	 * Respuesta: 200 con todos los expertos, ordenados por apellido y nombre.
	 */
	@GetMapping
	public ResponseEntity<?> buscarTodos() {
		try {
			List<ExpertoDTO> expertos = expertoService.buscarTodos();
			return new ResponseEntity<List<ExpertoDTO>>(expertos, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error inesperado al buscar los expertos", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/expertos/{id}
	 * Respuesta: 200 con el experto.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			ExpertoDTO experto = expertoService.buscarPorId(id);
			return new ResponseEntity<ExpertoDTO>(experto, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ExpertoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar el experto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/expertos
	 * Cuerpo:    { "primerNombreEx": "Laura", "segundoNombreEx": "Sofia", "primerApellidoEx": "Mejia",
	 *              "segundoApellidoEx": "Rios", "especialidad": "Conservacion de cintas magneticas",
	 *              "afiliacion": "Museo Nacional del Videojuego", "correo": "laura.mejia@expertos-ludo.org" }
	 * Respuesta: 201 con el experto creado.
	 */
	@PostMapping
	public ResponseEntity<?> crear(@RequestBody ExpertoDTO expertoDTO) {
		try {
			ExpertoDTO expertoCreado = expertoService.crear(expertoDTO);
			return new ResponseEntity<ExpertoDTO>(expertoCreado, HttpStatus.CREATED);
		} catch (DatoExpertoInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al crear un experto", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/expertos/{id}
	 * Cuerpo:    los mismos campos que el POST.
	 * Respuesta: 200 con el experto actualizado.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody ExpertoDTO expertoDTO) {
		try {
			ExpertoDTO expertoActualizado = expertoService.actualizar(id, expertoDTO);
			return new ResponseEntity<ExpertoDTO>(expertoActualizado, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ExpertoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoExpertoInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar el experto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/expertos/{id}
	 * Respuesta: 204 sin cuerpo si se elimino; 409 si el experto participa en algun proyecto.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			expertoService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ExpertoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (RegistroAsociadoException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar el experto " + id, e);
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