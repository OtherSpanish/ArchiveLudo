/*
 * Un evento de la bitácora de riesgo de un videojuego (tabla EVENTO_DE_RIESGO).
 * Los nombres de los campos coinciden con el MER y con el JSON del back.
 *
 *  - tipoEvento: texto corto del tipo de evento ("Cierre de servidores").
 *  - fecha: texto 'yyyy-MM-dd'.
 *  - fuente: opcional, de dónde sale la información del evento.
 */
export interface EventoRiesgo {
  idEvento: number;
  tipoEvento: string;
  fecha: string;
  descripcion: string;
  fuente?: string;
}
