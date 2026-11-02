import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { PaginaVideojuegos } from '../../../shared/models/api-model';
import { Videojuego } from '../../../shared/models/videojuego-model';

/*
 * Pantalla "En riesgo": todos los videojuegos cuyo estado de conservación
 * es "en riesgo", los que necesitan atención primero.
 *
 * Funciona como el Catálogo, pero sin filtros ni búsqueda: el estado
 * siempre es 'en riesgo'. Los juegos se muestran en páginas y el botón
 * "Ver más" agrega la siguiente.
 */
@Component({
  selector: 'app-en-riesgo',
  templateUrl: './en-riesgo-component.html',
  styleUrl: './en-riesgo-component.css',
  standalone: false,
})
export class EnRiesgoComponent implements OnInit {

  /* Estado que se pide al back, tal cual lo guarda (con espacio). */
  private readonly estado: string = 'en riesgo';

  /* Juegos que se están mostrando (se acumulan al pulsar "Ver más"). */
  juegos: Videojuego[] = [];

  /* Cuántos juegos en riesgo hay en total, no solo los que se ven. */
  total: number = 0;

  /* Página pedida por última vez. Empieza en 0. */
  pagina: number = 0;

  /* Juegos por página. Con datos reales puedes subirlo a 20 o 24. */
  tamanoPagina: number = 8;

  /* true mientras se espera una respuesta. */
  cargando: boolean = true;

  /* true si el back respondió con error. */
  hayError: boolean = false;

  constructor(
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) { }

  /* Pide una página: GET /api/videojuegos?estado=en riesgo&pagina=..&limite=.. */
  private cargarPagina(): void {
    this.cargando = true;
    this.hayError = false;

    this.api.buscarVideojuegos(this.estado, '', this.pagina, this.tamanoPagina).subscribe({
      next: (respuesta) => this.alRecibirPagina(respuesta),
      error: (error) => this.mostrarError(error)
    });
  }

  /* Suma los juegos recibidos a los que ya estaban en pantalla. */
  private alRecibirPagina(respuesta: PaginaVideojuegos): void {
    this.juegos = this.juegos.concat(respuesta.juegos);
    this.total = respuesta.total;
    this.cargando = false;
    this.cambios.markForCheck();
  }

  /* Muestra el aviso de error y deja el detalle en la consola (F12). */
  private mostrarError(error: unknown): void {
    console.error('No se pudieron cargar los juegos en riesgo', error);
    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  /* Al abrir la pantalla se pide la primera página. */
  ngOnInit(): void {
    this.cargarPagina();
  }

  /* "Ver más": pide la página siguiente y la agrega a las que ya se ven. */
  verMas(): void {
    this.pagina = this.pagina + 1;
    this.cargarPagina();
  }
}
