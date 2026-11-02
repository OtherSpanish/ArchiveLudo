package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

/**
 * Datos de una custodia que viajan por HTTP.
 * Tiene los mismos atributos que Custodia y ademas nombreInstitucion,
 * que el service completa para que el front no tenga que pedir la institucion aparte.
 */
public class CustodiaDTO {

	private long idCustodia;
	private long idCopia;
	private long idInstitucion;
	private String nombreInstitucion;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private String condicionAlmacenamiento;

	public CustodiaDTO() {

	}

	public CustodiaDTO(long idCustodia, long idCopia, long idInstitucion, String nombreInstitucion,
			LocalDate fechaInicio, LocalDate fechaFin, String condicionAlmacenamiento) {
		super();
		this.idCustodia = idCustodia;
		this.idCopia = idCopia;
		this.idInstitucion = idInstitucion;
		this.nombreInstitucion = nombreInstitucion;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.condicionAlmacenamiento = condicionAlmacenamiento;
	}

	@Override
	public String toString() {
		return "CustodiaDTO [idCustodia=" + idCustodia + ", idCopia=" + idCopia + ", idInstitucion="
				+ idInstitucion + ", nombreInstitucion=" + nombreInstitucion + ", fechaInicio=" + fechaInicio
				+ ", fechaFin=" + fechaFin + ", condicionAlmacenamiento=" + condicionAlmacenamiento + "]";
	}

	public long getIdCustodia() {
		return idCustodia;
	}

	public void setIdCustodia(long idCustodia) {
		this.idCustodia = idCustodia;
	}

	public long getIdCopia() {
		return idCopia;
	}

	public void setIdCopia(long idCopia) {
		this.idCopia = idCopia;
	}

	public long getIdInstitucion() {
		return idInstitucion;
	}

	public void setIdInstitucion(long idInstitucion) {
		this.idInstitucion = idInstitucion;
	}

	public String getNombreInstitucion() {
		return nombreInstitucion;
	}

	public void setNombreInstitucion(String nombreInstitucion) {
		this.nombreInstitucion = nombreInstitucion;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}

	public String getCondicionAlmacenamiento() {
		return condicionAlmacenamiento;
	}

	public void setCondicionAlmacenamiento(String condicionAlmacenamiento) {
		this.condicionAlmacenamiento = condicionAlmacenamiento;
	}

}