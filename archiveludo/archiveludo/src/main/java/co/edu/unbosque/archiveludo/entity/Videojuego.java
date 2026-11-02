package co.edu.unbosque.archiveludo.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import co.edu.unbosque.archiveludo.model.EstadoConservacion;

/**
 * Entity de Videojuego (tabla VIDEOJUEGO).
 * idEstudio: estudio desarrollador (FK, obligatorio).
 * idUsuario: usuario que registro el videojuego (FK).
 * idsEditoras: companias editoras del juego; es la relacion N:M que se guarda en COMPED_VID.
 */
public class Videojuego {

	private long idVideojuego;
	private String titulo;
	private String genero;
	private String plataformaOriginal;
	private String descripcion;
	private int anioLanzamiento;
	private EstadoConservacion estadoConservacion;
	private long idEstudio;
	private long idUsuario;
	private List<Long> idsEditoras;

	public Videojuego() {
		this.idsEditoras = new ArrayList<Long>();
	}

	public Videojuego(String titulo, String genero, String plataformaOriginal, String descripcion,
			int anioLanzamiento, EstadoConservacion estadoConservacion, long idEstudio, long idUsuario,
			List<Long> idsEditoras) {
		super();
		this.titulo = titulo;
		this.genero = genero;
		this.plataformaOriginal = plataformaOriginal;
		this.descripcion = descripcion;
		this.anioLanzamiento = anioLanzamiento;
		this.estadoConservacion = estadoConservacion;
		this.idEstudio = idEstudio;
		this.idUsuario = idUsuario;
		this.idsEditoras = idsEditoras;
	}

	public Videojuego(long idVideojuego, String titulo, String genero, String plataformaOriginal,
			String descripcion, int anioLanzamiento, EstadoConservacion estadoConservacion, long idEstudio,
			long idUsuario, List<Long> idsEditoras) {
		super();
		this.idVideojuego = idVideojuego;
		this.titulo = titulo;
		this.genero = genero;
		this.plataformaOriginal = plataformaOriginal;
		this.descripcion = descripcion;
		this.anioLanzamiento = anioLanzamiento;
		this.estadoConservacion = estadoConservacion;
		this.idEstudio = idEstudio;
		this.idUsuario = idUsuario;
		this.idsEditoras = idsEditoras;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idVideojuego);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Videojuego other = (Videojuego) obj;
		return idVideojuego == other.idVideojuego;
	}

	@Override
	public String toString() {
		return "Videojuego [idVideojuego=" + idVideojuego + ", titulo=" + titulo + ", genero=" + genero
				+ ", plataformaOriginal=" + plataformaOriginal + ", descripcion=" + descripcion
				+ ", anioLanzamiento=" + anioLanzamiento + ", estadoConservacion=" + estadoConservacion
				+ ", idEstudio=" + idEstudio + ", idUsuario=" + idUsuario + ", idsEditoras=" + idsEditoras + "]";
	}

	public long getIdVideojuego() {
		return idVideojuego;
	}

	public void setIdVideojuego(long idVideojuego) {
		this.idVideojuego = idVideojuego;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getGenero() {
		return genero;
	}

	public void setGenero(String genero) {
		this.genero = genero;
	}

	public String getPlataformaOriginal() {
		return plataformaOriginal;
	}

	public void setPlataformaOriginal(String plataformaOriginal) {
		this.plataformaOriginal = plataformaOriginal;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getAnioLanzamiento() {
		return anioLanzamiento;
	}

	public void setAnioLanzamiento(int anioLanzamiento) {
		this.anioLanzamiento = anioLanzamiento;
	}

	public EstadoConservacion getEstadoConservacion() {
		return estadoConservacion;
	}

	public void setEstadoConservacion(EstadoConservacion estadoConservacion) {
		this.estadoConservacion = estadoConservacion;
	}

	public long getIdEstudio() {
		return idEstudio;
	}

	public void setIdEstudio(long idEstudio) {
		this.idEstudio = idEstudio;
	}

	public long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public List<Long> getIdsEditoras() {
		return idsEditoras;
	}

	public void setIdsEditoras(List<Long> idsEditoras) {
		this.idsEditoras = idsEditoras;
	}

}