package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

public class EstatusLegalDTO {

	private long idEstatus;
	private long idVideojuego;
	private long idUsuario;
	private String estatus;
	private LocalDate fechaDeterminacion;
	private String observacion;
	private Boolean vigente;

	public EstatusLegalDTO() {

	}

	public EstatusLegalDTO(long idEstatus, long idVideojuego, long idUsuario, String estatus,
			LocalDate fechaDeterminacion, String observacion, Boolean vigente) {
		super();
		this.idEstatus = idEstatus;
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.estatus = estatus;
		this.fechaDeterminacion = fechaDeterminacion;
		this.observacion = observacion;
		this.vigente = vigente;
	}

	@Override
	public String toString() {
		return "EstatusLegalDTO [idEstatus=" + idEstatus + ", idVideojuego=" + idVideojuego + ", idUsuario="
				+ idUsuario + ", estatus=" + estatus + ", fechaDeterminacion=" + fechaDeterminacion
				+ ", observacion=" + observacion + ", vigente=" + vigente + "]";
	}

	public long getIdEstatus() {
		return idEstatus;
	}

	public void setIdEstatus(long idEstatus) {
		this.idEstatus = idEstatus;
	}

	public long getIdVideojuego() {
		return idVideojuego;
	}

	public void setIdVideojuego(long idVideojuego) {
		this.idVideojuego = idVideojuego;
	}

	public long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}

	public LocalDate getFechaDeterminacion() {
		return fechaDeterminacion;
	}

	public void setFechaDeterminacion(LocalDate fechaDeterminacion) {
		this.fechaDeterminacion = fechaDeterminacion;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

	public Boolean getVigente() {
		return vigente;
	}

	public void setVigente(Boolean vigente) {
		this.vigente = vigente;
	}

}