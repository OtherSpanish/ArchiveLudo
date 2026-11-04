package co.edu.unbosque.archiveludo.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

/**
 * Configuracion de Cloudinary, donde se guardan las imagenes de los videojuegos y los informes en PDF.
 * Las credenciales llegan en una sola URL (cloudinary://api_key:api_secret@cloud_name)
 * desde application-local.properties o desde la variable de entorno CLOUDINARY_URL; nunca van en el repositorio.
 */
@Configuration
public class CloudinaryConfig {

	private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

	@Value("${cloudinary.url}")
	private String cloudinaryUrl;

	/**
	 * Crea el cliente de Cloudinary que usa AlmacenamientoArchivosService.
	 * Si falta la URL, la app no arranca y el mensaje dice que configurar,
	 * en vez de fallar mas tarde al subir el primer archivo.
	 *
	 * @return cliente de Cloudinary listo para subir y borrar archivos
	 */
	@Bean
	public Cloudinary cloudinary() {
		if (cloudinaryUrl == null || cloudinaryUrl.trim().isEmpty()) {
			throw new IllegalStateException("Falta la configuracion de Cloudinary: define cloudinary.url en "
					+ "application-local.properties o la variable de entorno CLOUDINARY_URL "
					+ "(formato cloudinary://api_key:api_secret@cloud_name)");
		}

		Cloudinary cloudinary = new Cloudinary(cloudinaryUrl.trim());
		// Solo se muestra el cloud name, que no es secreto; la api key y el secret nunca se escriben en el log
		log.info("Cloudinary configurado para la nube '" + cloudinary.config.cloudName + "'");
		return cloudinary;
	}

}