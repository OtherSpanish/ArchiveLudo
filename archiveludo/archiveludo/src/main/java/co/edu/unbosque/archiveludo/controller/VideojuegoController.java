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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.archiveludo.dto.VideojuegoDTO;
import co.edu.unbosque.archiveludo.exceptions.AnioInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.CampoObligatorioException;
import co.edu.unbosque.archiveludo.exceptions.CompaniaEditoraNoEncontradaException;
import co.edu.unbosque.archiveludo.exceptions.CredencialInvalidaException;
import co.edu.unbosque.archiveludo.exceptions.EstadoConservacionInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.EstudioNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.PaginacionInvalidaException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.VideojuegoService;

/**
 * Endpoints de videojuegos.
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 * Los parametros de busqueda se reciben como opcionales (required = false) para que,
 * si faltan, los valide el service y la respuesta salga con el formato { estado, mensaje }.
 */
@RestController
@RequestMapping("/api/videojuegos")
public class VideojuegoController {

	private static final Logger log = LoggerFactory.getLogger(VideojuegoController.class);

	/** Cabecera donde viaja el total de coincidencias del catalogo (expuesta en CorsConfig). */
	private static final String CABECERA_TOTAL = "X-Total-Count";

	@Autowired
	private VideojuegoService videojuegoService;

	
	/**
	 * GET /api/videojuegos?estado=en riesgo&q=ambar&pagina=0&limite=10
	 * Todos los parametros son opcionales.
	 * Respuesta: 200 con los videojuegos de la pagina pedida y la cabecera
	 *            X-Total-Count con el total de coincidencias en toda la base.
	 */
	@GetMapping
	public ResponseEntity<?> buscarCatalogo(@RequestParam(name = "estado", required = false) String estado,
			@RequestParam(name = "q", required = false) String q,
			@RequestParam(name = "pagina", required = false) String pagina,
			@RequestParam(name = "limite", required = false) String limite) {
		try {
			List<VideojuegoDTO> videojuegos = videojuegoService.buscarCatalogo(estado, q, pagina, limite);
			long total = videojuegoService.contarCatalogo(estado, q);
			return ResponseEntity.ok().header(CABECERA_TOTAL, String.valueOf(total)).body(videojuegos);
		} catch (EstadoConservacionInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (PaginacionInvalidaException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al consultar el catalogo", e);
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
	 * POST /api/videojuegos
	 * Cuerpo:    { "titulo": "Deus Ex", "genero": "RPG", "plataformaOriginal": "PC",
	 *              "descripcion": "...", "anioLanzamiento": 2000, "estadoConservacion": "activo",
	 *              "idEstudio": 3, "idsEditoras": [2, 5] }
	 * Respuesta: 201 con el videojuego creado. El usuario que lo registra sale del token.
	 */
	@PostMapping
	public ResponseEntity<?> crear(@RequestBody VideojuegoDTO videojuegoDTO,
			@AuthenticationPrincipal UserDetails usuarioAutenticado) {
		try {
			VideojuegoDTO videojuegoCreado = videojuegoService.crear(videojuegoDTO,
					usuarioAutenticado.getUsername());
			return new ResponseEntity<VideojuegoDTO>(videojuegoCreado, HttpStatus.CREATED);
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (AnioInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EstadoConservacionInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EstudioNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (CompaniaEditoraNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (CredencialInvalidaException e) {
			return crearRespuestaError(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al crear el videojuego", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/videojuegos/{id}
	 * Cuerpo:    los mismos campos que el POST. "idsEditoras" reemplaza la lista anterior.
	 * Respuesta: 200 con el videojuego actualizado.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody VideojuegoDTO videojuegoDTO) {
		try {
			VideojuegoDTO videojuegoActualizado = videojuegoService.actualizar(id, videojuegoDTO);
			return new ResponseEntity<VideojuegoDTO>(videojuegoActualizado, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (AnioInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EstadoConservacionInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (EstudioNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (CompaniaEditoraNoEncontradaException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar el videojuego " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/videojuegos/{id}
	 * Respuesta: 204 sin cuerpo si se elimino;
	 *            409 si tiene copias, estatus legales o eventos de riesgo.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			videojuegoService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (RegistroAsociadoException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar el videojuego " + id, e);
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