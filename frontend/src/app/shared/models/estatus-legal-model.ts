/*
 * Un registro del historial de estatus legal (GET /api/videojuegos/{id}/estatus-legal).
 *
 *  - estatus: ya viene como texto legible ("Protegido", "Dominio público",
 *    "Huérfano", "Abandonware").
 *  - fechaDeterminacion: texto 'yyyy-MM-dd'.
 *  - vigente: true solo en UN registro de la lista, el más reciente.
 *    Si la lista llega vacía, el juego está "Sin determinar".
 */
export interface EstatusLegal {
  idEstatus: number;
  idVideojuego: number;
  idUsuario: number;
  estatus: string;
  fechaDeterminacion: string;
  observacion?: string;
  vigente: boolean;
}

/*
 * Cuerpo de POST /api/videojuegos/{id}/estatus-legal.
 * No lleva idEstatus, idVideojuego (va en la URL), idUsuario (sale del token)
 * ni vigente (lo calcula el back).
 */
export interface EstatusLegalNuevo {
  estatus: string;
  fechaDeterminacion: string;
  observacion?: string;
}
