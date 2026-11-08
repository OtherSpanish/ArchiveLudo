/**
 * Participación de un experto en un proyecto de restauración.
 * Viene dentro de cada proyecto y de GET /api/proyectos/{id}/participaciones.
 * Sin fechaSalida, el experto sigue en el proyecto.
 */
export interface Participacion {
  idParticipacion: number;
  idProyecto: number;
  idExperto: number;
  rolExperto: string;
  fechaIncorporacion: string;
  fechaSalida?: string;
}

/** Cuerpo de POST /api/proyectos/{id}/participaciones (y de cada fila al crear un proyecto). */
export interface ParticipacionNueva {
  idExperto: number;
  rolExperto: string;
  fechaIncorporacion: string;
  fechaSalida?: string;
}

/** Cuerpo de PUT /api/participaciones/{id}: no cambian ni el experto ni el proyecto. */
export interface ParticipacionEditada {
  rolExperto: string;
  fechaIncorporacion: string;
  fechaSalida?: string;
}
