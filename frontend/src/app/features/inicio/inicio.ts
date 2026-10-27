import { Component } from '@angular/core';
import { Videojuego } from '../../shared/models/videojuego.model';

/*
 * Un contador de la parte de arriba: etiqueta, número y color del punto.
 */
interface Contador {
  etiqueta: string;
  valor: number;
  color: 'info' | 'ok' | 'warn' | 'bad';
}

/*
 * Un videojuego junto con su línea de "meta" (nº de copias, estatus legal, etc.).
 * La meta no es un campo de VIDEOJUEGO: la calcula el backend, y por eso
 * la guardamos aparte, junto al videojuego.
 */
interface JuegoConMeta {
  videojuego: Videojuego;
  meta: string;
}

/*
 * Una fila del Inicio: "En riesgo crítico", "Perdidos" o "Restaurados con éxito".
 *  - color: el color del punto junto al título (coincide con las clases del CSS)
 *  - estado: valor de la base de datos, para el enlace "Ver todos"
 *  - total: cuántos juegos hay en esa categoría en total (no solo los visibles)
 */
interface FilaJuegos {
  titulo: string;
  color: 'ok' | 'warn' | 'bad';
  estado: string;
  total: number;
  juegos: JuegoConMeta[];
}

@Component({
  selector: 'app-inicio',
  templateUrl: './inicio.html',
  styleUrl: './inicio.css',
  standalone: false
})
export class Inicio {

  /*
   * DATOS DE PRUEBA de los 4 contadores. Más adelante vendrán de
   * GET /api/inicio/resumen y estos números inventados desaparecen.
   */
  contadores: Contador[] = [
    { etiqueta: 'Títulos catalogados', valor: 248, color: 'info' },
    { etiqueta: 'En riesgo', valor: 61, color: 'warn' },
    { etiqueta: 'Perdidos', valor: 37, color: 'bad' },
    { etiqueta: 'Con proyecto activo', valor: 19, color: 'ok' }
  ];

  /*
   * DATOS DE PRUEBA de las filas. Más adelante este arreglo se llena con lo que
   * devuelva la API (GET /api/videojuegos?estado=...) y estos
   * datos inventados desaparecen.
   */
  filas: FilaJuegos[] = [
    {
      titulo: 'En riesgo crítico',
      color: 'warn',
      estado: 'en_riesgo',
      total: 61,
      juegos: [
        {
          videojuego: {
            idVideojuego: 1,
            titulo: 'Torre de Ámbar Online',
            plataformaOriginal: 'PC',
            anioLanzamiento: 2007,
            estadoConservacion: 'en_riesgo'
          },
          meta: 'Sin copias físicas registradas'
        },
        {
          videojuego: {
            idVideojuego: 2,
            titulo: 'Crónicas de Nébula',
            plataformaOriginal: 'PS2',
            anioLanzamiento: 2003,
            estadoConservacion: 'en_riesgo'
          },
          meta: '1 copia · Sin proyecto activo'
        },
        {
          videojuego: {
            idVideojuego: 3,
            titulo: 'Ciudad Neón 2039',
            plataformaOriginal: 'Dreamcast',
            anioLanzamiento: 2000,
            estadoConservacion: 'en_riesgo'
          },
          meta: '2 copias · Estatus legal: huérfano'
        }
      ]
    },
    {
      titulo: 'Perdidos',
      color: 'bad',
      estado: 'perdido',
      total: 37,
      juegos: [
        {
          videojuego: {
            idVideojuego: 4,
            titulo: 'Mundo Tinta',
            plataformaOriginal: 'PC',
            anioLanzamiento: 1999,
            estadoConservacion: 'perdido'
          },
          meta: 'Sin copias registradas'
        },
        {
          videojuego: {
            idVideojuego: 5,
            titulo: 'Legión Delta',
            plataformaOriginal: 'Saturn',
            anioLanzamiento: 1997,
            estadoConservacion: 'perdido'
          },
          meta: 'Código fuente perdido'
        },
        {
          videojuego: {
            idVideojuego: 6,
            titulo: 'Ruinas de Océano',
            plataformaOriginal: 'N64',
            anioLanzamiento: 2000,
            estadoConservacion: 'perdido'
          },
          meta: 'Servidores cerrados'
        }
      ]
    },
    {
      titulo: 'Restaurados con éxito',
      color: 'ok',
      estado: 'restaurado',
      total: 12,
      juegos: [
        {
          videojuego: {
            idVideojuego: 7,
            titulo: 'Aventura Marea',
            plataformaOriginal: 'GBA',
            anioLanzamiento: 2002,
            estadoConservacion: 'restaurado'
          },
          meta: 'Informe final publicado'
        },
        {
          videojuego: {
            idVideojuego: 8,
            titulo: 'Furia Quark',
            plataformaOriginal: 'Arcade',
            anioLanzamiento: 1994,
            estadoConservacion: 'restaurado'
          },
          meta: '3 copias · Dominio público'
        },
        {
          videojuego: {
            idVideojuego: 9,
            titulo: 'Sendero Lunar',
            plataformaOriginal: 'SNES',
            anioLanzamiento: 1996,
            estadoConservacion: 'restaurado'
          },
          meta: 'Informe final publicado'
        }
      ]
    }
  ];
}
