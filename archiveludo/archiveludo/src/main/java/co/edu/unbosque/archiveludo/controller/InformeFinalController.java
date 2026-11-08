package co.edu.unbosque.archiveludo.controller;

import java.util.LinkedHashMap;
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

import co.edu.unbosque.archiveludo.dto.InformeFinalDTO;
import co.edu.unbosque.archiveludo.exceptions.AlmacenamientoArchivosException;
import co.edu.unbosque.archiveludo.exceptions.CredencialInvalidaException;
import co.edu.unbosque.archiveludo.exceptions.DatosInformeInvalidosException;
import co.edu.unbosque.archiveludo.exceptions.GeneracionPdfException;
import co.edu.unbosque.archiveludo.exceptions.IdInvalidoException;
import co.edu.unbosque.archiveludo.exceptions.InformeNoEncontradoException;
import co.edu.unbosque.archiveludo.exceptions.InformeYaExisteException;
import co.edu.unbosque.archiveludo.exceptions.ProyectoNoEncontradoException;
import co.edu.unbosque.archiveludo.service.InformeFinalService;

/**
 * Informe final (PDF) de un proyecto de restauracion: /api/proyectos/{id}/informe.
 * Ver, crear y regenerar: curador o administrador. Eliminar: solo administrador.
 */
@RestController
@RequestMapping("/api/proyectos/{id}/informe")
public class InformeFinalController {

	private static final Logger log = LoggerFactory.getLogger(InformeFinalController.class);

	@Autowired
	private InformeFinalService informeService;

	/**
	 * GET /api/proyectos/{id}/informe
	 * Respuesta: 200 con { idProyecto, urlInforme }. 404 si el proyecto no existe o no tiene informe.
	 */
	@GetMapping
	public ResponseEntity<?> buscar(@PathVariable String id) {
		try {
			InformeFinalDTO informe = informeService.buscar(id);
			return new ResponseEntity<InformeFinalDTO>(informe, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ProyectoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (InformeNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al buscar el informe del proyecto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * POST /api/proyectos/{id}/informe
	 * Cuerpo:    { "fechaPublicacion": "2026-11-03", "resumenResultados": "...", "resultado": "exitoso" }
	 * Respuesta: 201 con los datos del informe y su urlInforme.
	 *            400 si el proyecto no esta finalizado o los datos no son validos; 409 si ya tiene informe.
	 */
	@PostMapping
	public ResponseEntity<?> crear(@PathVariable String id, @RequestBody InformeFinalDTO informeDTO,
			@AuthenticationPrincipal UserDetails usuarioAutenticado) {
		try {
			InformeFinalDTO informe = informeService.crear(id, informeDTO, usuarioAutenticado.getUsername());
			return new ResponseEntity<InformeFinalDTO>(informe, HttpStatus.CREATED);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ProyectoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatosInformeInvalidosException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (InformeYaExisteException e) {
			return crearRespuestaError(HttpStatus.CONFLICT, e.getMessage());
		} catch (CredencialInvalidaException e) {
			return crearRespuestaError(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (GeneracionPdfException e) {
			log.error("No se pudo generar el PDF del proyecto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (AlmacenamientoArchivosException e) {
			return crearRespuestaError(HttpStatus.BAD_GATEWAY, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al crear el informe del proyecto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * PUT /api/proyectos/{id}/informe
	 * Cuerpo:    igual que el POST.
	 * Respuesta: 200 con el informe regenerado y su nueva urlInforme. 404 si el proyecto no tiene informe.
	 */
	@PutMapping
	public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody InformeFinalDTO informeDTO,
			@AuthenticationPrincipal UserDetails usuarioAutenticado) {
		try {
			InformeFinalDTO informe = informeService.actualizar(id, informeDTO, usuarioAutenticado.getUsername());
			return new ResponseEntity<InformeFinalDTO>(informe, HttpStatus.OK);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ProyectoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (InformeNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (DatosInformeInvalidosException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (CredencialInvalidaException e) {
			return crearRespuestaError(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (GeneracionPdfException e) {
			log.error("No se pudo generar el PDF del proyecto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (AlmacenamientoArchivosException e) {
			return crearRespuestaError(HttpStatus.BAD_GATEWAY, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al regenerar el informe del proyecto " + id, e);
			return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
		}
	}

	/**
	 * DELETE /api/proyectos/{id}/informe
	 * Respuesta: 204 sin cuerpo. 404 si el proyecto no tiene informe.
	 */
	@DeleteMapping
	public ResponseEntity<?> eliminar(@PathVariable String id) {
		try {
			informeService.eliminar(id);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (IdInvalidoException e) {
			return crearRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (ProyectoNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (InformeNoEncontradoException e) {
			return crearRespuestaError(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (AlmacenamientoArchivosException e) {
			return crearRespuestaError(HttpStatus.BAD_GATEWAY, e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al eliminar el informe del proyecto " + id, e);
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