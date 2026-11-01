package co.edu.unbosque.archiveludo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.archiveludo.dto.UsuarioDTO;
import co.edu.unbosque.archiveludo.exceptions.CampoObligatorioException;
import co.edu.unbosque.archiveludo.exceptions.ContraseniaUsuarioException;
import co.edu.unbosque.archiveludo.exceptions.CorreoElectronicoException;
import co.edu.unbosque.archiveludo.exceptions.CorreoYaRegistradoException;
import co.edu.unbosque.archiveludo.exceptions.RolInvalidoException;
import co.edu.unbosque.archiveludo.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

	@Autowired
	private UsuarioService usuarioService;

	/**
	 * POST /api/usuarios (solo administrador)
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
	 * Arma el cuerpo { estado, mensaje } que espera el front.
	 */
	private ResponseEntity<Map<String, Object>> crearRespuestaError(HttpStatus estado, String mensaje) {
		Map<String, Object> cuerpo = new LinkedHashMap<String, Object>();
		cuerpo.put("estado", estado.value());
		cuerpo.put("mensaje", mensaje);
		return new ResponseEntity<Map<String, Object>>(cuerpo, estado);
	}

}