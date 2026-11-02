package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * DTO con los datos comunes de toda copia. Sus subtipos son CopiaFisicaDTO y CopiaDigitalDTO.
 * El campo "tipoCopia" del JSON decide que clase crea Jackson:
 * "fisica" -> CopiaFisicaDTO, "digital" -> CopiaDigitalDTO.
 * Si "tipoCopia" falta o trae otro valor, Jackson crea un CopiaDTO (defaultImpl)
 * y el service responde 400 con un mensaje claro.
 * idInstitucionActual y nombreInstitucionActual no son columnas de COPIA: el service los completa
 * con la custodia vigente. Son objetos (Long, String) para que se omitan si la copia no tiene custodia vigente.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "tipoCopia",
		visible = true, defaultImpl = CopiaDTO.class)
@JsonSubTypes({
		@JsonSubTypes.Type(value = CopiaFisicaDTO.class, name = "fisica"),
		@JsonSubTypes.Type(value = CopiaDigitalDTO.class, name = "digital")
})
public class CopiaDTO {

	private long idCopia;
	private long idVideojuego;
	private long idUsuario;
	private String tipoCopia;
	private String estadoIntegridad;
	private LocalDate fechaRegistro;
	private Long idInstitucionActual;
	private String nombreInstitucionActual;

	public CopiaDTO() {

	}

	public CopiaDTO(long idCopia, long idVideojuego, long idUsuario, String tipoCopia, String estadoIntegridad,
			LocalDate fechaRegistro) {
		super();
		this.idCopia = idCopia;
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.tipoCopia = tipoCopia;
		this.estadoIntegridad = estadoIntegridad;
		this.fechaRegistro = fechaRegistro;
	}

	@Override
	public String toString() {
		return "CopiaDTO [idCopia=" + idCopia + ", idVideojuego=" + idVideojuego + ", idUsuario=" + idUsuario
				+ ", tipoCopia=" + tipoCopia + ", estadoIntegridad=" + estadoIntegridad + ", fechaRegistro="
				+ fechaRegistro + ", idInstitucionActual=" + idInstitucionActual + ", nombreInstitucionActual="
				+ nombreInstitucionActual + "]";
	}

	public long getIdCopia() {
		return idCopia;
	}

	public void setIdCopia(long idCopia) {
		this.idCopia = idCopia;
	}

	public long getIdVideojuego() {
		return idVideojuego;
	}

	public void setIdVideojuego(long idVideojuego) {
		this.idVideojuego = idVideojuego;
	}

	public long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getTipoCopia() {
		return tipoCopia;
	}

	public void setTipoCopia(String tipoCopia) {
		this.tipoCopia = tipoCopia;
	}

	public String getEstadoIntegridad() {
		return estadoIntegridad;
	}

	public void setEstadoIntegridad(String estadoIntegridad) {
		this.estadoIntegridad = estadoIntegridad;
	}

	public LocalDate getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(LocalDate fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Long getIdInstitucionActual() {
		return idInstitucionActual;
	}

	public void setIdInstitucionActual(Long idInstitucionActual) {
		this.idInstitucionActual = idInstitucionActual;
	}

	public String getNombreInstitucionActual() {
		return nombreInstitucionActual;
	}

	public void setNombreInstitucionActual(String nombreInstitucionActual) {
		this.nombreInstitucionActual = nombreInstitucionActual;
	}

}