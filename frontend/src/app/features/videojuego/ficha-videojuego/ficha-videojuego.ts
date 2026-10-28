import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Videojuego } from '../../../shared/models/videojuego.model';
import { Custodia, Institucion } from '../../../shared/models/custodia.model';
import { CopiaConCustodia, DatosFicha } from '../../../shared/models/api.model';

// ===== BACK: descomentar estas 2 líneas al conectar =====
// import { ChangeDetectorRef } from '@angular/core';
// import { ArchiveludoApi } from '../../../core/archiveludo-api';

/*
 * Las cuatro pestañas de la ficha. Cada valor es el identificador interno
 * de la pestaña; el texto que ve el usuario se arma en el HTML.
 */
type Pestana = 'copias' | 'proyectos' | 'legal' | 'eventos';

/*
 * Una institución junto con cuántas copias de ESTE videojuego custodia.
 * Es un tipo solo del front: no viene del back, lo armamos a partir de
 * las custodias vigentes de las copias (ver agruparInstituciones).
 */
interface InstitucionConCopias {
  institucion: Institucion;
  numeroCopias: number;
}

/*
 * Pantalla de la ficha de un videojuego (/videojuegos/:id).
 * Incluye: cabecera, pestañas, pestaña Copias e historial de custodia.
 */
@Component({
  selector: 'app-ficha-videojuego',
  templateUrl: './ficha-videojuego.html',
  styleUrl: './ficha-videojuego.css',
  standalone: false,
})
export class FichaVideojuego implements OnInit {
  /*
   * El juego que se está mostrando. Empieza sin valor (undefined) y se llena
   * cuando se carga. El "?" indica que puede no tener valor: si el id de la URL
   * no existe, se queda así y el HTML muestra un mensaje de "no encontrado".
   */
  videojuego?: Videojuego;

  estatusLegal: string = '';

  numeroCopias: number = 0;

  numeroProyectos: number = 0;

  /* Copias del juego, cada una con su custodia vigente (pestaña Copias, columna izquierda). */
  copias: CopiaConCustodia[] = [];

  /* Instituciones que custodian copias de este juego (columna derecha). */
  instituciones: InstitucionConCopias[] = [];

  /*
   * Copia cuyo historial de custodia se muestra abajo. Por ahora siempre es
   * la primera de la lista. Vale 0 mientras el juego no tenga copias.
   */
  idCopiaHistorial: number = 0;

  /* Periodos de custodia de esa copia, para la línea de tiempo. */
  custodiasHistorial: Custodia[] = [];

  /* Pestaña que se está viendo. Empieza en "Copias", como en el boceto. */
  pestanaActiva: Pestana = 'copias';

  // ==================================================================
  // ===== HARDCODE: borrar este bloque al conectar el back =====
  // (incluye instituciones, copias y custodias de prueba, datosPrueba,
  //  el constructor y las funciones cargarJuego y cargarHistorial)
  // ==================================================================

  /* Instituciones de prueba. Se definen primero porque las copias y custodias las usan. */
  private museo: Institucion = {
    idInstitucion: 1,
    nombre: 'Museo de Cultura Invernadero',
    tipo: 'Museo',
    pais: 'Colombia',
    contacto: 'archivo@museoinvernadero.example',
  };

  private universidad: Institucion = {
    idInstitucion: 2,
    nombre: 'Universidad del Altiplano',
    tipo: 'Universidad',
    pais: 'Colombia',
    contacto: 'preservacion@altiplano.example',
  };

