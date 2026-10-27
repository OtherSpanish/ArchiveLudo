import { Component, Input } from '@angular/core';

/*
 * Describe cómo se ve un estado: qué texto muestra y de qué color es el punto.
 * "color" solo puede ser una de estas cuatro opciones, y cada una
 * corresponde a una clase en el archivo CSS del componente.
 */
interface Apariencia {
  texto: string;
  color: 'ok' | 'warn' | 'bad' | 'neutral';
}

/*
 * Componente reutilizable que muestra un estado como "punto de color + texto".
 *
 * Uso en cualquier HTML:
 *   <app-estado-badge estado="en_riesgo"></app-estado-badge>
 *   <app-estado-badge [estado]="videojuego.estadoConservacion"></app-estado-badge>
 *
 * Sirve para dos tipos de estado de la base de datos:
 *   - estado_conservacion (tabla VIDEOJUEGO)
 *   - estado_integridad   (tabla COPIA)
 * Los valores de ambos son distintos entre sí, así que caben en un solo mapa.
 */
@Component({
  selector: 'app-estado-badge',
  templateUrl: './estado-badge.html',
  styleUrl: './estado-badge.css',
  standalone: false
})
export class EstadoBadge {

  /*
   * @Input significa que el componente padre le pasa este valor desde su HTML.
   * Aquí llega el valor tal cual está en la base de datos, por ejemplo 'en_riesgo'.
   */
  @Input() estado: string = '';

  /*
   * Mapa de "valor en la base de datos" -> "apariencia".
   * Sale de la sección 2 de la guía de diseño.
   * Es un objeto normal: se busca por la llave, por ejemplo
   * this.mapaEstados['perdido'] devuelve { texto: 'Perdido', color: 'bad' }.
   */
  private mapaEstados: Record<string, Apariencia> = {
    // estado_conservacion (VIDEOJUEGO)
    'activo': { texto: 'Activo', color: 'ok' },
    'en_riesgo': { texto: 'En riesgo', color: 'warn' },
    'parcialmente_perdido': { texto: 'Parcialmente perdido', color: 'warn' },
    'perdido': { texto: 'Perdido', color: 'bad' },
    'restaurado': { texto: 'Restaurado', color: 'ok' },

    // estado_integridad (COPIA)
    'buena': { texto: 'Buena', color: 'ok' },
    'deteriorada': { texto: 'Deteriorada', color: 'warn' },
    'dañada': { texto: 'Dañada', color: 'bad' },
    'irrecuperable': { texto: 'Irrecuperable', color: 'bad' }
  };

  /*
   * Devuelve la apariencia del estado actual.
   * Si llega un valor que no está en el mapa (por ejemplo, un estado nuevo
   * que agregue el backend), no se rompe: muestra el texto original en gris.
   */
  obtenerApariencia(): Apariencia {
    const apariencia = this.mapaEstados[this.estado];
    if (apariencia) {
      return apariencia;
    }
    return { texto: this.estado, color: 'neutral' };
  }

  /* Texto que se muestra al lado del punto. */
  get texto(): string {
    return this.obtenerApariencia().texto;
  }

  /* Nombre de la clase CSS del color: 'ok', 'warn', 'bad' o 'neutral'. */
  get color(): string {
    return this.obtenerApariencia().color;
  }
}
