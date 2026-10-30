package co.edu.unbosque.archiveludo.dto;

public class EstudioDTO {

	private long idEstado;
	private String nombre;
	private String pais;
	private String estadoActual;
	private int anioFundacion;
	private int anioCierre;
	public EstudioDTO() {
		// TODO Auto-generated constructor stub
	}
	public EstudioDTO(long idEstado, String nombre, String pais, String estadoActual, int anioFundacion,
			int anioCierre) {
		super();
		this.idEstado = idEstado;
		this.nombre = nombre;
		this.pais = pais;
		this.estadoActual = estadoActual;
		this.anioFundacion = anioFundacion;
		this.anioCierre = anioCierre;
	}
	public long getIdEstado() {
		return idEstado;
	}
	public void setIdEstado(long idEstado) {
		this.idEstado = idEstado;
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
	public int getAnioFundacion() {
		return anioFundacion;
	}
	public void setAnioFundacion(int anioFundacion) {
		this.anioFundacion = anioFundacion;
	}
	public int getAnioCierre() {
		return anioCierre;
	}
	public void setAnioCierre(int anioCierre) {
		this.anioCierre = anioCierre;
	}
	
	
	

	
}
