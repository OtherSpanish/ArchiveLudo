import { Directive, Input, TemplateRef, ViewContainerRef, effect } from '@angular/core';
import { AuthService } from '../../core/auth-service';
import { Rol } from '../models/sesion-model';

/*
 * Directiva estructural: dibuja un elemento solo si la persona con sesión
 * tiene alguno de los roles indicados.
 *
 * Uso en un HTML:
 *   <a *appHasRole="['curador', 'administrador']">Registrar copia</a>
 *
 * El "*" le dice a Angular que este elemento es una plantilla que la
 * directiva puede dibujar o quitar. Es lo mismo que hace @if, pero
 * decidiendo por el rol.
 *
 * OJO: esto solo oculta botones para que la interfaz sea coherente. La
 * protección real de las acciones la hace el back, que debe rechazar con
 * 403 lo que el rol no permite.
 */
@Directive({
  selector: '[appHasRole]',
  standalone: false
})
export class HasRoleDirective {

  /* Los roles que pueden ver el elemento. Llegan desde el HTML. */
  private roles: Rol[] = [];

  /* true si el elemento está dibujado ahora mismo. Evita dibujarlo dos veces. */
  private mostrado: boolean = false;

  /*
   * TemplateRef es el elemento que lleva el "*appHasRole" (la plantilla).
   * ViewContainerRef es el lugar de la pantalla donde se dibuja o se quita.
   * AuthService sabe quién tiene la sesión.
   * Angular entrega las tres solas (inyección de dependencias).
   */
  constructor(
    private plantilla: TemplateRef<unknown>,
    private contenedor: ViewContainerRef,
    private auth: AuthService
  ) {
    /*
     * effect() ejecuta esta función ahora y otra vez cada vez que cambia algo
     * del signal de la sesión que se lea dentro (auth.tieneRol lo lee). Así el
     * elemento aparece o desaparece al iniciar o cerrar sesión.
     */
    effect(() => this.actualizar());
  }

  /* Recibe la lista de roles del HTML: *appHasRole="['curador']" */
  @Input() set appHasRole(roles: Rol[]) {
    this.roles = roles;
    this.actualizar();
  }

  /* Dibuja el elemento si el rol está permitido y no estaba, o lo quita si ya no. */
  private actualizar(): void {
    const permitido = this.auth.tieneRol(this.roles);

    if (permitido && !this.mostrado) {
      this.contenedor.createEmbeddedView(this.plantilla);
      this.mostrado = true;
    } else if (!permitido && this.mostrado) {
      this.contenedor.clear();
      this.mostrado = false;
    }
  }
}
