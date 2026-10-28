import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Videojuego } from '../../../shared/models/videojuego.model';
import { DatosFicha } from '../../../shared/models/api.model';

// ===== BACK: descomentar estas 2 líneas al conectar =====
// import { ChangeDetectorRef } from '@angular/core';
// import { ArchiveludoApi } from '../../../core/archiveludo-api';

/*
 * Las cuatro pestañas de la ficha. Cada valor es el identificador interno
 * de la pestaña; el texto que ve el usuario se arma en el HTML.
 */
type Pestana = 'copias' | 'proyectos' | 'legal' | 'eventos';

/*
 * Pantalla de la ficha de un videojuego (/videojuegos/:id).
 * En este paso: cabecera y pestañas (el contenido de cada pestaña viene después).
 */
@Component({
  selector: 'app-ficha-videojuego',
  templateUrl: './ficha-videojuego.html',
  styleUrl: './ficha-videojuego.css',
  standalone: false,
})
export class FichaVideojuego implements OnInit {
  /*
   * El juego que se está mostrando. Empieza sin valor (undefined) y se llena
   * cuando se carga. El "?" indica que puede no tener valor: si el id de la URL
   * no existe, se queda así y el HTML muestra un mensaje de "no encontrado".
   */
  videojuego?: Videojuego;

  estatusLegal: string = '';

  numeroCopias: number = 0;

  numeroProyectos: number = 0;

  /* Pestaña que se está viendo. Empieza en "Copias", como en el boceto. */
  pestanaActiva: Pestana = 'copias';

  // ==================================================================
  // ===== HARDCODE: borrar este bloque al conectar el back =====
  // (incluye datosPrueba, el constructor y la función cargarJuego)
  // ==================================================================

  /*
   * DATOS DE PRUEBA. Los ids coinciden con los de las tarjetas de Inicio,
   * así los enlaces funcionan.
   */
  private datosPrueba: DatosFicha[] = [
    {
      videojuego: {
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
      },
      estatusLegal: 'Huérfano',
      numeroCopias: 1,
      numeroProyectos: 0,
    },
    {
      videojuego: {
        idVideojuego: 8,
        titulo: 'Furia Quark',
        plataformaOriginal: 'Arcade',
        anioLanzamiento: 1994,
        estadoConservacion: 'restaurado',
        estudio: {
          idEstudio: 2,
          nombre: 'Quark Interactive',
          pais: 'Japón',
          anioFundacion: 1989,
          anioCierre: 2001,
          estadoActual: 'desaparecido',
        },
      },
      estatusLegal: 'Dominio público',
      numeroCopias: 3,
      numeroProyectos: 1,
    },
  ];

  /*
   * ActivatedRoute es la herramienta de Angular para leer datos de la URL actual.
   * Se pide en el constructor y Angular la entrega sola (inyección de dependencias).
   */
  constructor(private route: ActivatedRoute) {}

  /*
   * Recorre los datos de prueba con un for normal.
   * Si encuentra un juego con ese id, guarda el juego y sus datos extra.
   */
  private cargarJuego(id: number): void {
    this.limpiarJuego();

    for (const datos of this.datosPrueba) {
      if (datos.videojuego.idVideojuego === id) {
        this.videojuego = datos.videojuego;
        this.estatusLegal = datos.estatusLegal;
        this.numeroCopias = datos.numeroCopias;
        this.numeroProyectos = datos.numeroProyectos;
      }
    }
  }

  // ==================================================================
  // ===== FIN DEL HARDCODE =====
  // ==================================================================

  // ==================================================================
  // ===== BACK: descomentar todo este bloque al conectar =====
  // (en WebStorm: seleccionarlo y Ctrl + /)
  // ==================================================================

