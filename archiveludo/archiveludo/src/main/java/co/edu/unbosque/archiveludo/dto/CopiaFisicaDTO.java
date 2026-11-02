package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

public class CopiaFisicaDTO extends CopiaDTO {

	private String numeroSerie;
	private String ubicacionFisica;
	private String tipoSoporte;

	public CopiaFisicaDTO() {

	}

	public CopiaFisicaDTO(long idCopia, long idVideojuego, long idUsuario, String tipoCopia,
			String estadoIntegridad, LocalDate fechaRegistro, String numeroSerie, String ubicacionFisica,
			String tipoSoporte) {
		super(idCopia, idVideojuego, idUsuario, tipoCopia, estadoIntegridad, fechaRegistro);
		this.numeroSerie = numeroSerie;
		this.ubicacionFisica = ubicacionFisica;
		this.tipoSoporte = tipoSoporte;
	}

	@Override
	public String toString() {
		return super.toString() + " CopiaFisicaDTO [numeroSerie=" + numeroSerie + ", ubicacionFisica="
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