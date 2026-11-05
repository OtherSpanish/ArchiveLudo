/**
 * Institución que custodia copias (museo, universidad, archivo o colección
 * privada). Viene de GET /api/instituciones y GET /api/instituciones/{id}.
 *
 *  - tipo: ya llega legible ("Museo", "Universidad", "Archivo", "Colección privada").
 *  - telefonos: lista de números (puede venir vacía).
 *  - correos: lista de correos (puede venir vacía).
 * Ya no hay contacto, dirección ni ciudad: esos campos no existen en el back.
 */
export interface Institucion {
  idInstitucion: number;
  nombre: string;
  tipo: string;
  pais: string;
  telefonos: number[];
  correos: string[];
  /** Cuántas copias guarda hoy (custodias vigentes). La calcula el back; siempre llega. */
  cantidadCopiasCustodiadas: number;
  /**
   * Cuántas copias de UN videojuego custodia hoy. Solo llega en
   * GET /api/videojuegos/{id}/instituciones; en los demás endpoints no viene.
   */
  cantidadCopiasDelVideojuego?: number;
}

/**
 * Cuerpo de POST /api/instituciones y PUT /api/instituciones/{id}.
 *  - tipo: texto legible ("Museo", "Universidad", "Archivo", "Colección privada").
 *  - telefonos y correos: al menos uno de cada uno; en un PUT REEMPLAZAN las listas.
 */
export interface InstitucionNueva {
  nombre: string;
  tipo: string;
  pais: string;
  telefonos: number[];
  correos: string[];
}

/**
 * Un periodo en el que una institución tuvo una copia bajo su custodia.
 * Viene de la tabla CUSTODIA.
 *
 * fechaFin vacía (con "?") significa que es la custodia vigente.
 * Las fechas llegan como texto con formato 'AAAA-MM-DD'.
 */
export interface Custodia {
  idCustodia: number;
  idCopia: number;
  idInstitucion: number;
  nombreInstitucion?: string;
  fechaInicio: string;
  fechaFin?: string;
  condicionAlmacenamiento: string;
}

/**
 * Cuerpo de POST /api/copias/{id}/custodias y PUT /api/custodias/{id}.
 * En el POST no va fechaFin: el back siempre crea la vigente y cierra la anterior.
 */
export interface CustodiaNueva {
  idInstitucion: number;
  fechaInicio: string;
  fechaFin?: string;
  condicionAlmacenamiento: string;
}
