package co.edu.unbosque.archiveludo.entity;

import java.util.Objects;

public class CompaniaEditora {

	private long idEditora;
	private String nombre;
	private String pais;
	
	public CompaniaEditora() {
		// TODO Auto-generated constructor stub
	}

	public CompaniaEditora(long idEditora, String nombre, String pais) {
		super();
		this.idEditora = idEditora;
		this.nombre = nombre;
		this.pais = pais;
	}

	public CompaniaEditora(String nombre, String pais) {
		super();
		this.nombre = nombre;
		this.pais = pais;
	} 
	

	@Override
	public int hashCode() {
		return Objects.hash(idEditora);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompaniaEditora other = (CompaniaEditora) obj;
		return Objects.equals(idEditora, other.idEditora);
	}

	@Override
	public String toString() {
		return "CompaniaEditora [idEditora=" + idEditora + ", nombre=" + nombre + ", pais=" + pais + "]";
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
	 
}
