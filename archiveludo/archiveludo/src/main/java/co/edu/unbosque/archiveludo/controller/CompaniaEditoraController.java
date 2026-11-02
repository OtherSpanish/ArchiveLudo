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

import co.edu.unbosque.archiveludo.dto.CompaniaEditoraDTO;
import co.edu.unbosque.archiveludo.exceptions.CampoObligatorioException;
import co.edu.unbosque.archiveludo.exceptions.CompaniaEditoraNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.service.CompaniaEditoraService;

/**
 * CRUD de companias editoras.
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 */
@RestController
@RequestMapping("/api/editoras")
public class CompaniaEditoraController {

	private static final Logger log = LoggerFactory.getLogger(CompaniaEditoraController.class);

	@Autowired
	private CompaniaEditoraService companiaEditoraService;

	/**
	 * GET /api/editoras
	 */
	@GetMapping
	public ResponseEntity<?> buscarTodas() {
		try {
			List<CompaniaEditoraDTO> editoras = companiaEditoraService.buscarTodas();
			return new ResponseEntity<List<CompaniaEditoraDTO>>(editoras, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error inesperado al listar las companias editoras", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/editoras/{id}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			CompaniaEditoraDTO editora = companiaEditoraService.buscarPorId(id);
			return new ResponseEntity<CompaniaEditoraDTO>(editora, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CompaniaEditoraNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar la compania editora " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/editoras
	 * Cuerpo:    { "nombre": "Eidos Interactive", "pais": "Reino Unido" }
	 * Respuesta: 201 con la editora creada.
	 */
	@PostMapping
	public ResponseEntity<?> crear(@RequestBody CompaniaEditoraDTO companiaEditoraDTO) {
		try {
			CompaniaEditoraDTO editoraCreada = companiaEditoraService.crear(companiaEditoraDTO);
			return new ResponseEntity<CompaniaEditoraDTO>(editoraCreada, HttpStatus.CREATED);
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al crear la compania editora", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/editoras/{id}
	 * Cuerpo:    { "nombre": "...", "pais": "..." }
	 * Respuesta: 200 con la editora actualizada.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody CompaniaEditoraDTO companiaEditoraDTO) {
		try {
			CompaniaEditoraDTO editoraActualizada = companiaEditoraService.actualizar(id, companiaEditoraDTO);
			return new ResponseEntity<CompaniaEditoraDTO>(editoraActualizada, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CompaniaEditoraNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar la compania editora " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/editoras/{id}
	 * Respuesta: 204 sin cuerpo si se elimino; 409 si publica algun videojuego.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			companiaEditoraService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CompaniaEditoraNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (RegistroAsociadoException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar la compania editora " + id, e);
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