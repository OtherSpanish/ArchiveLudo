import { Videojuego } from './videojuego-model';
import { ProyectoDeRestauracion } from './proyecto-model';
import { EstadoIntegridad, TipoCopia } from './copia-model';
import { Institucion } from './custodia-model';

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

/**
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

/**
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

/**
 * GET /api/instituciones
 * Lista de la pantalla /instituciones: una fila por institución.
 *  - numeroCopias: cuántas copias custodia ahora mismo (custodias sin
 *    fechaFin). Lo calcula el backend. Si no custodia ninguna, es 0.
 * Es un arreglo simple, sin paginación: se espera que haya pocas instituciones.
 * Orden: alfabético por nombre.
 */
export interface InstitucionConCopias {
  institucion: Institucion;
  numeroCopias: number;
}
