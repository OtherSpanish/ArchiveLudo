// Valores exactos que manda el back en estadoConservacion (con espacios).
// OJO: 'perdido' ya no existe; ahora es 'totalmente perdido'.
export type EstadoConservacion =
  | 'activo'
  | 'en riesgo'
  | 'parcialmente perdido'
  | 'totalmente perdido'
  | 'restaurado';

export interface Estudio {
  idEstudio: number;
  nombre: string;
  pais: string;
  anioFundacion: number;
  anioCierre?: number;
  estadoActual: string;
}

export interface Videojuego {
  idVideojuego: number;
  titulo: string;
  plataformaOriginal: string;
  anioLanzamiento: number;
  genero?: string;
  descripcion?: string;
  estadoConservacion: EstadoConservacion;
  estudio?: Estudio;

  // Campos que manda el back (VideojuegoDTO). Son opcionales porque no todas
  // las pantallas los usan.
  idEstudio?: number;
  idUsuario?: number;
  idsEditoras?: number[];
}

/**
 * Cuerpo de POST /api/videojuegos y PUT /api/videojuegos/{id}.
 * No lleva idVideojuego (va en la URL) ni idUsuario (sale del token).
 *  - anioLanzamiento: entre 1950 y el año actual.
 *  - idsEditoras: en un PUT REEMPLAZA la lista anterior ([] las quita todas).
 */
export interface VideojuegoNuevo {
  titulo: string;
  genero: string;
  plataformaOriginal: string;
  descripcion?: string;
  anioLanzamiento: number;
  estadoConservacion: EstadoConservacion;
  idEstudio: number;
  idsEditoras: number[];
}

/**
 * Cuerpo de POST /api/estudios y PUT /api/estudios/{id}.
 *  - estadoActual: 'activo' o 'desaparecido'.
 *  - anioFundacion: entre 1800 y el año actual.
 *  - anioCierre: SOLO si está desaparecido (obligatorio); si está activo no se envía.
 */
export interface EstudioNuevo {
  nombre: string;
  pais: string;
  estadoActual: string;
  anioFundacion: number;
  anioCierre?: number;
}
