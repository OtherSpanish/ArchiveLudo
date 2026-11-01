package co.edu.unbosque.archiveludo.dto;

public class CompaniaEditoraDTO {

	private long idEditora;
	private String nombre;
	private String pais;
	public CompaniaEditoraDTO() {
		// TODO Auto-generated constructor stub
	}
	public long getIdEditora() {
		return idEditora;
	}
	public void setIdEditora(long idEditora) {
		this.idEditora = idEditora;
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
	@Override
	public String toString() {
		return "CompaniaEditora [idEditora=" + idEditora + ", nombre=" + nombre + ", pais=" + pais + "]";
	}
	public CompaniaEditoraDTO(long idEditora, String nombre, String pais) {
		super();
		this.idEditora = idEditora;
		this.nombre = nombre;
		this.pais = pais;
	}
}
