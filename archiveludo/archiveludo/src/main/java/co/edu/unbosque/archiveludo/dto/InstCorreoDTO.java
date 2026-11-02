package co.edu.unbosque.archiveludo.dto;

public class InstCorreoDTO {

	private long idInstitucion;
	private String correo;

	public InstCorreoDTO() {

	}

	public InstCorreoDTO(long idInstitucion, String correo) {
		super();
		this.idInstitucion = idInstitucion;
		this.correo = correo;
	}

	@Override
	public String toString() {
		return "InstCorreoDTO [idInstitucion=" + idInstitucion + ", correo=" + correo + "]";
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