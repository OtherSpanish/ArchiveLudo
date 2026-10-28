import { Component, Input } from '@angular/core';
import { Videojuego } from '../../models/videojuego.model';

/*
 * Banner grande de la pantalla Inicio con el videojuego que más urgentemente
 * necesita intervención.
 *
 * Uso:
 *   <app-banner-prioridad
 *     [videojuego]="juegoPrioritario"
 *     [descripcion]="textoDescripcion"
 *     [criterios]="listaDeCriterios">
 *   </app-banner-prioridad>
 *
 * Recibe tres datos del componente padre:
 *   - videojuego: el juego en prioridad (título, plataforma, año y estudio)
 *   - descripcion: párrafo que explica por qué es prioridad
 *   - criterios: los tres criterios de riesgo que cumple, como textos cortos
 *     (por ejemplo "Sin copias físicas registradas")
 *
 * Los tres criterios salen de la propuesta del proyecto: sin copias físicas,
 * sin proyecto de restauración activo y estudio desarrollador desaparecido.
 */
@Component({
  selector: 'app-banner-prioridad',
  templateUrl: './banner-prioridad.html',
  styleUrl: './banner-prioridad.css',
  standalone: false,
})
export class BannerPrioridad {
  /* El "!" le dice a TypeScript: "el padre siempre me va a pasar este valor". */
  @Input() videojuego!: Videojuego;

  @Input() descripcion: string = '';

  @Input() criterios: string[] = [];

  /*
   * Línea de subtítulo, por ejemplo:
   * "PC · 2007 · Estudio: Foro Digital (desaparecido)"
   *
   * El estudio es opcional en el modelo (lleva "?"), así que primero
   * revisamos que exista antes de usar su nombre.
   */
  get subtitulo(): string {
    let texto = this.videojuego.plataformaOriginal + ' · ' + this.videojuego.anioLanzamiento;

    if (this.videojuego.estudio) {
      texto += ' · Estudio: ' + this.videojuego.estudio.nombre;
      texto += ' (' + this.videojuego.estudio.estadoActual + ')';
    }

    return texto;
  }
}
