import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared-module';
import { authGuard } from '../../core/auth-guard';
import { ProyectosComponent } from './proyectos/proyectos-component';

/*
 * Ruta de esta pantalla. Cualquier persona con sesión puede verla,
 * por eso solo lleva authGuard (sin roleGuard).
 */
const routes: Routes = [
  { path: 'proyectos', component: ProyectosComponent, canActivate: [authGuard] },
];

/*
 * Módulo de la pantalla "Proyectos".
 * SharedModule trae <app-proyecto-card>; RouterModule.forChild registra la
 * ruta y además deja usar routerLink en el HTML.
 */
@NgModule({
  declarations: [ProyectosComponent],
  imports: [CommonModule, SharedModule, RouterModule.forChild(routes)],
})
export class ProyectosModule {}
