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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.archiveludo.dto.VideojuegoDTO;
import co.edu.unbosque.archiveludo.exceptions.AnioInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.CampoObligatorioException;
import co.edu.unbosque.archiveludo.exceptions.EstadoConservacionInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.VideojuegoService;

/**
 * Endpoints de lectura de videojuegos. Requieren token de cualquier rol.
 * Los parametros se reciben como opcionales (required = false) para que,
 * si faltan, los valide el service y la respuesta salga con el formato { estado, mensaje }.
 */
@RestController
@RequestMapping("/api/videojuegos")
public class VideojuegoController {

	private static final Logger log = LoggerFactory.getLogger(VideojuegoController.class);

	@Autowired
	private VideojuegoService videojuegoService;

	/**
	 * GET /api/videojuegos
	 * Respuesta: 200 con todos los videojuegos.
	 */
	@GetMapping
	public ResponseEntity<?> buscarTodos() {
		try {
			List<VideojuegoDTO> videojuegos = videojuegoService.buscarTodos();
			return new ResponseEntity<List<VideojuegoDTO>>(videojuegos, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error inesperado al listar los videojuegos", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/videojuegos/{id}
	 * Respuesta: 200 con la ficha del videojuego.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			VideojuegoDTO videojuego = videojuegoService.buscarPorId(id);
			return new ResponseEntity<VideojuegoDTO>(videojuego, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar el videojuego " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/videojuegos/por-titulo?titulo=zelda
	 * Respuesta: 200 con los videojuegos cuyo titulo contiene el texto.
	 */
	@GetMapping("/por-titulo")
	public ResponseEntity<?> buscarPorTitulo(@RequestParam(name = "titulo", required = false) String titulo) {
		try {
			List<VideojuegoDTO> videojuegos = videojuegoService.buscarPorTitulo(titulo);
			return new ResponseEntity<List<VideojuegoDTO>>(videojuegos, HttpStatus.OK);
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar por titulo", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/videojuegos/por-genero?genero=RPG
	 */
	@GetMapping("/por-genero")
	public ResponseEntity<?> buscarPorGenero(@RequestParam(name = "genero", required = false) String genero) {
		try {
			List<VideojuegoDTO> videojuegos = videojuegoService.buscarPorGenero(genero);
			return new ResponseEntity<List<VideojuegoDTO>>(videojuegos, HttpStatus.OK);
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar por genero", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/videojuegos/por-plataforma?plataformaOriginal=Nintendo 64
	 */
	@GetMapping("/por-plataforma")
	public ResponseEntity<?> buscarPorPlataformaOriginal(
			@RequestParam(name = "plataformaOriginal", required = false) String plataformaOriginal) {
		try {
			List<VideojuegoDTO> videojuegos = videojuegoService.buscarPorPlataformaOriginal(plataformaOriginal);
			return new ResponseEntity<List<VideojuegoDTO>>(videojuegos, HttpStatus.OK);
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar por plataforma", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/videojuegos/por-anio?anioLanzamiento=1998
	 */
	@GetMapping("/por-anio")
	public ResponseEntity<?> buscarPorAnioLanzamiento(
			@RequestParam(name = "anioLanzamiento", required = false) String anioLanzamiento) {
		try {
			List<VideojuegoDTO> videojuegos = videojuegoService.buscarPorAnioLanzamiento(anioLanzamiento);
			return new ResponseEntity<List<VideojuegoDTO>>(videojuegos, HttpStatus.OK);
		} catch (AnioInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar por anio", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/videojuegos/por-estado?estadoConservacion=en riesgo
	 */
	@GetMapping("/por-estado")
	public ResponseEntity<?> buscarPorEstadoConservacion(
			@RequestParam(name = "estadoConservacion", required = false) String estadoConservacion) {
		try {
			List<VideojuegoDTO> videojuegos = videojuegoService.buscarPorEstadoConservacion(estadoConservacion);
			return new ResponseEntity<List<VideojuegoDTO>>(videojuegos, HttpStatus.OK);
		} catch (EstadoConservacionInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar por estado de conservacion", e);
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