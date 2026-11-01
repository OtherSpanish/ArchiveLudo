package co.edu.unbosque.archiveludo.dto;

public class EstudioDTO {

	private long idEstudio;
	private String nombre;
	private String pais;
	private String estadoActual;
	private Integer anioFundacion;
	private Integer anioCierre;

	public EstudioDTO() {

	}

	public EstudioDTO(long idEstudio, String nombre, String pais, String estadoActual, Integer anioFundacion,
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
	public String toString() {
		return "EstudioDTO [idEstudio=" + idEstudio + ", nombre=" + nombre + ", pais=" + pais + ", estadoActual="
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

	public String getEstadoActual() {
		return estadoActual;
	}

	public void setEstadoActual(String estadoActual) {
		this.estadoActual = estadoActual;
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