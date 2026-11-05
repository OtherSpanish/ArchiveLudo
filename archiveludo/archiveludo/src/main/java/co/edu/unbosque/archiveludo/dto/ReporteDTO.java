package co.edu.unbosque.archiveludo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla de un reporte, lista para que el front la pinte.
 * Cada fila tiene un valor por columna, en el mismo orden de "columnas", ya como texto legible.
 * No corresponde a una tabla de la BD: lo arma ReporteService.
 */
public class ReporteDTO {

	private String titulo;
	private List<String> columnas;
	private List<List<String>> filas;

	public ReporteDTO() {
		this.columnas = new ArrayList<String>();
		this.filas = new ArrayList<List<String>>();
	}

	public ReporteDTO(String titulo, List<String> columnas, List<List<String>> filas) {
		super();
		this.titulo = titulo;
		this.columnas = columnas;
		this.filas = filas;
	}

	@Override
	public String toString() {
		return "ReporteDTO [titulo=" + titulo + ", columnas=" + columnas + ", filas=" + filas + "]";
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public List<String> getColumnas() {
		return columnas;
	}

	public void setColumnas(List<String> columnas) {
		this.columnas = columnas;
	}

	public List<List<String>> getFilas() {
		return filas;
	}

	public void setFilas(List<List<String>> filas) {
		this.filas = filas;
	}

}