package co.edu.unbosque.archiveludo.entity;

import java.util.Objects;

/**
 * Entity de InstTelefono (tabla INST_TELEFONO): un telefono de una institucion.
 * Su llave primaria es compuesta (idInstitucion, telefono): no tiene id propio.
 */
public class InstTelefono {

	private long idInstitucion;
	private long telefono;

	public InstTelefono() {

	}

	public InstTelefono(long idInstitucion, long telefono) {
		super();
		this.idInstitucion = idInstitucion;
		this.telefono = telefono;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idInstitucion, telefono);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InstTelefono other = (InstTelefono) obj;
		return idInstitucion == other.idInstitucion && telefono == other.telefono;
	}

	@Override
	public String toString() {
		return "InstTelefono [idInstitucion=" + idInstitucion + ", telefono=" + telefono + "]";
	}

	public long getIdInstitucion() {
		return idInstitucion;
	}

	public void setIdInstitucion(long idInstitucion) {
		this.idInstitucion = idInstitucion;
	}

	public long getTelefono() {
		return telefono;
	}

	public void setTelefono(long telefono) {
		this.telefono = telefono;
	}

}