package co.edu.unbosque.archiveludo.dto;

public class InstTelefonoDTO {

	private long idInstitucion;
	private long telefono;

	public InstTelefonoDTO() {

	}

	public InstTelefonoDTO(long idInstitucion, long telefono) {
		super();
		this.idInstitucion = idInstitucion;
		this.telefono = telefono;
	}

	@Override
	public String toString() {
		return "InstTelefonoDTO [idInstitucion=" + idInstitucion + ", telefono=" + telefono + "]";
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