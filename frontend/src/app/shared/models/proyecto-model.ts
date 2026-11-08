/**
 * Modelos de los proyectos de restauración.
 * Los nombres de los campos coinciden con el JSON del back.
 */

import { Participacion, ParticipacionNueva } from './participacion-model';

/**
 * Estado de un proyecto de restauración (PROYECTO_DE_RESTAURACION.estado).
 * Solo 'finalizado' lleva fechaFin.
 */
export type EstadoProyecto = 'activo' | 'pausado' | 'finalizado';

/**
 * Un proyecto de restauración (GET /api/proyectos y GET /api/videojuegos/{id}/proyectos).
 * Llega plano, con sus participaciones adentro.
 *  - fechaFin: solo si el estado es 'finalizado'.
 *  - descripcion y herramientaEmulador: opcionales.
 *  - tieneInforme: true si el proyecto ya tiene su informe final (PDF).
 */
export interface ProyectoDeRestauracion {
  idProyecto: number;
  idVideojuego: number;
  /** Título del videojuego del proyecto (lo llena el back; no se envía). */
  tituloVideojuego: string;
  /** Plataforma original del videojuego (lo llena el back; no se envía). */
  plataformaVideojuego: string;
  idUsuario: number;
  nombre: string;
  descripcion?: string;
  herramientaEmulador?: string;
  estado: EstadoProyecto;
  fechaInicio: string;
  fechaFin?: string;
  /** Lo calcula el back (no se envía). El enlace del PDF se pide aparte, en /api/proyectos/{id}/informe. */
  tieneInforme: boolean;
  participaciones: Participacion[];
}

/**
 * Cuerpo de POST /api/videojuegos/{id}/proyectos: el proyecto con al menos
 * una participación. Con 'activo' o 'pausado' NO se envía fechaFin.
 */
export interface ProyectoNuevo {
  nombre: string;
  descripcion?: string;
  herramientaEmulador?: string;
  estado: EstadoProyecto;
  fechaInicio: string;
  fechaFin?: string;
  participaciones: ParticipacionNueva[];
}

/** Cuerpo de PUT /api/proyectos/{id}: solo los datos del proyecto. */
export interface ProyectoEditado {
  nombre: string;
  descripcion?: string;
  herramientaEmulador?: string;
  estado: EstadoProyecto;
  fechaInicio: string;
  fechaFin?: string;
}
