import { Component, Input } from '@angular/core';
import { Videojuego } from '../../models/videojuego-model';

/**
 * Banner grande de la pantalla Inicio con el videojuego que más urgentemente
 * necesita intervención.
 *
 * Uso:
 *   <app-banner-prioridad
 *     [videojuego]="juegoPrioritario"
 *     [descripcion]="textoDescripcion"
 *     [nombreEstudio]="nombreDelEstudio"
 *     [criterios]="listaDeCriterios">
 *   </app-banner-prioridad>
 *
 * Recibe cuatro datos del componente padre:
 *   - videojuego: el juego en prioridad (título, plataforma, año y estudio)
 *   - descripcion: párrafo que explica por qué es prioridad
 *   - nombreEstudio: estudio desarrollador, para el subtítulo
 *   - criterios: los motivos que entrega el back, como textos cortos
 *     (por ejemplo "Ninguna copia bajo custodia vigente")
 */
@Component({
  selector: 'app-banner-prioridad',
  templateUrl: './banner-prioridad-component.html',
  styleUrl: './banner-prioridad-component.css',
  standalone: false,
})
export class BannerPrioridadComponent {
  /** El "!" le dice a TypeScript: "el padre siempre me va a pasar este valor". */
  @Input() videojuego!: Videojuego;

  @Input() descripcion: string = '';

  @Input() criterios: string[] = [];

  /** Nombre del estudio desarrollador (opcional). El estado del estudio ya viene dentro de los criterios. */
  @Input() nombreEstudio: string = '';

  /**
   * Línea de subtítulo, por ejemplo:
   * "PC · 2007 · Estudio: Foro Digital"
   *
   * El estudio es opcional, así que primero revisamos que exista.
   */
  get subtitulo(): string {
    let texto = this.videojuego.plataformaOriginal + ' · ' + this.videojuego.anioLanzamiento;

    if (this.nombreEstudio) {
      texto += ' · Estudio: ' + this.nombreEstudio;
    }

    return texto;
  }
}
