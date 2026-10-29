import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

import { LoginComponent } from './login/login-component';

/*
 * Módulo de autenticación. Por ahora solo tiene la pantalla de login.
 * /login es la única ruta pública de la app: no lleva guards.
 * ReactiveFormsModule da las herramientas del formulario.
 */
const routes: Routes = [{ path: 'login', component: LoginComponent }];

@NgModule({
  declarations: [LoginComponent],
  imports: [CommonModule, RouterModule.forChild(routes), ReactiveFormsModule],
  exports: [LoginComponent],
})
export class AuthModule {}
