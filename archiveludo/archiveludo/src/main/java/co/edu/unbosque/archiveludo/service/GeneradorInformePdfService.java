package co.edu.unbosque.archiveludo.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import co.edu.unbosque.archiveludo.dto.ExpertoDTO;
import co.edu.unbosque.archiveludo.dto.InformeFinalDTO;
import co.edu.unbosque.archiveludo.dto.ParticipacionDTO;
import co.edu.unbosque.archiveludo.dto.ProyectoDeRestauracionDTO;
import co.edu.unbosque.archiveludo.dto.VideojuegoDTO;
import co.edu.unbosque.archiveludo.exceptions.GeneracionPdfException;
import co.edu.unbosque.archiveludo.model.ResultadoInforme;

/**
 * Genera el PDF del informe final de un proyecto de restauracion con OpenPDF.
 * El documento incluye los datos del proyecto, del videojuego, del resultado
 * y de los expertos que participaron. El enlace al documento no va dentro del PDF.
 */
@Service
public class GeneradorInformePdfService {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Bogota");

	private static final Color COLOR_ENCABEZADO = new Color(40, 44, 52);
	private static final Color COLOR_ETIQUETA = new Color(235, 237, 240);

	private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
	private static final Font FUENTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL,
			Color.DARK_GRAY);
	private static final Font FUENTE_SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
	private static final Font FUENTE_ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
	private static final Font FUENTE_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10);
	private static final Font FUENTE_ENCABEZADO_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10,
			Font.NORMAL, Color.WHITE);
	private static final Font FUENTE_PIE = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.GRAY);

	/**
	 * Construye el PDF del informe final.
	 *
	 * @param proyecto    proyecto finalizado, con sus participaciones
	 * @param videojuego  videojuego del proyecto
	 * @param expertos    expertos del proyecto, con el idExperto como llave
	 * @param informe     datos que el usuario lleno en el formulario
	 * @param generadoPor correo del usuario que genera el informe
	 * @return los bytes del PDF
	 * @throws GeneracionPdfException si OpenPDF no puede construir el documento
	 */
	public byte[] generar(ProyectoDeRestauracionDTO proyecto, VideojuegoDTO videojuego, Map<Long, ExpertoDTO> expertos,
			InformeFinalDTO informe, String generadoPor) {
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		Document documento = new Document(PageSize.A4, 50, 50, 50, 50);

		try {
			PdfWriter.getInstance(documento, salida);
			documento.open();

			agregarTitulo(documento, proyecto);
			agregarSeccionProyecto(documento, proyecto);
			agregarSeccionVideojuego(documento, videojuego);
			agregarSeccionResultado(documento, informe);
			agregarSeccionExpertos(documento, proyecto.getParticipaciones(), expertos);
			agregarPie(documento, generadoPor);

			documento.close();
		} catch (DocumentException e) {
			throw new GeneracionPdfException(proyecto.getIdProyecto());
		}

		return salida.toByteArray();
	}

	private void agregarTitulo(Document documento, ProyectoDeRestauracionDTO proyecto) throws DocumentException {
		Paragraph titulo = new Paragraph("Informe final de restauración", FUENTE_TITULO);
		titulo.setAlignment(Element.ALIGN_CENTER);
		documento.add(titulo);

		Paragraph subtitulo = new Paragraph(proyecto.getNombre(), FUENTE_SUBTITULO);
		subtitulo.setAlignment(Element.ALIGN_CENTER);
		subtitulo.setSpacingAfter(4);
		documento.add(subtitulo);

		Paragraph archivo = new Paragraph("ArchiveLudo - Archivo de preservación de videojuegos", FUENTE_PIE);
		archivo.setAlignment(Element.ALIGN_CENTER);
		archivo.setSpacingAfter(10);
		documento.add(archivo);
	}

	private void agregarSeccionProyecto(Document documento, ProyectoDeRestauracionDTO proyecto)
			throws DocumentException {
		agregarTituloSeccion(documento, "Proyecto");

		PdfPTable tabla = crearTablaDatos();
		agregarFila(tabla, "Nombre", proyecto.getNombre());
		agregarFila(tabla, "Descripción", textoOpcional(proyecto.getDescripcion(), "Sin descripción"));
		agregarFila(tabla, "Herramienta o emulador", textoOpcional(proyecto.getHerramientaEmulador(), "No registrada"));
		agregarFila(tabla, "Estado", proyecto.getEstado());
		agregarFila(tabla, "Fecha de inicio", formatearFecha(proyecto.getFechaInicio(), "No registrada"));
		agregarFila(tabla, "Fecha de fin", formatearFecha(proyecto.getFechaFin(), "No registrada"));
		documento.add(tabla);
	}

	private void agregarSeccionVideojuego(Document documento, VideojuegoDTO videojuego) throws DocumentException {
		agregarTituloSeccion(documento, "Videojuego");

		PdfPTable tabla = crearTablaDatos();
		agregarFila(tabla, "Título", videojuego.getTitulo());
		agregarFila(tabla, "Plataforma original", textoOpcional(videojuego.getPlataformaOriginal(), "No registrada"));
		agregarFila(tabla, "Año de lanzamiento", String.valueOf(videojuego.getAnioLanzamiento()));
		agregarFila(tabla, "Estado de conservación", textoOpcional(videojuego.getEstadoConservacion(), "No registrado"));
		documento.add(tabla);
	}

	private void agregarSeccionResultado(Document documento, InformeFinalDTO informe) throws DocumentException {
		agregarTituloSeccion(documento, "Resultado");

		ResultadoInforme resultado = ResultadoInforme.buscarPorValor(informe.getResultado());
		String textoResultado = informe.getResultado();
		if (resultado != null) {
			textoResultado = resultado.getTextoLegible();
		}

		PdfPTable tabla = crearTablaDatos();
		agregarFila(tabla, "Resultado", textoResultado);
		agregarFila(tabla, "Fecha de publicación", formatearFecha(informe.getFechaPublicacion(), "No registrada"));
		documento.add(tabla);

		Paragraph tituloResumen = new Paragraph("Resumen de resultados", FUENTE_ETIQUETA);
		tituloResumen.setSpacingBefore(8);
		tituloResumen.setSpacingAfter(4);
		documento.add(tituloResumen);

		Paragraph resumen = new Paragraph(informe.getResumenResultados(), FUENTE_TEXTO);
		resumen.setAlignment(Element.ALIGN_JUSTIFIED);
		documento.add(resumen);
	}

	private void agregarSeccionExpertos(Document documento, List<ParticipacionDTO> participaciones,
			Map<Long, ExpertoDTO> expertos) throws DocumentException {
		agregarTituloSeccion(documento, "Expertos participantes");

		PdfPTable tabla = new PdfPTable(new float[] { 2.2f, 2.2f, 1.8f, 1.2f, 1.2f });
		tabla.setWidthPercentage(100);
		tabla.setHeaderRows(1);

		agregarEncabezado(tabla, "Experto");
		agregarEncabezado(tabla, "Especialidad");
		agregarEncabezado(tabla, "Rol");
		agregarEncabezado(tabla, "Incorporación");
		agregarEncabezado(tabla, "Salida");

		if (participaciones != null) {
			for (ParticipacionDTO participacion : participaciones) {
				ExpertoDTO experto = expertos.get(participacion.getIdExperto());

				String nombre = "Experto " + participacion.getIdExperto();
				String especialidad = "No registrada";
				if (experto != null) {
					nombre = construirNombreExperto(experto);
					especialidad = experto.getEspecialidad();
				}

				agregarCelda(tabla, nombre);
				agregarCelda(tabla, especialidad);
				agregarCelda(tabla, participacion.getRolExperto());
				agregarCelda(tabla, formatearFecha(participacion.getFechaIncorporacion(), "-"));
				agregarCelda(tabla, formatearFecha(participacion.getFechaSalida(), "Hasta el final"));
			}
		}

		documento.add(tabla);
	}

	private void agregarPie(Document documento, String generadoPor) throws DocumentException {
		String fechaHora = LocalDateTime.now(ZONA_HORARIA).format(FORMATO_FECHA_HORA);

		Paragraph pie = new Paragraph("Generado por " + generadoPor + " el " + fechaHora, FUENTE_PIE);
		pie.setAlignment(Element.ALIGN_RIGHT);
		pie.setSpacingBefore(20);
		documento.add(pie);
	}

	private void agregarTituloSeccion(Document documento, String texto) throws DocumentException {
		Paragraph seccion = new Paragraph(texto, FUENTE_SECCION);
		seccion.setSpacingBefore(14);
		seccion.setSpacingAfter(6);
		documento.add(seccion);
	}

	/**
	 * Tabla de dos columnas: etiqueta a la izquierda y valor a la derecha.
	 */
	private PdfPTable crearTablaDatos() {
		PdfPTable tabla = new PdfPTable(new float[] { 1.3f, 3f });
		tabla.setWidthPercentage(100);
		return tabla;
	}

	private void agregarFila(PdfPTable tabla, String etiqueta, String valor) {
		PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, FUENTE_ETIQUETA));
		celdaEtiqueta.setBackgroundColor(COLOR_ETIQUETA);
		celdaEtiqueta.setPadding(5);
		tabla.addCell(celdaEtiqueta);

		agregarCelda(tabla, valor);
	}

	private void agregarEncabezado(PdfPTable tabla, String texto) {
		PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_ENCABEZADO_TABLA));
		celda.setBackgroundColor(COLOR_ENCABEZADO);
		celda.setPadding(5);
		tabla.addCell(celda);
	}

	private void agregarCelda(PdfPTable tabla, String texto) {
		PdfPCell celda = new PdfPCell(new Phrase(textoOpcional(texto, "-"), FUENTE_TEXTO));
		celda.setPadding(5);
		tabla.addCell(celda);
	}

	/**
	 * Une las partes del nombre del experto que vengan con contenido.
	 * Solo se usa para mostrarlo en el PDF; no es un atributo del DTO.
	 */
	private String construirNombreExperto(ExpertoDTO experto) {
		StringBuilder nombre = new StringBuilder();
		agregarParte(nombre, experto.getPrimerNombreEx());
		agregarParte(nombre, experto.getSegundoNombreEx());
		agregarParte(nombre, experto.getPrimerApellidoEx());
		agregarParte(nombre, experto.getSegundoApellidoEx());
		return nombre.toString();
	}

	private void agregarParte(StringBuilder nombre, String parte) {
		if (parte != null && !parte.trim().isEmpty()) {
			if (nombre.length() > 0) {
				nombre.append(" ");
			}
			nombre.append(parte.trim());
		}
	}

	private String textoOpcional(String valor, String porDefecto) {
		if (valor == null || valor.trim().isEmpty()) {
			return porDefecto;
		}
		return valor;
	}

	private String formatearFecha(LocalDate fecha, String porDefecto) {
		if (fecha == null) {
			return porDefecto;
		}
		return fecha.format(FORMATO_FECHA);
	}

}