import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Videojuego } from '../../../shared/models/videojuego-model';
import {
  FiltrosReporte,
  OPCIONES_REPORTE,
  OpcionReporte,
  Reporte,
  TipoReporte
} from '../../../shared/models/reporte-model';

import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

/**
 * Pantalla "Reportes": seis reportes del archivo.
 *
 * La persona elige un tipo de reporte y, si quiere, un videojuego y un rango
 * de fechas (desde / hasta). Al pulsar "Generar reporte" se pide la tabla al
 * back y se dibuja. Todos los reportes usan la misma tabla, porque el back
 * responde { titulo, columnas, filas } con todo ya formateado.
 *
 * Los filtros se validan en el back: si algo está mal (por ejemplo "desde"
 * posterior a "hasta") responde 400 con un mensaje, y aquí se muestra tal cual.
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

  /** true mientras se espera la respuesta del back. */
  cargando: boolean = false;

  /** true si el back respondió con error. */
  hayError: boolean = false;

  /** Texto del error (el mensaje del back, o uno general si no hay). */
  mensajeError: string = 'No pudimos generar el reporte. Intenta de nuevo más tarde.';

  /** El reporte ya generado. Es null mientras no se haya generado ninguno. */
  reporte: Reporte | null = null;

  /** Videojuego del reporte generado, para el encabezado. Vacío si fue de todos. */
  juegoDelReporte: string = '';

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

  /** Arma el formulario, deja elegido el juego de la URL (si viene) y carga el selector. */
  ngOnInit(): void {
    this.formulario = this.constructorFormulario.group({
      videojuego: [''],
      tipo: ['estatus', Validators.required],
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

  /** El selector muestra los primeros 50 juegos (el límite máximo del back). */
  private cargarJuegos(): void {
    this.api.buscarVideojuegos('', '', 0, 50).subscribe({
      next: (pagina) => {
        this.juegos = pagina.juegos;
        this.cambios.markForCheck();
      },
      error: (error) => this.alFallar(error)
    });
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

  /** Botón "Generar reporte": arma los filtros y pide la tabla al back. */
  generar(): void {
    const valores = this.formulario.value;
    const tipo = valores.tipo as TipoReporte;

    // Los filtros vacíos no se envían (el servicio los descarta).
    const filtros: FiltrosReporte = {
      idVideojuego: valores.videojuego ? Number(valores.videojuego) : undefined,
      desde: valores.desde,
      hasta: valores.hasta
    };

    // Se guarda el texto del encabezado ahora, para que no cambie si
    // la persona toca el formulario mientras mira el reporte.
    this.juegoDelReporte = filtros.idVideojuego ? this.buscarTitulo(filtros.idVideojuego) : '';

    this.cargando = true;
    this.hayError = false;
    this.reporte = null;

    this.api.obtenerReporte(tipo, filtros).subscribe({
      next: (reporte) => this.alRecibirReporte(reporte),
      error: (error) => this.alFallar(error)
    });
  }

  private alRecibirReporte(reporte: Reporte): void {
    this.reporte = reporte;
    this.cargando = false;
    this.cambios.markForCheck();
  }

  /** Muestra el mensaje del back si lo trae ({ estado, mensaje }); si no, el texto general. */
  private alFallar(error: { error?: { mensaje?: string } }): void {
    console.error('No se pudo generar el reporte', error);

    if (error.error && error.error.mensaje) {
      this.mensajeError = error.error.mensaje;
    } else {
      this.mensajeError = 'No pudimos generar el reporte. Intenta de nuevo más tarde.';
    }

    this.cargando = false;
    this.hayError = true;
    this.cambios.markForCheck();
  }

  /** Botón "Limpiar fechas": borra el rango para ver el reporte completo. */
  limpiarFechas(): void {
    this.formulario.patchValue({ desde: '', hasta: '' });
  }

  /** Botón "Imprimir": abre el diálogo de impresión; los estilos @media print dejan solo la tabla. */
  imprimir(): void {
    window.print();
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
}
