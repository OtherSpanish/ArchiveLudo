import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { InstitucionConCopias } from '../../../shared/models/api-model';

// ===== BACK: descomentar esta línea al conectar =====
// import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

/*
 * Pantalla de la lista de instituciones (/instituciones).
 * Muestra una tarjeta por institución con su número de copias en custodia.
 */
@Component({
  selector: 'app-lista-instituciones',
  templateUrl: './lista-instituciones-component.html',
  styleUrl: './lista-instituciones-component.css',
  standalone: false
})
export class ListaInstitucionesComponent implements OnInit {

  /* Instituciones que se muestran. Empieza vacío y se llena al cargar. */
  instituciones: InstitucionConCopias[] = [];

  /*
   * true mientras se espera la respuesta. Evita mostrar "No hay instituciones"
   * durante el instante en que todavía no llegan los datos.
   */
  cargando: boolean = true;

  // ==================================================================
  // ===== HARDCODE: borrar este bloque al conectar el back =====
  // (incluye datosPrueba, el constructor y la función cargarInstituciones)
  // ==================================================================

  /*
   * DATOS DE PRUEBA. Los ids 1 y 2 coinciden con las instituciones de la
   * ficha de videojuego. Solo esos dos tienen ficha de prueba; con los
   * ids 3 y 4, la ficha muestra "No encontramos esa institución".
   */
  private datosPrueba: InstitucionConCopias[] = [
    {
      institucion: {
        idInstitucion: 3,
        nombre: 'Archivo Digital del Sur',
        tipo: 'Archivo',
        pais: 'Chile',
        contacto: 'contacto@archivodelsur.example'
      },
      numeroCopias: 0
    },
    {
      institucion: {
        idInstitucion: 4,
        nombre: 'Fundación Retroteca',
        tipo: 'Fundación',
        pais: 'México',
        contacto: 'hola@retroteca.example'
      },
      numeroCopias: 1
    },
    {
      institucion: {
        idInstitucion: 1,
        nombre: 'Museo de Cultura Invernadero',
        tipo: 'Museo',
        pais: 'Colombia',
        contacto: 'archivo@museoinvernadero.example'
      },
      numeroCopias: 2
    },
    {
      institucion: {
        idInstitucion: 2,
        nombre: 'Universidad del Altiplano',
        tipo: 'Universidad',
        pais: 'Colombia',
        contacto: 'preservacion@altiplano.example'
      },
      numeroCopias: 2
    }
  ];

  /*
   * ChangeDetectorRef sirve para avisarle a Angular que esta pantalla cambió
   * y hay que redibujarla. Angular lo entrega solo (inyección de dependencias).
   */
  constructor(private cambios: ChangeDetectorRef) { }

  /*
   * Simula lo que haría el back: entrega las instituciones en orden
   * alfabético por nombre. localeCompare compara texto respetando tildes.
   */
  private cargarInstituciones(): void {
    const ordenadas = this.datosPrueba.slice();

    ordenadas.sort((a, b) => a.institucion.nombre.localeCompare(b.institucion.nombre));

    this.instituciones = ordenadas;
    this.cargando = false;
    this.cambios.markForCheck();
  }

  // ==================================================================
  // ===== FIN DEL HARDCODE =====
  // ==================================================================


  // ==================================================================
  // ===== BACK: descomentar todo este bloque al conectar =====
  // (en WebStorm: seleccionarlo y Ctrl + /)
  // ==================================================================

  // // ArchiveludoApiService habla con el back. ChangeDetectorRef ya está
  // // importado arriba, porque la pantalla lo necesita siempre.
  // constructor(
  //   private api: ArchiveludoApiService,
  //   private cambios: ChangeDetectorRef
  // ) { }
  //
  // // Pide la lista: GET /api/instituciones (el back ya la entrega ordenada)
  // private cargarInstituciones(): void {
  //   this.api.obtenerInstituciones().subscribe({
  //     next: (lista) => this.alRecibirInstituciones(lista),
  //     error: (error) => this.mostrarError(error)
  //   });
  // }
  //
  // private alRecibirInstituciones(lista: InstitucionConCopias[]): void {
  //   this.instituciones = lista;
  //   this.cargando = false;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Por ahora el error solo se escribe en la consola del navegador (F12).
  // private mostrarError(error: unknown): void {
  //   console.error('No se pudo cargar la lista de instituciones', error);
  //   this.cargando = false;
  //
  //   this.cambios.markForCheck();
  // }

  // ==================================================================
  // ===== FIN DEL BLOQUE BACK =====
  // ==================================================================


  /* ngOnInit se ejecuta una vez, cuando el componente ya está listo. */
  ngOnInit(): void {
    this.cargarInstituciones();
  }

  /* Texto bajo el título, en singular o plural: "1 institución" / "4 instituciones" */
  get textoTotal(): string {
    if (this.instituciones.length === 1) {
      return '1 institución';
    }
    return this.instituciones.length + ' instituciones';
  }
}
