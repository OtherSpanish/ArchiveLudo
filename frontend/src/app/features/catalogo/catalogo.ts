import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { JuegoConMeta } from '../../shared/models/api.model';
import { EstadoConservacion } from '../../shared/models/videojuego.model';
// (la línea de arriba de EstadoConservacion solo la usa el hardcode:
//  bórrala junto con el bloque HARDCODE al conectar el back)

// ===== BACK: descomentar estas 2 líneas al conectar =====
// import { ArchiveludoApi } from '../../core/archiveludo-api';
// import { JuegosPorEstado } from '../../shared/models/api.model';

/*
 * Un botón de filtro de la fila de arriba.
 *  - etiqueta: el texto que ve el usuario
 *  - valor: el estado de la base de datos que se manda en la URL
 *    ('' significa "todos", sin filtrar)
 */
interface FiltroEstado {
  etiqueta: string;
  valor: string;
}

/*
 * Pantalla del Catálogo (/catalogo).
 * Lee de la URL el filtro de estado (?estado=en_riesgo) y el texto de
 * búsqueda (?q=torre), y muestra las tarjetas de los juegos que coinciden.
 * Si hay más juegos de los que caben en una página, el botón "Ver más"
 * carga la siguiente y la agrega al final.
 */
@Component({
  selector: 'app-catalogo',
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
  standalone: false,
})
export class Catalogo implements OnInit {
  /* Botones de filtro, en el orden en que se muestran. */
  filtros: FiltroEstado[] = [
    { etiqueta: 'Todos', valor: '' },
    { etiqueta: 'Activos', valor: 'activo' },
    { etiqueta: 'En riesgo', valor: 'en_riesgo' },
    { etiqueta: 'Parcialmente perdidos', valor: 'parcialmente_perdido' },
    { etiqueta: 'Perdidos', valor: 'perdido' },
    { etiqueta: 'Restaurados', valor: 'restaurado' },
  ];

  /* Estado elegido en la URL. Vacío significa "todos". */
  estadoSeleccionado: string = '';

  /* Texto de búsqueda de la URL. Vacío significa "sin búsqueda". */
  texto: string = '';

  /* Juegos que se están mostrando (se van acumulando al pulsar "Ver más"). */
  juegos: JuegoConMeta[] = [];

  /* Cuántos juegos coinciden en total, no solo los que se muestran. */
  total: number = 0;

  /* Página que se pidió por última vez. Empieza en 0. */
  pagina: number = 0;

  /*
   * Cuántos juegos trae cada página. Está en 8 para que con los datos de
   * prueba se pueda probar el botón "Ver más". Con datos reales súbelo a 20 o 24.
   */
  tamanoPagina: number = 8;

  /* true mientras se espera una respuesta. Sirve para no mostrar "sin resultados" antes de tiempo. */
  cargando: boolean = true;

  // ==================================================================
  // ===== HARDCODE: borrar este bloque al conectar el back =====
  // (incluye datosPrueba, el constructor y las funciones
  //  crearJuego, sinTildes y cargarPagina)
  // ==================================================================

