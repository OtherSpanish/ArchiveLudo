import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared-module';
import { authGuard } from '../../core/auth-guard';
import { EnRiesgoComponent } from './en-riesgo/en-riesgo-component';

/*
 * Ruta de esta pantalla. Cualquier persona con sesión puede verla,
 * por eso solo lleva authGuard (sin roleGuard).
 */
const routes: Routes = [
  { path: 'en-riesgo', component: EnRiesgoComponent, canActivate: [authGuard] },
];

/*
 * Módulo de la pantalla "En riesgo".
 * SharedModule trae <app-juego-card>; RouterModule.forChild registra la ruta.
 */
@NgModule({
  declarations: [EnRiesgoComponent],
  imports: [CommonModule, SharedModule, RouterModule.forChild(routes)],
})
export class EnRiesgoModule {}
