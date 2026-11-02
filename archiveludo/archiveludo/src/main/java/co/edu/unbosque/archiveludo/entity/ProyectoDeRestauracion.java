package co.edu.unbosque.archiveludo.entity;

import java.time.LocalDate;
import java.util.Objects;

import co.edu.unbosque.archiveludo.model.EstadoProyecto;

/**
 * Entity de ProyectoDeRestauracion (tabla PROYECTO_DE_RESTAURACION).
 * Un proyecto restaura un videojuego ("Recibir") y lo crea un usuario ("Crear").
 * Sus expertos estan en la tabla PARTICIPACION (entity Participacion).
 */
public class ProyectoDeRestauracion {

	private long idProyecto;
	private long idVideojuego;
	private long idUsuario;
	private String nombre;
	private String descripcion;
	private String herramientaEmulador;
	private EstadoProyecto estado;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;

	public ProyectoDeRestauracion() {

	}

	public ProyectoDeRestauracion(long idVideojuego, long idUsuario, String nombre, String descripcion,
			String herramientaEmulador, EstadoProyecto estado, LocalDate fechaInicio, LocalDate fechaFin) {
		super();
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.herramientaEmulador = herramientaEmulador;
		this.estado = estado;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	public ProyectoDeRestauracion(long idProyecto, long idVideojuego, long idUsuario, String nombre,
			String descripcion, String herramientaEmulador, EstadoProyecto estado, LocalDate fechaInicio,
			LocalDate fechaFin) {
		super();
		this.idProyecto = idProyecto;
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.herramientaEmulador = herramientaEmulador;
		this.estado = estado;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idProyecto);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProyectoDeRestauracion other = (ProyectoDeRestauracion) obj;
		return idProyecto == other.idProyecto;
	}

	@Override
	public String toString() {
		return "ProyectoDeRestauracion [idProyecto=" + idProyecto + ", idVideojuego=" + idVideojuego
				+ ", idUsuario=" + idUsuario + ", nombre=" + nombre + ", descripcion=" + descripcion
				+ ", herramientaEmulador=" + herramientaEmulador + ", estado=" + estado + ", fechaInicio="
				+ fechaInicio + ", fechaFin=" + fechaFin + "]";
	}

	public long getIdProyecto() {
		return idProyecto;
	}

	public void setIdProyecto(long idProyecto) {
		this.idProyecto = idProyecto;
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

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getHerramientaEmulador() {
		return herramientaEmulador;
	}

	public void setHerramientaEmulador(String herramientaEmulador) {
		this.herramientaEmulador = herramientaEmulador;
	}

	public EstadoProyecto getEstado() {
		return estado;
	}

	public void setEstado(EstadoProyecto estado) {
		this.estado = estado;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}

}