package co.edu.unbosque.archiveludo.entity;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity de Custodia (tabla CUSTODIA).
 * Indica que una copia estuvo (o esta) guardada en una institucion durante un periodo.
 * fechaFin en null significa que es la custodia vigente.
 */
public class Custodia {

	private long idCustodia;
	private long idCopia;
	private long idInstitucion;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private String condicionAlmacenamiento;

	public Custodia() {

	}

	public Custodia(long idCopia, long idInstitucion, LocalDate fechaInicio, LocalDate fechaFin,
			String condicionAlmacenamiento) {
		super();
		this.idCopia = idCopia;
		this.idInstitucion = idInstitucion;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.condicionAlmacenamiento = condicionAlmacenamiento;
	}

	public Custodia(long idCustodia, long idCopia, long idInstitucion, LocalDate fechaInicio, LocalDate fechaFin,
			String condicionAlmacenamiento) {
		super();
		this.idCustodia = idCustodia;
		this.idCopia = idCopia;
		this.idInstitucion = idInstitucion;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.condicionAlmacenamiento = condicionAlmacenamiento;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idCustodia);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Custodia other = (Custodia) obj;
		return idCustodia == other.idCustodia;
	}

	@Override
	public String toString() {
		return "Custodia [idCustodia=" + idCustodia + ", idCopia=" + idCopia + ", idInstitucion=" + idInstitucion
				+ ", fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin + ", condicionAlmacenamiento="
				+ condicionAlmacenamiento + "]";
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