  /*
   * DATOS DE PRUEBA: 14 juegos. Los ids 1 a 9 son los mismos de Inicio.
   * Solo los ids 1 y 8 tienen ficha de prueba; con los demás, la ficha
   * muestra "No encontramos ese videojuego".
   */
  private datosPrueba: JuegoConMeta[] = [
    this.crearJuego(
      1,
      'Torre de Ámbar Online',
      'PC',
      2007,
      'en_riesgo',
      'Sin copias registradas · Sin proyecto activo',
    ),
    this.crearJuego(
      2,
      'Crónicas de Nébula',
      'PS2',
      2003,
      'en_riesgo',
      '1 copia · Sin proyecto activo',
    ),
    this.crearJuego(
      3,
      'Ciudad Neón 2039',
      'Dreamcast',
      2000,
      'en_riesgo',
      '2 copias · Sin proyecto activo',
    ),
    this.crearJuego(4, 'Mundo Tinta', 'PC', 1999, 'perdido', 'Sin copias registradas'),
    this.crearJuego(
      5,
      'Legión Delta',
      'Saturn',
      1997,
      'perdido',
      'Sin copias registradas · Código fuente perdido',
    ),
    this.crearJuego(6, 'Ruinas de Océano', 'N64', 2000, 'perdido', '1 copia · Servidores cerrados'),
    this.crearJuego(
      7,
      'Aventura Marea',
      'GBA',
      2002,
      'restaurado',
      '2 copias · Informe final publicado',
    ),
    this.crearJuego(
      8,
      'Furia Quark',
      'Arcade',
      1994,
      'restaurado',
      '3 copias · Informe final publicado',
    ),
    this.crearJuego(
      9,
      'Sendero Lunar',
      'SNES',
      1996,
      'restaurado',
      '1 copia · Informe final publicado',
    ),
    this.crearJuego(10, 'Cosecha Cuántica', 'PC', 2005, 'activo', '4 copias'),
    this.crearJuego(11, 'Piratas del Vacío', 'Xbox', 2002, 'parcialmente_perdido', '2 copias'),
    this.crearJuego(12, 'Ritmo Sideral', 'PS1', 1998, 'activo', '3 copias'),
    this.crearJuego(13, 'Fortaleza Gélida', 'NES', 1991, 'parcialmente_perdido', '1 copia'),
    this.crearJuego(14, 'Órbita Cero', 'PC', 2010, 'activo', '5 copias'),
  ];

  /*
   * ActivatedRoute es la herramienta de Angular para leer datos de la URL actual.
   * ChangeDetectorRef sirve para avisarle a Angular que esta pantalla cambió
   * y hay que redibujarla (ver alCambiarFiltros).
   * Las dos se piden en el constructor y Angular las entrega solas.
   */
  constructor(
    private route: ActivatedRoute,
    private cambios: ChangeDetectorRef,
  ) {}

  /*
   * Arma un juego de prueba con los datos mínimos, para no repetir
   * la estructura completa 14 veces.
   */
  private crearJuego(
    id: number,
    titulo: string,
    plataforma: string,
    anio: number,
    estado: EstadoConservacion,
    meta: string,
  ): JuegoConMeta {
    return {
      videojuego: {
        idVideojuego: id,
        titulo: titulo,
        plataformaOriginal: plataforma,
        anioLanzamiento: anio,
        estadoConservacion: estado,
      },
      meta: meta,
    };
  }

