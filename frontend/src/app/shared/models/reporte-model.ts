/*
 * Los seis reportes de trazabilidad. El valor es la parte de la URL del back:
 * GET /api/reportes/{tipo}
 */
export type TipoReporte =
  | 'estatus-legal'
  | 'eventos-riesgo'
  | 'proyectos'
  | 'copias'
  | 'custodias'
  | 'expertos';

/* Una opción del selector de reportes: su valor, su nombre y qué responde. */
export interface OpcionReporte {
  valor: TipoReporte;
  etiqueta: string;
  descripcion: string;
}

/* Los reportes disponibles, en el orden en que se muestran en el selector. */
export const OPCIONES_REPORTE: OpcionReporte[] = [
  {
    valor: 'estatus-legal',
    etiqueta: 'Estatus legal',
    descripcion: 'Cómo ha cambiado el estatus legal del juego a lo largo del tiempo.'
  },
  {
    valor: 'eventos-riesgo',
    etiqueta: 'Eventos de riesgo',
    descripcion: 'Los eventos de riesgo que ha sufrido el juego.'
  },
  {
    valor: 'proyectos',
    etiqueta: 'Proyectos de restauración',
    descripcion: 'Los proyectos de restauración que ha habido sobre el juego.'
  },
  {
    valor: 'copias',
    etiqueta: 'Copias en el tiempo',
    descripcion: 'Cuántas copias ha tenido el juego a lo largo del tiempo.'
  },
  {
    valor: 'custodias',
    etiqueta: 'Custodias',
    descripcion: 'En qué institución ha estado cada copia, desde cuándo y hasta cuándo.'
  },
  {
    valor: 'expertos',
    etiqueta: 'Participación de expertos',
    descripcion: 'Qué expertos participaron en la restauración del juego en el rango elegido.'
  }
];

/*
 * GET /api/reportes/{tipo}?idVideojuego=8&desde=2025-01-01&hasta=2025-12-31
 * Un reporte como tabla genérica: los nombres de las columnas y las filas,
 * donde cada celda es un texto.
 *  - "desde" y "hasta" son opcionales; si no vienen, el reporte es completo.
 *  - Las fechas viajan como yyyy-MM-dd y la pantalla las formatea sola.
 *  - Una celda vacía se muestra como "—". Para algo en curso, el back
 *    manda un texto como "Vigente".
 *  - Si no hay registros, "filas" viene vacía (las columnas siempre vienen).
 */
export interface ReporteTabla {
  columnas: string[];
  filas: string[][];
}