  /*
   * Copias de prueba. La llave es el id del videojuego:
   * 1 = Torre de Ámbar Online (1 copia), 8 = Furia Quark (3 copias).
   * Coinciden con numeroCopias de datosPrueba.
   */
  private copiasPrueba: Record<number, CopiaConCustodia[]> = {
    1: [
      {
        copia: {
          idCopia: 4,
          idVideojuego: 1,
          tipoCopia: 'digital',
          estadoIntegridad: 'deteriorada',
          fechaRegistro: '2022-05-30',
          formatoArchivo: 'ISO (imagen de disco)',
          tamanio: '640 MB',
          hashVerificacion: 'sha256:c7d1e9a0b8346f52',
        },
        custodiaActual: {
          idCustodia: 4,
          idCopia: 4,
          institucion: this.universidad,
          fechaInicio: '2022-05-30',
          condicionAlmacenamiento: 'Servidor de archivo con respaldo semanal',
        },
      },
    ],
    8: [
      {
        copia: {
          idCopia: 1,
          idVideojuego: 8,
          tipoCopia: 'fisica',
          estadoIntegridad: 'deteriorada',
          fechaRegistro: '2019-03-12',
          tipoSoporte: 'Placa de circuito arcade',
          numeroSerie: 'QX-94-0127',
          ubicacionFisica: 'Vitrina 3, sala de archivo',
        },
        custodiaActual: {
          idCustodia: 1,
          idCopia: 1,
          institucion: this.museo,
          fechaInicio: '2019-08-05',
          condicionAlmacenamiento: 'Vitrina con humedad y temperatura controladas',
        },
      },
      {
        copia: {
          idCopia: 2,
          idVideojuego: 8,
          tipoCopia: 'digital',
          estadoIntegridad: 'buena',
          fechaRegistro: '2020-08-06',
          formatoArchivo: 'ZIP (imagen de respaldo)',
          tamanio: '2 MB',
          hashVerificacion: 'sha256:9f2c4a7e1b03d58c',
        },
        custodiaActual: {
          idCustodia: 2,
          idCopia: 2,
          institucion: this.universidad,
          fechaInicio: '2020-08-06',
          condicionAlmacenamiento: 'Servidor de archivo con respaldo diario',
        },
      },
      {
        copia: {
          idCopia: 3,
          idVideojuego: 8,
          tipoCopia: 'digital',
          estadoIntegridad: 'buena',
          fechaRegistro: '2021-02-15',
          formatoArchivo: 'ZIP (ROM comprimida)',
          tamanio: '3 MB',
          hashVerificacion: 'sha256:41ab90de77c3f215',
        },
        custodiaActual: {
          idCustodia: 3,
          idCopia: 3,
          institucion: this.museo,
          fechaInicio: '2021-02-15',
          condicionAlmacenamiento: 'Disco de archivo con copia en la nube',
        },
      },
    ],
  };

  /*
   * Historial de custodia de prueba. La llave es el id de la COPIA.
   * La copia 1 tiene dos periodos (Universidad y luego Museo), como el boceto.
   * Las demás tienen uno solo, y es el vigente (no tiene fechaFin).
   */
  private custodiasPrueba: Record<number, Custodia[]> = {
    1: [
      {
        idCustodia: 5,
        idCopia: 1,
        institucion: this.universidad,
        fechaInicio: '2019-03-12',
        fechaFin: '2019-08-04',
        condicionAlmacenamiento: 'Armario metálico sin control de humedad',
      },
      {
        idCustodia: 1,
        idCopia: 1,
        institucion: this.museo,
        fechaInicio: '2019-08-05',
        condicionAlmacenamiento: 'Vitrina con humedad y temperatura controladas',
      },
    ],
    2: [
      {
        idCustodia: 2,
        idCopia: 2,
        institucion: this.universidad,
        fechaInicio: '2020-08-06',
        condicionAlmacenamiento: 'Servidor de archivo con respaldo diario',
      },
    ],
    3: [
      {
        idCustodia: 3,
        idCopia: 3,
        institucion: this.museo,
        fechaInicio: '2021-02-15',
        condicionAlmacenamiento: 'Disco de archivo con copia en la nube',
      },
    ],
    4: [
      {
        idCustodia: 4,
        idCopia: 4,
        institucion: this.universidad,
        fechaInicio: '2022-05-30',
        condicionAlmacenamiento: 'Servidor de archivo con respaldo semanal',
      },
    ],
  };

  /*
   * DATOS DE PRUEBA de la cabecera. Los ids coinciden con los de las
   * tarjetas de Inicio, así los enlaces funcionan.
   */
  private datosPrueba: DatosFicha[] = [
    {
      videojuego: {
        idVideojuego: 1,
        titulo: 'Torre de Ámbar Online',
        plataformaOriginal: 'PC',
        anioLanzamiento: 2007,
        estadoConservacion: 'en_riesgo',
        estudio: {
          idEstudio: 1,
          nombre: 'Foro Digital',
          pais: 'Colombia',
          anioFundacion: 1998,
          anioCierre: 2009,
          estadoActual: 'desaparecido',
        },
      },
      estatusLegal: 'Huérfano',
      numeroCopias: 1,
      numeroProyectos: 0,
    },
    {
      videojuego: {
        idVideojuego: 8,
        titulo: 'Furia Quark',
        plataformaOriginal: 'Arcade',
        anioLanzamiento: 1994,
        estadoConservacion: 'restaurado',
        estudio: {
          idEstudio: 2,
          nombre: 'Quark Interactive',
          pais: 'Japón',
          anioFundacion: 1989,
          anioCierre: 2001,
          estadoActual: 'desaparecido',
        },
      },
      estatusLegal: 'Dominio público',
      numeroCopias: 3,
      numeroProyectos: 1,
    },
  ];

