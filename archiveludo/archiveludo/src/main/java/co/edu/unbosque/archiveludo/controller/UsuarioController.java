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

import co.edu.unbosque.archiveludo.dto.UsuarioDTO;
import co.edu.unbosque.archiveludo.exceptions.CampoObligatorioException;
import co.edu.unbosque.archiveludo.exceptions.ContraseniaUsuarioException;
import co.edu.unbosque.archiveludo.exceptions.CorreoElectronicoException;
import co.edu.unbosque.archiveludo.exceptions.CorreoYaRegistradoException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.RegistroAsociadoException;
import co.edu.unbosque.archiveludo.exceptions.RolInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.UsuarioNoEncontradoException;
import co.edu.unbosque.archiveludo.service.UsuarioService;

/**
 * CRUD de usuarios. Todos los endpoints son solo para el administrador (SecurityConfig).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

	@Autowired
	private UsuarioService usuarioService;

	/**
	 * GET /api/usuarios
	 * Respuesta: 200 con todos los usuarios (sin contrasena).
	 */
	@GetMapping
	public ResponseEntity<?> buscarTodos() {
		try {
			List<UsuarioDTO> usuarios = usuarioService.buscarTodos();
			return new ResponseEntity<List<UsuarioDTO>>(usuarios, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error inesperado al listar los usuarios", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * GET /api/usuarios/{id}
	 * Respuesta: 200 con el usuario (sin contrasena).
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable String id) {
		try {
			UsuarioDTO usuario = usuarioService.buscarPorId(id);
			return new ResponseEntity<UsuarioDTO>(usuario, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (UsuarioNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar el usuario " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/usuarios
	 * Cuerpo:    { "primerNombre": "Juan", "primerApellido": "Perez", "correo": "juan@gmail.com",
	 *              "contrasenia": "Juan2026", "rol": "curador" }
	 * Respuesta: 201 con el usuario creado (sin contrasena).
	 */
	@PostMapping
	public ResponseEntity<?> crear(@RequestBody UsuarioDTO usuarioDTO) {
		try {
			UsuarioDTO usuarioCreado = usuarioService.crear(usuarioDTO);
			return new ResponseEntity<UsuarioDTO>(usuarioCreado, HttpStatus.CREATED);
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CorreoElectronicoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ContraseniaUsuarioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (RolInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CorreoYaRegistradoException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al crear el usuario", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/usuarios/{id}
	 * Cuerpo:    los mismos campos que el POST; "contrasenia" es opcional
	 *            (si no se envia, se conserva la actual).
	 * Respuesta: 200 con el usuario actualizado (sin contrasena).
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody UsuarioDTO usuarioDTO) {
		try {
			UsuarioDTO usuarioActualizado = usuarioService.actualizar(id, usuarioDTO);
			return new ResponseEntity<UsuarioDTO>(usuarioActualizado, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (UsuarioNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CorreoElectronicoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ContraseniaUsuarioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (RolInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CorreoYaRegistradoException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al actualizar el usuario " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/usuarios/{id}
	 * Respuesta: 204 sin cuerpo si se elimino;
	 *            409 si el usuario registro copias, estatus legales o eventos de riesgo.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			usuarioService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (UsuarioNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (RegistroAsociadoException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar el usuario " + id, e);
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