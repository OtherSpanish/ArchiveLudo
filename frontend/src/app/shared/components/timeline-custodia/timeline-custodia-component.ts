import { Component, Input } from '@angular/core';
import { Custodia } from '../../models/custodia-model';

/*
 * Un hito de la línea de tiempo, ya con los textos listos para mostrar.
 *  - id: identifica el hito (es el idCustodia)
 *  - vigente: true si es la custodia actual (no tiene fechaFin).
 *    El punto del hito vigente es azul y los de hitos pasados son grises.
 */
interface Hito {
  id: number;
  institucion: string;
  periodo: string;
  condiciones: string;
  vigente: boolean;
}

/*
 * Historial de custodia de UNA copia, como línea de tiempo vertical.
 * Cada vez que la copia cambia de institución se registra un periodo nuevo.
 *
 * Uso:
 *   <app-timeline-custodia [idCopia]="1" [custodias]="listaDeCustodias"></app-timeline-custodia>
 *
 * Recibe dos datos del componente padre:
 *   - idCopia: la copia a la que pertenece el historial (para el título "C-001")
 *   - custodias: la lista de periodos de custodia (viene de GET /api/copias/{id}/custodias)
 */
@Component({
  selector: 'app-timeline-custodia',
  templateUrl: './timeline-custodia-component.html',
  styleUrl: './timeline-custodia-component.css',
  standalone: false,
})
export class TimelineCustodiaComponent {
  @Input() idCopia: number = 0;

  @Input() custodias: Custodia[] = [];

  /* Nombres de los meses, para convertir '2019-03-12' en 'Marzo 2019'. */
  private meses: string[] = [
    'enero',
    'febrero',
    'marzo',
    'abril',
    'mayo',
    'junio',
    'julio',
    'agosto',
    'septiembre',
    'octubre',
    'noviembre',
    'diciembre',
  ];

  /*
   * Código de la copia en el mismo formato que usa copia-card:
   * la copia 1 se muestra como "C-001".
   */
  get codigo(): string {
    const numero = String(this.idCopia).padStart(3, '0');
    return 'C-' + numero;
  }

  /* Título de la sección: "Historial de custodia de la copia C-001" */
  get titulo(): string {
    return 'Historial de custodia de la copia ' + this.codigo;
  }

  /*
   * Convierte la lista de custodias en la lista de hitos que se dibuja.
   * Primero ordena las custodias de la más antigua a la más reciente,
   * así la línea de tiempo siempre queda en orden cronológico.
   */
  get hitos(): Hito[] {
    const ordenadas = this.custodias.slice();
    ordenadas.sort(this.compararPorFechaInicio);

    const lista: Hito[] = [];

    for (const custodia of ordenadas) {
      const vigente = !custodia.fechaFin;

      lista.push({
        id: custodia.idCustodia,
        institucion: custodia.institucion.nombre,
        periodo: this.armarPeriodo(custodia),
        condiciones: this.armarCondiciones(custodia),
        vigente: vigente,
      });
    }

    return lista;
  }

  /*
   * Función de comparación para sort(): devuelve un número negativo si "a"
   * va antes que "b", cero si empatan y positivo si va después.
   * Las fechas son texto 'AAAA-MM-DD', y comparadas como texto ya quedan
   * en orden cronológico.
   */
  private compararPorFechaInicio(a: Custodia, b: Custodia): number {
    return a.fechaInicio.localeCompare(b.fechaInicio);
  }

  /*
   * Texto del periodo:
   *   - custodia pasada: "Marzo 2019 – Agosto 2019"
   *   - custodia vigente: "Agosto 2019 – hoy (vigente)"
   */
  private armarPeriodo(custodia: Custodia): string {
    const inicio = this.formatearMes(custodia.fechaInicio);

    if (custodia.fechaFin) {
      return inicio + ' – ' + this.formatearMes(custodia.fechaFin);
    }

    return inicio + ' – hoy (vigente)';
  }

  /* Texto de las condiciones de almacenamiento; si faltan, lo dice. */
  private armarCondiciones(custodia: Custodia): string {
    if (custodia.condicionAlmacenamiento) {
      return 'Condiciones: ' + custodia.condicionAlmacenamiento;
    }
    return 'Condiciones: sin registrar';
  }

  /*
   * Convierte '2019-03-12' en 'Marzo 2019'.
   * split('-') parte el texto en tres pedazos: año, mes y día.
   * El mes llega como '03', así que se convierte a número y se le resta 1
   * para usarlo como posición en la lista de meses (enero es la posición 0).
   */
  private formatearMes(fecha: string): string {
    const partes = fecha.split('-');
    if (partes.length !== 3) {
      return fecha;
    }

    const posicion = Number(partes[1]) - 1;
    const nombre = this.meses[posicion];
    if (!nombre) {
      return fecha;
    }

    const nombreConMayuscula = nombre.charAt(0).toUpperCase() + nombre.slice(1);
    return nombreConMayuscula + ' ' + partes[0];
  }
}
