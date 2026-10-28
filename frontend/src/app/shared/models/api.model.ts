import { Videojuego } from './videojuego.model';
import { Copia, EstadoIntegridad, TipoCopia } from './copia.model';
import { Custodia, Institucion } from './custodia.model';

/*
 * CONTRATO CON EL BACK.
 * Cada interfaz de este archivo describe el JSON que debe devolver un
 * endpoint de Spring Boot. Los nombres de los campos tienen que coincidir
 * exactamente con los del DTO de Java. Pásaselo a quien arme la API.
 */

/*
 * GET /api/inicio/resumen
 * Datos de los 4 contadores y del banner de prioridad.
 *
 * El párrafo del banner sale de videojuegoPrioritario.descripcion (el campo
 * "descripcion" de VIDEOJUEGO en el MER), por eso no viene aparte.
 * criteriosPrioritario son los tres criterios de riesgo que cumple, como texto.
 */
export interface ResumenInicio {
  totalTitulos: number;
  totalEnRiesgo: number;
  totalPerdidos: number;
  totalConProyectoActivo: number;
  videojuegoPrioritario: Videojuego;
  criteriosPrioritario: string[];
}

/*
 * Un videojuego junto con su línea de "meta" (nº de copias, estatus legal, etc.).
 * La meta no es un campo de VIDEOJUEGO: la calcula el backend.
 */
export interface JuegoConMeta {
  videojuego: Videojuego;
  meta: string;
}

/*
 * GET /api/videojuegos?estado=en_riesgo&limite=10
 * Una fila del Inicio: cuántos juegos hay en total en esa categoría
 * (el número de "Ver todos (61)") y solo los primeros, para el carrusel.
 * El Catálogo usa la misma forma, con los parámetros extra q y pagina.
 */
export interface JuegosPorEstado {
  total: number;
  juegos: JuegoConMeta[];
}

/*
 * GET /api/videojuegos/{id}
 * Cabecera de la ficha.
 *  - estatusLegal: el estatus legal vigente. No es una columna: es el registro
 *    de ESTATUS_LEGAL con la fechaDeterminacion más reciente de ese videojuego,
 *    y lo calcula el backend.
 *  - numeroCopias y numeroProyectos: los números de las pestañas.
 */
export interface DatosFicha {
  videojuego: Videojuego;
  estatusLegal: string;
  numeroCopias: number;
  numeroProyectos: number;
}

/*
 * GET /api/videojuegos/{id}/copias
 * Una copia junto con su custodia vigente (la que no tiene fechaFin).
 * custodiaActual es opcional: una copia puede no tener custodia registrada.
 */
export interface CopiaConCustodia {
  copia: Copia;
  custodiaActual?: Custodia;
}

/*
 * GET /api/copias/{id}/custodias
 * No necesita un tipo propio: devuelve una lista de Custodia
 * (historial completo, con fechaInicio y fechaFin).
 */

/*
 * Una copia que una institución custodia ahora mismo, junto con el
 * videojuego al que pertenece. Se usa en la ficha de institución.
 *  - fechaInicio y condicionAlmacenamiento: los de la custodia vigente
 *    de esa copia en esa institución.
 */
export interface CopiaCustodiada {
  idCopia: number;
  tipoCopia: TipoCopia;
  estadoIntegridad: EstadoIntegridad;
  videojuego: Videojuego;
  fechaInicio: string;
  condicionAlmacenamiento?: string;
}

/*
 * GET /api/instituciones/{id}
 * Ficha de una institución.
 *  - telefonos y correos: las listas completas, de las tablas
 *    INST_TELEFONO e INST_CORREO. Pueden venir vacías.
 *  - copias: las copias con custodia vigente en esta institución
 *    (custodias sin fechaFin), de cualquier videojuego.
 */
export interface DatosInstitucion {
  institucion: Institucion;
  telefonos: string[];
  correos: string[];
  copias: CopiaCustodiada[];
}
