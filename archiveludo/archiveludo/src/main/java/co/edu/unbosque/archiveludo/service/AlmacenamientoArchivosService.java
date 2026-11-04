package co.edu.unbosque.archiveludo.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import co.edu.unbosque.archiveludo.exceptions.AlmacenamientoArchivosException;

/**
 * Sube y borra archivos en Cloudinary: las imagenes de los videojuegos y los informes finales en PDF.
 * La BD solo guarda la URL que devuelve Cloudinary; el archivo vive en Cloudinary.
 * Los demas services usan esta clase; ningun controller la llama directamente.
 */
@Service
public class AlmacenamientoArchivosService {

	private static final Logger log = LoggerFactory.getLogger(AlmacenamientoArchivosService.class);

	/** Carpetas dentro de la cuenta de Cloudinary. */
	private static final String CARPETA_VIDEOJUEGOS = "archiveludo/videojuegos/";
	private static final String CARPETA_INFORMES = "archiveludo/informes/";

	/** Parte de la URL de Cloudinary que va justo antes del public_id. */
	private static final String MARCA_UPLOAD = "/upload/";

	private final Cloudinary cloudinary;

	public AlmacenamientoArchivosService(Cloudinary cloudinary) {
		this.cloudinary = cloudinary;
	}

	// ──────────────────────────────────────────────
	// Subir
	// ──────────────────────────────────────────────

	/**
	 * Sube la imagen de un videojuego. El nombre en Cloudinary es siempre el mismo para cada juego
	 * (archiveludo/videojuegos/videojuego-15), asi que una imagen nueva reemplaza a la anterior.
	 * El contenido ya debe venir validado (tipo y tamanio) por LanzadorException.
	 *
	 * @param idVideojuego id del videojuego
	 * @param contenido    bytes de la imagen
	 * @return URL segura (https) de la imagen en Cloudinary
	 * @throws AlmacenamientoArchivosException si Cloudinary no recibe el archivo
	 */
	public String subirImagenVideojuego(long idVideojuego, byte[] contenido) {
		String publicId = CARPETA_VIDEOJUEGOS + "videojuego-" + idVideojuego;
		return subirArchivo(contenido, publicId);
	}

	/**
	 * Sube el informe final de un proyecto. El nombre lleva una parte aleatoria
	 * (archiveludo/informes/informe-proyecto-6-a1b2c3d4) para que el enlace no se pueda adivinar.
	 *
	 * @param idProyecto id del proyecto
	 * @param contenido  bytes del PDF
	 * @return URL segura (https) del PDF en Cloudinary
	 * @throws AlmacenamientoArchivosException si Cloudinary no recibe el archivo
	 */
	public String subirInformeProyecto(long idProyecto, byte[] contenido) {
		String parteAleatoria = UUID.randomUUID().toString().substring(0, 8);
		String publicId = CARPETA_INFORMES + "informe-proyecto-" + idProyecto + "-" + parteAleatoria;
		return subirArchivo(contenido, publicId);
	}

	// ──────────────────────────────────────────────
	// Borrar
	// ──────────────────────────────────────────────

	/**
	 * Borra de Cloudinary el archivo de una URL guardada en la BD.
	 * Si la URL es null o no es de Cloudinary, no hace nada.
	 *
	 * @param url URL del archivo (la que se guardo en la BD)
	 * @throws AlmacenamientoArchivosException si Cloudinary no responde
	 */
	public void eliminarArchivo(String url) {
		String publicId = extraerPublicId(url);
		if (publicId == null) {
			return;
		}

		try {
			cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
					"resource_type", "image",
					"invalidate", true));
		} catch (Exception e) {
			log.error("No se pudo borrar de Cloudinary el archivo " + publicId, e);
			throw new AlmacenamientoArchivosException("No se pudo borrar el archivo en Cloudinary");
		}
	}

	// ──────────────────────────────────────────────
	// Utilidades
	// ──────────────────────────────────────────────

	/**
	 * Sube los bytes a Cloudinary con el public_id dado y devuelve la URL https.
	 * Imagenes y PDF se suben como recurso "image": Cloudinary trata el PDF como un documento
	 * y lo entrega con su extension .pdf.
	 * overwrite reemplaza el archivo si ya existe ese public_id;
	 * invalidate borra la copia vieja de la cache para que se vea la nueva.
	 */
	private String subirArchivo(byte[] contenido, String publicId) {
		try {
			Map<?, ?> resultado = cloudinary.uploader().upload(contenido, ObjectUtils.asMap(
					"public_id", publicId,
					"resource_type", "image",
					"overwrite", true,
					"invalidate", true));

			Object urlSegura = resultado.get("secure_url");
			if (urlSegura == null) {
				throw new AlmacenamientoArchivosException("Cloudinary no devolvio la URL del archivo");
			}
			return urlSegura.toString();
		} catch (AlmacenamientoArchivosException e) {
			throw e;
		} catch (Exception e) {
			log.error("No se pudo subir a Cloudinary el archivo " + publicId, e);
			throw new AlmacenamientoArchivosException("No se pudo guardar el archivo en Cloudinary");
		}
	}

	/**
	 * Saca el public_id de una URL de Cloudinary. Por ejemplo, de
	 * https://res.cloudinary.com/nube/image/upload/v1730/archiveludo/informes/informe-proyecto-6-a1b2.pdf
	 * saca archiveludo/informes/informe-proyecto-6-a1b2 (sin la version "v1730" ni la extension ".pdf").
	 *
	 * @param url URL completa del archivo
	 * @return el public_id, o null si la URL esta vacia o no es de Cloudinary
	 */
	private String extraerPublicId(String url) {
		if (url == null || url.trim().isEmpty()) {
			return null;
		}

		int posicionUpload = url.indexOf(MARCA_UPLOAD);
		if (posicionUpload < 0) {
			return null;
		}
		String ruta = url.substring(posicionUpload + MARCA_UPLOAD.length());

		// Quitar la version ("v1730/") si viene al inicio
		int primeraBarra = ruta.indexOf('/');
		if (primeraBarra > 0 && ruta.substring(0, primeraBarra).matches("v\\d+")) {
			ruta = ruta.substring(primeraBarra + 1);
		}

		// Quitar la extension (".pdf", ".jpg") del ultimo tramo
		int ultimaBarra = ruta.lastIndexOf('/');
		int ultimoPunto = ruta.lastIndexOf('.');
		if (ultimoPunto > ultimaBarra) {
			ruta = ruta.substring(0, ultimoPunto);
		}
		return ruta;
	}

}