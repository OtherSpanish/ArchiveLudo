import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../shared/models/sesion-model';
import { AuthService } from './auth-service';

/*
 * Guard de rol: deja pasar solo a quien tiene uno de los roles que la ruta
 * declara en su "data". Si no lo tiene, lo manda al Inicio.
 *
 * Se usa en una ruta así:
 *   {
 *     path: 'videojuegos/:id/copias/nueva',
 *     component: ...,
 *     canActivate: [authGuard, roleGuard],
 *     data: { roles: ['curador', 'administrador'] }
 *   }
 *
 * Va siempre DESPUÉS de authGuard, que ya revisó que haya sesión.
 */
export const roleGuard: CanActivateFn = function (ruta) {
  const auth = inject(AuthService);
  const router = inject(Router);

  const rolesPermitidos = ruta.data['roles'] as Rol[];

  if (auth.tieneRol(rolesPermitidos)) {
    return true;
  }

  return router.createUrlTree(['/']);
};
