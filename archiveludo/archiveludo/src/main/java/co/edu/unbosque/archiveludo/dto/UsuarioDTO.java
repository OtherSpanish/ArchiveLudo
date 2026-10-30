package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

public class UsuarioDTO {

	private Long idUsuario;
	private String primerNombre;
	private String segundoNombre;
	private String primerApellido;
	private String segundoApellido;
	private String correo;
	private String contrasenia;
	private String rol;
	private LocalDate fechaCreacion;

	public UsuarioDTO() {

	}

	public UsuarioDTO(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido,
			String correo, String contrasenia, String rol, LocalDate fechaCreacion) {
		super();
		this.primerNombre = primerNombre;
		this.segundoNombre = segundoNombre;
		this.primerApellido = primerApellido;
		this.segundoApellido = segundoApellido;
		this.correo = correo;
		this.contrasenia = contrasenia;
		this.rol = rol;
		this.fechaCreacion = fechaCreacion;
	}

	@Override
	public String toString() {
		return "UsuarioDTO [idUsuario=" + idUsuario + ", primerNombre=" + primerNombre + ", segundoNombre="
				+ segundoNombre + ", primerApellido=" + primerApellido + ", segundoApellido=" + segundoApellido
				+ ", correo=" + correo + ", rol=" + rol + ", fechaCreacion=" + fechaCreacion + "]";
	}

	public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getPrimerNombre() {
		return primerNombre;
	}

	public void setPrimerNombre(String primerNombre) {
		this.primerNombre = primerNombre;
	}

	public String getSegundoNombre() {
		return segundoNombre;
	}

	public void setSegundoNombre(String segundoNombre) {
		this.segundoNombre = segundoNombre;
	}

	public String getPrimerApellido() {
		return primerApellido;
	}

	public void setPrimerApellido(String primerApellido) {
		this.primerApellido = primerApellido;
	}

	public String getSegundoApellido() {
		return segundoApellido;
	}

	public void setSegundoApellido(String segundoApellido) {
		this.segundoApellido = segundoApellido;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getContrasenia() {
		return contrasenia;
	}

	public void setContrasenia(String contrasenia) {
		this.contrasenia = contrasenia;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public LocalDate getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDate fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

}