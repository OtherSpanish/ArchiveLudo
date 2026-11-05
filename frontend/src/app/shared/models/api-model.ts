import { Videojuego } from './videojuego-model';
import { ProyectoDeRestauracion } from './proyecto-model';

/**
 * CONTRATO CON EL BACK.
 * Cada interfaz de este archivo describe el JSON que debe devolver un
 * endpoint de Spring Boot. Los nombres de los campos tienen que coincidir
 * exactamente con los del DTO de Java. Pásaselo a quien arme la API.
 */

/**
 * Un videojuego junto con su línea de "meta" (nº de copias, estatus legal, etc.).
 * La meta no es un campo de VIDEOJUEGO: la calcula el backend.
 */
export interface JuegoConMeta {
  videojuego: Videojuego;
  meta: string;
}

/**
 * GET /api/videojuegos?estado=en_riesgo&limite=10
 * Una fila del Inicio: cuántos juegos hay en total en esa categoría
 * (el número de "Ver todos (61)") y solo los primeros, para el carrusel.
 * El Catálogo usa la misma forma, con los parámetros extra q y pagina.
 */
/**
 * Página de videojuegos del back real (Catálogo y En riesgo).
 * El back devuelve un arreglo plano en el cuerpo y el total en la cabecera
 * X-Total-Count; el servicio los junta en este objeto.
 */
export interface PaginaVideojuegos {
  total: number;
  juegos: Videojuego[];
}

/**
 * Respuesta de GET /api/proyectos: el arreglo de proyectos en el cuerpo y su
 * cantidad en la cabecera X-Total-Count; el servicio los junta en este objeto.
 */
export interface ListaProyectos {
  total: number;
  proyectos: ProyectoDeRestauracion[];
}

export interface JuegosPorEstado {
  total: number;
  juegos: JuegoConMeta[];
}

/**
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

/**
 * GET /api/copias/{id}/custodias
 * No necesita un tipo propio: devuelve una lista de Custodia
 * (historial completo, con fechaInicio y fechaFin).
 */
