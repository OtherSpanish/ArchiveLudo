import { Component } from '@angular/core';

/*
 * Pantalla que se muestra cuando la URL no coincide con ninguna ruta
 * (por ejemplo /algo-que-no-existe). No necesita datos: solo avisa y
 * ofrece volver al inicio.
 *
 * Si la persona no tiene sesión, "Volver al inicio" pasa por el authGuard
 * y la lleva a /login.
 */
@Component({
  selector: 'app-no-encontrado',
  templateUrl: './no-encontrado-component.html',
  styleUrl: './no-encontrado-component.css',
  standalone: false,
})
export class NoEncontradoComponent {}
