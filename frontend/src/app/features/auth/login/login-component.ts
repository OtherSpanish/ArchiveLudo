import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/auth-service';
import { CredencialesLogin } from '../../../shared/models/sesion-model';

/* Una cuenta de prueba que se muestra como ayuda en la pantalla (solo hardcode). */
interface CuentaPrueba {
  nombreUsuario: string;
  rol: string;
}

/*
 * Pantalla de inicio de sesión (/login). Es la única ruta pública.
 * El login lo hace AuthService: con el hardcode busca en cuentas de prueba
 * y con el back llama a POST /api/auth/login. Esta pantalla no cambia entre
 * las dos versiones.
 */
@Component({
  selector: 'app-login',
  templateUrl: './login-component.html',
  styleUrl: './login-component.css',
  standalone: false
})
export class LoginComponent implements OnInit {

  /* El formulario con usuario y contraseña. Se arma en ngOnInit. */
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

  // ==================================================================
  // ===== HARDCODE: borrar este bloque al conectar el back =====
  // (incluye cuentasPrueba; el HTML muestra la ayuda solo si la lista
  //  tiene elementos, así que no hay que tocarlo)
  // ==================================================================

  /* Cuentas de prueba que existen en AuthService. Todas con la contraseña 1234. */
  cuentasPrueba: CuentaPrueba[] = [
    { nombreUsuario: 'consulta', rol: 'Consulta' },
    { nombreUsuario: 'curador', rol: 'Curador' },
    { nombreUsuario: 'admin', rol: 'Administrador' }
  ];

  // ==================================================================
  // ===== FIN DEL HARDCODE =====
  // ==================================================================


  // ==================================================================
  // ===== BACK: descomentar esta línea al conectar =====
  // (con el back no hay cuentas de prueba, así que la lista queda vacía)
  // ==================================================================

  // cuentasPrueba: CuentaPrueba[] = [];

  // ==================================================================
  // ===== FIN DEL BLOQUE BACK =====
  // ==================================================================

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
      nombreUsuario: ['', Validators.required],
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
    this.errorLogin = 'Usuario o contraseña incorrectos.';
    this.formulario.controls['contrasenia'].setValue('');

    this.cambios.markForCheck();
  }
}
