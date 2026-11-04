package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

/**
 * Datos del informe final de un proyecto de restauracion.
 * Los datos del formulario solo se usan para generar el PDF; en la base
 * de datos queda guardado unicamente el enlace (urlInforme).
 */
public class InformeFinalDTO {

	private long idProyecto;
	private LocalDate fechaPublicacion;
	private String resumenResultados;
	private String resultado;
	private String urlInforme;

	public InformeFinalDTO() {
		// TODO Auto-generated constructor stub
	}

	public InformeFinalDTO(LocalDate fechaPublicacion, String resumenResultados, String resultado) {
		super();
		this.fechaPublicacion = fechaPublicacion;
		this.resumenResultados = resumenResultados;
		this.resultado = resultado;
	}

	public InformeFinalDTO(long idProyecto, LocalDate fechaPublicacion, String resumenResultados, String resultado,
			String urlInforme) {
		super();
		this.idProyecto = idProyecto;
		this.fechaPublicacion = fechaPublicacion;
		this.resumenResultados = resumenResultados;
		this.resultado = resultado;
		this.urlInforme = urlInforme;
	}

	@Override
	public String toString() {
		return "InformeFinalDTO [idProyecto=" + idProyecto + ", fechaPublicacion=" + fechaPublicacion
				+ ", resumenResultados=" + resumenResultados + ", resultado=" + resultado + ", urlInforme="
				+ urlInforme + "]";
	}

	public long getIdProyecto() {
		return idProyecto;
	}

	public void setIdProyecto(long idProyecto) {
		this.idProyecto = idProyecto;
	}

	public LocalDate getFechaPublicacion() {
		return fechaPublicacion;
	}

	public void setFechaPublicacion(LocalDate fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}

	public String getResumenResultados() {
		return resumenResultados;
	}

	public void setResumenResultados(String resumenResultados) {
		this.resumenResultados = resumenResultados;
	}

	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	public String getUrlInforme() {
		return urlInforme;
	}

	public void setUrlInforme(String urlInforme) {
		this.urlInforme = urlInforme;
	}

}