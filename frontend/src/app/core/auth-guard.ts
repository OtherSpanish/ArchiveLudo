import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth-service';

/*
 * Guard de autenticación: deja pasar solo a quien tiene sesión iniciada.
 * Si no la tiene, lo manda a /login y guarda en "volverA" la dirección a la
 * que quería entrar, para llevarlo allí después de iniciar sesión.
 *
 * Se usa en una ruta así:  { path: 'catalogo', component: ..., canActivate: [authGuard] }
 *
 * inject() pide a Angular los servicios que necesita, igual que un constructor.
 */
export const authGuard: CanActivateFn = function (ruta, estado) {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.estaAutenticado()) {
    return true;
  }

  return router.createUrlTree(['/login'], { queryParams: { volverA: estado.url } });
};
