import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Institucion } from '../../../shared/models/custodia-model';
import { CopiaCustodiada } from '../../../shared/models/api-model';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

/*
 * Pantalla de la ficha de una institución (/instituciones/:id).
 * Muestra sus datos y sus teléfonos y correos. Las copias que custodia
 * llegarán cuando el back tenga custodias.
 */
@Component({
  selector: 'app-ficha-institucion',
  templateUrl: './ficha-institucion-component.html',
  styleUrl: './ficha-institucion-component.css',
  standalone: false,
})
export class FichaInstitucionComponent implements OnInit {
  /*
   * La institución que se está mostrando. Empieza sin valor (undefined) y se
   * llena cuando se carga. Si el id de la URL no existe, se queda así y el
   * HTML muestra un mensaje de "no encontrada".
   */
  institucion?: Institucion;

  telefonos: string[] = [];

  correos: string[] = [];

  /* Copias que esta institución custodia ahora mismo. */
  copias: CopiaCustodiada[] = [];

  /*
   * true mientras se espera la respuesta. Evita mostrar "No encontramos esa
   * institución" durante el instante en que todavía no llegan los datos.
   */
  cargando: boolean = true;

  /*
   * ActivatedRoute lee datos de la URL actual.
   * ArchiveludoApiService habla con el back.
   * ChangeDetectorRef avisa a Angular que hay que redibujar la pantalla.
   */
  constructor(
    private route: ActivatedRoute,
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) { }

  /* Pide la institución: GET /api/instituciones/{id} */
  private cargarInstitucion(id: number): void {
    this.limpiar();

    this.api.obtenerInstitucion(id).subscribe({
      next: (institucion) => this.alRecibirInstitucion(institucion),
      error: (error) => this.mostrarError(error)
    });
  }

  /*
   * Guarda la institución. Los teléfonos llegan como números y aquí se
   * pasan a texto para mostrarlos. Las copias quedan vacías por ahora.
   */
  private alRecibirInstitucion(institucion: Institucion): void {
    this.institucion = institucion;
    this.telefonos = (institucion.telefonos || []).map((numero) => String(numero));
    this.correos = institucion.correos || [];
    this.cargando = false;

    this.cambios.markForCheck();
  }

  /*
   * Si el back responde error (por ejemplo 404 porque el id no existe), la
   * institución se queda sin valor y el HTML muestra "No encontramos esa institución".
   */
  private mostrarError(error: unknown): void {
    console.error('No se pudo cargar la ficha de la institución', error);
    this.cargando = false;

    this.cambios.markForCheck();
  }


  /*
   * ngOnInit se ejecuta una vez, cuando el componente ya está listo.
   * paramMap avisa cada vez que cambian los parámetros de la URL, así que la
   * ficha se actualiza aunque el usuario pase de /instituciones/1 a /instituciones/2
   * sin salir de la pantalla.
   */
  ngOnInit(): void {
    this.route.paramMap.subscribe({
      next: (parametros) => this.alCambiarParametros(parametros),
    });
  }

  /* Lee el :id de la URL y pide esa institución. */
  private alCambiarParametros(parametros: ParamMap): void {
    // En /instituciones/2 el parámetro "id" vale '2', pero como TEXTO.
    // Number() lo convierte a número.
    const id = Number(parametros.get('id'));

    this.cargarInstitucion(id);
  }

  /* Borra la institución anterior, para no mostrar datos mezclados al cambiar de id. */
  private limpiar(): void {
    this.institucion = undefined;
    this.telefonos = [];
    this.correos = [];
    this.copias = [];
    this.cargando = true;
  }

  /* Línea de tipo y país: "Museo · Colombia" */
  get tipoYPais(): string {
    if (!this.institucion) {
      return '';
    }
    return this.institucion.tipo + ' · ' + this.institucion.pais;
  }

  /* "Custodia 4 copias" a partir de cantidadCopiasCustodiadas (la calcula el back). */
  get textoCopias(): string {
    if (!this.institucion) {
      return '';
    }

    const total = this.institucion.cantidadCopiasCustodiadas;

    if (total === 0) {
      return 'No custodia copias por ahora';
    }
    if (total === 1) {
      return 'Custodia 1 copia';
    }
    return 'Custodia ' + total + ' copias';
  }

  /* Código de la copia en el formato de copia-card: la copia 1 es "C-001". */
  codigo(idCopia: number): string {
    return 'C-' + String(idCopia).padStart(3, '0');
  }

  /* Texto de la etiqueta de tipo: "FÍSICA" o "DIGITAL" */
  textoTipo(tipoCopia: string): string {
    if (tipoCopia === 'fisica') {
      return 'FÍSICA';
    }
    return 'DIGITAL';
  }

  /* "Plataforma · año" de un videojuego: "Arcade · 1994" */
  plataformaYAnio(copia: CopiaCustodiada): string {
    return copia.videojuego.plataformaOriginal + ' · ' + copia.videojuego.anioLanzamiento;
  }

  /*
   * Convierte '2019-08-05' en '05/08/2019'.
   * split('-') parte el texto en tres pedazos: año, mes y día.
   */
  formatearFecha(fecha: string): string {
    const partes = fecha.split('-');
    if (partes.length !== 3) {
      return fecha;
    }
    return partes[2] + '/' + partes[1] + '/' + partes[0];
  }
}
