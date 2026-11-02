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

import co.edu.unbosque.archiveludo.dto.ParticipacionDTO;
import co.edu.unbosque.archiveludo.exceptions.DatoProyectoInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.ExpertoNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.ParticipacionNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.ProyectoNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.UltimaParticipacionException;
import co.edu.unbosque.archiveludo.service.ParticipacionService;

/**
 * Participaciones de los expertos en los proyectos de restauracion.
 * El listado y el registro van por el proyecto (/api/proyectos/{id}/participaciones);
 * ver, editar y quitar una participacion, por su propio id (/api/participaciones/{id}).
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 */
@RestController
@RequestMapping("/api")
public class ParticipacionController {

	private static final Logger log = LoggerFactory.getLogger(ParticipacionController.class);

	@Autowired
	private ParticipacionService participacionService;

	/**
	 * GET /api/proyectos/{id}/participaciones
	 * Respuesta: 200 con las participaciones del proyecto, en el orden en que entraron los expertos.
	 */
	@GetMapping("/proyectos/{id}/participaciones")
	public ResponseEntity<?> buscarPorProyecto(@PathVariable String id) {
		try {
			List<ParticipacionDTO> participaciones = participacionService.buscarPorProyecto(id);
			return new ResponseEntity<List<ParticipacionDTO>>(participaciones, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ProyectoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar las participaciones del proyecto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/proyectos/{id}/participaciones
	 * Cuerpo:    { "idExperto": 5, "rolExperto": "Restauradora de cartuchos", "fechaIncorporacion": "2026-03-01" }
	 * Respuesta: 201 con la participacion creada.
	 */
	@PostMapping("/proyectos/{id}/participaciones")
	public ResponseEntity<?> crear(@PathVariable String id, @RequestBody ParticipacionDTO participacionDTO) {
		try {
			ParticipacionDTO participacionCreada = participacionService.crear(id, participacionDTO);
			return new ResponseEntity<ParticipacionDTO>(participacionCreada, HttpStatus.CREATED);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ProyectoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (ExpertoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoProyectoInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al agregar una participacion al proyecto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/participaciones/{id}
	 * Respuesta: 200 con la participacion.
	 */
	@GetMapping("/participaciones/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			ParticipacionDTO participacion = participacionService.buscarPorId(id);
			return new ResponseEntity<ParticipacionDTO>(participacion, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ParticipacionNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar la participacion " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/participaciones/{id}
	 * Cuerpo:    { "rolExperto": "Lider de restauracion", "fechaIncorporacion": "2026-03-01", "fechaSalida": "2026-10-15" }
	 * Respuesta: 200 con la participacion actualizada.
	 */
	@PutMapping("/participaciones/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody ParticipacionDTO participacionDTO) {
		try {
			ParticipacionDTO participacionActualizada = participacionService.actualizar(id, participacionDTO);
			return new ResponseEntity<ParticipacionDTO>(participacionActualizada, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ParticipacionNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoProyectoInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar la participacion " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/participaciones/{id}
	 * Respuesta: 204 sin cuerpo si se elimino; 409 si era la unica participacion del proyecto.
	 */
	@DeleteMapping("/participaciones/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			participacionService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ParticipacionNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (UltimaParticipacionException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar la participacion " + id, e);
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