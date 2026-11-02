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

import co.edu.unbosque.archiveludo.dto.InstitucionDTO;
import co.edu.unbosque.archiveludo.exceptions.CampoObligatorioException;
import co.edu.unbosque.archiveludo.exceptions.DatoInstitucionInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.InstitucionNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.service.InstitucionService;

/**
 * CRUD de instituciones custodias (con sus telefonos y correos).
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 */
@RestController
@RequestMapping("/api/instituciones")
public class InstitucionController {

	private static final Logger log = LoggerFactory.getLogger(InstitucionController.class);

	@Autowired
	private InstitucionService institucionService;

	/**
	 * GET /api/instituciones
	 */
	@GetMapping
	public ResponseEntity<?> buscarTodas() {
		try {
			List<InstitucionDTO> instituciones = institucionService.buscarTodas();
			return new ResponseEntity<List<InstitucionDTO>>(instituciones, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error inesperado al listar las instituciones", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/instituciones/{id}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			InstitucionDTO institucion = institucionService.buscarPorId(id);
			return new ResponseEntity<InstitucionDTO>(institucion, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (InstitucionNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar la institucion " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/instituciones
	 * Cuerpo:    { "nombre": "Museo del Videojuego", "tipo": "museo", "pais": "Colombia",
	 *              "direccion": "Calle 100 # 15-20", "telefonos": [6015551234],
	 *              "correos": ["contacto@museovideojuego.org"] }
	 * Respuesta: 201 con la institucion creada.
	 */
	@PostMapping
	public ResponseEntity<?> crear(@RequestBody InstitucionDTO institucionDTO) {
		try {
			InstitucionDTO institucionCreada = institucionService.crear(institucionDTO);
			return new ResponseEntity<InstitucionDTO>(institucionCreada, HttpStatus.CREATED);
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (DatoInstitucionInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al crear la institucion", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/instituciones/{id}
	 * Cuerpo:    los mismos campos que el POST. Las listas reemplazan a las anteriores.
	 * Respuesta: 200 con la institucion actualizada.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody InstitucionDTO institucionDTO) {
		try {
			InstitucionDTO institucionActualizada = institucionService.actualizar(id, institucionDTO);
			return new ResponseEntity<InstitucionDTO>(institucionActualizada, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (InstitucionNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (DatoInstitucionInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar la institucion " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/instituciones/{id}
	 * Respuesta: 204 sin cuerpo si se elimino; 409 si tiene custodias.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			institucionService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (InstitucionNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (RegistroAsociadoException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar la institucion " + id, e);
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