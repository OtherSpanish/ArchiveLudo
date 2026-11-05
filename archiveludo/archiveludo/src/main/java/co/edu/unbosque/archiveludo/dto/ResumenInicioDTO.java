package co.edu.unbosque.archiveludo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Todo lo que muestra la pantalla de Inicio en una sola respuesta:
 * contadores del catalogo, el videojuego prioritario y tres listas cortas (maximo 4 cada una).
 * Lo arma InicioService; no corresponde a una tabla.
 */
public class ResumenInicioDTO {

	private long titulosCatalogados;
	private long enRiesgo;
	private long totalmentePerdidos;
	private long restaurados;
	private PrioridadIntervencionDTO prioridadIntervencion;
	private List<VideojuegoDTO> enRiesgoLista;
	private List<VideojuegoDTO> perdidosLista;
	private List<VideojuegoDTO> restauradosLista;

	public ResumenInicioDTO() {
		this.enRiesgoLista = new ArrayList<VideojuegoDTO>();
		this.perdidosLista = new ArrayList<VideojuegoDTO>();
		this.restauradosLista = new ArrayList<VideojuegoDTO>();
	}

	public ResumenInicioDTO(long titulosCatalogados, long enRiesgo, long totalmentePerdidos, long restaurados,
			PrioridadIntervencionDTO prioridadIntervencion, List<VideojuegoDTO> enRiesgoLista,
			List<VideojuegoDTO> perdidosLista, List<VideojuegoDTO> restauradosLista) {
		super();
		this.titulosCatalogados = titulosCatalogados;
		this.enRiesgo = enRiesgo;
		this.totalmentePerdidos = totalmentePerdidos;
		this.restaurados = restaurados;
		this.prioridadIntervencion = prioridadIntervencion;
		this.enRiesgoLista = enRiesgoLista;
		this.perdidosLista = perdidosLista;
		this.restauradosLista = restauradosLista;
	}

	@Override
	public String toString() {
		return "ResumenInicioDTO [titulosCatalogados=" + titulosCatalogados + ", enRiesgo=" + enRiesgo
				+ ", totalmentePerdidos=" + totalmentePerdidos + ", restaurados=" + restaurados
				+ ", prioridadIntervencion=" + prioridadIntervencion + ", enRiesgoLista=" + enRiesgoLista
				+ ", perdidosLista=" + perdidosLista + ", restauradosLista=" + restauradosLista + "]";
	}

	public long getTitulosCatalogados() {
		return titulosCatalogados;
	}

	public void setTitulosCatalogados(long titulosCatalogados) {
		this.titulosCatalogados = titulosCatalogados;
	}

	public long getEnRiesgo() {
		return enRiesgo;
	}

	public void setEnRiesgo(long enRiesgo) {
		this.enRiesgo = enRiesgo;
	}

	public long getTotalmentePerdidos() {
		return totalmentePerdidos;
	}

	public void setTotalmentePerdidos(long totalmentePerdidos) {
		this.totalmentePerdidos = totalmentePerdidos;
	}

	public long getRestaurados() {
		return restaurados;
	}

	public void setRestaurados(long restaurados) {
		this.restaurados = restaurados;
	}

	public PrioridadIntervencionDTO getPrioridadIntervencion() {
		return prioridadIntervencion;
	}

	public void setPrioridadIntervencion(PrioridadIntervencionDTO prioridadIntervencion) {
		this.prioridadIntervencion = prioridadIntervencion;
	}

	public List<VideojuegoDTO> getEnRiesgoLista() {
		return enRiesgoLista;
	}

	public void setEnRiesgoLista(List<VideojuegoDTO> enRiesgoLista) {
		this.enRiesgoLista = enRiesgoLista;
	}

	public List<VideojuegoDTO> getPerdidosLista() {
		return perdidosLista;
	}

	public void setPerdidosLista(List<VideojuegoDTO> perdidosLista) {
		this.perdidosLista = perdidosLista;
	}

	public List<VideojuegoDTO> getRestauradosLista() {
		return restauradosLista;
	}

	public void setRestauradosLista(List<VideojuegoDTO> restauradosLista) {
		this.restauradosLista = restauradosLista;
	}

}