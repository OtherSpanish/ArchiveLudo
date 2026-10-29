import { Component, Input } from '@angular/core';
import { ProyectoConDetalle } from '../../models/api-model';

/*
 * Tarjeta de un proyecto de restauración, para la pestaña Proyectos
 * de la ficha de videojuego.
 *
 * Uso:
 *   <app-proyecto-card [detalle]="item"></app-proyecto-card>
 *
 * Recibe un solo dato del componente padre:
 *   - detalle: el proyecto junto con sus expertos
 *     (el tipo ProyectoConDetalle de api-model.ts)
 */
@Component({
  selector: 'app-proyecto-card',
  templateUrl: './proyecto-card-component.html',
  styleUrl: './proyecto-card-component.css',
  standalone: false
})
export class ProyectoCardComponent {

  /* El "!" le dice a TypeScript: "el padre siempre me va a pasar este valor". */
  @Input() detalle!: ProyectoConDetalle;

  /* Código del proyecto en el mismo formato que las copias: el proyecto 1 es "P-001". */
  get codigo(): string {
    return 'P-' + String(this.detalle.proyecto.idProyecto).padStart(3, '0');
  }

  /*
   * Texto del período del proyecto:
   *  - sin fecha de fin (proyecto activo): "Desde 05/03/2024"
   *  - con fecha de fin: "05/03/2024 – 20/09/2024"
   */
  get periodo(): string {
    const inicio = this.formatearFecha(this.detalle.proyecto.fechaInicio);
    const fin = this.detalle.proyecto.fechaFin;

    if (fin) {
      return inicio + ' – ' + this.formatearFecha(fin);
    }
    return 'Desde ' + inicio;
  }

  /* Texto de expertos, en singular o plural: "1 experto" / "3 expertos" */
  get textoExpertos(): string {
    const cantidad = this.detalle.expertos.length;

    if (cantidad === 1) {
      return '1 experto';
    }
    return cantidad + ' expertos';
  }

  /*
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
