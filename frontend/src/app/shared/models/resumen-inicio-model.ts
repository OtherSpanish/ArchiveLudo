import { Videojuego } from './videojuego-model';

/**
 * GET /api/inicio/resumen
 * El videojuego que más urge intervenir, con los motivos que lo ponen primero.
 * Lo decide el back entre los juegos "en riesgo".
 *  - videojuego: el Videojuego completo (trae urlImagen y descripcion)
 *  - nombreEstudio: nombre del estudio desarrollador, si lo tiene
 *  - motivos: textos listos para mostrar; puede venir vacía
 */
export interface PrioridadIntervencion {
  videojuego: Videojuego;
  nombreEstudio?: string;
  motivos: string[];
}

/**
 * GET /api/inicio/resumen
 * Todo el contenido de la pantalla de Inicio en una sola respuesta.
 *  - Los cuatro contadores traen el total real (sirven para "Ver todos (N)").
 *  - prioridadIntervencion no viene si no hay juegos en riesgo.
 *  - Cada lista trae como máximo 4 videojuegos.
 */
export interface ResumenInicio {
  titulosCatalogados: number;
  enRiesgo: number;
  totalmentePerdidos: number;
  restaurados: number;
  prioridadIntervencion?: PrioridadIntervencion;
  enRiesgoLista: Videojuego[];
  perdidosLista: Videojuego[];
  restauradosLista: Videojuego[];
}
