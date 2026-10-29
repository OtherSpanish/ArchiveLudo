import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ProyectoConJuego } from '../../../shared/models/proyecto-con-juego-model';

// ===== BACK: descomenta este import cuando conectes el back =====
// import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

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

  // ==================================================================
  // ===== HARDCODE: borra desde aquí hasta "FIN HARDCODE"
  //  cuando conectes el back (incluye este constructor y cargarProyectos)
  // ==================================================================

  /* 4 proyectos de prueba: 3 activos y 1 finalizado. Solo los juegos 1 y 8 tienen ficha. */
  private proyectosPrueba: ProyectoConJuego[] = [
    {
      videojuego: { idVideojuego: 1, titulo: 'Torre de Ámbar Online', plataformaOriginal: 'PC', anioLanzamiento: 2007, estadoConservacion: 'en_riesgo' },
      detalle: {
        proyecto: { idProyecto: 1, estado: 'activo', fechaInicio: '2026-03-10', herramientaEmulador: 'Servidor privado + emulación de cliente' },
        expertos: [
          { idExperto: 1, nombre: 'Laura Mendoza', rolEnProyecto: 'Líder técnica', fechaIncorporacion: '2026-03-10' },
          { idExperto: 2, nombre: 'Andrés Quintero', rolEnProyecto: 'Ingeniero de redes', fechaIncorporacion: '2026-04-02' }
        ]
      }
    },
    {
      videojuego: { idVideojuego: 2, titulo: 'Sombras de Vellum', plataformaOriginal: 'PlayStation', anioLanzamiento: 1999, estadoConservacion: 'en_riesgo' },
      detalle: {
        proyecto: { idProyecto: 2, estado: 'activo', fechaInicio: '2026-06-15', herramientaEmulador: 'DuckStation' },
        expertos: [
          { idExperto: 3, nombre: 'Camila Rojas', rolEnProyecto: 'Restauradora de imagen de disco', fechaIncorporacion: '2026-06-15' }
        ]
      }
    },
    {
      videojuego: { idVideojuego: 8, titulo: 'Furia Quark', plataformaOriginal: 'Arcade', anioLanzamiento: 1994, estadoConservacion: 'restaurado' },
      detalle: {
        proyecto: { idProyecto: 3, estado: 'finalizado', fechaInicio: '2025-02-01', fechaFin: '2025-11-20', herramientaEmulador: 'MAME' },
        expertos: [
          { idExperto: 4, nombre: 'Julián Pardo', rolEnProyecto: 'Especialista en ROMs', fechaIncorporacion: '2025-02-01' },
          { idExperto: 3, nombre: 'Camila Rojas', rolEnProyecto: 'Control de calidad', fechaIncorporacion: '2025-05-12' }
        ]
      }
    },
    {
      videojuego: { idVideojuego: 3, titulo: 'Códice Nómada', plataformaOriginal: 'Web', anioLanzamiento: 2011, estadoConservacion: 'en_riesgo' },
      detalle: {
        proyecto: { idProyecto: 4, estado: 'activo', fechaInicio: '2026-09-01' },
        expertos: []
      }
    }
  ];

  /* ChangeDetectorRef avisa a Angular que hay que redibujar la pantalla. */
  constructor(private cambios: ChangeDetectorRef) { }

  /* Entrega los proyectos de prueba. */
  private cargarProyectos(): void {
    this.proyectos = this.proyectosPrueba;
    this.cargando = false;
    this.cambios.markForCheck();
  }

  // ==================================================================
  // ===== FIN HARDCODE
  // ==================================================================

  // ==================================================================
  // ===== BACK: cuando conectes el back, borra el bloque HARDCODE y
  //  descomenta esto (y el import de arriba):
  // ==================================================================
  //
  // constructor(
  //   private api: ArchiveludoApiService,
  //   private cambios: ChangeDetectorRef
  // ) { }
  //
  // private cargarProyectos(): void {
  //   this.cargando = true;
  //   this.hayError = false;
  //
  //   this.api.obtenerTodosLosProyectos().subscribe({
  //     next: (respuesta) => {
  //       this.proyectos = respuesta;
  //       this.cargando = false;
  //       this.cambios.markForCheck();
  //     },
  //     error: () => {
  //       this.cargando = false;
  //       this.hayError = true;
  //       this.cambios.markForCheck();
  //     }
  //   });
  // }

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
