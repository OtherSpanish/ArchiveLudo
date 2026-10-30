package co.edu.unbosque.archiveludo.entity;

import java.util.Objects;

import co.edu.unbosque.archiveludo.model.EstadoConservacion;

public class Videojuego {

	private long idVideojuego;
	private String titulo;
	private String genero;
	private String plataformaOriginal;
	private String descripcion;
	private int anioLanzamiento;
	private EstadoConservacion estadoConservacion;
	
	public Videojuego() {
		// TODO Auto-generated constructor stub
	}

	
	public Videojuego(String titulo, String genero, String plataformaOriginal, String descripcion, int anioLanzamiento,
			EstadoConservacion estadoConservacion) {
		super();
		this.titulo = titulo;
		this.genero = genero;
		this.plataformaOriginal = plataformaOriginal;
		this.descripcion = descripcion;
		this.anioLanzamiento = anioLanzamiento;
		this.estadoConservacion = estadoConservacion;
	}


	public Videojuego(long idVideojuego, String titulo, String genero, String plataformaOriginal, String descripcion,
			int anioLanzamiento, EstadoConservacion estadoConservacion) {
		super();
		this.idVideojuego = idVideojuego;
		this.titulo = titulo;
		this.genero = genero;
		this.plataformaOriginal = plataformaOriginal;
		this.descripcion = descripcion;
		this.anioLanzamiento = anioLanzamiento;
		this.estadoConservacion = estadoConservacion;
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
		return Objects.equals(idVideojuego, other.idVideojuego);
	}
	
	

	@Override
	public String toString() {
		return "Videojuego [idVideojuego=" + idVideojuego + ", titulo=" + titulo + ", genero=" + genero
				+ ", plataformaOriginal=" + plataformaOriginal + ", descripcion=" + descripcion + ", anioLanzamiento="
				+ anioLanzamiento + ", estadoConservacion=" + estadoConservacion + "]";
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
	
	
	
}
