/*
 * Un evento de la bitácora de riesgo (GET /api/videojuegos/{id}/eventos-riesgo).
 *
 *  - tipoEvento: ya viene como texto legible.
 *  - fechaEvento: texto 'yyyy-MM-dd'.
 *  - fuente: opcional; si empieza por "http" se muestra como enlace.
 */
export interface EventoRiesgo {
  idEvento: number;
  idVideojuego: number;
  idUsuario: number;
  tipoEvento: string;
  fechaEvento: string;
  descripcion: string;
  fuente?: string;
}
