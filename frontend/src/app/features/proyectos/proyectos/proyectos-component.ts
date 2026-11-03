import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Experto } from '../../../shared/models/experto-model';
import { ProyectoDeRestauracion } from '../../../shared/models/proyecto-model';
import { Videojuego } from '../../../shared/models/videojuego-model';

import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

/* Un botón de filtro: lo que dice y el estado que representa ('' = todos). */
interface FiltroProyecto {
  etiqueta: string;
  valor: string;
}

/*
 * Pantalla "Proyectos": todos los proyectos de restauración de todos los
 * videojuegos. Cada tarjeta muestra el nombre del juego (con enlace a su
 * ficha) y debajo la tarjeta del proyecto con sus expertos.
 *
 * GET /api/proyectos solo trae el idVideojuego de cada proyecto: los títulos
 * se piden aparte (uno por videojuego distinto) y los nombres de los expertos
 * salen de GET /api/expertos.
 *
 * El back entrega la lista completa y el filtro por estado se hace aquí,
 * en el front, porque son pocos proyectos.
 */
@Component({
  selector: 'app-proyectos',
  templateUrl: './proyectos-component.html',
  styleUrl: './proyectos-component.css',
  standalone: false,
})
export class ProyectosComponent implements OnInit {

  /* Botones de filtro, en el orden en que se muestran. */
  filtros: FiltroProyecto[] = [
    { etiqueta: 'Todos', valor: '' },
    { etiqueta: 'Activos', valor: 'activo' },
    { etiqueta: 'Pausados', valor: 'pausado' },
    { etiqueta: 'Finalizados', valor: 'finalizado' },
  ];

  /* Filtro elegido. Vacío significa "todos". */
  filtroActivo: string = '';

  /* Todos los proyectos que llegaron (sin filtrar). */
  proyectos: ProyectoDeRestauracion[] = [];

  /* Videojuegos de esos proyectos, por id. Mientras llegan, se muestra "Videojuego #id". */
  juegos: Record<number, Videojuego> = {};

  /* Catálogo de expertos, para los nombres de cada tarjeta. */
  expertos: Experto[] = [];

  /* true mientras se espera la respuesta. */
  cargando: boolean = true;

  /* true si el back respondió con error. */
  hayError: boolean = false;

  /* Texto del error (cambia si el servidor aún no ofrece los proyectos). */
  mensajeError: string = 'No pudimos cargar los proyectos. Intenta de nuevo más tarde.';

  /* ArchiveludoApiService habla con el back; ChangeDetectorRef avisa a Angular que hay que redibujar. */
  constructor(
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) { }

  /* GET /api/proyectos y GET /api/expertos. */
  private cargarProyectos(): void {
    this.cargando = true;
    this.hayError = false;

    this.api.obtenerTodosLosProyectos().subscribe({
      next: (respuesta) => this.alRecibirProyectos(respuesta),
      error: (error) => this.alFallar(error)
    });

    // Si falla, las tarjetas muestran "Experto #id" en lugar del nombre.
    this.api.obtenerExpertos().subscribe({
      next: (expertos) => this.alRecibirExpertos(expertos),
      error: (error) => console.error('No se pudieron cargar los expertos', error)
    });
  }

  private alRecibirProyectos(proyectos: ProyectoDeRestauracion[]): void {
    this.proyectos = proyectos;
    this.cargando = false;
    this.cambios.markForCheck();

    this.cargarJuegos(proyectos);
  }

  private alRecibirExpertos(expertos: Experto[]): void {
    this.expertos = expertos;
    this.cambios.markForCheck();
  }

  /*
   * Pide cada videojuego distinto una sola vez (GET /api/videojuegos/{id}).
   * Si un juego falla, solo ese título queda como "Videojuego #id".
   */
  private cargarJuegos(proyectos: ProyectoDeRestauracion[]): void {
    const pedidos: number[] = [];

    for (const proyecto of proyectos) {
      if (!pedidos.includes(proyecto.idVideojuego)) {
        pedidos.push(proyecto.idVideojuego);
      }
    }

    for (const idVideojuego of pedidos) {
      this.api.obtenerVideojuego(idVideojuego).subscribe({
        next: (juego) => this.alRecibirJuego(juego),
        error: (error) => console.error('No se pudo cargar el videojuego ' + idVideojuego, error)
      });
    }
  }

  private alRecibirJuego(juego: Videojuego): void {
    this.juegos[juego.idVideojuego] = juego;
    this.cambios.markForCheck();
  }

  /* Título del videojuego de un proyecto. */
  tituloJuego(proyecto: ProyectoDeRestauracion): string {
    const juego = this.juegos[proyecto.idVideojuego];

    if (juego) {
      return juego.titulo;
    }
    return 'Videojuego #' + proyecto.idVideojuego;
  }

  /* Plataforma y año, o vacío si el juego aún no llegó. */
  detalleJuego(proyecto: ProyectoDeRestauracion): string {
    const juego = this.juegos[proyecto.idVideojuego];

    if (juego) {
      return juego.plataformaOriginal + ' · ' + juego.anioLanzamiento;
    }
    return '';
  }

  /* 404: el servidor todavía no tiene este servicio. Otro código: fallo general. */
  private alFallar(error: { status?: number }): void {
    console.error('No se pudieron cargar los proyectos', error);

    if (error.status === 404) {
      this.mensajeError = 'Los proyectos de restauración aún no están disponibles en el servidor.';
    }

    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  /* Al abrir la pantalla se piden los proyectos. */
  ngOnInit(): void {
    this.cargarProyectos();
  }

  /* Cambia el filtro al pulsar un botón. */
  seleccionarFiltro(valor: string): void {
    this.filtroActivo = valor;
  }

  /* Los proyectos que se dibujan: todos, o solo los del estado elegido. */
  get proyectosVisibles(): ProyectoDeRestauracion[] {
    if (this.filtroActivo === '') {
      return this.proyectos;
    }

    const resultado: ProyectoDeRestauracion[] = [];
    for (const item of this.proyectos) {
      if (item.estado === this.filtroActivo) {
        resultado.push(item);
      }
    }
    return resultado;
  }

  /* Cuántos proyectos hay de un estado ('' cuenta todos). Va en los botones de filtro. */
  contar(valor: string): number {
    if (valor === '') {
      return this.proyectos.length;
    }

    let cantidad = 0;
    for (const item of this.proyectos) {
      if (item.estado === valor) {
        cantidad = cantidad + 1;
      }
    }
    return cantidad;
  }
}
