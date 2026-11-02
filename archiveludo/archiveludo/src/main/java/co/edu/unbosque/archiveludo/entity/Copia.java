package co.edu.unbosque.archiveludo.entity;

import java.time.LocalDate;
import java.util.Objects;

import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.model.TipoCopia;

/**
 * Entity de Copia (tabla COPIA): los atributos comunes a toda copia.
 * Es abstracta porque toda copia es de uno de sus subtipos (especializacion disjunta y total):
 * CopiaFisica o CopiaDigital. tipoCopia indica cual, y lo asignan los constructores de cada subtipo.
 * idUsuario es el usuario que registro la copia.
 */
public abstract class Copia {

	private long idCopia;
	private long idVideojuego;
	private long idUsuario;
	private TipoCopia tipoCopia;
	private EstadoIntegridad estadoIntegridad;
	private LocalDate fechaRegistro;

	public Copia() {

	}

	public Copia(long idVideojuego, long idUsuario, TipoCopia tipoCopia, EstadoIntegridad estadoIntegridad,
			LocalDate fechaRegistro) {
		super();
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.tipoCopia = tipoCopia;
		this.estadoIntegridad = estadoIntegridad;
		this.fechaRegistro = fechaRegistro;
	}

	public Copia(long idCopia, long idVideojuego, long idUsuario, TipoCopia tipoCopia,
			EstadoIntegridad estadoIntegridad, LocalDate fechaRegistro) {
		super();
		this.idCopia = idCopia;
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.tipoCopia = tipoCopia;
		this.estadoIntegridad = estadoIntegridad;
		this.fechaRegistro = fechaRegistro;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idCopia);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Copia other = (Copia) obj;
		return idCopia == other.idCopia;
	}

	@Override
	public String toString() {
		return "Copia [idCopia=" + idCopia + ", idVideojuego=" + idVideojuego + ", idUsuario=" + idUsuario
				+ ", tipoCopia=" + tipoCopia + ", estadoIntegridad=" + estadoIntegridad + ", fechaRegistro="
				+ fechaRegistro + "]";
	}

	public long getIdCopia() {
		return idCopia;
	}

	public void setIdCopia(long idCopia) {
		this.idCopia = idCopia;
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

	public TipoCopia getTipoCopia() {
		return tipoCopia;
	}

	public void setTipoCopia(TipoCopia tipoCopia) {
		this.tipoCopia = tipoCopia;
	}

	public EstadoIntegridad getEstadoIntegridad() {
		return estadoIntegridad;
	}

	public void setEstadoIntegridad(EstadoIntegridad estadoIntegridad) {
		this.estadoIntegridad = estadoIntegridad;
	}

	public LocalDate getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(LocalDate fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

}