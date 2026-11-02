package co.edu.unbosque.archiveludo.entity;

import java.util.Objects;

/**
 * Entity de InstCorreo (tabla INST_CORREO): un correo de una institucion.
 * Su llave primaria es compuesta (idInstitucion, correo): no tiene id propio.
 */
public class InstCorreo {

	private long idInstitucion;
	private String correo;

	public InstCorreo() {

	}

	public InstCorreo(long idInstitucion, String correo) {
		super();
		this.idInstitucion = idInstitucion;
		this.correo = correo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idInstitucion, correo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InstCorreo other = (InstCorreo) obj;
		return idInstitucion == other.idInstitucion && Objects.equals(correo, other.correo);
	}

	@Override
	public String toString() {
		return "InstCorreo [idInstitucion=" + idInstitucion + ", correo=" + correo + "]";
	}

	public long getIdInstitucion() {
		return idInstitucion;
	}

	public void setIdInstitucion(long idInstitucion) {
		this.idInstitucion = idInstitucion;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

}