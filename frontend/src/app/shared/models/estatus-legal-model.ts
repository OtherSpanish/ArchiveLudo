/*
 * Un registro del historial de estatus legal (tabla ESTATUS_LEGAL).
 * Los nombres de los campos coinciden con el MER y con el JSON del back.
 *
 *  - estatus: el estatus ya como texto legible ("Dominio público", "Huérfano").
 *    El back traduce el valor de la base de datos, igual que hace con la
 *    cabecera de la ficha.
 *  - fechaDeterminacion: texto 'yyyy-MM-dd'.
 *
 * No hay un campo "esVigente": el vigente es el registro con la
 * fechaDeterminacion más reciente, o sea el primero de la lista (el back
 * la entrega ordenada de más reciente a más antigua).
 */
export interface EstatusLegal {
  idEstatusLegal: number;
  estatus: string;
  fechaDeterminacion: string;
}
