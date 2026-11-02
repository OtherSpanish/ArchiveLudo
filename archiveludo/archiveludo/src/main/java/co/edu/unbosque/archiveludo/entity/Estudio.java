package co.edu.unbosque.archiveludo.entity;

import java.util.Objects;

import co.edu.unbosque.archiveludo.model.EstadoEstudio;

/**
 * Entity de Estudio (tabla ESTUDIO).
 * anioCierre es Integer porque un estudio activo no tiene anio de cierre (NULL en la BD).
 */
public class Estudio {

	private long idEstudio;
	private String nombre;
	private String pais;
	private EstadoEstudio estadoActual;
	private Integer anioFundacion;
	private Integer anioCierre;

	public Estudio() {

	}

	public Estudio(String nombre, String pais, EstadoEstudio estadoActual, Integer anioFundacion, Integer anioCierre) {
		super();
		this.nombre = nombre;
		this.pais = pais;
		this.estadoActual = estadoActual;
		this.anioFundacion = anioFundacion;
		this.anioCierre = anioCierre;
	}

	public Estudio(long idEstudio, String nombre, String pais, EstadoEstudio estadoActual, Integer anioFundacion,
			Integer anioCierre) {
		super();
		this.idEstudio = idEstudio;
		this.nombre = nombre;
		this.pais = pais;
		this.estadoActual = estadoActual;
		this.anioFundacion = anioFundacion;
		this.anioCierre = anioCierre;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idEstudio);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Estudio other = (Estudio) obj;
		return idEstudio == other.idEstudio;
	}

	@Override
	public String toString() {
		return "Estudio [idEstudio=" + idEstudio + ", nombre=" + nombre + ", pais=" + pais + ", estadoActual="
				+ estadoActual + ", anioFundacion=" + anioFundacion + ", anioCierre=" + anioCierre + "]";
	}

	public long getIdEstudio() {
		return idEstudio;
	}

	public void setIdEstudio(long idEstudio) {
		this.idEstudio = idEstudio;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public EstadoEstudio getEstadoActual() {
		return estadoActual;
	}

	public void setEstadoActual(EstadoEstudio estadoEstudio) {
		this.estadoActual = estadoEstudio;
	}

	public Integer getAnioFundacion() {
		return anioFundacion;
	}

	public void setAnioFundacion(Integer anioFundacion) {
		this.anioFundacion = anioFundacion;
	}

	public Integer getAnioCierre() {
		return anioCierre;
	}

	public void setAnioCierre(Integer anioCierre) {
		this.anioCierre = anioCierre;
	}

}