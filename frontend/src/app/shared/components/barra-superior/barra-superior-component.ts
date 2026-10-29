import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth-service';
import { ETIQUETAS_ROL, Sesion } from '../../models/sesion-model';

interface EnlaceNavegacion {
  etiqueta: string;
  ruta: string;
}

/*
 * Barra superior de toda la app: logo, navegación, buscador y datos del
 * usuario. Solo se dibuja cuando hay una sesión iniciada; en /login no aparece.
 */
@Component({
  selector: 'app-barra-superior',
  templateUrl: './barra-superior-component.html',
  styleUrl: './barra-superior-component.css',
  standalone: false
})
export class BarraSuperiorComponent {

  enlaces: EnlaceNavegacion[] = [
    { etiqueta: 'Inicio', ruta: '/' },
    { etiqueta: 'Catálogo', ruta: '/catalogo' },
    { etiqueta: 'En riesgo', ruta: '/en-riesgo' },
    { etiqueta: 'Proyectos', ruta: '/proyectos' },
    { etiqueta: 'Instituciones', ruta: '/instituciones' },
    { etiqueta: 'Reportes', ruta: '/reportes' }
  ];

  /*
   * Router cambia de pantalla; AuthService guarda quién tiene la sesión.
   * Angular entrega las dos solas (inyección de dependencias).
   */
  constructor(
    private router: Router,
    private auth: AuthService
  ) { }

  /*
   * La sesión actual, o null si nadie ha entrado. Lee el signal de
   * AuthService, así que la barra se actualiza sola cuando alguien inicia
   * o cierra sesión, aunque lo haga otra pantalla.
   */
  get sesion(): Sesion | null {
    return this.auth.sesion();
  }

  /* Texto del rol para mostrar: 'curador' -> 'Curador' */
  get textoRol(): string {
    const actual = this.auth.sesion();
    if (actual === null) {
      return '';
    }
    return ETIQUETAS_ROL[actual.rol];
  }

  buscar(texto: string): void {
    const textoLimpio = texto.trim();
    if (textoLimpio.length === 0) {
      return;
    }
    this.router.navigate(['/catalogo'], { queryParams: { q: textoLimpio } });
  }

  /* Se ejecuta al pulsar "Cerrar sesión": borra la sesión y lleva al login. */
  cerrarSesion(): void {
    this.auth.cerrarSesion();
    this.router.navigate(['/login']);
  }
}
