package co.edu.unbosque.archiveludo.entity;

import java.time.LocalDate;

import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.model.TipoCopia;

/**
 * Entity de CopiaFisica (tabla COPIA_FISICA, subtipo de COPIA).
 * Los constructores fijan siempre tipoCopia = FISICA.
 */
public class CopiaFisica extends Copia {

	private String numeroSerie;
	private String ubicacionFisica;
	private String tipoSoporte;

	public CopiaFisica() {
		super();
		setTipoCopia(TipoCopia.FISICA);
	}

	public CopiaFisica(long idVideojuego, long idUsuario, EstadoIntegridad estadoIntegridad,
			LocalDate fechaRegistro, String numeroSerie, String ubicacionFisica, String tipoSoporte) {
		super(idVideojuego, idUsuario, TipoCopia.FISICA, estadoIntegridad, fechaRegistro);
		this.numeroSerie = numeroSerie;
		this.ubicacionFisica = ubicacionFisica;
		this.tipoSoporte = tipoSoporte;
	}

	public CopiaFisica(long idCopia, long idVideojuego, long idUsuario, EstadoIntegridad estadoIntegridad,
			LocalDate fechaRegistro, String numeroSerie, String ubicacionFisica, String tipoSoporte) {
		super(idCopia, idVideojuego, idUsuario, TipoCopia.FISICA, estadoIntegridad, fechaRegistro);
		this.numeroSerie = numeroSerie;
		this.ubicacionFisica = ubicacionFisica;
		this.tipoSoporte = tipoSoporte;
	}

	@Override
	public String toString() {
		return super.toString() + " CopiaFisica [numeroSerie=" + numeroSerie + ", ubicacionFisica="
				+ ubicacionFisica + ", tipoSoporte=" + tipoSoporte + "]";
	}

	public String getNumeroSerie() {
		return numeroSerie;
	}

	public void setNumeroSerie(String numeroSerie) {
		this.numeroSerie = numeroSerie;
	}

	public String getUbicacionFisica() {
		return ubicacionFisica;
	}

	public void setUbicacionFisica(String ubicacionFisica) {
		this.ubicacionFisica = ubicacionFisica;
	}

	public String getTipoSoporte() {
		return tipoSoporte;
	}

	public void setTipoSoporte(String tipoSoporte) {
		this.tipoSoporte = tipoSoporte;
	}

}