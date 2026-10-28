import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared-module';
import { Catalogo } from './catalogo';

/*
 * Rutas de esta pantalla. "forChild" las suma a las rutas generales
 * de la app (las que están en app-routing-module.ts).
 */
const routes: Routes = [{ path: 'catalogo', component: Catalogo }];

@NgModule({
  declarations: [Catalogo],
  imports: [CommonModule, RouterModule.forChild(routes), SharedModule],
  exports: [Catalogo],
})
export class CatalogoModule {}
