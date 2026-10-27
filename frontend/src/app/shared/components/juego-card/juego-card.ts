import { Component, Input } from '@angular/core';
import { Videojuego } from '../../models/videojuego.model';

/*
 * Tarjeta de un videojuego para los carruseles de la pantalla Inicio.
 *
 * Uso:
 *   <app-juego-card [videojuego]="juego" [indice]="0" meta="3 copias · Huérfano">
 *   </app-juego-card>
 *
 * Recibe tres datos del componente padre:
 *   - videojuego: el objeto con los datos del juego
 *   - indice: posición en la fila; sirve para rotar el color de la portada
 *   - meta: línea extra opcional (nº de copias, estatus legal, etc.)
 */
@Component({
  selector: 'app-juego-card',
  templateUrl: './juego-card.html',
  styleUrl: './juego-card.css',
  standalone: false
})
export class JuegoCard {

  /* El "!" le dice a TypeScript: "el padre siempre me va a pasar este valor". */
  @Input() videojuego!: Videojuego;

  @Input() indice: number = 0;

  @Input() meta: string = '';

  /*
   * Colores planos de relleno para las portadas, mientras no haya imágenes
   * reales. Salen de la sección "Portadas de relleno" de la guía de diseño.
   */
  private coloresPortada: string[] = [
    '#2B3A55',
    '#3A2B55',
    '#1F4A52',
    '#4A3A24',
    '#2B4A3A',
    '#4A2B3F'
  ];

  /*
   * Elige el color según la posición de la tarjeta.
   * El "%" (módulo) hace que, al llegar al sexto color, vuelva a empezar:
   * índice 0 -> color 0, índice 6 -> color 0, índice 7 -> color 1...
   */
  get colorPortada(): string {
    const posicion = this.indice % this.coloresPortada.length;
    return this.coloresPortada[posicion];
  }

  /*
   * Saca las iniciales del título: "Torre de Ámbar Online" -> "TD".
   * Toma la primera letra de las dos primeras palabras.
   */
  get iniciales(): string {
    const palabras = this.videojuego.titulo.trim().split(' ');
    let resultado = '';

    for (const palabra of palabras) {
      if (palabra.length > 0 && resultado.length < 2) {
        resultado += palabra.charAt(0).toUpperCase();
      }
    }

    return resultado;
  }

  /* Línea de subtítulo: "PC · 2007" */
  get subtitulo(): string {
    return this.videojuego.plataformaOriginal + ' · ' + this.videojuego.anioLanzamiento;
  }
}
