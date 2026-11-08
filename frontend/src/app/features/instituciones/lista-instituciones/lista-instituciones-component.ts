import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { Institucion } from '../../../shared/models/custodia-model';

/**
 * Pantalla de la lista de instituciones (/instituciones).
 * Muestra una tarjeta por institución, con su tipo, país y contacto.
 * (El número de copias en custodia llegará cuando el back tenga custodias.)
 */
@Component({
  selector: 'app-lista-instituciones',
  templateUrl: './lista-instituciones-component.html',
  styleUrl: './lista-instituciones-component.css',
  standalone: false
})
export class ListaInstitucionesComponent implements OnInit {

  /** Instituciones que se muestran. Empieza vacío y se llena al cargar. */
  instituciones: Institucion[] = [];

  /**
   * true mientras se espera la respuesta. Evita mostrar "No hay instituciones"
   * durante el instante en que todavía no llegan los datos.
   */
  cargando: boolean = true;

  /** true si el back respondió con error. */
  hayError: boolean = false;

  /**
   * ArchiveludoApiService habla con el back.
   * ChangeDetectorRef avisa a Angular que hay que redibujar la pantalla.
   */
  constructor(
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) { }

  /** ngOnInit se ejecuta una vez, cuando el componente ya está listo. */
  ngOnInit(): void {
    this.cargarInstituciones();
  }

  /** Pide la lista: GET /api/instituciones */
  private cargarInstituciones(): void {
    this.api.obtenerInstituciones().subscribe({
      next: (lista) => this.alRecibirInstituciones(lista),
      error: (error) => this.mostrarError(error)
    });
  }

  /** Guarda la lista, ordenada alfabéticamente por nombre (localeCompare respeta las tildes). */
  private alRecibirInstituciones(lista: Institucion[]): void {
    const ordenadas = lista.slice();
    ordenadas.sort((a, b) => a.nombre.localeCompare(b.nombre));

    this.instituciones = ordenadas;
    this.cargando = false;

    this.cambios.markForCheck();
  }

  /** El detalle del error queda en la consola (F12). */
  private mostrarError(error: unknown): void {
    console.error('No se pudo cargar la lista de instituciones', error);
    this.cargando = false;
    this.hayError = true;

    this.cambios.markForCheck();
  }

  /** Texto bajo el título, en singular o plural: "1 institución" / "4 instituciones" */
  get textoTotal(): string {
    if (this.instituciones.length === 1) {
      return '1 institución';
    }
    return this.instituciones.length + ' instituciones';
  }
}
