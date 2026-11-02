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

import co.edu.unbosque.archiveludo.dto.CopiaDTO;
import co.edu.unbosque.archiveludo.exceptions.CampoObligatorioException;
import co.edu.unbosque.archiveludo.exceptions.CopiaNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.CredencialInvalidaException;
import co.edu.unbosque.archiveludo.exceptions.DatoCopiaInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.EstadoIntegridadInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.exceptions.TamanioInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.CopiaService;

/**
 * Copias (fisicas y digitales) de los videojuegos.
 * El listado y el registro van por el videojuego (/api/videojuegos/{id}/copias);
 * ver, editar y eliminar una copia, por su propio id (/api/copias/{id}).
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 */
@RestController
@RequestMapping("/api")
public class CopiaController {

	private static final Logger log = LoggerFactory.getLogger(CopiaController.class);

	@Autowired
	private CopiaService copiaService;

	/**
	 * GET /api/videojuegos/{id}/copias
	 * Respuesta: 200 con las copias del videojuego. Cada copia trae sus campos comunes,
	 *            los de su subtipo (segun "tipoCopia") y, si tiene, la institucion de su custodia vigente.
	 */
	@GetMapping("/videojuegos/{id}/copias")
	public ResponseEntity<?> buscarPorVideojuego(@PathVariable String id) {
		try {
			List<CopiaDTO> copias = copiaService.buscarPorVideojuego(id);
			return new ResponseEntity<List<CopiaDTO>>(copias, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar las copias del videojuego " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/videojuegos/{id}/copias
	 * Cuerpo (fisica):  { "tipoCopia": "fisica", "estadoIntegridad": "buena", "numeroSerie": "SN-7781",
	 *                     "ubicacionFisica": "Bodega A, estante 2", "tipoSoporte": "cartucho" }
	 * Cuerpo (digital): { "tipoCopia": "digital", "estadoIntegridad": "buena", "formatoArchivo": "iso",
	 *                     "tamanio": 734003200, "hashVerificacion": "9b1f0c3d..." }
	 * Respuesta: 201 con la copia creada. El usuario que la registra sale del token.
	 */
	@PostMapping("/videojuegos/{id}/copias")
	public ResponseEntity<?> crear(@PathVariable String id, @RequestBody CopiaDTO copiaDTO,
			@AuthenticationPrincipal UserDetails usuarioAutenticado) {
		try {
			CopiaDTO copiaCreada = copiaService.crear(id, copiaDTO, usuarioAutenticado.getUsername());
			return new ResponseEntity<CopiaDTO>(copiaCreada, HttpStatus.CREATED);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoCopiaInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EstadoIntegridadInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (TamanioInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CredencialInvalidaException e) {
			return crearRespuestaError(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al registrar la copia del videojuego " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/copias/{id}
	 * Respuesta: 200 con la copia, los campos de su subtipo y su institucion actual (si tiene).
	 */
	@GetMapping("/copias/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			CopiaDTO copia = copiaService.buscarPorId(id);
			return new ResponseEntity<CopiaDTO>(copia, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CopiaNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar la copia " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/copias/{id}
	 * Cuerpo:    el mismo formato del POST. "tipoCopia" debe ser igual al de la copia guardada.
	 * Respuesta: 200 con la copia actualizada.
	 */
	@PutMapping("/copias/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody CopiaDTO copiaDTO) {
		try {
			CopiaDTO copiaActualizada = copiaService.actualizar(id, copiaDTO);
			return new ResponseEntity<CopiaDTO>(copiaActualizada, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CopiaNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoCopiaInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EstadoIntegridadInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (TamanioInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar la copia " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/copias/{id}
	 * Respuesta: 204 sin cuerpo si se elimino; 409 si la copia tiene custodias.
	 */
	@DeleteMapping("/copias/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			copiaService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CopiaNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (RegistroAsociadoException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar la copia " + id, e);
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