package co.edu.unbosque.archiveludo.dto;

import java.time.LocalDate;

public class CopiaDigitalDTO extends CopiaDTO {

	private String formatoArchivo;
	private long tamanio;
	private String hashVerificacion;

	public CopiaDigitalDTO() {

	}

	public CopiaDigitalDTO(long idCopia, long idVideojuego, long idUsuario, String tipoCopia,
			String estadoIntegridad, LocalDate fechaRegistro, String formatoArchivo, long tamanio,
			String hashVerificacion) {
		super(idCopia, idVideojuego, idUsuario, tipoCopia, estadoIntegridad, fechaRegistro);
		this.formatoArchivo = formatoArchivo;
		this.tamanio = tamanio;
		this.hashVerificacion = hashVerificacion;
	}

	@Override
	public String toString() {
		return super.toString() + " CopiaDigitalDTO [formatoArchivo=" + formatoArchivo + ", tamanio=" + tamanio
				+ ", hashVerificacion=" + hashVerificacion + "]";
	}

	public String getFormatoArchivo() {
		return formatoArchivo;
	}

	public void setFormatoArchivo(String formatoArchivo) {
		this.formatoArchivo = formatoArchivo;
	}

	public long getTamanio() {
		return tamanio;
	}

	public void setTamanio(long tamanio) {
		this.tamanio = tamanio;
	}

	public String getHashVerificacion() {
		return hashVerificacion;
	}

	public void setHashVerificacion(String hashVerificacion) {
		this.hashVerificacion = hashVerificacion;
	}

}