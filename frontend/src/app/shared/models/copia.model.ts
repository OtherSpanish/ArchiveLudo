export type TipoCopia = 'fisica' | 'digital';

export type EstadoIntegridad =
  | 'buena'
  | 'deteriorada'
  | 'dañada'
  | 'irrecuperable';

export interface Copia {
  idCopia: number;
  idVideojuego: number;
  tipoCopia: TipoCopia;
  estadoIntegridad: EstadoIntegridad;
  fechaRegistro: string;

  // Campos de COPIA_FISICA (solo si tipoCopia es 'fisica')
  tipoSoporte?: string;
  numeroSerie?: string;
  ubicacionFisica?: string;

  // Campos de COPIA_DIGITAL (solo si tipoCopia es 'digital')
  formatoArchivo?: string;
  tamanio?: string;
  hashVerificacion?: string;
}
