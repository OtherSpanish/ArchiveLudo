import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { JuegoConMeta } from '../../shared/models/api-model';
import { ResumenInicio } from '../../shared/models/resumen-inicio-model';
import { Videojuego } from '../../shared/models/videojuego-model';
import { ArchiveludoApiService } from '../../core/archiveludo-api-service';

/**
 * Un contador de la parte de arriba: etiqueta, número y color del punto.
 * Es un tipo solo del front: los números los entrega el back.
 */
interface Contador {
  etiqueta: string;
  valor: number;
  color: 'info' | 'ok' | 'warn' | 'bad';
}

/**
 * Una fila del Inicio: "En riesgo crítico", "Totalmente perdidos" o "Restaurados con éxito".
 *  - color: el color del punto junto al título (coincide con las clases del CSS)
 *  - estado: valor de la base de datos, para el enlace "Ver todos"
 *  - total: cuántos juegos hay en esa categoría en total (no solo los visibles)
 */
interface FilaJuegos {
  titulo: string;
  color: 'ok' | 'warn' | 'bad';
  estado: string;
  total: number;
  juegos: JuegoConMeta[];
}

/**
 * Pantalla de Inicio. Todo su contenido sale de UNA sola llamada,
 * GET /api/inicio/resumen: el back entrega los contadores, el juego
 * prioritario con sus motivos y las tres listas. Aquí solo se presenta.
 */
@Component({
  selector: 'app-inicio',
  templateUrl: './inicio-component.html',
  styleUrl: './inicio-component.css',
  standalone: false,
})
export class InicioComponent implements OnInit {

  /** La respuesta del back. El "?" indica que al principio no tiene valor. */
  resumen?: ResumenInicio;

  /** true si la petición falló; se muestra un aviso en la pantalla. */
  hayError: boolean = false;

  /** ChangeDetectorRef avisa a Angular que hay datos nuevos para redibujar. */
  constructor(private api: ArchiveludoApiService, private cambios: ChangeDetectorRef) { }

  /** Angular llama a ngOnInit una vez, cuando la pantalla está lista. */
  ngOnInit(): void {
    this.api.obtenerResumenInicio().subscribe({
      next: (resumen) => this.alRecibirResumen(resumen),
      error: (error) => this.mostrarError(error)
    });
  }

  private alRecibirResumen(resumen: ResumenInicio): void {
    this.resumen = resumen;
    this.cambios.markForCheck();
  }

  /** Los cuatro contadores, tal como llegan del back. */
  get contadores(): Contador[] {
    if (!this.resumen) {
      return [];
    }

    return [
      { etiqueta: 'Títulos catalogados', valor: this.resumen.titulosCatalogados, color: 'info' },
      { etiqueta: 'En riesgo', valor: this.resumen.enRiesgo, color: 'warn' },
      { etiqueta: 'Totalmente perdidos', valor: this.resumen.totalmentePerdidos, color: 'bad' },
      { etiqueta: 'Restaurados', valor: this.resumen.restaurados, color: 'ok' }
    ];
  }

  /** Las tres filas, con las listas que entrega el back. */
  get filas(): FilaJuegos[] {
    if (!this.resumen) {
      return [];
    }

    return [
      {
        titulo: 'En riesgo crítico', color: 'warn', estado: 'en riesgo',
        total: this.resumen.enRiesgo, juegos: this.conMeta(this.resumen.enRiesgoLista)
      },
      {
        titulo: 'Totalmente perdidos', color: 'bad', estado: 'totalmente perdido',
        total: this.resumen.totalmentePerdidos, juegos: this.conMeta(this.resumen.perdidosLista)
      },
      {
        titulo: 'Restaurados con éxito', color: 'ok', estado: 'restaurado',
        total: this.resumen.restaurados, juegos: this.conMeta(this.resumen.restauradosLista)
      }
    ];
  }

  /** La tarjeta recibe el juego dentro de un JuegoConMeta; la línea "meta" va vacía. */
  private conMeta(videojuegos: Videojuego[]): JuegoConMeta[] {
    const juegos: JuegoConMeta[] = [];

    for (const videojuego of videojuegos) {
      juegos.push({ videojuego: videojuego, meta: '' });
    }

    return juegos;
  }

  /** El detalle queda en la consola (F12) y la pantalla muestra un aviso. */
  private mostrarError(error: unknown): void {
    console.error('No se pudo cargar la información del Inicio', error);
    this.hayError = true;
    this.cambios.markForCheck();
  }
}
