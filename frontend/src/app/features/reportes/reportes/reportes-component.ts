import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Videojuego } from '../../../shared/models/videojuego-model';
import { OPCIONES_REPORTE, OpcionReporte, ReporteTabla, TipoReporte } from '../../../shared/models/reporte-model';

// ===== BACK: descomenta este import cuando conectes el back =====
// import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

/*
 * Pantalla "Reportes": trazabilidad de un videojuego.
 *
 * La persona elige un videojuego, un tipo de reporte y, si quiere, un rango
 * de fechas (desde / hasta). Al pulsar "Generar reporte" se pide la tabla
 * al back y se dibuja. Todos los reportes se dibujan con la misma tabla,
 * porque el back responde { columnas, filas }.
 *
 * También acepta ?videojuego=8 en la URL para dejar el juego ya elegido
 * (sirve para enlazar desde la ficha de un juego).
 */
@Component({
  selector: 'app-reportes',
  templateUrl: './reportes-component.html',
  styleUrl: './reportes-component.css',
  standalone: false,
})
export class ReportesComponent implements OnInit {

  /* Los seis tipos de reporte, para el selector. */
  opciones: OpcionReporte[] = OPCIONES_REPORTE;

  /* Videojuegos que se pueden elegir en el selector. */
  juegos: Videojuego[] = [];

  /* El formulario con los cuatro campos. Se arma en ngOnInit. */
  formulario!: FormGroup;

  /* true después del primer intento de generar; muestra los errores de campos sin tocar. */
  enviado: boolean = false;

  /* true mientras se espera la respuesta del back. */
  cargando: boolean = false;

  /* true si el back respondió con error. */
  hayError: boolean = false;

  /* El reporte ya generado. Es null mientras no se haya generado ninguno. */
  reporte: ReporteTabla | null = null;

  /* Encabezado del reporte generado: qué reporte y de qué juego. */
  etiquetaDelReporte: string = '';
  juegoDelReporte: string = '';

  /* Meses abreviados para formatear las fechas de las celdas. */
  private meses: string[] = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  // ==================================================================
  // ===== HARDCODE: borra desde aquí hasta "FIN HARDCODE"
  //  cuando conectes el back (incluye este constructor, cargarJuegos
  //  y cargarReporte)
  // ==================================================================

  /* Videojuegos de prueba. Solo el 1 y el 8 tienen datos de reporte. */
  private juegosPrueba: Videojuego[] = [
    { idVideojuego: 1, titulo: 'Torre de Ámbar Online', plataformaOriginal: 'PC', anioLanzamiento: 2007, estadoConservacion: 'en_riesgo' },
    { idVideojuego: 2, titulo: 'Sombras de Vellum', plataformaOriginal: 'PlayStation', anioLanzamiento: 1999, estadoConservacion: 'en_riesgo' },
    { idVideojuego: 3, titulo: 'Códice Nómada', plataformaOriginal: 'Web', anioLanzamiento: 2011, estadoConservacion: 'en_riesgo' },
    { idVideojuego: 8, titulo: 'Furia Quark', plataformaOriginal: 'Arcade', anioLanzamiento: 1994, estadoConservacion: 'restaurado' }
  ];

