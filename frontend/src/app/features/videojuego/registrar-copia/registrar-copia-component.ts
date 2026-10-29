import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Videojuego } from '../../../shared/models/videojuego-model';
import { CopiaNueva, EstadoIntegridad, TipoCopia } from '../../../shared/models/copia-model';

// ===== BACK: descomentar esta línea al conectar =====
// import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

/* Una opción de los botones de selección: el valor que se guarda y el texto que se ve. */
interface Opcion<T> {
  valor: T;
  etiqueta: string;
}

/*
 * Pantalla para registrar una copia nueva de un videojuego
 * (/videojuegos/:id/copias/nueva).
 *
 * El formulario cambia según el tipo de copia (generalización del MER):
 *   - física:  soporte, número de serie, ubicación
 *   - digital: formato, tamaño, hash
 * Registrar la copia NO crea su custodia; eso es una acción aparte.
 */
@Component({
  selector: 'app-registrar-copia',
  templateUrl: './registrar-copia-component.html',
  styleUrl: './registrar-copia-component.css',
  standalone: false
})
export class RegistrarCopiaComponent implements OnInit {

  /* Videojuego al que se le agrega la copia. Sin valor hasta que se carga. */
  videojuego?: Videojuego;

  /* Id que viene en la URL (/videojuegos/8/copias/nueva -> 8). */
  idVideojuego: number = 0;

  /* true mientras se espera el videojuego. Evita mostrar "no encontrado" antes de tiempo. */
  cargando: boolean = true;

  /* El formulario. Se arma en ngOnInit (ver crearFormulario). */
  formulario!: FormGroup;

  /*
   * true después del primer intento de enviar. Sirve para mostrar los errores
   * de los campos que el usuario nunca tocó.
   */
  enviado: boolean = false;

  /* true mientras se espera la respuesta del back; deshabilita el botón de guardar. */
  guardando: boolean = false;

  /* Mensaje si el back responde un error al guardar. Vacío si no hay error. */
  errorEnvio: string = '';

  /* Fecha máxima del campo de fecha: hoy. No se puede registrar una copia en el futuro. */
  hoy: string = this.fechaHoy();

  /* Opciones del tipo de copia, en el orden en que se muestran. */
  opcionesTipo: Opcion<TipoCopia>[] = [
    { valor: 'fisica', etiqueta: 'Física' },
    { valor: 'digital', etiqueta: 'Digital' }
  ];

  /* Opciones del estado de integridad (mismos valores que la tabla COPIA). */
  opcionesEstado: Opcion<EstadoIntegridad>[] = [
    { valor: 'buena', etiqueta: 'Buena' },
    { valor: 'deteriorada', etiqueta: 'Deteriorada' },
    { valor: 'dañada', etiqueta: 'Dañada' },
    { valor: 'irrecuperable', etiqueta: 'Irrecuperable' }
  ];

  /* Campos que solo existen en una copia física, y cuáles de ellos son obligatorios. */
  private camposFisica: string[] = ['tipoSoporte', 'numeroSerie', 'ubicacionFisica'];
  private obligatoriosFisica: string[] = ['tipoSoporte', 'ubicacionFisica'];

  /* Campos que solo existen en una copia digital, y cuáles de ellos son obligatorios. */
  private camposDigital: string[] = ['formatoArchivo', 'tamanio', 'hashVerificacion'];
  private obligatoriosDigital: string[] = ['formatoArchivo', 'tamanio', 'hashVerificacion'];

  // ==================================================================
  // ===== HARDCODE: borrar este bloque al conectar el back =====
  // (incluye titulosPrueba, el constructor y las funciones
  //  cargarJuego y enviarCopia)
  // ==================================================================

  /*
   * Juegos de prueba. La llave es el id del videojuego. Solo existen los
   * mismos dos que tienen ficha de prueba (1 y 8); con otro id se ve
   * "No encontramos ese videojuego".
   */
  private titulosPrueba: Record<number, Videojuego> = {
    1: {
      idVideojuego: 1,
      titulo: 'Torre de Ámbar Online',
      plataformaOriginal: 'PC',
      anioLanzamiento: 2007,
      estadoConservacion: 'en_riesgo'
    },
    8: {
      idVideojuego: 8,
      titulo: 'Furia Quark',
      plataformaOriginal: 'Arcade',
      anioLanzamiento: 1994,
      estadoConservacion: 'restaurado'
    }
  };

