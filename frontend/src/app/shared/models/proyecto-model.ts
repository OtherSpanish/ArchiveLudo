/*
 * Modelos de la pestaña Proyectos.
 * Los nombres de los campos coinciden con el MER y con el JSON del back.
 */

/*
 * Estado de un proyecto de restauración (PROYECTO_DE_RESTAURACION.estado).
 * OJO: por ahora solo se conocen estos dos; confirmar con el script SQL.
 */
export type EstadoProyecto = 'activo' | 'finalizado';

/*
 * Un experto asignado a un proyecto (tabla EXP_PROY_REST + datos de EXPERTO).
 * La fecha viene como texto 'yyyy-MM-dd'.
 */
export interface ExpertoEnProyecto {
  idExperto: number;
  nombre: string;
  rolEnProyecto: string;
  fechaIncorporacion: string;
}

/*
 * Un proyecto de restauración (PROYECTO_DE_RESTAURACION).
 *  - fechaFin: opcional, un proyecto activo no la tiene.
 *  - herramientaEmulador: opcional, el emulador o herramienta usada.
 */
export interface ProyectoRestauracion {
  idProyecto: number;
  estado: EstadoProyecto;
  fechaInicio: string;
  fechaFin?: string;
  herramientaEmulador?: string;
}
