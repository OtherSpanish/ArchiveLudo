import { Component, Input } from '@angular/core';
import { Institucion } from '../../models/custodia-model';

/*
 * Tarjeta de una institución custodia, para la columna derecha de la
 * pestaña "Copias" de la ficha.
 *
 * Uso:
 *   <app-institucion-card [institucion]="inst" [numeroCopias]="2"></app-institucion-card>
 *
 * Recibe dos datos del componente padre:
 *   - institucion: los datos de la institución (nombre, tipo, país, contacto)
 *   - numeroCopias: cuántas copias de ESTE videojuego custodia esa institución
 */
@Component({
  selector: 'app-institucion-card',
  templateUrl: './institucion-card-component.html',
  styleUrl: './institucion-card-component.css',
  standalone: false,
})
export class InstitucionCardComponent {
  /* El "!" le dice a TypeScript: "el padre siempre me va a pasar este valor". */
  @Input() institucion!: Institucion;

  @Input() numeroCopias: number = 0;

  /* Línea de tipo y país: "Museo · Colombia" */
  get tipoYPais(): string {
    return this.institucion.tipo + ' · ' + this.institucion.pais;
  }

  /* Texto de copias, en singular o plural: "Custodia 1 copia" / "Custodia 2 copias" */
  get textoCopias(): string {
    if (this.numeroCopias === 1) {
      return 'Custodia 1 copia de este juego';
    }
    return 'Custodia ' + this.numeroCopias + ' copias de este juego';
  }

  /* Contacto de la institución; si no hay, lo dice en vez de dejar un hueco. */
  get textoContacto(): string {
    if (this.institucion.contacto) {
      return 'Contacto: ' + this.institucion.contacto;
    }
    return 'Contacto: sin datos';
  }
}
