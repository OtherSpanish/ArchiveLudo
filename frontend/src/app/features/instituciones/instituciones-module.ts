import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { authGuard } from '../../core/auth-guard';
import { SharedModule } from '../../shared/shared-module';
import { FichaInstitucionComponent } from './ficha-institucion/ficha-institucion-component';
import { ListaInstitucionesComponent } from './lista-instituciones/lista-instituciones-component';

/*
 * Rutas de las pantallas de instituciones, ambas con authGuard
 * (exigen tener sesión iniciada):
 *  - /instituciones      -> lista de todas las instituciones
 *  - /instituciones/:id  -> ficha de una institución
 */
const routes: Routes = [
  { path: 'instituciones', component: ListaInstitucionesComponent, canActivate: [authGuard] },
  { path: 'instituciones/:id', component: FichaInstitucionComponent, canActivate: [authGuard] }
];

@NgModule({
  declarations: [
    ListaInstitucionesComponent,
    FichaInstitucionComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    SharedModule
  ],
  exports: [
    ListaInstitucionesComponent,
    FichaInstitucionComponent
  ]
})
export class InstitucionesModule { }