  /*
   * Datos de prueba de los reportes. La primera llave es el id del juego y
   * la segunda el tipo de reporte. La primera columna siempre es una fecha
   * (yyyy-MM-dd), que es la que usa el filtro de rango de las pruebas.
   */
  private datosPrueba: Record<number, Partial<Record<TipoReporte, ReporteTabla>>> = {
    1: {
      'estatus-legal': {
        columnas: ['Fecha de determinación', 'Estatus legal'],
        filas: [
          ['2012-05-03', 'Protegido por derechos de autor'],
          ['2026-02-14', 'Obra huérfana']
        ]
      },
      'eventos-riesgo': {
        columnas: ['Fecha', 'Tipo de evento', 'Descripción', 'Fuente'],
        filas: [
          ['2021-06-30', 'Cierre de servidores', 'La editora apagó los servidores oficiales.', 'Comunicado oficial'],
          ['2024-01-12', 'Quiebra del estudio', 'El estudio desarrollador cerró definitivamente.', 'Prensa especializada']
        ]
      },
      'proyectos': {
        columnas: ['Fecha de inicio', 'Fecha de fin', 'Estado', 'Herramienta / emulador'],
        filas: [
          ['2026-03-10', 'Vigente', 'Activo', 'Servidor privado + emulación de cliente']
        ]
      },
      'copias': {
        columnas: ['Fecha', 'Copias', 'Cambio'],
        filas: [
          ['2020-01-15', '1', 'Se registró una copia digital'],
          ['2023-08-09', '2', 'Se registró una copia física']
        ]
      },
      'custodias': {
        columnas: ['Desde', 'Hasta', 'Institución', 'Copia'],
        filas: [
          ['2020-01-15', '2023-05-01', 'Museo del Videojuego', 'Copia #1 (digital)'],
          ['2023-05-01', 'Vigente', 'Archivo Digital Andino', 'Copia #1 (digital)'],
          ['2023-08-09', 'Vigente', 'Museo del Videojuego', 'Copia #2 (física)']
        ]
      },
      'expertos': {
        columnas: ['Desde', 'Hasta', 'Experto', 'Rol', 'Proyecto'],
        filas: [
          ['2026-03-10', 'Vigente', 'Laura Mendoza', 'Líder técnica', 'P-001'],
          ['2026-04-02', 'Vigente', 'Andrés Quintero', 'Ingeniero de redes', 'P-001']
        ]
      }
    },
    8: {
      'estatus-legal': {
        columnas: ['Fecha de determinación', 'Estatus legal'],
        filas: [
          ['2016-09-01', 'En disputa legal'],
          ['2025-12-01', 'Liberado por el titular']
        ]
      },
      'eventos-riesgo': {
        columnas: ['Fecha', 'Tipo de evento', 'Descripción', 'Fuente'],
        filas: [
          ['2022-03-18', 'Fallo de soporte', 'La placa original dejó de arrancar.', 'Informe del museo']
        ]
      },
      'proyectos': {
        columnas: ['Fecha de inicio', 'Fecha de fin', 'Estado', 'Herramienta / emulador'],
        filas: [
          ['2025-02-01', '2025-11-20', 'Finalizado', 'MAME']
        ]
      },
      'copias': {
        columnas: ['Fecha', 'Copias', 'Cambio'],
        filas: [
          ['2019-07-02', '1', 'Se registró una copia física'],
          ['2025-11-20', '2', 'Se registró una copia digital']
        ]
      },
      'custodias': {
        columnas: ['Desde', 'Hasta', 'Institución', 'Copia'],
        filas: [
          ['2019-07-02', 'Vigente', 'Museo del Videojuego', 'Copia #3 (física)'],
          ['2025-11-20', 'Vigente', 'Archivo Digital Andino', 'Copia #4 (digital)']
        ]
      },
      'expertos': {
        columnas: ['Desde', 'Hasta', 'Experto', 'Rol', 'Proyecto'],
        filas: [
          ['2025-02-01', '2025-11-20', 'Julián Pardo', 'Especialista en ROMs', 'P-003'],
          ['2025-05-12', '2025-11-20', 'Camila Rojas', 'Control de calidad', 'P-003']
        ]
      }
    }
  };

  /*
   * ActivatedRoute lee la URL; FormBuilder arma el formulario;
   * ChangeDetectorRef avisa a Angular que hay que redibujar.
   */
  constructor(
    private route: ActivatedRoute,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) { }

  /* Entrega los videojuegos de prueba para el selector. */
  private cargarJuegos(): void {
    this.juegos = this.juegosPrueba;
    this.cambios.markForCheck();
  }

