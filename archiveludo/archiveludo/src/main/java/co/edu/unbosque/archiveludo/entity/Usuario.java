package co.edu.unbosque.archiveludo.entity;

import java.time.LocalDate;
import java.util.Objects;

import co.edu.unbosque.archiveludo.model.Rol;

/**
 * Entity de Usuario.
 * El rol se guarda como un valor del enum Rol del modelo.
 */
public class Usuario {

	private Long idUsuario;
	private String primerNombre;
	private String segundoNombre;
	private String primerApellido;
	private String segundoApellido;
	private String correo;
	private String hashContrasenia;
	private Rol rol;
	private LocalDate fechaCreacion;

	public Usuario() {

	}

	public Usuario(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido,
			String correo, String hashContrasenia, Rol rol, LocalDate fechaCreacion) {
		super();
		this.primerNombre = primerNombre;
		this.segundoNombre = segundoNombre;
		this.primerApellido = primerApellido;
		this.segundoApellido = segundoApellido;
		this.correo = correo;
		this.hashContrasenia = hashContrasenia;
		this.rol = rol;
		this.fechaCreacion = fechaCreacion;
	}

	public Usuario(Long idUsuario, String primerNombre, String segundoNombre, String primerApellido,
			String segundoApellido, String correo, String hashContrasenia, Rol rol, LocalDate fechaCreacion) {
		super();
		this.idUsuario = idUsuario;
		this.primerNombre = primerNombre;
		this.segundoNombre = segundoNombre;
		this.primerApellido = primerApellido;
		this.segundoApellido = segundoApellido;
		this.correo = correo;
		this.hashContrasenia = hashContrasenia;
		this.rol = rol;
		this.fechaCreacion = fechaCreacion;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idUsuario);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(idUsuario, other.idUsuario);
	}

	@Override
	public String toString() {
		return "Usuario [idUsuario=" + idUsuario + ", primerNombre=" + primerNombre + ", segundoNombre="
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

	public String getHashContrasenia() {
		return hashContrasenia;
	}

	public void setHashContrasenia(String hashContrasenia) {
		this.hashContrasenia = hashContrasenia;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public LocalDate getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDate fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

}