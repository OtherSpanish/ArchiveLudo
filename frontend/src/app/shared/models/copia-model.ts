/*
 * Modelos de las copias de un videojuego.
 * Los nombres de los campos coinciden con el MER y con el JSON del back.
 */

/* Una copia es física o digital (generalización disjunta del MER). */
export type TipoCopia = 'fisica' | 'digital';

/* Estado de integridad (COPIA.estadoIntegridad). */
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

/*
 * POST /api/videojuegos/{id}/copias
 * Cuerpo que envía el formulario "Registrar copia". El back responde
 * 201 con la Copia creada (ya con su idCopia).
 *
 * No lleva idCopia (lo genera la base de datos) ni idVideojuego (va en la URL).
 *  - fechaRegistro: texto 'yyyy-MM-dd'.
 *  - Si tipoCopia es 'fisica', se envían tipoSoporte, numeroSerie y
 *    ubicacionFisica, y NO los campos digitales. Si es 'digital', al revés.
 *    Los campos que no corresponden se omiten del JSON.
 */
export interface CopiaNueva {
  tipoCopia: TipoCopia;
  estadoIntegridad: EstadoIntegridad;
  fechaRegistro: string;

  // Solo si tipoCopia es 'fisica'
  tipoSoporte?: string;
  numeroSerie?: string;
  ubicacionFisica?: string;

  // Solo si tipoCopia es 'digital'
  formatoArchivo?: string;
  tamanio?: string;
  hashVerificacion?: string;
}
