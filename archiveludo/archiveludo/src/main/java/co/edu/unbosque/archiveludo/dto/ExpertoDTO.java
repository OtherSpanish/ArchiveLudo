package co.edu.unbosque.archiveludo.dto;

/**
 * Datos de un experto que viajan por HTTP. Tiene los mismos atributos que Experto.
 */
public class ExpertoDTO {

	private long idExperto;
	private String primerNombreEx;
	private String segundoNombreEx;
	private String primerApellidoEx;
	private String segundoApellidoEx;
	private String especialidad;
	private String afiliacion;
	private String correo;

	public ExpertoDTO() {

	}

	public ExpertoDTO(long idExperto, String primerNombreEx, String segundoNombreEx, String primerApellidoEx,
			String segundoApellidoEx, String especialidad, String afiliacion, String correo) {
		super();
		this.idExperto = idExperto;
		this.primerNombreEx = primerNombreEx;
		this.segundoNombreEx = segundoNombreEx;
		this.primerApellidoEx = primerApellidoEx;
		this.segundoApellidoEx = segundoApellidoEx;
		this.especialidad = especialidad;
		this.afiliacion = afiliacion;
		this.correo = correo;
	}

	@Override
	public String toString() {
		return "ExpertoDTO [idExperto=" + idExperto + ", primerNombreEx=" + primerNombreEx + ", segundoNombreEx="
				+ segundoNombreEx + ", primerApellidoEx=" + primerApellidoEx + ", segundoApellidoEx="
				+ segundoApellidoEx + ", especialidad=" + especialidad + ", afiliacion=" + afiliacion + ", correo="
				+ correo + "]";
	}

	public long getIdExperto() {
		return idExperto;
	}

	public void setIdExperto(long idExperto) {
		this.idExperto = idExperto;
	}

	public String getPrimerNombreEx() {
		return primerNombreEx;
	}

	public void setPrimerNombreEx(String primerNombreEx) {
		this.primerNombreEx = primerNombreEx;
	}

	public String getSegundoNombreEx() {
		return segundoNombreEx;
	}

	public void setSegundoNombreEx(String segundoNombreEx) {
		this.segundoNombreEx = segundoNombreEx;
	}

	public String getPrimerApellidoEx() {
		return primerApellidoEx;
	}

	public void setPrimerApellidoEx(String primerApellidoEx) {
		this.primerApellidoEx = primerApellidoEx;
	}

	public String getSegundoApellidoEx() {
		return segundoApellidoEx;
	}

	public void setSegundoApellidoEx(String segundoApellidoEx) {
		this.segundoApellidoEx = segundoApellidoEx;
	}

	public String getEspecialidad() {
		return especialidad;
	}

	public void setEspecialidad(String especialidad) {
		this.especialidad = especialidad;
	}

	public String getAfiliacion() {
		return afiliacion;
	}

	public void setAfiliacion(String afiliacion) {
		this.afiliacion = afiliacion;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

}