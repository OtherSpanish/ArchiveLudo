import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Videojuego } from '../../../shared/models/videojuego-model';
import { OPCIONES_REPORTE, OpcionReporte, ReporteTabla, TipoReporte } from '../../../shared/models/reporte-model';

import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { EstatusLegal } from '../../../shared/models/estatus-legal-model';
import { EventoRiesgo } from '../../../shared/models/evento-riesgo-model';
import { Copia } from '../../../shared/models/copia-model';

/**
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

  /** Los seis tipos de reporte, para el selector. */
  opciones: OpcionReporte[] = OPCIONES_REPORTE;

  /** Videojuegos que se pueden elegir en el selector. */
  juegos: Videojuego[] = [];

  /** El formulario con los cuatro campos. Se arma en ngOnInit. */
  formulario!: FormGroup;

  /** true después del primer intento de generar; muestra los errores de campos sin tocar. */
  enviado: boolean = false;

  /** true mientras se espera la respuesta del back. */
  cargando: boolean = false;

  /** true si el back respondió con error. */
  hayError: boolean = false;

  /** Texto del error (cambia si ese reporte aún no está disponible). */
  mensajeError: string = 'No pudimos generar el reporte. Intenta de nuevo más tarde.';

  /** El reporte ya generado. Es null mientras no se haya generado ninguno. */
  reporte: ReporteTabla | null = null;

  /** Encabezado del reporte generado: qué reporte y de qué juego. */
  etiquetaDelReporte: string = '';
  juegoDelReporte: string = '';

  /** Meses abreviados para formatear las fechas de las celdas. */
  private meses: string[] = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  /**
   * ApiService habla con el back; ActivatedRoute lee la URL; FormBuilder arma
   * el formulario; ChangeDetectorRef avisa a Angular que hay que redibujar.
   */
  constructor(
    private api: ArchiveludoApiService,
    private route: ActivatedRoute,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) { }

  /** El selector muestra los primeros 50 juegos (el límite máximo del back). */
  private cargarJuegos(): void {
    this.api.buscarVideojuegos('', '', 0, 50).subscribe({
      next: (pagina) => {
        this.juegos = pagina.juegos;
        this.cambios.markForCheck();
      },
      error: (error) => this.alFallar(error, 'No pudimos cargar la lista de videojuegos.')
    });
  }

  /**
   * El back no tiene un endpoint de reportes. Tres reportes se arman aquí con
   * los endpoints que sí existen (estatus legal, eventos de riesgo y copias);
   * los otros tres (proyectos, custodias y expertos) dependen de servicios que
   * el back todavía no tiene.
   */
  private cargarReporte(tipo: TipoReporte, idVideojuego: number, desde: string, hasta: string): void {
    if (tipo === 'estatus-legal') {
      this.api.obtenerEstatusLegal(idVideojuego).subscribe({
        next: (lista) => this.mostrarReporte(this.tablaEstatus(lista), desde, hasta),
        error: (error) => this.alFallar(error, this.mensajeError)
      });
    } else if (tipo === 'eventos-riesgo') {
      this.api.obtenerEventosRiesgo(idVideojuego).subscribe({
        next: (lista) => this.mostrarReporte(this.tablaEventos(lista), desde, hasta),
        error: (error) => this.alFallar(error, this.mensajeError)
      });
    } else if (tipo === 'copias') {
      this.api.obtenerCopias(idVideojuego).subscribe({
        next: (lista) => this.mostrarReporte(this.tablaCopias(lista), desde, hasta),
        error: (error) => this.alFallar(error, this.mensajeError)
      });
    } else {
      this.alFallar({}, 'Este reporte aún no está disponible: el servidor todavía no ofrece esa información.');
    }
  }

  /** Tabla del reporte "Estatus legal": una fila por registro, del más antiguo al más reciente. */
  private tablaEstatus(lista: EstatusLegal[]): ReporteTabla {
    const filas: string[][] = [];

    for (const registro of lista) {
      filas.push([registro.fechaDeterminacion, registro.estatus, registro.observacion || '']);
    }

    return { columnas: ['Fecha de determinación', 'Estatus legal', 'Observación'], filas: filas };
  }

  /** Tabla del reporte "Eventos de riesgo". */
  private tablaEventos(lista: EventoRiesgo[]): ReporteTabla {
    const filas: string[][] = [];

    for (const evento of lista) {
      filas.push([evento.fechaEvento, evento.tipoEvento, evento.descripcion, evento.fuente || '']);
    }

    return { columnas: ['Fecha', 'Tipo de evento', 'Descripción', 'Fuente'], filas: filas };
  }

  /**
   * Tabla del reporte "Copias en el tiempo": con las copias ordenadas por
   * fecha de registro, la columna "Copias" cuenta cuántas había en ese momento.
   */
  private tablaCopias(lista: Copia[]): ReporteTabla {
    const ordenadas = lista.slice();
    ordenadas.sort((a, b) => a.fechaRegistro.localeCompare(b.fechaRegistro));

    const filas: string[][] = [];
    let acumulado = 0;

    for (const copia of ordenadas) {
      acumulado = acumulado + 1;
      const clase = copia.tipoCopia === 'fisica' ? 'física' : 'digital';
      filas.push([copia.fechaRegistro.substring(0, 10), String(acumulado), 'Se registró una copia ' + clase]);
    }

    return { columnas: ['Fecha', 'Copias', 'Cambio'], filas: filas };
  }

  /** Ordena por la primera columna (una fecha yyyy-MM-dd), recorta por el rango y muestra la tabla. */
  private mostrarReporte(tabla: ReporteTabla, desde: string, hasta: string): void {
    const ordenadas = tabla.filas.slice();
    ordenadas.sort((a, b) => a[0].localeCompare(b[0]));

    const filas: string[][] = [];
    for (const fila of ordenadas) {
      const despuesDeDesde = desde === '' || fila[0] >= desde;
      const antesDeHasta = hasta === '' || fila[0] <= hasta;
      if (despuesDeDesde && antesDeHasta) {
        filas.push(fila);
      }
    }

    this.reporte = { columnas: tabla.columnas, filas: filas };
    this.cargando = false;
    this.cambios.markForCheck();
  }

  /** Muestra el aviso de error con el texto dado; el detalle queda en la consola (F12). */
  private alFallar(error: unknown, mensaje: string): void {
    console.error('No se pudo generar el reporte', error);
    this.mensajeError = mensaje;
    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  /** Arma el formulario, deja elegido el juego de la URL (si viene) y carga el selector. */
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

  /** true si la fecha "desde" es posterior a la fecha "hasta". Las fechas yyyy-MM-dd se comparan como texto. */
  get rangoInvalido(): boolean {
    const desde = this.formulario.value.desde;
    const hasta = this.formulario.value.hasta;
    return desde !== '' && hasta !== '' && desde > hasta;
  }

  /** La descripción del tipo de reporte elegido, que se muestra bajo el selector. */
  get descripcionTipo(): string {
    const tipo = this.formulario.value.tipo;
    for (const opcion of this.opciones) {
      if (opcion.valor === tipo) {
        return opcion.descripcion;
      }
    }
    return '';
  }

  /** Botón "Generar reporte": valida el formulario y pide la tabla. */
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
    this.mensajeError = 'No pudimos generar el reporte. Intenta de nuevo más tarde.';
    this.reporte = null;

    this.cargarReporte(tipo, idVideojuego, valores.desde, valores.hasta);
  }

  /** Botón "Limpiar fechas": borra el rango para ver el reporte completo. */
  limpiarFechas(): void {
    this.formulario.patchValue({ desde: '', hasta: '' });
  }

  /** El nombre del tipo de reporte, para el encabezado. */
  private buscarEtiqueta(tipo: TipoReporte): string {
    for (const opcion of this.opciones) {
      if (opcion.valor === tipo) {
        return opcion.etiqueta;
      }
    }
    return '';
  }

  /** El título del videojuego, para el encabezado. */
  private buscarTitulo(idVideojuego: number): string {
    for (const juego of this.juegos) {
      if (juego.idVideojuego === idVideojuego) {
        return juego.titulo;
      }
    }
    return '';
  }

  /**
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
