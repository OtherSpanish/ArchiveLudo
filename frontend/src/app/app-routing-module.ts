import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { authGuard } from './core/auth-guard';
import { roleGuard } from './core/role-guard';
import { InicioComponent } from './features/inicio/inicio-component';
import { FichaVideojuegoComponent } from './features/videojuego/ficha-videojuego/ficha-videojuego-component';
import { RegistrarCopiaComponent } from './features/videojuego/registrar-copia/registrar-copia-component';
import { FormularioVideojuegoComponent } from './features/videojuego/formulario-videojuego/formulario-videojuego-component';

/**
 * Rutas principales de la app. Catálogo, Instituciones y Login declaran las
 * suyas dentro de sus propios módulos.
 *
 * canActivate pone guards en la ruta; se ejecutan en orden:
 *  - authGuard: exige tener sesión iniciada (si no, lleva a /login).
 *  - roleGuard: exige uno de los roles de "data.roles" (si no, lleva al Inicio).
 *
 *  - /                              -> Inicio
 *  - /videojuegos/:id               -> ficha del videojuego
 *  - /videojuegos/nuevo             -> formulario para crear un videojuego (curador y administrador)
 *  - /videojuegos/:id/editar        -> formulario para editar un videojuego (curador y administrador)
 *  - /videojuegos/:id/copias/nueva  -> formulario para registrar una copia
 *  - /copias/:idCopia/editar        -> el mismo formulario, para editar una copia
 *                                      (solo curador y administrador)
 */
const routes: Routes = [
  {
    path: '',
    component: InicioComponent,
    canActivate: [authGuard]
  },
  {
    // OJO: 'videojuegos/nuevo' va ANTES de 'videojuegos/:id'; si no, Angular
    // tomaría la palabra "nuevo" como si fuera un id.
    path: 'videojuegos/nuevo',
    component: FormularioVideojuegoComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['curador', 'administrador'] }
  },
  {
    path: 'videojuegos/:id/editar',
    component: FormularioVideojuegoComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['curador', 'administrador'] }
  },
  {
    path: 'videojuegos/:id',
    component: FichaVideojuegoComponent,
    canActivate: [authGuard]
  },
  {
    path: 'videojuegos/:id/copias/nueva',
    component: RegistrarCopiaComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['curador', 'administrador'] }
  },
  {
    // Editar una copia existente. Usa el mismo formulario que registrar.
    path: 'copias/:idCopia/editar',
    component: RegistrarCopiaComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['curador', 'administrador'] }
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
