package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

/**
 * Datos de una participacion que viajan por HTTP. Tiene los mismos atributos que Participacion.
 */
public class ParticipacionDTO {

	private long idParticipacion;
	private long idProyecto;
	private long idExperto;
	private String rolExperto;
	private LocalDate fechaIncorporacion;
	private LocalDate fechaSalida;

	public ParticipacionDTO() {

	}

	public ParticipacionDTO(long idParticipacion, long idProyecto, long idExperto, String rolExperto,
			LocalDate fechaIncorporacion, LocalDate fechaSalida) {
		super();
		this.idParticipacion = idParticipacion;
		this.idProyecto = idProyecto;
		this.idExperto = idExperto;
		this.rolExperto = rolExperto;
		this.fechaIncorporacion = fechaIncorporacion;
		this.fechaSalida = fechaSalida;
	}

	@Override
	public String toString() {
		return "ParticipacionDTO [idParticipacion=" + idParticipacion + ", idProyecto=" + idProyecto
				+ ", idExperto=" + idExperto + ", rolExperto=" + rolExperto + ", fechaIncorporacion="
				+ fechaIncorporacion + ", fechaSalida=" + fechaSalida + "]";
	}

	public long getIdParticipacion() {
		return idParticipacion;
	}

	public void setIdParticipacion(long idParticipacion) {
		this.idParticipacion = idParticipacion;
	}

	public long getIdProyecto() {
		return idProyecto;
	}

	public void setIdProyecto(long idProyecto) {
		this.idProyecto = idProyecto;
	}

	public long getIdExperto() {
		return idExperto;
	}

	public void setIdExperto(long idExperto) {
		this.idExperto = idExperto;
	}

	public String getRolExperto() {
		return rolExperto;
	}

	public void setRolExperto(String rolExperto) {
		this.rolExperto = rolExperto;
	}

	public LocalDate getFechaIncorporacion() {
		return fechaIncorporacion;
	}

	public void setFechaIncorporacion(LocalDate fechaIncorporacion) {
		this.fechaIncorporacion = fechaIncorporacion;
	}

	public LocalDate getFechaSalida() {
		return fechaSalida;
	}

	public void setFechaSalida(LocalDate fechaSalida) {
		this.fechaSalida = fechaSalida;
	}

}