import { Component } from '@angular/core';
import { Router } from '@angular/router';

interface EnlaceNavegacion {
  etiqueta: string;
  ruta: string;
}

@Component({
  selector: 'app-barra-superior',
  templateUrl: './barra-superior.html',
  styleUrl: './barra-superior.css',
  standalone: false
})
export class BarraSuperior {

  enlaces: EnlaceNavegacion[] = [
    { etiqueta: 'Inicio', ruta: '/' },
    { etiqueta: 'Catálogo', ruta: '/catalogo' },
    { etiqueta: 'En riesgo', ruta: '/en-riesgo' },
    { etiqueta: 'Proyectos', ruta: '/proyectos' },
    { etiqueta: 'Instituciones', ruta: '/instituciones' },
    { etiqueta: 'Reportes', ruta: '/reportes' }
  ];

  // Por ahora fijo; luego vendrá del login (JWT)
  rolUsuario: string = 'Curador';

  constructor(private router: Router) { }

  buscar(texto: string): void {
    const textoLimpio = texto.trim();
    if (textoLimpio.length === 0) {
      return;
    }
    this.router.navigate(['/catalogo'], { queryParams: { q: textoLimpio } });
  }
}
