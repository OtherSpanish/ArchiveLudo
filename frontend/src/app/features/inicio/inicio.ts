import { Component } from '@angular/core';
import { Videojuego } from '../../shared/models/videojuego.model';
import { JuegoConMeta } from '../../shared/models/api.model';

// ===== BACK: descomentar estas 3 líneas al conectar =====
// import { ChangeDetectorRef } from '@angular/core';
// import { ArchiveludoApi } from '../../core/archiveludo-api';
// import { JuegosPorEstado, ResumenInicio } from '../../shared/models/api.model';

/*
 * Un contador de la parte de arriba: etiqueta, número y color del punto.
 * Es un tipo solo del front: el back manda los totales sueltos
 * (ver ResumenInicio) y aquí armamos los contadores.
 */
interface Contador {
  etiqueta: string;
  valor: number;
  color: 'info' | 'ok' | 'warn' | 'bad';
}

/*
 * Una fila del Inicio: "En riesgo crítico", "Perdidos" o "Restaurados con éxito".
 *  - color: el color del punto junto al título (coincide con las clases del CSS)
 *  - estado: valor de la base de datos, para pedirlo a la API y para el enlace "Ver todos"
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
  standalone: false,
})
export class Inicio {
  // ==================================================================
  // ===== HARDCODE: borrar todo este bloque al conectar el back =====
  // ==================================================================

  contadores: Contador[] = [
    { etiqueta: 'Títulos catalogados', valor: 248, color: 'info' },
    { etiqueta: 'En riesgo', valor: 61, color: 'warn' },
    { etiqueta: 'Perdidos', valor: 37, color: 'bad' },
    { etiqueta: 'Con proyecto activo', valor: 19, color: 'ok' },
  ];

  juegoPrioritario: Videojuego = {
    idVideojuego: 1,
    titulo: 'Torre de Ámbar Online',
    plataformaOriginal: 'PC',
    anioLanzamiento: 2007,
    estadoConservacion: 'en_riesgo',
    estudio: {
      idEstudio: 1,
      nombre: 'Foro Digital',
      pais: 'Colombia',
      anioFundacion: 1998,
      anioCierre: 2009,
      estadoActual: 'desaparecido',
    },
  };

  descripcionPrioritario: string =
    'Juego en línea cuyos servidores ya no existen. No tiene copias físicas ' +
    'registradas ni un proyecto de restauración en marcha, y su estudio ya cerró.';

  criteriosPrioritario: string[] = [
    'Sin copias físicas registradas',
    'Sin proyecto de restauración activo',
    'Estudio desarrollador desaparecido',
  ];

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
            estadoConservacion: 'en_riesgo',
          },
          meta: 'Sin copias físicas registradas',
        },
        {
          videojuego: {
            idVideojuego: 2,
            titulo: 'Crónicas de Nébula',
            plataformaOriginal: 'PS2',
            anioLanzamiento: 2003,
            estadoConservacion: 'en_riesgo',
          },
          meta: '1 copia · Sin proyecto activo',
        },
        {
          videojuego: {
            idVideojuego: 3,
            titulo: 'Ciudad Neón 2039',
            plataformaOriginal: 'Dreamcast',
            anioLanzamiento: 2000,
            estadoConservacion: 'en_riesgo',
          },
          meta: '2 copias · Estatus legal: huérfano',
        },
      ],
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
            estadoConservacion: 'perdido',
          },
          meta: 'Sin copias registradas',
        },
        {
          videojuego: {
            idVideojuego: 5,
            titulo: 'Legión Delta',
            plataformaOriginal: 'Saturn',
            anioLanzamiento: 1997,
            estadoConservacion: 'perdido',
          },
          meta: 'Código fuente perdido',
        },
        {
          videojuego: {
            idVideojuego: 6,
            titulo: 'Ruinas de Océano',
            plataformaOriginal: 'N64',
            anioLanzamiento: 2000,
            estadoConservacion: 'perdido',
          },
          meta: 'Servidores cerrados',
        },
      ],
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
            estadoConservacion: 'restaurado',
          },
          meta: 'Informe final publicado',
        },
        {
          videojuego: {
            idVideojuego: 8,
            titulo: 'Furia Quark',
            plataformaOriginal: 'Arcade',
            anioLanzamiento: 1994,
            estadoConservacion: 'restaurado',
          },
          meta: '3 copias · Dominio público',
        },
        {
          videojuego: {
            idVideojuego: 9,
            titulo: 'Sendero Lunar',
            plataformaOriginal: 'SNES',
            anioLanzamiento: 1996,
            estadoConservacion: 'restaurado',
          },
          meta: 'Informe final publicado',
        },
      ],
    },
  ];

  // ==================================================================
  // ===== FIN DEL HARDCODE =====
  // ==================================================================

  // ==================================================================
  // ===== BACK: descomentar todo este bloque al conectar =====
  // (en WebStorm: seleccionarlo y Ctrl + /)
  // ==================================================================

  // // Empiezan vacíos y se llenan cuando el back responde.
  // contadores: Contador[] = [];
  //
  // // El "?" indica que al principio no tiene valor. El HTML ya revisa
  // // que exista antes de dibujar el banner.
  // juegoPrioritario?: Videojuego;
  //
  // descripcionPrioritario: string = '';
  //
  // criteriosPrioritario: string[] = [];
  //
  // // Las tres filas existen desde el inicio (con total 0 y sin juegos);
  // // cada una se llena con su propia llamada a la API.
  // filas: FilaJuegos[] = [
  //   { titulo: 'En riesgo crítico', color: 'warn', estado: 'en_riesgo', total: 0, juegos: [] },
  //   { titulo: 'Perdidos', color: 'bad', estado: 'perdido', total: 0, juegos: [] },
  //   { titulo: 'Restaurados con éxito', color: 'ok', estado: 'restaurado', total: 0, juegos: [] }
  // ];
  //
  // // ChangeDetectorRef: avisa a Angular que hay datos nuevos para redibujar.
  // // Se llama a markForCheck() cada vez que llega una respuesta.
  // constructor(private api: ArchiveludoApi, private cambios: ChangeDetectorRef) { }
  //
  // // Angular llama a ngOnInit una vez, cuando la pantalla está lista.
  // ngOnInit(): void {
  //   this.cargarResumen();
  //
  //   for (const fila of this.filas) {
  //     this.cargarFila(fila);
  //   }
  // }
  //
  // // Pide contadores y banner: GET /api/inicio/resumen
  // private cargarResumen(): void {
  //   this.api.obtenerResumenInicio().subscribe({
  //     next: (resumen) => this.recibirResumen(resumen),
  //     error: (error) => this.mostrarError(error)
  //   });
  // }
  //
  // private recibirResumen(resumen: ResumenInicio): void {
  //   this.contadores = [
  //     { etiqueta: 'Títulos catalogados', valor: resumen.totalTitulos, color: 'info' },
  //     { etiqueta: 'En riesgo', valor: resumen.totalEnRiesgo, color: 'warn' },
  //     { etiqueta: 'Perdidos', valor: resumen.totalPerdidos, color: 'bad' },
  //     { etiqueta: 'Con proyecto activo', valor: resumen.totalConProyectoActivo, color: 'ok' }
  //   ];
  //
  //   this.juegoPrioritario = resumen.videojuegoPrioritario;
  //   this.descripcionPrioritario = resumen.videojuegoPrioritario.descripcion || '';
  //   this.criteriosPrioritario = resumen.criteriosPrioritario;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Pide los juegos de una fila: GET /api/videojuegos?estado=...&limite=10
  // private cargarFila(fila: FilaJuegos): void {
  //   this.api.obtenerVideojuegosPorEstado(fila.estado, 10).subscribe({
  //     next: (respuesta) => this.llenarFila(fila, respuesta),
  //     error: (error) => this.mostrarError(error)
  //   });
  // }
  //
  // private llenarFila(fila: FilaJuegos, respuesta: JuegosPorEstado): void {
  //   fila.total = respuesta.total;
  //   fila.juegos = respuesta.juegos;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Por ahora el error solo se escribe en la consola del navegador (F12).
  // private mostrarError(error: unknown): void {
  //   console.error('No se pudo cargar la información del Inicio', error);
  // }

  // ==================================================================
  // ===== FIN DEL BLOQUE BACK =====
  // ==================================================================
}
