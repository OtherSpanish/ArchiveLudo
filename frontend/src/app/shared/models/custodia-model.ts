/*
 * Institución que custodia copias (museo, universidad, biblioteca,
 * coleccionista privado). Viene de la tabla INSTITUCION.
 *
 * En el MER los teléfonos y correos están en tablas aparte
 * (INST_TELEFONO e INST_CORREO). Aquí los resumimos en un solo texto,
 * "contacto", que es lo que la guía de diseño pide que mande la API.
 */
export interface Institucion {
  idInstitucion: number;
  nombre: string;
  tipo: string;
  pais: string;
  contacto?: string;
}

/*
 * Un periodo en el que una institución tuvo una copia bajo su custodia.
 * Viene de la tabla CUSTODIA.
 *
 * fechaFin vacía (con "?") significa que es la custodia vigente.
 * Las fechas llegan como texto con formato 'AAAA-MM-DD'.
 */
export interface Custodia {
  idCustodia: number;
  idCopia: number;
  institucion: Institucion;
  fechaInicio: string;
  fechaFin?: string;
  condicionAlmacenamiento?: string;
}
