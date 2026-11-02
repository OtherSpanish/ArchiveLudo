package co.edu.unbosque.archiveludo.entity;

import java.time.LocalDate;
import java.util.Objects;

import co.edu.unbosque.archiveludo.model.TipoEventoRiesgo;

/**
 * Entity de EventoDeRiesgo (tabla EVENTO_DE_RIESGO).
 * Cada fila es un hecho que puso en riesgo a un videojuego (bitacora).
 */
public class EventoDeRiesgo {

	private long idEvento;
	private long idVideojuego;
	private long idUsuario;
	private TipoEventoRiesgo tipoEvento;
	private LocalDate fechaEvento;
	private String descripcion;
	private String fuente;

	public EventoDeRiesgo() {

	}

	public EventoDeRiesgo(long idVideojuego, long idUsuario, TipoEventoRiesgo tipoEvento, LocalDate fechaEvento,
			String descripcion, String fuente) {
		super();
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.tipoEvento = tipoEvento;
		this.fechaEvento = fechaEvento;
		this.descripcion = descripcion;
		this.fuente = fuente;
	}

	public EventoDeRiesgo(long idEvento, long idVideojuego, long idUsuario, TipoEventoRiesgo tipoEvento,
			LocalDate fechaEvento, String descripcion, String fuente) {
		super();
		this.idEvento = idEvento;
		this.idVideojuego = idVideojuego;
		this.idUsuario = idUsuario;
		this.tipoEvento = tipoEvento;
		this.fechaEvento = fechaEvento;
		this.descripcion = descripcion;
		this.fuente = fuente;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idEvento);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventoDeRiesgo other = (EventoDeRiesgo) obj;
		return idEvento == other.idEvento;
	}

	@Override
	public String toString() {
		return "EventoDeRiesgo [idEvento=" + idEvento + ", idVideojuego=" + idVideojuego + ", idUsuario=" + idUsuario
				+ ", tipoEvento=" + tipoEvento + ", fechaEvento=" + fechaEvento + ", descripcion=" + descripcion
				+ ", fuente=" + fuente + "]";
	}

	public long getIdEvento() {
		return idEvento;
	}

	public void setIdEvento(long idEvento) {
		this.idEvento = idEvento;
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

	public TipoEventoRiesgo getTipoEvento() {
		return tipoEvento;
	}

	public void setTipoEvento(TipoEventoRiesgo tipoEvento) {
		this.tipoEvento = tipoEvento;
	}

	public LocalDate getFechaEvento() {
		return fechaEvento;
	}

	public void setFechaEvento(LocalDate fechaEvento) {
		this.fechaEvento = fechaEvento;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getFuente() {
		return fuente;
	}

	public void setFuente(String fuente) {
		this.fuente = fuente;
	}

}