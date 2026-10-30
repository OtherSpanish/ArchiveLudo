package co.edu.unbosque.archiveludo.dto;

public class VideojuegoDTO {

	private long idVideojuego;
	private String titulo;
	private String genero;
	private String plataformaOriginal;
	private String descripcion;
	private int anioLanzamiento;
	private String estadoConservacion;

	public VideojuegoDTO() {
		// TODO Auto-generated constructor stub
	}

	public VideojuegoDTO(long idVideojuego, String titulo, String genero, String plataformaOriginal, String descripcion,
			int anioLanzamiento, String estadoConservacion) {
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
	public String toString() {
		return "VideojuegoDTO [idVideojuego=" + idVideojuego + ", titulo=" + titulo + ", genero=" + genero
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

	public String getEstadoConservacion() {
		return estadoConservacion;
	}

	public void setEstadoConservacion(String estadoConservacion) {
		this.estadoConservacion = estadoConservacion;
	}
	
}
