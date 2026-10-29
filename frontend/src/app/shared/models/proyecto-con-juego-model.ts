import { Videojuego } from './videojuego-model';
import { ProyectoConDetalle } from './api-model';

/*
 * GET /api/proyectos
 * Un proyecto de restauración (con sus expertos) junto con el videojuego
 * al que pertenece. Se usa en la pantalla global de Proyectos, donde cada
 * tarjeta tiene que decir de qué juego es.
 */
export interface ProyectoConJuego {
  videojuego: Videojuego;
  detalle: ProyectoConDetalle;
}