  // // ArchiveludoApi habla con el back. ChangeDetectorRef avisa a Angular
  // // que hay datos nuevos para redibujar la pantalla.
  // constructor(
  //   private route: ActivatedRoute,
  //   private api: ArchiveludoApi,
  //   private cambios: ChangeDetectorRef
  // ) { }
  //
  // // Pide la cabecera: GET /api/videojuegos/{id}
  // private cargarJuego(id: number): void {
  //   this.limpiarJuego();
  //
  //   this.api.obtenerFicha(id).subscribe({
  //     next: (datos) => this.recibirFicha(datos),
  //     error: (error) => this.mostrarError(error)
  //   });
  // }
  //
  // private recibirFicha(datos: DatosFicha): void {
  //   this.videojuego = datos.videojuego;
  //   this.estatusLegal = datos.estatusLegal;
  //   this.numeroCopias = datos.numeroCopias;
  //   this.numeroProyectos = datos.numeroProyectos;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Si el back responde error (por ejemplo 404 porque el id no existe), el juego
  // // se queda sin valor y el HTML muestra "No encontramos ese videojuego".
  // private mostrarError(error: unknown): void {
  //   console.error('No se pudo cargar la ficha del videojuego', error);
  //   this.cambios.markForCheck();
  // }

  // ==================================================================
  // ===== FIN DEL BLOQUE BACK =====
  // ==================================================================

  /*
   * ngOnInit se ejecuta una vez, cuando el componente ya está listo.
   * paramMap avisa cada vez que cambian los parámetros de la URL, así que la
   * ficha se actualiza aunque el usuario pase de /videojuegos/1 a /videojuegos/8
   * sin salir de la pantalla.
   */
  ngOnInit(): void {
    this.route.paramMap.subscribe({
      next: (parametros) => this.alCambiarParametros(parametros),
    });
  }

  /* Lee el :id de la URL y pide ese juego. */
  private alCambiarParametros(parametros: ParamMap): void {
    // En /videojuegos/8 el parámetro "id" vale '8', pero como TEXTO.
    // Number() lo convierte a número para poder compararlo con idVideojuego.
    const id = Number(parametros.get('id'));

    this.cargarJuego(id);
  }

  /* Borra el juego anterior, para no mostrar datos mezclados al cambiar de id. */
  private limpiarJuego(): void {
    this.videojuego = undefined;
    this.estatusLegal = '';
    this.numeroCopias = 0;
    this.numeroProyectos = 0;
  }

  /* Cambia la pestaña activa cuando el usuario hace clic en una. */
  seleccionarPestana(pestana: Pestana): void {
    this.pestanaActiva = pestana;
  }

  /*
   * Colores planos de relleno para la portada (sección "Portadas de relleno"
   * de la guía de diseño). Es la misma lista que usa juego-card.
   */
  private coloresPortada: string[] = [
    '#2B3A55',
    '#3A2B55',
    '#1F4A52',
    '#4A3A24',
    '#2B4A3A',
    '#4A2B3F',
  ];

  /*
   * Color de la portada según el id del juego, así cada juego conserva
   * siempre el mismo color. El "%" hace que vuelva a empezar después del sexto.
   */
  get colorPortada(): string {
    if (!this.videojuego) {
      return this.coloresPortada[0];
    }
    const posicion = this.videojuego.idVideojuego % this.coloresPortada.length;
    return this.coloresPortada[posicion];
  }

  /* Iniciales del título: "Furia Quark" -> "FQ" */
  get iniciales(): string {
    if (!this.videojuego) {
      return '';
    }

    const palabras = this.videojuego.titulo.trim().split(' ');
    let resultado = '';

    for (const palabra of palabras) {
      if (palabra.length > 0 && resultado.length < 2) {
        resultado += palabra.charAt(0).toUpperCase();
      }
    }

    return resultado;
  }

  /* Línea de plataforma y año: "Arcade · 1994" */
  get plataformaYAnio(): string {
    if (!this.videojuego) {
      return '';
    }
    return this.videojuego.plataformaOriginal + ' · ' + this.videojuego.anioLanzamiento;
  }

  /* Línea del estudio: "Estudio: Quark Interactive (desaparecido)" */
  get textoEstudio(): string {
    if (!this.videojuego || !this.videojuego.estudio) {
      return '';
    }
    const estudio = this.videojuego.estudio;
    return 'Estudio: ' + estudio.nombre + ' (' + estudio.estadoActual + ')';
  }
}
