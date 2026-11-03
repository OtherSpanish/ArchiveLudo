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
 *   - institucion: los datos de la institución (nombre, tipo, país, teléfonos y correos)
 *   - numeroCopias: cuántas copias de ESTE videojuego custodia esa institución (opcional)
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

  /*
   * Opcional: cuántas copias de ESTE videojuego custodia la institución (ficha
   * del juego). Si no viene, se usa el total que calcula el back en
   * institucion.cantidadCopiasCustodiadas (lista de instituciones).
   */
  @Input() numeroCopias?: number;

  /* Línea de tipo y país: "Museo · Colombia" */
  get tipoYPais(): string {
    return this.institucion.tipo + ' · ' + this.institucion.pais;
  }

  /* Texto de copias, en singular o plural: "Custodia 1 copia" / "Custodia 2 copias" */
  get textoCopias(): string {
    if (this.numeroCopias !== undefined) {
      if (this.numeroCopias === 1) {
        return 'Custodia 1 copia de este juego';
      }
      return 'Custodia ' + this.numeroCopias + ' copias de este juego';
    }

    const total = this.institucion.cantidadCopiasCustodiadas;

    if (total === undefined || total === null) {
      return '';
    }
    if (total === 0) {
      return 'No custodia copias por ahora';
    }
    if (total === 1) {
      return 'Custodia 1 copia';
    }
    return 'Custodia ' + total + ' copias';
  }

  /*
   * Contacto de la institución: el primer correo, o si no hay, el primer
   * teléfono. Si tiene más de uno, se avisa con "(+N más)".
   */
  get textoContacto(): string {
    const correos = this.institucion.correos || [];
    const telefonos = this.institucion.telefonos || [];
    const total = correos.length + telefonos.length;

    if (total === 0) {
      return 'Contacto: sin datos';
    }

    let principal = String(telefonos[0]);
    if (correos.length > 0) {
      principal = correos[0];
    }

    if (total > 1) {
      return 'Contacto: ' + principal + ' (+' + (total - 1) + ' más)';
    }
    return 'Contacto: ' + principal;
  }
}
