/*
 * Experto en restauración. Viene de GET /api/expertos.
 * El nombre llega en cuatro partes (no hay campo "nombre"): se arma con
 * nombreCompletoExperto.
 */
export interface Experto {
  idExperto: number;
  primerNombreEx: string;
  segundoNombreEx?: string;
  primerApellidoEx: string;
  segundoApellidoEx?: string;
  especialidad: string;
  afiliacion?: string;
  correo: string;
}

/* Cuerpo de POST /api/expertos y PUT /api/expertos/{id}. */
export interface ExpertoNuevo {
  primerNombreEx: string;
  segundoNombreEx?: string;
  primerApellidoEx: string;
  segundoApellidoEx?: string;
  especialidad: string;
  afiliacion?: string;
  correo: string;
}

/* Une las partes del nombre que vengan, separadas por un espacio. */
export function nombreCompletoExperto(experto: Experto): string {
  const partes: string[] = [];

  if (experto.primerNombreEx) {
    partes.push(experto.primerNombreEx);
  }
  if (experto.segundoNombreEx) {
    partes.push(experto.segundoNombreEx);
  }
  if (experto.primerApellidoEx) {
    partes.push(experto.primerApellidoEx);
  }
  if (experto.segundoApellidoEx) {
    partes.push(experto.segundoApellidoEx);
  }

  return partes.join(' ');
}
