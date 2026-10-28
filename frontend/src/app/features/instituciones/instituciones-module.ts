import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared-module';
import { FichaInstitucion } from './ficha-institucion/ficha-institucion';

/*
 * Rutas de esta pantalla. "forChild" las suma a las rutas generales
 * de la app (las que están en app-routing-module.ts).
 * En /instituciones/2, el ":id" vale 2.
 */
const routes: Routes = [{ path: 'instituciones/:id', component: FichaInstitucion }];

@NgModule({
  declarations: [FichaInstitucion],
  imports: [CommonModule, RouterModule.forChild(routes), SharedModule],
  exports: [FichaInstitucion],
})
export class InstitucionesModule {}