  /*
   * Pasa el texto a minúsculas y le quita las tildes, para que buscar
   * "ambar" encuentre "Ámbar". normalize('NFD') separa cada letra de su
   * tilde, y el replace borra las tildes sueltas.
   */
  private sinTildes(texto: string): string {
    return texto
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  /*
   * Simula lo que haría el back: filtra los datos de prueba por estado y
   * por texto, y entrega solo la página pedida, que se suma a los juegos
   * que ya estaban en pantalla.
   */
  private cargarPagina(): void {
    const coinciden: JuegoConMeta[] = [];
    const textoBuscado = this.sinTildes(this.texto);

    for (const item of this.datosPrueba) {
      const coincideEstado =
        this.estadoSeleccionado === '' ||
        item.videojuego.estadoConservacion === this.estadoSeleccionado;

      const coincideTexto =
        textoBuscado === '' || this.sinTildes(item.videojuego.titulo).includes(textoBuscado);

      if (coincideEstado && coincideTexto) {
        coinciden.push(item);
      }
    }

    this.total = coinciden.length;

    // Recorta solo los juegos de la página pedida
    const inicio = this.pagina * this.tamanoPagina;
    const fin = Math.min(inicio + this.tamanoPagina, coinciden.length);
    const estaPagina: JuegoConMeta[] = [];

    for (let i = inicio; i < fin; i++) {
      estaPagina.push(coinciden[i]);
    }

    this.juegos = this.juegos.concat(estaPagina);
    this.cargando = false;
  }

  // ==================================================================
  // ===== FIN DEL HARDCODE =====
  // ==================================================================

  // ==================================================================
  // ===== BACK: descomentar todo este bloque al conectar =====
  // (en WebStorm: seleccionarlo y Ctrl + /)
  // ==================================================================

  // // ArchiveludoApi habla con el back. ChangeDetectorRef ya está
  // // importado arriba, porque la pantalla lo necesita siempre.
  // constructor(
  //   private route: ActivatedRoute,
  //   private api: ArchiveludoApi,
  //   private cambios: ChangeDetectorRef
  // ) { }
  //
  // // Pide una página: GET /api/videojuegos?pagina=0&limite=8&estado=...&q=...
  // private cargarPagina(): void {
  //   this.api.buscarVideojuegos(
  //     this.estadoSeleccionado,
  //     this.texto,
  //     this.pagina,
  //     this.tamanoPagina
  //   ).subscribe({
  //     next: (respuesta) => this.alRecibirPagina(respuesta),
  //     error: (error) => this.mostrarError(error)
  //   });
  // }
  //
  // // Suma los juegos recibidos a los que ya estaban en pantalla.
  // private alRecibirPagina(respuesta: JuegosPorEstado): void {
  //   this.total = respuesta.total;
  //   this.juegos = this.juegos.concat(respuesta.juegos);
  //   this.cargando = false;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Por ahora el error solo se escribe en la consola del navegador (F12).
  // private mostrarError(error: unknown): void {
  //   console.error('No se pudo cargar el catálogo', error);
  //   this.cargando = false;
  //
  //   this.cambios.markForCheck();
  // }

  // ==================================================================
  // ===== FIN DEL BLOQUE BACK =====
  // ==================================================================

  /*
   * ngOnInit se ejecuta una vez, cuando el componente ya está listo.
   * queryParamMap avisa cada vez que cambian los parámetros de la URL
   * (lo que va después del "?"), así que la pantalla se actualiza sola
   * cuando el usuario pulsa un filtro o busca algo desde la barra superior.
   */
  ngOnInit(): void {
    this.route.queryParamMap.subscribe({
      next: (parametros) => this.alCambiarFiltros(parametros),
    });
  }

  /* Lee el estado y el texto de la URL, y empieza de cero desde la primera página. */
  private alCambiarFiltros(parametros: ParamMap): void {
    this.estadoSeleccionado = this.leerEstado(parametros.get('estado'));
    this.texto = (parametros.get('q') || '').trim();

    this.pagina = 0;
    this.juegos = [];
    this.total = 0;
    this.cargando = true;

    this.cargarPagina();

    /*
     * Este cambio lo provoca la URL (por ejemplo, una búsqueda hecha desde la
     * barra superior), no un clic dentro de esta pantalla. Sin este aviso,
     * Angular no sabe que hay que redibujar y la pantalla se queda como estaba.
     */
    this.cambios.markForCheck();
  }

  /*
   * Solo acepta estados que existen en la lista de filtros. Si la URL trae
   * uno inventado (?estado=xyz), se ignora y se muestran todos.
   */
  private leerEstado(valor: string | null): string {
    if (!valor) {
      return '';
    }

    for (const filtro of this.filtros) {
      if (filtro.valor === valor) {
        return valor;
      }
    }

    return '';
  }

  /* Botón "Ver más": pide la página siguiente y la agrega al final. */
  verMas(): void {
    this.pagina++;
    this.cargando = true;

    this.cargarPagina();

    this.cambios.markForCheck();
  }

  /* Texto debajo del título: "14 videojuegos", "1 videojuego" o vacío mientras carga la primera página. */
  get textoResumen(): string {
    if (this.cargando && this.juegos.length === 0) {
      return '';
    }

    if (this.total === 1) {
      return '1 videojuego';
    }

    return this.total + ' videojuegos';
  }
}
