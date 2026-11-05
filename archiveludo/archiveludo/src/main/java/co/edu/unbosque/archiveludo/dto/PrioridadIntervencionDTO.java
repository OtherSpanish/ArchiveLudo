package co.edu.unbosque.archiveludo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Videojuego que mas urge intervenir, con los motivos que lo ponen primero.
 * Lo calcula InicioService entre los videojuegos en riesgo; no corresponde a una tabla.
 */
public class PrioridadIntervencionDTO {

	private VideojuegoDTO videojuego;
	private String nombreEstudio;
	private List<String> motivos;

	public PrioridadIntervencionDTO() {
		this.motivos = new ArrayList<String>();
	}

	public PrioridadIntervencionDTO(VideojuegoDTO videojuego, String nombreEstudio, List<String> motivos) {
		super();
		this.videojuego = videojuego;
		this.nombreEstudio = nombreEstudio;
		this.motivos = motivos;
	}

	@Override
	public String toString() {
		return "PrioridadIntervencionDTO [videojuego=" + videojuego + ", nombreEstudio=" + nombreEstudio
				+ ", motivos=" + motivos + "]";
	}

	public VideojuegoDTO getVideojuego() {
		return videojuego;
	}

	public void setVideojuego(VideojuegoDTO videojuego) {
		this.videojuego = videojuego;
	}

	public String getNombreEstudio() {
		return nombreEstudio;
	}

	public void setNombreEstudio(String nombreEstudio) {
		this.nombreEstudio = nombreEstudio;
	}

	public List<String> getMotivos() {
		return motivos;
	}

	public void setMotivos(List<String> motivos) {
		this.motivos = motivos;
	}

}