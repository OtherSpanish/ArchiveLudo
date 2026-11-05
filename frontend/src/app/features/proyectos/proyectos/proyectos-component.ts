import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Experto } from '../../../shared/models/experto-model';
import { ListaProyectos } from '../../../shared/models/api-model';
import { ProyectoDeRestauracion } from '../../../shared/models/proyecto-model';

import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

/** Un botón de filtro: lo que dice y el estado que representa ('' = todos). */
interface FiltroProyecto {
  etiqueta: string;
  valor: string;
}

/**
 * Pantalla "Proyectos": todos los proyectos de restauración de todos los
 * videojuegos. Cada tarjeta muestra el nombre del juego (con enlace a su
 * ficha) y debajo la tarjeta del proyecto con sus expertos.
 *
 * GET /api/proyectos?estado=... ya trae el título y la plataforma del juego
 * de cada proyecto, y el back hace el filtro por estado. La cantidad sale de
 * la cabecera X-Total-Count. Solo los nombres de los expertos se cruzan con
 * GET /api/expertos.
 */
@Component({
  selector: 'app-proyectos',
  templateUrl: './proyectos-component.html',
  styleUrl: './proyectos-component.css',
  standalone: false,
})
export class ProyectosComponent implements OnInit {

  /** Botones de filtro, en el orden en que se muestran. */
  filtros: FiltroProyecto[] = [
    { etiqueta: 'Todos', valor: '' },
    { etiqueta: 'Activos', valor: 'activo' },
    { etiqueta: 'Pausados', valor: 'pausado' },
    { etiqueta: 'Finalizados', valor: 'finalizado' },
  ];

  /** Filtro elegido. Vacío significa "todos". */
  filtroActivo: string = '';

  /** Los proyectos que llegaron del back para el filtro elegido. */
  proyectos: ProyectoDeRestauracion[] = [];

  /** Cantidad de proyectos del filtro elegido (cabecera X-Total-Count). */
  total: number = 0;

  /** Catálogo de expertos, para los nombres de cada tarjeta. */
  expertos: Experto[] = [];

  /** true mientras se espera la respuesta. */
  cargando: boolean = true;

  /** true si el back respondió con error. */
  hayError: boolean = false;

  /** Texto del error (cambia si el servidor aún no ofrece los proyectos). */
  mensajeError: string = 'No pudimos cargar los proyectos. Intenta de nuevo más tarde.';

  /** ArchiveludoApiService habla con el back; ChangeDetectorRef avisa a Angular que hay que redibujar. */
  constructor(
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) { }

  /** Al abrir la pantalla se piden los proyectos y los expertos. */
  ngOnInit(): void {
    this.cargarProyectos();

    // Si falla, las tarjetas muestran "Experto #id" en lugar del nombre.
    this.api.obtenerExpertos().subscribe({
      next: (expertos) => this.alRecibirExpertos(expertos),
      error: (error) => console.error('No se pudieron cargar los expertos', error)
    });
  }

  /** GET /api/proyectos?estado=... con el filtro elegido. */
  private cargarProyectos(): void {
    this.cargando = true;
    this.hayError = false;

    this.api.obtenerTodosLosProyectos(this.filtroActivo).subscribe({
      next: (lista) => this.alRecibirProyectos(lista),
      error: (error) => this.alFallar(error)
    });
  }

  private alRecibirProyectos(lista: ListaProyectos): void {
    this.proyectos = lista.proyectos;
    this.total = lista.total;
    this.cargando = false;
    this.cambios.markForCheck();
  }

  private alRecibirExpertos(expertos: Experto[]): void {
    this.expertos = expertos;
    this.cambios.markForCheck();
  }

  /** Mensaje del back si lo trae ({ estado, mensaje }); 404: el servidor aún no tiene el servicio. */
  private alFallar(error: { status?: number; error?: { mensaje?: string } }): void {
    console.error('No se pudieron cargar los proyectos', error);

    if (error.status === 404) {
      this.mensajeError = 'Los proyectos de restauración aún no están disponibles en el servidor.';
    } else if (error.error && error.error.mensaje) {
      this.mensajeError = error.error.mensaje;
    }

    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  /** Cambia el filtro al pulsar un botón y vuelve a pedir los proyectos al back. */
  seleccionarFiltro(valor: string): void {
    if (valor === this.filtroActivo) {
      return;
    }

    this.filtroActivo = valor;
    this.cargarProyectos();
  }
}
