package co.edu.unbosque.archiveludo.entity;

import java.time.LocalDate;

import co.edu.unbosque.archiveludo.model.EstadoIntegridad;
import co.edu.unbosque.archiveludo.model.TipoCopia;

/**
 * Entity de CopiaDigital (tabla COPIA_DIGITAL, subtipo de COPIA).
 * Los constructores fijan siempre tipoCopia = DIGITAL. tamanio esta en bytes.
 */
public class CopiaDigital extends Copia {

	private String formatoArchivo;
	private long tamanio;
	private String hashVerificacion;

	public CopiaDigital() {
		super();
		setTipoCopia(TipoCopia.DIGITAL);
	}

	public CopiaDigital(long idVideojuego, long idUsuario, EstadoIntegridad estadoIntegridad,
			LocalDate fechaRegistro, String formatoArchivo, long tamanio, String hashVerificacion) {
		super(idVideojuego, idUsuario, TipoCopia.DIGITAL, estadoIntegridad, fechaRegistro);
		this.formatoArchivo = formatoArchivo;
		this.tamanio = tamanio;
		this.hashVerificacion = hashVerificacion;
	}

	public CopiaDigital(long idCopia, long idVideojuego, long idUsuario, EstadoIntegridad estadoIntegridad,
			LocalDate fechaRegistro, String formatoArchivo, long tamanio, String hashVerificacion) {
		super(idCopia, idVideojuego, idUsuario, TipoCopia.DIGITAL, estadoIntegridad, fechaRegistro);
		this.formatoArchivo = formatoArchivo;
		this.tamanio = tamanio;
		this.hashVerificacion = hashVerificacion;
	}

	@Override
	public String toString() {
		return super.toString() + " CopiaDigital [formatoArchivo=" + formatoArchivo + ", tamanio=" + tamanio
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