  /*
   * ActivatedRoute lee datos de la URL; Router cambia de pantalla;
   * FormBuilder arma el formulario; ChangeDetectorRef avisa a Angular que
   * hay que redibujar. Angular entrega las cuatro solas (inyección).
   */
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) { }

  /* Busca el juego en los datos de prueba. Si el id no existe, se queda sin valor. */
  private cargarJuego(id: number): void {
    this.videojuego = this.titulosPrueba[id];
    this.cargando = false;
    this.cambios.markForCheck();
  }

  /*
   * Simula lo que haría el back: acepta la copia y vuelve a la ficha.
   * OJO: la copia no se guarda en ningún lado, así que no aparecerá en la
   * lista de la ficha hasta que se conecte el back.
   */
  private enviarCopia(copia: CopiaNueva): void {
    this.guardando = false;
    this.volverALaFicha();
  }

  // ==================================================================
  // ===== FIN DEL HARDCODE =====
  // ==================================================================


  // ==================================================================
  // ===== BACK: descomentar todo este bloque al conectar =====
  // (en WebStorm: seleccionarlo y Ctrl + /)
  // ==================================================================

  // // ArchiveludoApiService habla con el back. ChangeDetectorRef ya está
  // // importado arriba, porque la pantalla lo necesita siempre.
  // constructor(
  //   private route: ActivatedRoute,
  //   private router: Router,
  //   private constructorFormulario: FormBuilder,
  //   private api: ArchiveludoApiService,
  //   private cambios: ChangeDetectorRef
  // ) { }
  //
  // // Pide el videojuego para mostrar su título: GET /api/videojuegos/{id}
  // private cargarJuego(id: number): void {
  //   this.api.obtenerFicha(id).subscribe({
  //     next: (datos) => this.alRecibirJuego(datos.videojuego),
  //     error: () => this.alFallarJuego()
  //   });
  // }
  //
  // private alRecibirJuego(videojuego: Videojuego): void {
  //   this.videojuego = videojuego;
  //   this.cargando = false;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Si el back responde error (por ejemplo 404), el juego se queda sin valor
  // // y el HTML muestra "No encontramos ese videojuego".
  // private alFallarJuego(): void {
  //   this.cargando = false;
  //
  //   this.cambios.markForCheck();
  // }
  //
  // // Envía la copia: POST /api/videojuegos/{id}/copias
  // private enviarCopia(copia: CopiaNueva): void {
  //   this.api.crearCopia(this.idVideojuego, copia).subscribe({
  //     next: () => this.alGuardar(),
  //     error: (error) => this.alFallarGuardado(error)
  //   });
  // }
  //
  // private alGuardar(): void {
  //   this.guardando = false;
  //   this.volverALaFicha();
  // }
  //
  // // Si falla, el formulario se queda como estaba y se muestra un mensaje.
  // private alFallarGuardado(error: unknown): void {
  //   console.error('No se pudo registrar la copia', error);
  //   this.guardando = false;
  //   this.errorEnvio = 'No se pudo registrar la copia. Revisa los datos e inténtalo de nuevo.';
  //
  //   this.cambios.markForCheck();
  // }

  // ==================================================================
  // ===== FIN DEL BLOQUE BACK =====
  // ==================================================================


  /*
   * ngOnInit se ejecuta una vez, cuando el componente ya está listo.
   * Arma el formulario y empieza a escuchar el id de la URL.
   */
  ngOnInit(): void {
    this.crearFormulario();

    this.route.paramMap.subscribe({
      next: (parametros) => this.alCambiarParametros(parametros)
    });
  }

  /* Lee el :id de la URL y pide ese juego. */
  private alCambiarParametros(parametros: ParamMap): void {
    // En /videojuegos/8/copias/nueva el parámetro "id" vale '8', pero como TEXTO.
    // Number() lo convierte a número.
    this.idVideojuego = Number(parametros.get('id'));
    this.cargando = true;

    this.cargarJuego(this.idVideojuego);
  }

  /*
   * Arma el formulario con todos los campos (físicos y digitales) y un valor
   * inicial para cada uno. Después ajusta cuáles son obligatorios según el
   * tipo elegido, y vuelve a ajustarlos cada vez que el usuario cambia de tipo.
   */
  private crearFormulario(): void {
    this.formulario = this.constructorFormulario.group({
      tipoCopia: ['digital', Validators.required],
      estadoIntegridad: ['buena', Validators.required],
      fechaRegistro: [this.hoy, Validators.required],
      tipoSoporte: [''],
      numeroSerie: [''],
      ubicacionFisica: [''],
      formatoArchivo: [''],
      tamanio: [''],
      hashVerificacion: ['']
    });

    this.aplicarValidadores('digital');

    this.formulario.controls['tipoCopia'].valueChanges.subscribe({
      next: (tipo) => this.aplicarValidadores(tipo)
    });
  }

  /*
   * Deja activos solo los campos del tipo elegido:
   *  - los del tipo elegido reciben sus validadores (los obligatorios llevan
   *    "required"; todos limitan el largo a 255 caracteres)
   *  - los del otro tipo se vacían y se quedan sin validadores, para que no
   *    bloqueen el envío ni viajen al back
   */
  private aplicarValidadores(tipo: TipoCopia): void {
    let activos = this.camposDigital;
    let obligatorios = this.obligatoriosDigital;
    let inactivos = this.camposFisica;

    if (tipo === 'fisica') {
      activos = this.camposFisica;
      obligatorios = this.obligatoriosFisica;
      inactivos = this.camposDigital;
    }

    for (const nombre of activos) {
      const control = this.formulario.controls[nombre];

      if (obligatorios.includes(nombre)) {
        control.setValidators([Validators.required, Validators.maxLength(255)]);
      } else {
        control.setValidators([Validators.maxLength(255)]);
      }
      control.updateValueAndValidity();
    }

    for (const nombre of inactivos) {
      const control = this.formulario.controls[nombre];

      control.clearValidators();
      control.setValue('');
      control.updateValueAndValidity();
    }
  }

  /* true si la copia que se está registrando es física. */
  get esFisica(): boolean {
    return this.formulario.controls['tipoCopia'].value === 'fisica';
  }

  /* true si la copia que se está registrando es digital. */
  get esDigital(): boolean {
    return this.formulario.controls['tipoCopia'].value === 'digital';
  }

  /*
   * Dice si hay que mostrar el error de un campo: solo si tiene un error Y
   * (el usuario ya pasó por él O ya intentó enviar el formulario).
   */
  mostrarError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /* Texto del error de un campo, según qué validación falló. */
  textoError(nombre: string): string {
    const control = this.formulario.controls[nombre];

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }
    if (control.hasError('maxlength')) {
      return 'Máximo 255 caracteres.';
    }
    return '';
  }

  /*
   * Se ejecuta al pulsar "Guardar copia". Si hay errores, los muestra y no
   * envía nada. Si todo está bien, arma el cuerpo y lo envía.
   */
  guardar(): void {
    this.enviado = true;
    this.errorEnvio = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando = true;
    this.enviarCopia(this.armarCopia());
  }

  /*
   * Arma el cuerpo que se envía al back (CopiaNueva). Solo incluye los campos
   * del tipo elegido, sin espacios sobrantes; el número de serie, que es
   * opcional, se omite si está vacío.
   */
  private armarCopia(): CopiaNueva {
    const datos = this.formulario.getRawValue();

    const copia: CopiaNueva = {
      tipoCopia: datos.tipoCopia,
      estadoIntegridad: datos.estadoIntegridad,
      fechaRegistro: datos.fechaRegistro
    };

    if (datos.tipoCopia === 'fisica') {
      copia.tipoSoporte = datos.tipoSoporte.trim();
      copia.ubicacionFisica = datos.ubicacionFisica.trim();

      const serie = datos.numeroSerie.trim();
      if (serie !== '') {
        copia.numeroSerie = serie;
      }
    } else {
      copia.formatoArchivo = datos.formatoArchivo.trim();
      copia.tamanio = datos.tamanio.trim();
      copia.hashVerificacion = datos.hashVerificacion.trim();
    }

    return copia;
  }

  /* Se ejecuta al pulsar "Cancelar": vuelve a la ficha sin guardar nada. */
  cancelar(): void {
    this.volverALaFicha();
  }

  /* Navega a /videojuegos/:id, la ficha del juego. */
  private volverALaFicha(): void {
    this.router.navigate(['/videojuegos', this.idVideojuego]);
  }

  /*
   * Fecha de hoy como texto 'yyyy-MM-dd', en la hora local del usuario.
   * No se usa toISOString() porque devuelve la fecha en UTC, y de noche en
   * Colombia ya sería "mañana". getMonth() empieza en 0, por eso el + 1.
   */
  private fechaHoy(): string {
    const ahora = new Date();
    const anio = ahora.getFullYear();
    const mes = String(ahora.getMonth() + 1).padStart(2, '0');
    const dia = String(ahora.getDate()).padStart(2, '0');

    return anio + '-' + mes + '-' + dia;
  }
}
