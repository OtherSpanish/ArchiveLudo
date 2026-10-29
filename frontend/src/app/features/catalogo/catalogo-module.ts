import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { authGuard } from '../../core/auth-guard';
import { SharedModule } from '../../shared/shared-module';
import { CatalogoComponent } from './catalogo-component';

/*
 * Rutas de esta pantalla. "forChild" las suma a las rutas generales
 * de la app (las que están en app-routing-module.ts).
 * authGuard exige tener sesión iniciada para ver el catálogo.
 */
const routes: Routes = [
  { path: 'catalogo', component: CatalogoComponent, canActivate: [authGuard] },
];

@NgModule({
  declarations: [CatalogoComponent],
  imports: [CommonModule, RouterModule.forChild(routes), SharedModule],
  exports: [CatalogoComponent],
})
export class CatalogoModule {}
