/**
 * Los seis reportes del archivo. El valor es la parte de la URL del back:
 * GET /api/reportes/{tipo}
 */
export type TipoReporte =
  | 'estatus'
  | 'eventos'
  | 'copias'
  | 'custodias'
  | 'proyectos'
  | 'expertos';

/** Una opción del selector de reportes: su valor, su nombre y qué responde. */
export interface OpcionReporte {
  valor: TipoReporte;
  etiqueta: string;
  descripcion: string;
}

/** Los reportes disponibles, en el orden en que se muestran en el selector. */
export const OPCIONES_REPORTE: OpcionReporte[] = [
  {
    valor: 'estatus',
    etiqueta: 'Estatus legal',
    descripcion: 'Cómo ha cambiado el estatus legal de los videojuegos a lo largo del tiempo.'
  },
  {
    valor: 'eventos',
    etiqueta: 'Eventos de riesgo',
    descripcion: 'Los eventos de riesgo que han sufrido los videojuegos.'
  },
  {
    valor: 'copias',
    etiqueta: 'Copias',
    descripcion: 'Las copias registradas, con su estado de integridad y la institución que las custodia hoy.'
  },
  {
    valor: 'custodias',
    etiqueta: 'Custodias',
    descripcion: 'En qué institución ha estado cada copia, desde cuándo y hasta cuándo.'
  },
  {
    valor: 'proyectos',
    etiqueta: 'Proyectos de restauración',
    descripcion: 'Los proyectos de restauración, con su estado y si ya tienen informe final.'
  },
  {
    valor: 'expertos',
    etiqueta: 'Participación de expertos',
    descripcion: 'Qué expertos participaron en cada proyecto, con su rol y sus fechas.'
  }
];

/**
 * Filtros opcionales de GET /api/reportes/{tipo}. Los que no se usan no se envían.
 *  - idVideojuego: solo ese videojuego
 *  - desde / hasta: rango de fechas, en formato AAAA-MM-DD
 */
export interface FiltrosReporte {
  idVideojuego?: number;
  desde?: string;
  hasta?: string;
}

/**
 * GET /api/reportes/{tipo}
 * Un reporte como tabla genérica, igual para los seis tipos.
 *  - Cada fila trae un texto por columna, en el mismo orden de "columnas".
 *  - Todo llega ya formateado por el back (fechas dd/MM/yyyy, "Sí", "Vigente"...);
 *    una celda vacía llega como "".
 *  - Si no hay registros, "filas" viene vacía (las columnas siempre vienen).
 */
export interface Reporte {
  titulo: string;
  columnas: string[];
  filas: string[][];
}
