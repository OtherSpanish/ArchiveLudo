package co.edu.unbosque.archiveludo.entity;

import java.util.Objects;

import co.edu.unbosque.archiveludo.model.TipoInstitucion;

/**
 * Entity de Institucion (tabla INSTITUCION). Solo tiene las columnas de su tabla.
 * Sus telefonos y correos (atributos multivaluados) estan en InstTelefono e InstCorreo.
 */
public class Institucion {

	private long idInstitucion;
	private String nombre;
	private TipoInstitucion tipo;
	private String pais;
	

	public Institucion() {

	}

	public Institucion(String nombre, TipoInstitucion tipo, String pais) {
		super();
		this.nombre = nombre;
		this.tipo = tipo;
		this.pais = pais;
	}

	public Institucion(long idInstitucion, String nombre, TipoInstitucion tipo, String pais) {
		super();
		this.idInstitucion = idInstitucion;
		this.nombre = nombre;
		this.tipo = tipo;
		this.pais = pais;
		
	}

	@Override
	public int hashCode() {
		return Objects.hash(idInstitucion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Institucion other = (Institucion) obj;
		return idInstitucion == other.idInstitucion;
	}

	@Override
	public String toString() {
		return "Institucion [idInstitucion=" + idInstitucion + ", nombre=" + nombre + ", tipo=" + tipo + ", pais="
				+ pais + "]";
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

	public TipoInstitucion getTipo() {
		return tipo;
	}

	public void setTipo(TipoInstitucion tipo) {
		this.tipo = tipo;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	
}