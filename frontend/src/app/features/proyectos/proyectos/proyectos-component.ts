import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ProyectoConJuego } from '../../../shared/models/proyecto-con-juego-model';

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
    { etiqueta: 'Finalizados', valor: 'finalizado' },
  ];

  /* Filtro elegido. Vacío significa "todos". */
  filtroActivo: string = '';

  /* Todos los proyectos que llegaron (sin filtrar). */
  proyectos: ProyectoConJuego[] = [];

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

  /* GET /api/proyectos: todos los proyectos con su videojuego y expertos. */
  private cargarProyectos(): void {
    this.cargando = true;
    this.hayError = false;

    this.api.obtenerTodosLosProyectos().subscribe({
      next: (respuesta) => {
        this.proyectos = respuesta;
        this.cargando = false;
        this.cambios.markForCheck();
      },
      error: (error) => this.alFallar(error)
    });
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
  get proyectosVisibles(): ProyectoConJuego[] {
    if (this.filtroActivo === '') {
      return this.proyectos;
    }

    const resultado: ProyectoConJuego[] = [];
    for (const item of this.proyectos) {
      if (item.detalle.proyecto.estado === this.filtroActivo) {
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
      if (item.detalle.proyecto.estado === valor) {
        cantidad = cantidad + 1;
      }
    }
    return cantidad;
  }
}
