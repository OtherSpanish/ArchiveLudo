import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { ArchiveludoApiService } from '../../core/archiveludo-api-service';
import { PaginaVideojuegos } from '../../shared/models/api-model';
import { Videojuego } from '../../shared/models/videojuego-model';

/*
 * Un botón de filtro de la fila de arriba.
 *  - etiqueta: el texto que ve el usuario
 *  - valor: el estado EXACTO que manda el back (con espacios) y que va en la URL
 *    ('' significa "todos", sin filtrar)
 */
interface FiltroEstado {
  etiqueta: string;
  valor: string;
}

/*
 * Pantalla del Catálogo (/catalogo).
 * Lee de la URL el filtro de estado (?estado=en riesgo) y el texto de
 * búsqueda (?q=torre), y pide al back los juegos que coinciden:
 *   GET /api/videojuegos?estado=...&q=...&pagina=0&limite=8
 * El back responde un arreglo y el total en la cabecera X-Total-Count
 * (el servicio ya los junta en { total, juegos }).
 * Si hay más juegos de los que caben en una página, el botón "Ver más"
 * carga la siguiente y la agrega al final.
 */
@Component({
  selector: 'app-catalogo',
  templateUrl: './catalogo-component.html',
  styleUrl: './catalogo-component.css',
  standalone: false,
})
export class CatalogoComponent implements OnInit {
  /* Botones de filtro, en el orden en que se muestran. */
  filtros: FiltroEstado[] = [
    { etiqueta: 'Todos', valor: '' },
    { etiqueta: 'Activos', valor: 'activo' },
    { etiqueta: 'En riesgo', valor: 'en riesgo' },
    { etiqueta: 'Parcialmente perdidos', valor: 'parcialmente perdido' },
    { etiqueta: 'Totalmente perdidos', valor: 'totalmente perdido' },
    { etiqueta: 'Restaurados', valor: 'restaurado' },
  ];

  /* Estado elegido en la URL. Vacío significa "todos". */
  estadoSeleccionado: string = '';

  /* Texto de búsqueda de la URL. Vacío significa "sin búsqueda". */
  texto: string = '';

  /* Juegos que se están mostrando (se van acumulando al pulsar "Ver más"). */
  juegos: Videojuego[] = [];

  /* Cuántos juegos coinciden en total, no solo los que se muestran. */
  total: number = 0;

  /* Página que se pidió por última vez. Empieza en 0. */
  pagina: number = 0;

  /* Cuántos juegos trae cada página. Con datos reales puedes subirlo a 20 o 24. */
  tamanoPagina: number = 8;

  /* true mientras se espera una respuesta. Evita mostrar "sin resultados" antes de tiempo. */
  cargando: boolean = true;

  /* true si el back respondió con error (o no se pudo conectar). */
  hayError: boolean = false;

  /*
   * ActivatedRoute lee datos de la URL actual.
   * ArchiveludoApiService habla con el back.
   * ChangeDetectorRef avisa a Angular que hay que redibujar la pantalla.
   */
  constructor(
    private route: ActivatedRoute,
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef,
  ) {}

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
    this.hayError = false;

    this.cargarPagina();

    /* El cambio lo provocó la URL, no un clic aquí: hay que avisar para que se redibuje. */
    this.cambios.markForCheck();
  }

  /* Pide una página al back: GET /api/videojuegos?pagina=..&limite=..&estado=..&q=.. */
  private cargarPagina(): void {
    this.api
      .buscarVideojuegos(this.estadoSeleccionado, this.texto, this.pagina, this.tamanoPagina)
      .subscribe({
        next: (respuesta) => this.alRecibirPagina(respuesta),
        error: (error) => this.mostrarError(error),
      });
  }

  /* Suma los juegos recibidos a los que ya estaban en pantalla. */
  private alRecibirPagina(respuesta: PaginaVideojuegos): void {
    this.total = respuesta.total;
    this.juegos = this.juegos.concat(respuesta.juegos);
    this.cargando = false;

    this.cambios.markForCheck();
  }

  /* Muestra el aviso de error en pantalla y deja el detalle en la consola (F12). */
  private mostrarError(error: unknown): void {
    console.error('No se pudo cargar el catálogo', error);
    this.cargando = false;
    this.hayError = true;

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
    if (this.hayError || (this.cargando && this.juegos.length === 0)) {
      return '';
    }

    if (this.total === 1) {
      return '1 videojuego';
    }

    return this.total + ' videojuegos';
  }
}
