import { Component, Input } from '@angular/core';
import { Experto, nombreCompletoExperto } from '../../models/experto-model';
import { Participacion } from '../../models/participacion-model';
import { ProyectoDeRestauracion } from '../../models/proyecto-model';

/**
 * Una fila de la lista de expertos de la tarjeta, ya con los textos listos.
 *  - nombre: el nombre armado con las cuatro partes (o "Experto #id" si el
 *    experto no está en el catálogo).
 *  - detalle: "Rol · desde 01/09/2026" o "Rol · desde 01/09/2026 · salió el 15/10/2026".
 */
interface FilaExperto {
  id: number;
  nombre: string;
  detalle: string;
}

/**
 * Tarjeta de un proyecto de restauración, para la pestaña Proyectos
 * de la ficha de videojuego y para la pantalla Proyectos.
 *
 * Uso:
 *   <app-proyecto-card [proyecto]="p" [expertos]="catalogoExpertos"></app-proyecto-card>
 *
 * Recibe dos datos del componente padre:
 *   - proyecto: el proyecto con sus participaciones adentro
 *   - expertos: el catálogo de GET /api/expertos, para mostrar el nombre de
 *     cada experto (la participación solo trae su idExperto)
 */
@Component({
  selector: 'app-proyecto-card',
  templateUrl: './proyecto-card-component.html',
  styleUrl: './proyecto-card-component.css',
  standalone: false
})
export class ProyectoCardComponent {

  /** El "!" le dice a TypeScript: "el padre siempre me va a pasar este valor". */
  @Input() proyecto!: ProyectoDeRestauracion;

  /** Catálogo de expertos. Puede estar vacío mientras carga. */
  @Input() expertos: Experto[] = [];

  /** Código del proyecto en el mismo formato que las copias: el proyecto 1 es "P-001". */
  get codigo(): string {
    return 'P-' + String(this.proyecto.idProyecto).padStart(3, '0');
  }

  /**
   * Texto del período del proyecto:
   *  - sin fecha de fin (activo o pausado): "Desde 05/03/2024"
   *  - con fecha de fin: "05/03/2024 – 20/09/2024"
   */
  get periodo(): string {
    const inicio = this.formatearFecha(this.proyecto.fechaInicio);
    const fin = this.proyecto.fechaFin;

    if (fin) {
      return inicio + ' – ' + this.formatearFecha(fin);
    }
    return 'Desde ' + inicio;
  }

  /** Texto de expertos, en singular o plural: "1 experto" / "3 expertos" */
  get textoExpertos(): string {
    const cantidad = this.proyecto.participaciones.length;

    if (cantidad === 1) {
      return '1 experto';
    }
    return cantidad + ' expertos';
  }

  /** Una fila por participación, con el nombre buscado en el catálogo. */
  get filasExpertos(): FilaExperto[] {
    const filas: FilaExperto[] = [];

    for (const participacion of this.proyecto.participaciones) {
      filas.push({
        id: participacion.idParticipacion,
        nombre: this.buscarNombre(participacion.idExperto),
        detalle: this.armarDetalle(participacion)
      });
    }

    return filas;
  }

  /** Busca el experto por id y arma su nombre; si no está, usa "Experto #id". */
  private buscarNombre(idExperto: number): string {
    for (const experto of this.expertos) {
      if (experto.idExperto === idExperto) {
        return nombreCompletoExperto(experto);
      }
    }
    return 'Experto #' + idExperto;
  }

  /** "Rol · desde dd/mm/aaaa" y, si ya salió, " · salió el dd/mm/aaaa". */
  private armarDetalle(participacion: Participacion): string {
    let texto = participacion.rolExperto + ' · desde ' + this.formatearFecha(participacion.fechaIncorporacion);

    if (participacion.fechaSalida) {
      texto = texto + ' · salió el ' + this.formatearFecha(participacion.fechaSalida);
    }

    return texto;
  }

  /**
   * Convierte '2024-03-05' en '05/03/2024'.
   * split('-') parte el texto en tres pedazos: año, mes y día.
   */
  formatearFecha(fecha: string): string {
    const partes = fecha.split('-');
    if (partes.length !== 3) {
      return fecha;
    }
    return partes[2] + '/' + partes[1] + '/' + partes[0];
  }
}
