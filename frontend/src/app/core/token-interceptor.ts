import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth-service';

/**
 * Interceptor del token: se ejecuta con CADA petición HTTP de la app.
 *  - Si hay sesión, agrega la cabecera "Authorization: Bearer <token>".
 *    El login es la excepción: todavía no hay token y el back no lo pide.
 *  - Si el back responde 401 (token vencido o inválido), cierra la sesión y
 *    lleva al login, guardando en "volverA" la pantalla donde estaba.
 *    Un 401 del propio login no cierra nada: solo significa credenciales malas.
 *  - El 403 (rol sin permiso) NO cierra la sesión: la sesión sigue siendo
 *    válida, y cada pantalla decide qué mensaje mostrar.
 *
 * Es una función (como authGuard), no una clase. inject() pide a Angular los
 * servicios que necesita, igual que un constructor.
 */
export const tokenInterceptor: HttpInterceptorFn = function (peticion, siguiente) {
  const auth = inject(AuthService);
  const router = inject(Router);

  const esLogin = peticion.url.includes('/auth/login');

  let peticionFinal = peticion;
  if (auth.token !== '' && !esLogin) {
    // Las peticiones son inmutables: clone() devuelve una copia con la cabecera agregada.
    peticionFinal = peticion.clone({
      setHeaders: { Authorization: 'Bearer ' + auth.token }
    });
  }

  return siguiente(peticionFinal).pipe(
    catchError(function (error: HttpErrorResponse) {
      if (error.status === 401 && !esLogin) {
        const paginaActual = router.url;
        auth.cerrarSesion();
        router.navigate(['/login'], { queryParams: { volverA: paginaActual } });
      }
      // El error se vuelve a lanzar para que la pantalla que pidió los datos también se entere.
      return throwError(() => error);
    })
  );
};
