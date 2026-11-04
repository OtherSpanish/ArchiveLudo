package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de un proyecto de restauracion que viajan por HTTP.
 * Tiene los mismos atributos que ProyectoDeRestauracion (con el estado como texto)
 * y ademas la lista de sus participaciones, que el service guarda y lee en la tabla PARTICIPACION.
 * tieneInforme es un campo derivado: el enlace del PDF solo se entrega por /api/proyectos/{id}/informe,
 * que no es visible para el rol consulta.
 */
public class ProyectoDeRestauracionDTO {

	private long idProyecto;
	private long idVideojuego;
	private long idUsuario;
	private String nombre;
	private String descripcion;
	private String herramientaEmulador;
	private String estado;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private boolean tieneInforme;
	private List<ParticipacionDTO> participaciones;

	public ProyectoDeRestauracionDTO() {
		this.participaciones = new ArrayList<ParticipacionDTO>();
	}

	public ProyectoDeRestauracionDTO(long idProyecto, long idVideojuego, long idUsuario, String nombre,
			String descripcion, String herramientaEmulador, String estado, LocalDate fechaInicio, LocalDate fechaFin,
			boolean tieneInforme, List<ParticipacionDTO> participaciones) {
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
		this.tieneInforme = tieneInforme;
		this.participaciones = participaciones;
	}

	@Override
	public String toString() {
		return "ProyectoDeRestauracionDTO [idProyecto=" + idProyecto + ", idVideojuego=" + idVideojuego
				+ ", idUsuario=" + idUsuario + ", nombre=" + nombre + ", descripcion=" + descripcion
				+ ", herramientaEmulador=" + herramientaEmulador + ", estado=" + estado + ", fechaInicio="
				+ fechaInicio + ", fechaFin=" + fechaFin + ", tieneInforme=" + tieneInforme + ", participaciones="
				+ participaciones + "]";
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

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
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

	public boolean isTieneInforme() {
		return tieneInforme;
	}

	public void setTieneInforme(boolean tieneInforme) {
		this.tieneInforme = tieneInforme;
	}

	public List<ParticipacionDTO> getParticipaciones() {
		return participaciones;
	}

	public void setParticipaciones(List<ParticipacionDTO> participaciones) {
		this.participaciones = participaciones;
	}

}