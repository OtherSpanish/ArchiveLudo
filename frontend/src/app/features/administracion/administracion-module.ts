import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

import { authGuard } from '../../core/auth-guard';
import { roleGuard } from '../../core/role-guard';
import { SharedModule } from '../../shared/shared-module';
import { AdminEditorasComponent } from './admin-editoras/admin-editoras-component';
import { AdminInstitucionesComponent } from './admin-instituciones/admin-instituciones-component';
import { AdminEstudiosComponent } from './admin-estudios/admin-estudios-component';
import { AdminUsuariosComponent } from './admin-usuarios/admin-usuarios-component';
import { AdminExpertosComponent } from './admin-expertos/admin-expertos-component';

/*
 * Pantallas de administración (catálogos que usa el formulario de videojuegos).
 * Solo curador y administrador (authGuard + roleGuard):
 *  - /administracion/estudios  -> lista y formulario de estudios
 *  - /administracion/editoras  -> lista y formulario de compañías editoras
 *  - /administracion/instituciones -> lista y formulario de instituciones
 *  - /administracion/expertos -> lista y formulario de expertos (eliminar: solo administrador)
 *  - /administracion/usuarios -> lista y formulario de usuarios (SOLO administrador)
 */
const routes: Routes = [
  {
    path: 'administracion/estudios',
    component: AdminEstudiosComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['curador', 'administrador'] }
  },
  {
    path: 'administracion/instituciones',
    component: AdminInstitucionesComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['curador', 'administrador'] }
  },
  {
    path: 'administracion/editoras',
    component: AdminEditorasComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['curador', 'administrador'] }
  },
  {
    path: 'administracion/expertos',
    component: AdminExpertosComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['curador', 'administrador'] }
  },
  {
    path: 'administracion/usuarios',
    component: AdminUsuariosComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['administrador'] }
  }
];

@NgModule({
  declarations: [
    AdminEstudiosComponent,
    AdminEditorasComponent,
    AdminInstitucionesComponent,
    AdminExpertosComponent,
    AdminUsuariosComponent
  ],
  imports: [CommonModule, RouterModule.forChild(routes), ReactiveFormsModule, SharedModule]
})
export class AdministracionModule {}