  /* Arma el reporte de prueba: busca los datos del juego y recorta por el rango de fechas. */
  private cargarReporte(tipo: TipoReporte, idVideojuego: number, desde: string, hasta: string): void {
    // Las columnas salen del juego 1, que tiene todos los tipos; así un juego
    // sin datos igual muestra la tabla vacía con sus encabezados.
    const base = this.datosPrueba[1][tipo] as ReporteTabla;
    const propios = this.datosPrueba[idVideojuego] ? this.datosPrueba[idVideojuego][tipo] : undefined;
    const origen = propios ? propios : { columnas: base.columnas, filas: [] as string[][] };

    const filas: string[][] = [];
    for (const fila of origen.filas) {
      const despuesDeDesde = desde === '' || fila[0] >= desde;
      const antesDeHasta = hasta === '' || fila[0] <= hasta;
      if (despuesDeDesde && antesDeHasta) {
        filas.push(fila);
      }
    }

    this.reporte = { columnas: origen.columnas, filas: filas };
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
  //   private route: ActivatedRoute,
  //   private constructorFormulario: FormBuilder,
  //   private cambios: ChangeDetectorRef
  // ) { }
  //
  // /* El selector muestra los primeros 50 juegos (el límite máximo del back). */
  // private cargarJuegos(): void {
  //   this.api.buscarVideojuegos('', '', 0, 50).subscribe({
  //     next: (respuesta) => {
  //       const lista: Videojuego[] = [];
  //       for (const item of respuesta.juegos) {
  //         lista.push(item.videojuego);
  //       }
  //       this.juegos = lista;
  //       this.cambios.markForCheck();
  //     },
  //     error: () => {
  //       this.hayError = true;
  //       this.cambios.markForCheck();
  //     }
  //   });
  // }
  //
  // private cargarReporte(tipo: TipoReporte, idVideojuego: number, desde: string, hasta: string): void {
  //   this.api.obtenerReporte(tipo, idVideojuego, desde, hasta).subscribe({
  //     next: (respuesta) => {
  //       this.reporte = respuesta;
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

  /* Arma el formulario, deja elegido el juego de la URL (si viene) y carga el selector. */
  ngOnInit(): void {
    this.formulario = this.constructorFormulario.group({
      videojuego: ['', Validators.required],
      tipo: ['estatus-legal', Validators.required],
      desde: [''],
      hasta: ['']
    });

    // ?videojuego=8 deja el juego ya elegido.
    const desdeUrl = this.route.snapshot.queryParamMap.get('videojuego');
    if (desdeUrl) {
      this.formulario.patchValue({ videojuego: desdeUrl });
    }

    this.cargarJuegos();
  }

  /* true si la fecha "desde" es posterior a la fecha "hasta". Las fechas yyyy-MM-dd se comparan como texto. */
  get rangoInvalido(): boolean {
    const desde = this.formulario.value.desde;
    const hasta = this.formulario.value.hasta;
    return desde !== '' && hasta !== '' && desde > hasta;
  }

  /* La descripción del tipo de reporte elegido, que se muestra bajo el selector. */
  get descripcionTipo(): string {
    const tipo = this.formulario.value.tipo;
    for (const opcion of this.opciones) {
      if (opcion.valor === tipo) {
        return opcion.descripcion;
      }
    }
    return '';
  }

  /* Botón "Generar reporte": valida el formulario y pide la tabla. */
  generar(): void {
    this.enviado = true;

    if (this.formulario.invalid || this.rangoInvalido) {
      return;
    }

    const valores = this.formulario.value;
    const tipo = valores.tipo as TipoReporte;
    const idVideojuego = Number(valores.videojuego);

    // Se guardan los textos del encabezado ahora, para que no cambien si
    // la persona toca el formulario mientras mira el reporte.
    this.etiquetaDelReporte = this.buscarEtiqueta(tipo);
    this.juegoDelReporte = this.buscarTitulo(idVideojuego);

    this.cargando = true;
    this.hayError = false;
    this.reporte = null;

    this.cargarReporte(tipo, idVideojuego, valores.desde, valores.hasta);
  }

  /* Botón "Limpiar fechas": borra el rango para ver el reporte completo. */
  limpiarFechas(): void {
    this.formulario.patchValue({ desde: '', hasta: '' });
  }

  /* El nombre del tipo de reporte, para el encabezado. */
  private buscarEtiqueta(tipo: TipoReporte): string {
    for (const opcion of this.opciones) {
      if (opcion.valor === tipo) {
        return opcion.etiqueta;
      }
    }
    return '';
  }

  /* El título del videojuego, para el encabezado. */
  private buscarTitulo(idVideojuego: number): string {
    for (const juego of this.juegos) {
      if (juego.idVideojuego === idVideojuego) {
        return juego.titulo;
      }
    }
    return '';
  }

  /*
   * Formatea una celda de la tabla: una fecha yyyy-MM-dd pasa a "14 feb 2026",
   * un texto vacío pasa a "—" y cualquier otro texto queda igual.
   * Se parte el texto a mano (no con new Date) para evitar que la zona
   * horaria corra el día.
   */
  formatearCelda(valor: string): string {
    if (valor === '') {
      return '—';
    }

    const esFecha = /^\d{4}-\d{2}-\d{2}$/.test(valor);
    if (!esFecha) {
      return valor;
    }

    const partes = valor.split('-');
    const mes = Number(partes[1]) - 1;
    return Number(partes[2]) + ' ' + this.meses[mes] + ' ' + partes[0];
  }
}
