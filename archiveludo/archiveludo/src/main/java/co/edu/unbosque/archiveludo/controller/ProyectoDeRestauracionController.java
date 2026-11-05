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

import co.edu.unbosque.archiveludo.dto.ProyectoDeRestauracionDTO;
import co.edu.unbosque.archiveludo.exceptions.AlmacenamientoArchivosException;
import co.edu.unbosque.archiveludo.exceptions.CredencialInvalidaException;
import co.edu.unbosque.archiveludo.exceptions.DatoProyectoInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.ExpertoNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.ExpertoRepetidoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.ProyectoNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.VideojuegoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.ProyectoDeRestauracionService;

/**
 * Proyectos de restauracion de los videojuegos, con sus participaciones.
 * El listado general va por /api/proyectos; los de un juego y la creacion, por el videojuego
 * (/api/videojuegos/{id}/proyectos); ver, editar y eliminar, por el id del proyecto (/api/proyectos/{id}).
 * Leer: cualquier rol. Crear y actualizar: curador o administrador. Eliminar: solo administrador.
 * El informe final tiene su propio controller (InformeFinalController).
 */
@RestController
@RequestMapping("/api")
public class ProyectoDeRestauracionController {

	private static final Logger log = LoggerFactory.getLogger(ProyectoDeRestauracionController.class);

	@Autowired
	private ProyectoDeRestauracionService proyectoService;

	/**
	 * GET /api/proyectos
	 * Respuesta: 200 con todos los proyectos, del mas reciente al mas antiguo, con sus participaciones.
	 */
	@GetMapping("/proyectos")
	public ResponseEntity<?> buscarTodos() {
		try {
			List<ProyectoDeRestauracionDTO> proyectos = proyectoService.buscarTodos();
			return new ResponseEntity<List<ProyectoDeRestauracionDTO>>(proyectos, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error inesperado al buscar los proyectos", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/videojuegos/{id}/proyectos
	 * Respuesta: 200 con los proyectos del videojuego, con sus participaciones.
	 */
	@GetMapping("/videojuegos/{id}/proyectos")
	public ResponseEntity<?> buscarPorVideojuego(@PathVariable String id) {
		try {
			List<ProyectoDeRestauracionDTO> proyectos = proyectoService.buscarPorVideojuego(id);
			return new ResponseEntity<List<ProyectoDeRestauracionDTO>>(proyectos, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar los proyectos del videojuego " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/proyectos/{id}
	 * Respuesta: 200 con el proyecto y sus participaciones.
	 */
	@GetMapping("/proyectos/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			ProyectoDeRestauracionDTO proyecto = proyectoService.buscarPorId(id);
			return new ResponseEntity<ProyectoDeRestauracionDTO>(proyecto, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ProyectoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar el proyecto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/videojuegos/{id}/proyectos
	 * Cuerpo:    { "nombre": "...", "descripcion": "...", "herramientaEmulador": "DOSBox", "estado": "activo",
	 *              "fechaInicio": "2026-09-01",
	 *              "participaciones": [ { "idExperto": 3, "rolExperto": "...", "fechaIncorporacion": "2026-09-01" } ] }
	 * Respuesta: 201 con el proyecto creado y sus participaciones. El usuario que lo crea sale del token.
	 *            409 si un experto viene repetido en la lista.
	 */
	@PostMapping("/videojuegos/{id}/proyectos")
	public ResponseEntity<?> crear(@PathVariable String id, @RequestBody ProyectoDeRestauracionDTO proyectoDTO,
			@AuthenticationPrincipal UserDetails usuarioAutenticado) {
		try {
			ProyectoDeRestauracionDTO proyectoCreado = proyectoService.crear(id, proyectoDTO,
					usuarioAutenticado.getUsername());
			return new ResponseEntity<ProyectoDeRestauracionDTO>(proyectoCreado, HttpStatus.CREATED);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (VideojuegoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (ExpertoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (ExpertoRepetidoException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (DatoProyectoInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CredencialInvalidaException e) {
			return crearRespuestaError(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al crear un proyecto para el videojuego " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/proyectos/{id}
	 * Cuerpo:    los datos del proyecto (como el POST). Las participaciones se ignoran aqui.
	 * Respuesta: 200 con el proyecto actualizado y sus participaciones.
	 *            400 si se intenta pasar a activo o pausado un proyecto que ya tiene informe final.
	 */
	@PutMapping("/proyectos/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody ProyectoDeRestauracionDTO proyectoDTO) {
		try {
			ProyectoDeRestauracionDTO proyectoActualizado = proyectoService.actualizar(id, proyectoDTO);
			return new ResponseEntity<ProyectoDeRestauracionDTO>(proyectoActualizado, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ProyectoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatoProyectoInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar el proyecto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/proyectos/{id}
	 * Respuesta: 204 sin cuerpo si se elimino (junto con sus participaciones y su informe final).
	 *            502 si no se pudo borrar el PDF en Cloudinary (en ese caso no se borra nada).
	 */
	@DeleteMapping("/proyectos/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			proyectoService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ProyectoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (AlmacenamientoArchivosException e) {
			return crearRespuestaError(HttpStatus.BAD_GATEWAY, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar el proyecto " + id, e);
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