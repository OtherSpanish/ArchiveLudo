package co.edu.unbosque.archiveludo.entity;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity de Participacion (tabla PARTICIPACION).
 * Une un experto con un proyecto ("Incluir" y "Realizar"), con su rol y su periodo de trabajo.
 * fechaSalida en null significa que el experto sigue en el proyecto.
 */
public class Participacion {

	private long idParticipacion;
	private long idProyecto;
	private long idExperto;
	private String rolExperto;
	private LocalDate fechaIncorporacion;
	private LocalDate fechaSalida;

	public Participacion() {

	}

	public Participacion(long idProyecto, long idExperto, String rolExperto, LocalDate fechaIncorporacion,
			LocalDate fechaSalida) {
		super();
		this.idProyecto = idProyecto;
		this.idExperto = idExperto;
		this.rolExperto = rolExperto;
		this.fechaIncorporacion = fechaIncorporacion;
		this.fechaSalida = fechaSalida;
	}

	public Participacion(long idParticipacion, long idProyecto, long idExperto, String rolExperto,
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
	public int hashCode() {
		return Objects.hash(idParticipacion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Participacion other = (Participacion) obj;
		return idParticipacion == other.idParticipacion;
	}

	@Override
	public String toString() {
		return "Participacion [idParticipacion=" + idParticipacion + ", idProyecto=" + idProyecto + ", idExperto="
				+ idExperto + ", rolExperto=" + rolExperto + ", fechaIncorporacion=" + fechaIncorporacion
				+ ", fechaSalida=" + fechaSalida + "]";
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