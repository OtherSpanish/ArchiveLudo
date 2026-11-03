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

/*
 * Cuerpo de POST /api/videojuegos/{id}/eventos-riesgo y PUT /api/eventos-riesgo/{id}.
 *  - tipoEvento: el texto legible de TIPOS_EVENTO (el back también acepta el valor de la BD).
 *  - fechaEvento: obligatoria, no puede ser futura.
 *  - descripcion: obligatoria, máximo 500 caracteres.
 *  - fuente: opcional, máximo 200 caracteres; si está vacía no se envía.
 * El usuario sale del token y el videojuego va en la URL.
 */
export interface EventoRiesgoNuevo {
  tipoEvento: string;
  fechaEvento: string;
  descripcion: string;
  fuente?: string;
}

/* Los cinco tipos de evento, tal como los muestra el back. */
export const TIPOS_EVENTO: string[] = [
  'Cierre de servidores',
  'Pérdida de código fuente',
  'Retiro de tienda digital',
  'Deterioro de medios',
  'Disputa legal'
];
