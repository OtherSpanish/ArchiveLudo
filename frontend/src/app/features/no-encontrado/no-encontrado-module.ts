import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { NoEncontradoComponent } from './no-encontrado/no-encontrado-component';

/*
 * La ruta "**" coincide con cualquier URL que ninguna otra ruta reconoció.
 * Sin authGuard a propósito: el 404 se puede ver sin sesión.
 *
 * IMPORTANTE: este módulo debe ser el ÚLTIMO en la lista "imports" del
 * AppModule. Angular revisa las rutas en el orden en que se registran, y
 * "**" acepta todo, así que si estuviera antes, taparía las pantallas que
 * vienen después.
 */
const routes: Routes = [{ path: '**', component: NoEncontradoComponent }];

@NgModule({
  declarations: [NoEncontradoComponent],
  imports: [CommonModule, RouterModule.forChild(routes)],
})
export class NoEncontradoModule {}
