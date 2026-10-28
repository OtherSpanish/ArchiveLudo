import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Institucion } from '../../../shared/models/custodia.model';
import { CopiaCustodiada, DatosInstitucion } from '../../../shared/models/api.model';

// ===== BACK: descomentar esta línea al conectar =====
// import { ArchiveludoApi } from '../../../core/archiveludo-api';

/*
 * Pantalla de la ficha de una institución (/instituciones/:id).
 * Muestra sus datos, sus teléfonos y correos, y las copias que custodia ahora.
 */
@Component({
  selector: 'app-ficha-institucion',
  templateUrl: './ficha-institucion.html',
  styleUrl: './ficha-institucion.css',
  standalone: false,
})
export class FichaInstitucion implements OnInit {
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

  // ==================================================================
  // ===== HARDCODE: borrar este bloque al conectar el back =====
  // (incluye datosPrueba, el constructor y la función cargarInstitucion)
  // ==================================================================

  /*
   * DATOS DE PRUEBA. Los ids y las copias coinciden con los de la ficha de
   * videojuego: 1 = Museo (2 copias de Furia Quark), 2 = Universidad
   * (1 copia de Furia Quark y 1 de Torre de Ámbar Online).
   * La llave es el id de la institución.
   */
  private datosPrueba: Record<number, DatosInstitucion> = {
    1: {
      institucion: {
        idInstitucion: 1,
        nombre: 'Museo de Cultura Invernadero',
        tipo: 'Museo',
        pais: 'Colombia',
        contacto: 'archivo@museoinvernadero.example',
      },
      telefonos: ['+57 601 555 0142'],
      correos: ['archivo@museoinvernadero.example', 'prensa@museoinvernadero.example'],
      copias: [
        {
          idCopia: 1,
          tipoCopia: 'fisica',
          estadoIntegridad: 'deteriorada',
          videojuego: {
            idVideojuego: 8,
            titulo: 'Furia Quark',
            plataformaOriginal: 'Arcade',
            anioLanzamiento: 1994,
            estadoConservacion: 'restaurado',
          },
          fechaInicio: '2019-08-05',
          condicionAlmacenamiento: 'Vitrina con humedad y temperatura controladas',
        },
        {
          idCopia: 3,
          tipoCopia: 'digital',
          estadoIntegridad: 'buena',
          videojuego: {
            idVideojuego: 8,
            titulo: 'Furia Quark',
            plataformaOriginal: 'Arcade',
            anioLanzamiento: 1994,
            estadoConservacion: 'restaurado',
          },
          fechaInicio: '2021-02-15',
          condicionAlmacenamiento: 'Disco de archivo con copia en la nube',
        },
      ],
    },
    2: {
      institucion: {
        idInstitucion: 2,
        nombre: 'Universidad del Altiplano',
        tipo: 'Universidad',
        pais: 'Colombia',
        contacto: 'preservacion@altiplano.example',
      },
      telefonos: ['+57 601 555 0188', '+57 601 555 0189'],
      correos: ['preservacion@altiplano.example'],
      copias: [
        {
          idCopia: 2,
          tipoCopia: 'digital',
          estadoIntegridad: 'buena',
          videojuego: {
            idVideojuego: 8,
            titulo: 'Furia Quark',
            plataformaOriginal: 'Arcade',
            anioLanzamiento: 1994,
            estadoConservacion: 'restaurado',
          },
          fechaInicio: '2020-08-06',
          condicionAlmacenamiento: 'Servidor de archivo con respaldo diario',
        },
        {
          idCopia: 4,
          tipoCopia: 'digital',
          estadoIntegridad: 'deteriorada',
          videojuego: {
            idVideojuego: 1,
            titulo: 'Torre de Ámbar Online',
            plataformaOriginal: 'PC',
            anioLanzamiento: 2007,
            estadoConservacion: 'en_riesgo',
          },
          fechaInicio: '2022-05-30',
          condicionAlmacenamiento: 'Servidor de archivo con respaldo semanal',
        },
      ],
    },
  };

  /*
   * ActivatedRoute es la herramienta de Angular para leer datos de la URL actual.
   * ChangeDetectorRef sirve para avisarle a Angular que esta pantalla cambió
   * y hay que redibujarla. Las dos se piden en el constructor y Angular
   * las entrega solas (inyección de dependencias).
   */
  constructor(
    private route: ActivatedRoute,
    private cambios: ChangeDetectorRef,
  ) {}

  /*
   * Busca la institución en los datos de prueba.
   * Si el id no existe, la institución se queda sin valor y el HTML
   * muestra "No encontramos esa institución".
   */
  private cargarInstitucion(id: number): void {
    this.limpiar();

    const datos = this.datosPrueba[id];
    if (datos) {
      this.recibirDatos(datos);
    }

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

  // // ArchiveludoApi habla con el back. ChangeDetectorRef ya está
  // // importado arriba, porque la pantalla lo necesita siempre.
  // constructor(
  //   private route: ActivatedRoute,
  //   private api: ArchiveludoApi,
  //   private cambios: ChangeDetectorRef
  // ) { }
  //
  // // Pide la institución: GET /api/instituciones/{id}
  // private cargarInstitucion(id: number): void {
  //   this.limpiar();
  //
  //   this.api.obtenerInstitucion(id).subscribe({
  //     next: (datos) => this.alRecibirInstitucion(datos),
  //     error: (error) => this.mostrarError(error)
  //   });
  // }
  //
  // private alRecibirInstitucion(datos: DatosInstitucion): void {
  //   this.recibirDatos(datos);
  //   this.cargando = false;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Si el back responde error (por ejemplo 404 porque el id no existe), la
  // // institución se queda sin valor y el HTML muestra "No encontramos esa institución".
  // private mostrarError(error: unknown): void {
  //   console.error('No se pudo cargar la ficha de la institución', error);
  //   this.cargando = false;
  //
  //   this.cambios.markForCheck();
  // }

  // ==================================================================
  // ===== FIN DEL BLOQUE BACK =====
  // ==================================================================

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

  /* Guarda en la pantalla los datos que llegaron (de la prueba o del back). */
  private recibirDatos(datos: DatosInstitucion): void {
    this.institucion = datos.institucion;
    this.telefonos = datos.telefonos;
    this.correos = datos.correos;
    this.copias = datos.copias;
  }

  /* Línea de tipo y país: "Museo · Colombia" */
  get tipoYPais(): string {
    if (!this.institucion) {
      return '';
    }
    return this.institucion.tipo + ' · ' + this.institucion.pais;
  }

  /* Texto de copias, en singular o plural: "Custodia 1 copia" / "Custodia 2 copias" */
  get textoCopias(): string {
    if (this.copias.length === 1) {
      return 'Custodia 1 copia';
    }
    return 'Custodia ' + this.copias.length + ' copias';
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
