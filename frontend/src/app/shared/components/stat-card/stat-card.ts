import { Component, Input } from '@angular/core';

/*
 * Tarjeta de contador de la parte de arriba de Inicio.
 *
 * Uso:
 *   <app-stat-card etiqueta="En riesgo" [valor]="61" color="warn"></app-stat-card>
 *
 * Recibe tres datos del componente padre:
 *   - etiqueta: el texto pequeño de arriba ("Títulos catalogados")
 *   - valor: el número grande
 *   - color: el color del punto: 'info' (azul), 'ok' (verde), 'warn' (amarillo) o 'bad' (rojo)
 *            Cada uno corresponde a una clase en el CSS del componente.
 */
@Component({
  selector: 'app-stat-card',
  templateUrl: './stat-card.html',
  styleUrl: './stat-card.css',
  standalone: false
})
export class StatCard {

  @Input() etiqueta: string = '';

  @Input() valor: number = 0;

  @Input() color: 'info' | 'ok' | 'warn' | 'bad' = 'info';
}
