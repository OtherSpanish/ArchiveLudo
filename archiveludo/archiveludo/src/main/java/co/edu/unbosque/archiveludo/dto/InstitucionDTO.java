package co.edu.unbosque.archiveludo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos de una institucion que viajan por HTTP.
 * Ademas de las columnas de INSTITUCION trae sus listas de telefonos y correos (INST_TELEFONO e INST_CORREO)
 * y cantidadCopiasCustodiadas, que el service calcula con las custodias vigentes.
 */
public class InstitucionDTO {

	private long idInstitucion;
	private String nombre;
	private String tipo;
	private String pais;
	private List<Long> telefonos;
	private List<String> correos;
	private long cantidadCopiasCustodiadas;

	public InstitucionDTO() {
		this.telefonos = new ArrayList<Long>();
		this.correos = new ArrayList<String>();
	}

	public InstitucionDTO(long idInstitucion, String nombre, String tipo, String pais, List<Long> telefonos,
			List<String> correos, long cantidadCopiasCustodiadas) {
		super();
		this.idInstitucion = idInstitucion;
		this.nombre = nombre;
		this.tipo = tipo;
		this.pais = pais;
		this.telefonos = telefonos;
		this.correos = correos;
		this.cantidadCopiasCustodiadas = cantidadCopiasCustodiadas;
	}

	@Override
	public String toString() {
		return "InstitucionDTO [idInstitucion=" + idInstitucion + ", nombre=" + nombre + ", tipo=" + tipo
				+ ", pais=" + pais + ", telefonos=" + telefonos + ", correos=" + correos
				+ ", cantidadCopiasCustodiadas=" + cantidadCopiasCustodiadas + "]";
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

	public List<Long> getTelefonos() {
		return telefonos;
	}

	public void setTelefonos(List<Long> telefonos) {
		this.telefonos = telefonos;
	}

	public List<String> getCorreos() {
		return correos;
	}

	public void setCorreos(List<String> correos) {
		this.correos = correos;
	}

	public long getCantidadCopiasCustodiadas() {
		return cantidadCopiasCustodiadas;
	}

	public void setCantidadCopiasCustodiadas(long cantidadCopiasCustodiadas) {
		this.cantidadCopiasCustodiadas = cantidadCopiasCustodiadas;
	}

}