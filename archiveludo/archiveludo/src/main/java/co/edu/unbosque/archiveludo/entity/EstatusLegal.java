package co.edu.unbosque.archiveludo.entity;

import java.time.LocalDate;
import java.util.Objects;

import co.edu.unbosque.archiveludo.model.TipoEstatusLegal;

/**
 * Entity de EstatusLegal (tabla ESTATUS_LEGAL).
 * Un videojuego tiene varios estatus a lo largo del tiempo; el vigente NO se guarda,
 * se calcula: es el de fechaDeterminacion mas reciente.
 */
public class EstatusLegal {

	private long idEstatus;
	private long idVideojuego;
	private long idUsuario;
	private TipoEstatusLegal estatus;
	private LocalDate fechaDeterminacion;
	private String observacion;

	public EstatusLegal() {

	}

	public EstatusLegal(long idVideojuego, long idUsuario, TipoEstatusLegal estatus, LocalDate fechaDeterminacion,
			String observacion) {
		super();
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.estatus = estatus;
		this.fechaDeterminacion = fechaDeterminacion;
		this.observacion = observacion;
	}

	public EstatusLegal(long idEstatus, long idVideojuego, long idUsuario, TipoEstatusLegal estatus,
			LocalDate fechaDeterminacion, String observacion) {
		super();
		this.idEstatus = idEstatus;
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.estatus = estatus;
		this.fechaDeterminacion = fechaDeterminacion;
		this.observacion = observacion;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idEstatus);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EstatusLegal other = (EstatusLegal) obj;
		return idEstatus == other.idEstatus;
	}

	@Override
	public String toString() {
		return "EstatusLegal [idEstatus=" + idEstatus + ", idVideojuego=" + idVideojuego + ", idUsuario=" + idUsuario
				+ ", estatus=" + estatus + ", fechaDeterminacion=" + fechaDeterminacion + ", observacion="
				+ observacion + "]";
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

	public TipoEstatusLegal getEstatus() {
		return estatus;
	}

	public void setEstatus(TipoEstatusLegal estatus) {
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

}