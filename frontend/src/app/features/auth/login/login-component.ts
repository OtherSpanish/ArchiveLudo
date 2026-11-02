import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/auth-service';
import { CredencialesLogin } from '../../../shared/models/sesion-model';

/* Una cuenta de prueba que se muestra como ayuda en la pantalla (el HTML la dibuja solo si la lista tiene elementos). */
interface CuentaPrueba {
  nombreUsuario: string;
  rol: string;
}

/*
 * Pantalla de inicio de sesión (/login). Es la única ruta pública.
 * El login lo hace AuthService, que llama a POST /api/auth/login.
 * Se entra con el CORREO y la contraseña.
 */
@Component({
  selector: 'app-login',
  templateUrl: './login-component.html',
  styleUrl: './login-component.css',
  standalone: false
})
export class LoginComponent implements OnInit {

  /* El formulario con correo y contraseña. Se arma en ngOnInit. */
  formulario!: FormGroup;

  /*
   * true después del primer intento de enviar. Sirve para mostrar los errores
   * de los campos que el usuario nunca tocó.
   */
  enviado: boolean = false;

  /* true mientras se espera la respuesta; deshabilita el botón de entrar. */
  iniciando: boolean = false;

  /* Mensaje si las credenciales no son válidas. Vacío si no hay error. */
  errorLogin: string = '';

  /*
   * Dirección a la que se vuelve después de entrar. Es la que el guard guardó
   * en el parámetro "volverA" de la URL; si no hay ninguna, el Inicio.
   */
  private destino: string = '/';

  /*
   * Cuentas de prueba para mostrar como ayuda. Con el back no hay: la lista
   * queda vacía y el HTML no dibuja el recuadro.
   */
  cuentasPrueba: CuentaPrueba[] = [];

  /*
   * Angular entrega estas cuatro herramientas solas (inyección de dependencias):
   * FormBuilder arma el formulario, AuthService hace el login, ActivatedRoute
   * lee la URL, Router cambia de pantalla y ChangeDetectorRef avisa que hay
   * que redibujar.
   */
  constructor(
    private constructorFormulario: FormBuilder,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private cambios: ChangeDetectorRef
  ) { }

  /*
   * ngOnInit se ejecuta una vez, cuando el componente ya está listo.
   * Si ya hay sesión, no tiene sentido ver el login: va directo al destino.
   */
  ngOnInit(): void {
    this.formulario = this.constructorFormulario.group({
      correo: ['', Validators.required],
      contrasenia: ['', Validators.required]
    });

    this.destino = this.leerDestino(this.route.snapshot.queryParamMap.get('volverA'));

    if (this.auth.estaAutenticado()) {
      this.router.navigateByUrl(this.destino);
    }
  }

  /*
   * Solo acepta destinos internos de la app: deben empezar con "/" y no con
   * "//" (que el navegador entendería como otro sitio web). Si la URL trae
   * cualquier otra cosa, se ignora y se va al Inicio.
   */
  private leerDestino(valor: string | null): string {
    if (valor && valor.startsWith('/') && !valor.startsWith('//')) {
      return valor;
    }
    return '/';
  }

  /* Dice si hay que mostrar el error de un campo: tiene error Y (lo tocaron O ya se intentó enviar). */
  mostrarError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /*
   * Se ejecuta al pulsar "Entrar". Si faltan datos, muestra los errores. Si
   * están completos, pide el login y espera la respuesta.
   */
  entrar(): void {
    this.enviado = true;
    this.errorLogin = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.iniciando = true;

    const credenciales = this.formulario.getRawValue() as CredencialesLogin;

    this.auth.iniciarSesion(credenciales).subscribe({
      next: () => this.alEntrar(),
      error: (error) => this.alFallar(error)
    });
  }

  /* Login correcto: la sesión ya quedó guardada en AuthService; se va al destino. */
  private alEntrar(): void {
    this.iniciando = false;
    this.router.navigateByUrl(this.destino);
  }

  /* Login fallido: se muestra el mensaje y se vacía la contraseña. */
  private alFallar(error: unknown): void {
    console.error('No se pudo iniciar sesión', error);

    this.iniciando = false;
    this.errorLogin = this.textoDelError(error);
    this.formulario.controls['contrasenia'].setValue('');

    this.cambios.markForCheck();
  }

  /*
   * Elige el mensaje según lo que respondió el back:
   *  - estado 0: la petición ni llegó (back apagado, o la app abierta en
   *    127.0.0.1 en vez de localhost y el navegador la bloqueó por CORS)
   *  - 400: el back explica qué falta, en el campo "mensaje"
   *  - 500 o más: falla del servidor
   *  - cualquier otro caso (el 401 normal): credenciales incorrectas
   */
  private textoDelError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'No se pudo conectar con el servidor. Revisa que el back esté encendido y que abras la app en localhost:4200.';
      }
      if (error.status === 400 && error.error && error.error.mensaje) {
        return error.error.mensaje;
      }
      if (error.status >= 500) {
        return 'El servidor tuvo un problema. Inténtalo más tarde.';
      }
    }
    return 'Correo o contraseña incorrectos.';
  }
}