  /*
   * ActivatedRoute es la herramienta de Angular para leer datos de la URL actual.
   * Se pide en el constructor y Angular la entrega sola (inyección de dependencias).
   */
  constructor(private route: ActivatedRoute) {}

  /*
   * Recorre los datos de prueba con un for normal.
   * Si encuentra un juego con ese id, guarda el juego, sus datos extra y sus copias.
   */
  private cargarJuego(id: number): void {
    this.limpiarJuego();

    for (const datos of this.datosPrueba) {
      if (datos.videojuego.idVideojuego === id) {
        this.videojuego = datos.videojuego;
        this.estatusLegal = datos.estatusLegal;
        this.numeroCopias = datos.numeroCopias;
        this.numeroProyectos = datos.numeroProyectos;

        const copiasDelJuego = this.copiasPrueba[id] || [];
        this.recibirCopias(copiasDelJuego);
      }
    }
  }

  /* Busca el historial de la copia en los datos de prueba. */
  private cargarHistorial(idCopia: number): void {
    this.custodiasHistorial = this.custodiasPrueba[idCopia] || [];
  }

  // ==================================================================
  // ===== FIN DEL HARDCODE =====
  // ==================================================================

  // ==================================================================
  // ===== BACK: descomentar todo este bloque al conectar =====
  // (en WebStorm: seleccionarlo y Ctrl + /)
  // ==================================================================

  // // ArchiveludoApi habla con el back. ChangeDetectorRef avisa a Angular
  // // que hay datos nuevos para redibujar la pantalla.
  // constructor(
  //   private route: ActivatedRoute,
  //   private api: ArchiveludoApi,
  //   private cambios: ChangeDetectorRef
  // ) { }
  //
  // // Pide dos cosas: la cabecera y las copias.
  // // El historial se pide después, cuando llegan las copias (ver cargarHistorial).
  // private cargarJuego(id: number): void {
  //   this.limpiarJuego();
  //
  //   // GET /api/videojuegos/{id}
  //   this.api.obtenerFicha(id).subscribe({
  //     next: (datos) => this.alRecibirFicha(datos),
  //     error: (error) => this.mostrarError(error)
  //   });
  //
  //   // GET /api/videojuegos/{id}/copias
  //   this.api.obtenerCopias(id).subscribe({
  //     next: (copias) => this.alRecibirCopias(copias),
  //     error: (error) => this.mostrarError(error)
  //   });
  // }
  //
  // private alRecibirFicha(datos: DatosFicha): void {
  //   this.videojuego = datos.videojuego;
  //   this.estatusLegal = datos.estatusLegal;
  //   this.numeroCopias = datos.numeroCopias;
  //   this.numeroProyectos = datos.numeroProyectos;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // private alRecibirCopias(copias: CopiaConCustodia[]): void {
  //   this.recibirCopias(copias);
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Pide el historial de custodia de una copia: GET /api/copias/{id}/custodias
  // private cargarHistorial(idCopia: number): void {
  //   this.api.obtenerCustodias(idCopia).subscribe({
  //     next: (custodias) => this.alRecibirCustodias(custodias),
  //     error: (error) => this.mostrarError(error)
  //   });
  // }
  //
  // private alRecibirCustodias(custodias: Custodia[]): void {
  //   this.custodiasHistorial = custodias;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Si el back responde error (por ejemplo 404 porque el id no existe), el juego
  // // se queda sin valor y el HTML muestra "No encontramos ese videojuego".
  // private mostrarError(error: unknown): void {
  //   console.error('No se pudo cargar la ficha del videojuego', error);
  //   this.cambios.markForCheck();
  // }

  // ==================================================================
  // ===== FIN DEL BLOQUE BACK =====
  // ==================================================================

  /*
   * ngOnInit se ejecuta una vez, cuando el componente ya está listo.
   * paramMap avisa cada vez que cambian los parámetros de la URL, así que la
   * ficha se actualiza aunque el usuario pase de /videojuegos/1 a /videojuegos/8
   * sin salir de la pantalla.
   */
  ngOnInit(): void {
    this.route.paramMap.subscribe({
      next: (parametros) => this.alCambiarParametros(parametros),
    });
  }

