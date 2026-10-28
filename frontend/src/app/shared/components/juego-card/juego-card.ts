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
  standalone: false,
})
export class JuegoCard {
  /* El "!" le dice a TypeScript: "el padre siempre me va a pasar este valor". */
  @Input() videojuego!: Videojuego;

  @Input() indice: number = 0;

  @Input() meta: string = '';

  /*
   * Cuántos colores de relleno hay definidos en styles.css
   * (--portada-1 hasta --portada-6).
   */
  private totalColoresPortada: number = 6;

  /*
   * Devuelve el color de la portada según la posición de la tarjeta.
   * Los colores NO están aquí: viven en styles.css como variables.
   * Este código solo elige el número y devuelve el nombre de la variable,
   * por ejemplo 'var(--portada-3)'.
   *
   * El "%" (módulo) hace que, al llegar al sexto color, vuelva a empezar:
   * índice 0 -> portada-1, índice 6 -> portada-1, índice 7 -> portada-2...
   */
  get colorPortada(): string {
    const numero = (this.indice % this.totalColoresPortada) + 1;
    return 'var(--portada-' + numero + ')';
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
