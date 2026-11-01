package co.edu.unbosque.archiveludo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
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
import co.edu.unbosque.archiveludo.exceptions.CredencialInvalidaException;
import co.edu.unbosque.archiveludo.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	private AuthService authService;

	/**
	 * POST /api/auth/registro
	 * Cuerpo:    { "primerNombre": "Juan", "primerApellido": "Perez",
	 *              "correo": "juan@gmail.com", "contrasenia": "Juan2026", "rol": "consulta" }
	 * Respuesta: 201 con el usuario creado (sin contrasena).
	 * La contrasena llega en texto plano y se guarda hasheada.
	 */
	@PostMapping("/registro")
	public ResponseEntity<?> registrar(@RequestBody UsuarioDTO usuarioDTO) {
		try {
			UsuarioDTO usuarioCreado = authService.registrar(usuarioDTO);
			return new ResponseEntity<UsuarioDTO>(usuarioCreado, HttpStatus.CREATED);
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CorreoElectronicoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ContraseniaUsuarioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (IllegalArgumentException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (DuplicateKeyException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
		} catch (Exception e) {
			log.error("Error inesperado en el registro", e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/auth/login
	 * Cuerpo:    { "correo": "juan@gmail.com", "contrasenia": "Juan2026" }
	 * Respuesta: 200 con los datos del usuario en el cuerpo y el token en la cabecera
	 *            Authorization: Bearer <token>
	 */
	@PostMapping("/login")
	public ResponseEntity<?> iniciarSesion(@RequestBody UsuarioDTO credenciales) {
		try {
			UsuarioDTO usuario = authService.iniciarSesion(credenciales);
			String token = authService.generarToken(usuario);
			return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).body(usuario);
		} catch (CampoObligatorioException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CredencialInvalidaException e) {
			return crearRespuestaError(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado en el inicio de sesion", e);
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