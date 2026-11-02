package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

public class EventoDeRiesgoDTO {

	private long idEvento;
	private long idVideojuego;
	private long idUsuario;
	private String tipoEvento;
	private LocalDate fechaEvento;
	private String descripcion;
	private String fuente;

	public EventoDeRiesgoDTO() {

	}

	public EventoDeRiesgoDTO(long idEvento, long idVideojuego, long idUsuario, String tipoEvento,
			LocalDate fechaEvento, String descripcion, String fuente) {
		super();
		this.idEvento = idEvento;
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.tipoEvento = tipoEvento;
		this.fechaEvento = fechaEvento;
		this.descripcion = descripcion;
		this.fuente = fuente;
	}

	@Override
	public String toString() {
		return "EventoDeRiesgoDTO [idEvento=" + idEvento + ", idVideojuego=" + idVideojuego + ", idUsuario="
				+ idUsuario + ", tipoEvento=" + tipoEvento + ", fechaEvento=" + fechaEvento + ", descripcion="
				+ descripcion + ", fuente=" + fuente + "]";
	}

	public long getIdEvento() {
		return idEvento;
	}

	public void setIdEvento(long idEvento) {
		this.idEvento = idEvento;
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

	public String getTipoEvento() {
		return tipoEvento;
	}

	public void setTipoEvento(String tipoEvento) {
		this.tipoEvento = tipoEvento;
	}

	public LocalDate getFechaEvento() {
		return fechaEvento;
	}

	public void setFechaEvento(LocalDate fechaEvento) {
		this.fechaEvento = fechaEvento;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getFuente() {
		return fuente;
	}

	public void setFuente(String fuente) {
		this.fuente = fuente;
	}

}