  /* Lee el :id de la URL y pide ese juego. */
  private alCambiarParametros(parametros: ParamMap): void {
    // En /videojuegos/8 el parámetro "id" vale '8', pero como TEXTO.
    // Number() lo convierte a número para poder compararlo con idVideojuego.
    const id = Number(parametros.get('id'));

    this.cargarJuego(id);
  }

  /* Borra el juego anterior, para no mostrar datos mezclados al cambiar de id. */
  private limpiarJuego(): void {
    this.videojuego = undefined;
    this.estatusLegal = '';
    this.numeroCopias = 0;
    this.numeroProyectos = 0;
    this.copias = [];
    this.instituciones = [];
    this.idCopiaHistorial = 0;
    this.custodiasHistorial = [];
  }

  /*
   * Guarda las copias, arma la lista de instituciones y pide el historial
   * de la primera copia. La usan tanto el hardcode como el bloque del back,
   * por eso está fuera de los dos.
   */
  private recibirCopias(copias: CopiaConCustodia[]): void {
    this.copias = copias;
    this.instituciones = this.agruparInstituciones(copias);

    if (copias.length > 0) {
      this.idCopiaHistorial = copias[0].copia.idCopia;
      this.cargarHistorial(this.idCopiaHistorial);
    }
  }

  /*
   * Arma la lista de instituciones custodias a partir de las custodias vigentes.
   * Recorre las copias, y por cada una:
   *   - si no tiene custodia vigente, la salta
   *   - si su institución ya está en la lista, suma 1 a su contador
   *   - si no está, la agrega con contador 1
   * Ejemplo: copias del Museo, la Universidad y el Museo ->
   *   [ Museo (2 copias), Universidad (1 copia) ]
   */
  private agruparInstituciones(copias: CopiaConCustodia[]): InstitucionConCopias[] {
    const lista: InstitucionConCopias[] = [];

    for (const item of copias) {
      if (!item.custodiaActual) {
        continue;
      }

      const institucion = item.custodiaActual.institucion;
      let existente: InstitucionConCopias | undefined = undefined;

      for (const entrada of lista) {
        if (entrada.institucion.idInstitucion === institucion.idInstitucion) {
          existente = entrada;
        }
      }

      if (existente) {
        existente.numeroCopias++;
      } else {
        lista.push({ institucion: institucion, numeroCopias: 1 });
      }
    }

    return lista;
  }

  /* Cambia la pestaña activa cuando el usuario hace clic en una. */
  seleccionarPestana(pestana: Pestana): void {
    this.pestanaActiva = pestana;
  }

  /*
   * Cuántos colores de relleno hay definidos en styles.css
   * (--portada-1 hasta --portada-6).
   */
  private totalColoresPortada: number = 6;

  /*
   * Color de la portada según el id del juego, así cada juego conserva
   * siempre el mismo color. Los colores NO están aquí: viven en styles.css
   * como variables. Este código solo elige el número y devuelve el nombre
   * de la variable, por ejemplo 'var(--portada-3)'.
   * El "%" hace que vuelva a empezar después del sexto color.
   */
  get colorPortada(): string {
    if (!this.videojuego) {
      return 'var(--portada-1)';
    }
    const numero = (this.videojuego.idVideojuego % this.totalColoresPortada) + 1;
    return 'var(--portada-' + numero + ')';
  }

  /* Iniciales del título: "Furia Quark" -> "FQ" */
  get iniciales(): string {
    if (!this.videojuego) {
      return '';
    }

    const palabras = this.videojuego.titulo.trim().split(' ');
    let resultado = '';

    for (const palabra of palabras) {
      if (palabra.length > 0 && resultado.length < 2) {
        resultado += palabra.charAt(0).toUpperCase();
      }
    }

    return resultado;
  }

  /* Línea de plataforma y año: "Arcade · 1994" */
  get plataformaYAnio(): string {
    if (!this.videojuego) {
      return '';
    }
    return this.videojuego.plataformaOriginal + ' · ' + this.videojuego.anioLanzamiento;
  }

  /* Línea del estudio: "Estudio: Quark Interactive (desaparecido)" */
  get textoEstudio(): string {
    if (!this.videojuego || !this.videojuego.estudio) {
      return '';
    }
    const estudio = this.videojuego.estudio;
    return 'Estudio: ' + estudio.nombre + ' (' + estudio.estadoActual + ')';
  }
}
