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

import co.edu.unbosque.archiveludo.dto.CustodiaDTO;
import co.edu.unbosque.archiveludo.exceptions.CopiaNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.CustodiaNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.DatoCustodiaInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.InstitucionNoEncontradaException;
import co.edu.unbosque.archiveludo.service.CustodiaService;

/**
 * Custodias de las copias (en que institucion esta cada copia y durante que periodo).
 * El historial y el registro van por la copia (/api/copias/{id}/custodias);
 * ver, editar y eliminar una custodia, por su propio id (/api/custodias/{id}).
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 */
@RestController
@RequestMapping("/api")
public class CustodiaController {

	private static final Logger log = LoggerFactory.getLogger(CustodiaController.class);

	@Autowired
	private CustodiaService custodiaService;

	/**
	 * GET /api/copias/{id}/custodias
	 * Respuesta: 200 con el historial de custodias de la copia, de la mas reciente a la mas antigua.
	 *            La vigente es la que no trae "fechaFin".
	 */
	@GetMapping("/copias/{id}/custodias")
	public ResponseEntity<?> buscarPorCopia(@PathVariable String id) {
		try {
			List<CustodiaDTO> custodias = custodiaService.buscarPorCopia(id);
			return new ResponseEntity<List<CustodiaDTO>>(custodias, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CopiaNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar las custodias de la copia " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/copias/{id}/custodias
	 * Cuerpo:    { "idInstitucion": 1, "fechaInicio": "2026-01-10",
	 *              "condicionAlmacenamiento": "Vitrina con control de humedad" }
	 * Respuesta: 201 con la custodia creada (vigente). Si la copia tenia otra vigente, queda cerrada.
	 */
	@PostMapping("/copias/{id}/custodias")
	public ResponseEntity<?> crear(@PathVariable String id, @RequestBody CustodiaDTO custodiaDTO) {
		try {
			CustodiaDTO custodiaCreada = custodiaService.crear(id, custodiaDTO);
			return new ResponseEntity<CustodiaDTO>(custodiaCreada, HttpStatus.CREATED);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CopiaNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (InstitucionNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoCustodiaInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al registrar la custodia de la copia " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/custodias/{id}
	 * Respuesta: 200 con la custodia.
	 */
	@GetMapping("/custodias/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			CustodiaDTO custodia = custodiaService.buscarPorId(id);
			return new ResponseEntity<CustodiaDTO>(custodia, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CustodiaNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar la custodia " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/custodias/{id}
	 * Cuerpo:    { "idInstitucion": 1, "fechaInicio": "2026-01-10", "fechaFin": "2026-06-30",
	 *              "condicionAlmacenamiento": "Bodega" }   ("fechaFin" opcional: sin ella queda vigente)
	 * Respuesta: 200 con la custodia actualizada.
	 */
	@PutMapping("/custodias/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody CustodiaDTO custodiaDTO) {
		try {
			CustodiaDTO custodiaActualizada = custodiaService.actualizar(id, custodiaDTO);
			return new ResponseEntity<CustodiaDTO>(custodiaActualizada, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CustodiaNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (InstitucionNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoCustodiaInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar la custodia " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/custodias/{id}
	 * Respuesta: 204 sin cuerpo si se elimino.
	 */
	@DeleteMapping("/custodias/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			custodiaService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CustodiaNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar la custodia " + id, e);
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