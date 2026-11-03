import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Copia } from '../../models/copia-model';

/**
 * Un campo de la tarjeta: etiqueta en mayúsculas arriba y valor debajo.
 * Ejemplo: { etiqueta: 'Número de serie', valor: 'QX-94-0127' }
 */
interface CampoCopia {
  etiqueta: string;
  valor: string;
}

/**
 * Tarjeta de una copia, para la pestaña "Copias" de la ficha.
 *
 * Uso:
 *   <app-copia-card [copia]="copia" (verHistorial)="abrirHistorial($event)"></app-copia-card>
 *
 * Recibe un dato del componente padre:
 *   - copia: la copia (física o digital) con sus campos. Si tiene custodia
 *     vigente, trae idInstitucionActual y nombreInstitucionActual (los
 *     calcula el back).
 * Avisa al padre con (verHistorial), (editar) y (eliminar), que emiten el
 * idCopia. Editar es para curador y administrador; eliminar, solo para el
 * administrador.
 *
 * Según el tipo de copia, la tarjeta muestra campos distintos
 * (generalización de COPIA en COPIA_FISICA y COPIA_DIGITAL del MER):
 *   - Física: tipo de soporte, número de serie, ubicación física
 *   - Digital: formato de archivo, tamaño, hash de verificación
 */
@Component({
  selector: 'app-copia-card',
  templateUrl: './copia-card-component.html',
  styleUrl: './copia-card-component.css',
  standalone: false,
})
export class CopiaCardComponent {
  /** El "!" le dice a TypeScript: "el padre siempre me va a pasar este valor". */
  @Input() copia!: Copia;

  /** Se emite el idCopia cuando el usuario pulsa "Ver historial de custodia". */
  @Output() verHistorial = new EventEmitter<number>();

  /** Se emite el idCopia cuando se pulsa "Editar". */
  @Output() editar = new EventEmitter<number>();

  /** Se emite el idCopia cuando se pulsa "Eliminar". */
  @Output() eliminar = new EventEmitter<number>();

  /** Lo llama el botón de la tarjeta. */
  pedirHistorial(): void {
    this.verHistorial.emit(this.copia.idCopia);
  }

  pedirEditar(): void {
    this.editar.emit(this.copia.idCopia);
  }

  pedirEliminar(): void {
    this.eliminar.emit(this.copia.idCopia);
  }

  /**
   * ID en el formato de la guía: la copia 1 se muestra como "C-001".
   * padStart(3, '0') rellena con ceros a la izquierda hasta tener 3 cifras.
   */
  get codigo(): string {
    const numero = String(this.copia.idCopia).padStart(3, '0');
    return 'C-' + numero;
  }

  /** Texto de la etiqueta de tipo: "FÍSICA" o "DIGITAL" */
  get textoTipo(): string {
    if (this.copia.tipoCopia === 'fisica') {
      return 'FÍSICA';
    }
    return 'DIGITAL';
  }

  /**
   * Nombre de la clase CSS de la etiqueta: 'fisica' o 'digital'.
   * Cada una tiene su color de borde y de texto en el CSS.
   */
  get claseTipo(): string {
    return this.copia.tipoCopia;
  }

  /**
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
      lista.push({ etiqueta: 'Tamaño', valor: this.formatearTamano(this.copia.tamanio) });
      lista.push({
        etiqueta: 'Hash de verificación',
        valor: this.valorOGuion(this.copia.hashVerificacion),
      });
    }

    return lista;
  }

  /**
   * Pie de la tarjeta, por ejemplo:
   * "Custodiada por: Museo de Cultura Invernadero · Registrada el 12/03/2019"
   * o, sin custodia vigente, "Sin custodia vigente · Registrada el 12/03/2019".
   */
  get textoPie(): string {
    let custodia = 'Sin custodia vigente';

    if (this.copia.nombreInstitucionActual) {
      custodia = 'Custodiada por: ' + this.copia.nombreInstitucionActual;
    }

    return custodia + ' · Registrada el ' + this.formatearFecha(this.copia.fechaRegistro);
  }

  /**
   * El back manda el tamaño en BYTES (por ejemplo 734003200).
   * Lo convierte a un texto legible: "700 MB". Divide entre 1024 mientras
   * el número sea grande, y avanza por las unidades B, KB, MB, GB.
   */
  private formatearTamano(bytes?: number): string {
    if (bytes === undefined || bytes === null) {
      return '—';
    }

    const unidades: string[] = ['B', 'KB', 'MB', 'GB', 'TB'];
    let valor = bytes;
    let posicion = 0;

    while (valor >= 1024 && posicion < unidades.length - 1) {
      valor = valor / 1024;
      posicion++;
    }

    // Hasta 1 decimal, sin ".0" sobrante: 700 -> "700", 1.5 -> "1.5"
    const texto = String(Math.round(valor * 10) / 10);

    return texto + ' ' + unidades[posicion];
  }

  /** Si el valor viene vacío, devuelve un guion largo en su lugar. */
  private valorOGuion(valor?: string): string {
    if (valor) {
      return valor;
    }
    return '—';
  }

  /**
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
