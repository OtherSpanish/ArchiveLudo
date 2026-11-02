package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * DTO comun a toda copia. Es abstracto como la entity Copia.
 * El campo "tipoCopia" del JSON decide que subtipo es:
 * "fisica" -> CopiaFisicaDTO, "digital" -> CopiaDigitalDTO.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "tipoCopia", visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = CopiaFisicaDTO.class, name = "fisica"),
		@JsonSubTypes.Type(value = CopiaDigitalDTO.class, name = "digital")
})
public abstract class CopiaDTO {

	private long idCopia;
	private long idVideojuego;
	private long idUsuario;
	private String tipoCopia;
	private String estadoIntegridad;
	private LocalDate fechaRegistro;

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
				+ fechaRegistro + "]";
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

}