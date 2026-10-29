import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { JuegoConMeta } from '../../../shared/models/api-model';

// ===== BACK: descomenta este import cuando conectes el back =====
// import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

/*
 * Pantalla "En riesgo": todos los videojuegos cuyo estado de conservación
 * es "en_riesgo", los que necesitan atención primero.
 *
 * Funciona como el Catálogo, pero sin filtros ni búsqueda: el estado
 * siempre es 'en_riesgo'. Los juegos se muestran en páginas y el botón
 * "Ver más" agrega la siguiente.
 */
@Component({
  selector: 'app-en-riesgo',
  templateUrl: './en-riesgo-component.html',
  styleUrl: './en-riesgo-component.css',
  standalone: false,
})
export class EnRiesgoComponent implements OnInit {

  /* Juegos que se están mostrando (se acumulan al pulsar "Ver más"). */
  juegos: JuegoConMeta[] = [];

  /* Cuántos juegos en riesgo hay en total, no solo los que se ven. */
  total: number = 0;

  /* Página pedida por última vez. Empieza en 0. */
  pagina: number = 0;

  /*
   * Juegos por página. Está en 2 para poder probar "Ver más" con los 3
   * juegos de prueba. Con datos reales súbelo a 20 o 24.
   */
  tamanoPagina: number = 2;

  /* true mientras se espera una respuesta. */
  cargando: boolean = true;

  /* true si el back respondió con error. */
  hayError: boolean = false;

  // ==================================================================
  // ===== HARDCODE: borra desde aquí hasta "FIN HARDCODE"
  //  cuando conectes el back (incluye este constructor y cargarPagina)
  // ==================================================================

  /* Los 3 juegos en riesgo de prueba (solo los ids 1 y 8 tienen ficha). */
  private todosPrueba: JuegoConMeta[] = [
    {
      videojuego: { idVideojuego: 1, titulo: 'Torre de Ámbar Online', plataformaOriginal: 'PC', anioLanzamiento: 2007, estadoConservacion: 'en_riesgo' },
      meta: '2 copias · Huérfano'
    },
    {
      videojuego: { idVideojuego: 2, titulo: 'Sombras de Vellum', plataformaOriginal: 'PlayStation', anioLanzamiento: 1999, estadoConservacion: 'en_riesgo' },
      meta: '1 copia · Servidores apagados'
    },
    {
      videojuego: { idVideojuego: 3, titulo: 'Códice Nómada', plataformaOriginal: 'Web', anioLanzamiento: 2011, estadoConservacion: 'en_riesgo' },
      meta: '1 copia · Sin custodia vigente'
    }
  ];

  /* ChangeDetectorRef avisa a Angular que hay que redibujar la pantalla. */
  constructor(private cambios: ChangeDetectorRef) { }

  /* Entrega la página actual de los datos de prueba. */
  private cargarPagina(): void {
    const inicio = this.pagina * this.tamanoPagina;
    const trozo = this.todosPrueba.slice(inicio, inicio + this.tamanoPagina);

    this.juegos = this.juegos.concat(trozo);
    this.total = this.todosPrueba.length;
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
  // private cargarPagina(): void {
  //   this.cargando = true;
  //   this.hayError = false;
  //
  //   this.api.buscarVideojuegos('en_riesgo', '', this.pagina, this.tamanoPagina).subscribe({
  //     next: (respuesta) => {
  //       this.juegos = this.juegos.concat(respuesta.juegos);
  //       this.total = respuesta.total;
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
