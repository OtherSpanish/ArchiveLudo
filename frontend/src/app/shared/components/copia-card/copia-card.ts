import { Component, Input } from '@angular/core';
import { Copia } from '../../models/copia.model';
import { Custodia } from '../../models/custodia.model';

/*
 * Un campo de la tarjeta: etiqueta en mayúsculas arriba y valor debajo.
 * Ejemplo: { etiqueta: 'Número de serie', valor: 'QX-94-0127' }
 */
interface CampoCopia {
  etiqueta: string;
  valor: string;
}

/*
 * Tarjeta de una copia, para la pestaña "Copias" de la ficha.
 *
 * Uso:
 *   <app-copia-card [copia]="copia" [custodiaActual]="custodia"></app-copia-card>
 *
 * Recibe dos datos del componente padre:
 *   - copia: la copia (física o digital) con sus campos
 *   - custodiaActual: la custodia vigente de esa copia (la que no tiene
 *     fechaFin). Es opcional: una copia puede no tener custodia registrada.
 *
 * Según el tipo de copia, la tarjeta muestra campos distintos
 * (generalización de COPIA en COPIA_FISICA y COPIA_DIGITAL del MER):
 *   - Física: tipo de soporte, número de serie, ubicación física
 *   - Digital: formato de archivo, tamaño, hash de verificación
 */
@Component({
  selector: 'app-copia-card',
  templateUrl: './copia-card.html',
  styleUrl: './copia-card.css',
  standalone: false,
})
export class CopiaCard {
  /* El "!" le dice a TypeScript: "el padre siempre me va a pasar este valor". */
  @Input() copia!: Copia;

  /* El "?" indica que puede no venir. */
  @Input() custodiaActual?: Custodia;

  /*
   * ID en el formato de la guía: la copia 1 se muestra como "C-001".
   * padStart(3, '0') rellena con ceros a la izquierda hasta tener 3 cifras.
   */
  get codigo(): string {
    const numero = String(this.copia.idCopia).padStart(3, '0');
    return 'C-' + numero;
  }

  /* Texto de la etiqueta de tipo: "FÍSICA" o "DIGITAL" */
  get textoTipo(): string {
    if (this.copia.tipoCopia === 'fisica') {
      return 'FÍSICA';
    }
    return 'DIGITAL';
  }

  /*
   * Nombre de la clase CSS de la etiqueta: 'fisica' o 'digital'.
   * Cada una tiene su color de borde y de texto en el CSS.
   */
  get claseTipo(): string {
    return this.copia.tipoCopia;
  }

  /*
   * Lista de campos que se muestran, según el tipo de copia.
   * Se arma con un if normal. Los valores opcionales (con "?") pueden venir
   * vacíos, así que si falta alguno mostramos un guion largo.
   */
  get campos(): CampoCopia[] {
    const lista: CampoCopia[] = [];

    if (this.copia.tipoCopia === 'fisica') {
      lista.push({ etiqueta: 'Tipo de soporte', valor: this.valorOGuion(this.copia.tipoSoporte) });
      lista.push({ etiqueta: 'Número de serie', valor: this.valorOGuion(this.copia.numeroSerie) });
      lista.push({
        etiqueta: 'Ubicación física',
        valor: this.valorOGuion(this.copia.ubicacionFisica),
      });
    } else {
      lista.push({
        etiqueta: 'Formato de archivo',
        valor: this.valorOGuion(this.copia.formatoArchivo),
      });
      lista.push({ etiqueta: 'Tamaño', valor: this.valorOGuion(this.copia.tamanio) });
      lista.push({
        etiqueta: 'Hash de verificación',
        valor: this.valorOGuion(this.copia.hashVerificacion),
      });
    }

    return lista;
  }

  /*
   * Pie de la tarjeta, por ejemplo:
   * "Custodia actual: Museo de Cultura Invernadero · Registrada el 12/03/2019"
   */
  get textoPie(): string {
    let nombreInstitucion = 'sin custodia registrada';

    if (this.custodiaActual) {
      nombreInstitucion = this.custodiaActual.institucion.nombre;
    }

    return (
      'Custodia actual: ' +
      nombreInstitucion +
      ' · Registrada el ' +
      this.formatearFecha(this.copia.fechaRegistro)
    );
  }

  /* Si el valor viene vacío, devuelve un guion largo en su lugar. */
  private valorOGuion(valor?: string): string {
    if (valor) {
      return valor;
    }
    return '—';
  }

  /*
   * Convierte '2019-03-12' en '12/03/2019'.
   * split('-') parte el texto en tres pedazos: año, mes y día.
   */
  private formatearFecha(fecha: string): string {
    const partes = fecha.split('-');
    if (partes.length !== 3) {
      return fecha;
    }
    return partes[2] + '/' + partes[1] + '/' + partes[0];
  }
}
