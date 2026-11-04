/**
 * Modelos del informe final de un proyecto de restauración (PDF).
 * El back genera el PDF con los datos del formulario y los del proyecto, lo sube
 * a Cloudinary y guarda solo el enlace. El front nunca habla con Cloudinary.
 */

/** Resultado del informe. Valores exactos que viajan en el JSON. */
export type ResultadoInforme = 'exitoso' | 'parcial' | 'fracaso';

/**
 * Cuerpo de POST y PUT /api/proyectos/{id}/informe.
 *  - fechaPublicacion: "YYYY-MM-DD".
 *  - resumenResultados: obligatorio, hasta 2000 caracteres.
 */
export interface InformeFinalSolicitud {
  fechaPublicacion: string;
  resumenResultados: string;
  resultado: ResultadoInforme;
}

/**
 * Respuesta del back. En el GET solo vienen idProyecto y urlInforme;
 * en el POST y el PUT vienen además los datos del formulario.
 */
export interface InformeFinal {
  idProyecto: number;
  urlInforme: string;
  fechaPublicacion?: string;
  resumenResultados?: string;
  resultado?: ResultadoInforme;
}
