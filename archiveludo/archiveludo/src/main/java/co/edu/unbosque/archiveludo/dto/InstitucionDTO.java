package co.edu.unbosque.archiveludo.dto;

import java.util.ArrayList;
import java.util.List;

public class InstitucionDTO {

	private long idInstitucion;
	private String nombre;
	private String tipo;
	private String pais;
	private List<Integer> telefonos;
	private List<String> correos;

	public InstitucionDTO() {
		this.telefonos = new ArrayList<Integer>();
		this.correos = new ArrayList<String>();
	}

	public InstitucionDTO(long idInstitucion, String nombre, String tipo, String pais, 
			List<Integer> telefonos, List<String> correos) {
		super();
		this.idInstitucion = idInstitucion;
		this.nombre = nombre;
		this.tipo = tipo;
		this.pais = pais;
		this.telefonos = telefonos;
		this.correos = correos;
	}

	@Override
	public String toString() {
		return "InstitucionDTO [idInstitucion=" + idInstitucion + ", nombre=" + nombre + ", tipo=" + tipo
				+ ", pais=" + pais +  ", telefonos=" + telefonos + ", correos="
				+ correos + "]";
	}

	public long getIdInstitucion() {
		return idInstitucion;
	}

	public void setIdInstitucion(long idInstitucion) {
		this.idInstitucion = idInstitucion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public List<Integer> getTelefonos() {
		return telefonos;
	}

	public void setTelefonos(List<Integer> telefonos) {
		this.telefonos = telefonos;
	}

	public List<String> getCorreos() {
		return correos;
	}

	public void setCorreos(List<String> correos) {
		this.correos = correos;
	}

}