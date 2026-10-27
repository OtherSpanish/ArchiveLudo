// Valores exactos que guarda la base de datos en estado_conservacion
export type EstadoConservacion =
  | 'activo'
  | 'en_riesgo'
  | 'parcialmente_perdido'
  | 'perdido'
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
}
