package co.edu.unbosque.archiveludo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuracion de OpenAPI (Swagger) para documentar la API REST de ArchiveLudo.
 * Define la informacion general, el esquema de seguridad JWT y las respuestas
 * de error reutilizables.
 */
@Configuration
public class OpenApiConfig {

	private static final String NOMBRE_ESQUEMA_SEGURIDAD = "bearerAuth";

	/**
	 * Construye la documentación OpenAPI que muestra Swagger UI.
	 *
	 * @return configuración de OpenAPI
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		String descripcionPrincipal = "<h2>API REST de ArchiveLudo</h2>"
				+ "<p>Plataforma para catalogar y hacer seguimiento a videojuegos en riesgo de pérdida: "
				+ "copias físicas y digitales, instituciones custodias, proyectos de restauración, "
				+ "expertos, estatus legal y eventos de riesgo.</p>"
				+ "<h3>Flujo básico de uso:</h3><ol>"
				+ "<li>Inicia sesión con <code>/api/auth/login</code></li>"
				+ "<li>Copia el token que llega en la cabecera <code>Authorization</code> de la respuesta</li>"
				+ "<li>Pulsa <strong>Authorize</strong> y pega el token</li>"
				+ "<li>Usa los demás endpoints</li></ol>"
				+ "<h3>Roles:</h3><ul>"
				+ "<li><strong>Consultante</strong>: solo lectura</li>"
				+ "<li><strong>Curador</strong>: lectura y registro de informacion</li>"
				+ "<li><strong>Administrador</strong>: todo lo anterior y gestión de usuarios</li></ul>"
				+ "<h3>Códigos de estado HTTP:</h3><ul>"
				+ "<li><strong>200/201</strong>: operación exitosa</li>"
				+ "<li><strong>400</strong>: datos o parámetros inválidos</li>"
				+ "<li><strong>401</strong>: sin sesión o token vencido</li>"
				+ "<li><strong>403</strong>: el rol no tiene permiso</li>"
				+ "<li><strong>404</strong>: el recurso no existe</li>"
				+ "<li><strong>409</strong>: conflicto con los datos existentes</li>"
				+ "<li><strong>422</strong>: regla de negocio violada</li></ul>";

		String descripcionSeguridad = "Autenticación mediante JWT (JSON Web Token)."
				+ "<ol><li>Obtén el token con <code>/api/auth/login</code></li>"
				+ "<li>Pulsa <strong>Authorize</strong> y pega solo el token </li>"
				+ "<li>Pulsa <strong>Authorize</strong> y luego <strong>Close</strong></li></ol>";

		Info informacion = new Info()
				.title("ArchiveLudo API")
				.version("1.0")
				.description(descripcionPrincipal)
				.contact(new Contact().name("Equipo ArchiveLudo - Bases de Datos 1, Universidad El Bosque"));

		SecurityScheme esquemaSeguridad = new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.description(descripcionSeguridad);

		Components componentes = new Components();
		componentes.addSecuritySchemes(NOMBRE_ESQUEMA_SEGURIDAD, esquemaSeguridad);
		componentes.addResponses("SolicitudInvalida",
				crearRespuestaError("Datos o parámetros invalidos", 400, "El parámetro 'id' tiene un valor inválido"));
		componentes.addResponses("NoAutenticado",
				crearRespuestaError("Sin sesión o token vencido", 401, "Debe iniciar sesión"));
		componentes.addResponses("SinPermiso",
				crearRespuestaError("El rol no tiene permiso", 403, "No tiene permisos para esta operación"));
		componentes.addResponses("NoEncontrado",
				crearRespuestaError("Recurso no encontrado", 404, "El recurso solicitado no existe"));

		SecurityRequirement requisitoSeguridad = new SecurityRequirement().addList(NOMBRE_ESQUEMA_SEGURIDAD);

		return new OpenAPI()
				.info(informacion)
				.components(componentes)
				.addSecurityItem(requisitoSeguridad);
	}

	/**
	 * Crea una respuesta de error de ejemplo con el formato { estado, mensaje }.
	 */
	private ApiResponse crearRespuestaError(String descripcion, int estado, String mensaje) {
		String ejemploJson = "{\"estado\": " + estado + ", \"mensaje\": \"" + mensaje + "\"}";
		Example ejemplo = new Example().value(ejemploJson);
		MediaType tipoJson = new MediaType().addExamples("error", ejemplo);
		Content contenido = new Content().addMediaType("application/json", tipoJson);
		return new ApiResponse().description(descripcion).content(contenido);
	}
}