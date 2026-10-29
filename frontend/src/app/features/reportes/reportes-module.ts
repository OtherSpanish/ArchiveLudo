import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { authGuard } from '../../core/auth-guard';
import { ReportesComponent } from './reportes/reportes-component';

/*
 * Ruta de esta pantalla. Cualquier persona con sesión puede consultar
 * los reportes, por eso solo lleva authGuard (sin roleGuard).
 */
const routes: Routes = [
  { path: 'reportes', component: ReportesComponent, canActivate: [authGuard] },
];

/*
 * Módulo de la pantalla "Reportes".
 * ReactiveFormsModule es para el formulario del reporte; no usa nada del
 * SharedModule porque dibuja su propia tabla.
 */
@NgModule({
  declarations: [ReportesComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)],
})
export class ReportesModule {}
