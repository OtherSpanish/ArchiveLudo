import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Videojuego } from '../../../shared/models/videojuego-model';
import { Copia, CopiaNueva, EstadoIntegridad, TipoCopia } from '../../../shared/models/copia-model';

import { HttpErrorResponse } from '@angular/common/http';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';

/** Unidad en la que el usuario escribe el tamaño; "factor" la convierte a bytes. */
interface UnidadTamanio {
  valor: string;
  etiqueta: string;
  factor: number;
}

/** Una opción de los botones de selección: el valor que se guarda y el texto que se ve. */
interface Opcion<T> {
  valor: T;
  etiqueta: string;
}

/**
 * Pantalla para registrar una copia nueva de un videojuego
 * (/videojuegos/:id/copias/nueva) o para editar una existente
 * (/copias/:idCopia/editar).
 *
 * Al editar, el tipo de copia queda bloqueado: el back lo rechaza si cambia.
 * Para pasar una copia de física a digital hay que eliminarla y registrar otra.
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

  /** Videojuego al que se le agrega la copia. Sin valor hasta que se carga. */
  videojuego?: Videojuego;

  /** Id que viene en la URL (/videojuegos/8/copias/nueva -> 8). */
  idVideojuego: number = 0;

  /** Id de la copia que se edita (/copias/11/editar -> 11). Vale 0 cuando se registra una nueva. */
  idCopia: number = 0;

  /** true cuando la pantalla edita una copia existente. */
  get esEdicion(): boolean {
    return this.idCopia > 0;
  }

  /** true mientras se espera el videojuego. Evita mostrar "no encontrado" antes de tiempo. */
  cargando: boolean = true;

  /** El formulario. Se arma en ngOnInit (ver crearFormulario). */
  formulario!: FormGroup;

  /**
   * true después del primer intento de enviar. Sirve para mostrar los errores
   * de los campos que el usuario nunca tocó.
   */
  enviado: boolean = false;

  /** true mientras se espera la respuesta del back; deshabilita el botón de guardar. */
  guardando: boolean = false;

  /** Mensaje si el back responde un error al guardar. Vacío si no hay error. */
  errorEnvio: string = '';

  /**
   * Unidades del tamaño. El usuario escribe "700" y elige "MB"; al enviar,
   * se convierte a bytes (700 x 1024 x 1024), que es lo que pide el back.
   */
  unidadesTamanio: UnidadTamanio[] = [
    { valor: 'KB', etiqueta: 'KB', factor: 1024 },
    { valor: 'MB', etiqueta: 'MB', factor: 1024 * 1024 },
    { valor: 'GB', etiqueta: 'GB', factor: 1024 * 1024 * 1024 }
  ];

  /** Opciones del tipo de copia, en el orden en que se muestran. */
  opcionesTipo: Opcion<TipoCopia>[] = [
    { valor: 'fisica', etiqueta: 'Física' },
    { valor: 'digital', etiqueta: 'Digital' }
  ];

  /** Opciones del estado de integridad (mismos valores que la tabla COPIA). */
  opcionesEstado: Opcion<EstadoIntegridad>[] = [
    { valor: 'buena', etiqueta: 'Buena' },
    { valor: 'deteriorada', etiqueta: 'Deteriorada' },
    { valor: 'dañada', etiqueta: 'Dañada' },
    { valor: 'irrecuperable', etiqueta: 'Irrecuperable' }
  ];

  /** Campos que solo existen en una copia física, y cuáles de ellos son obligatorios. */
  private camposFisica: string[] = ['tipoSoporte', 'numeroSerie', 'ubicacionFisica'];
  private obligatoriosFisica: string[] = ['tipoSoporte', 'ubicacionFisica'];

  /** Campos que solo existen en una copia digital, y cuáles de ellos son obligatorios. */
  private camposDigital: string[] = ['formatoArchivo', 'tamanio', 'hashVerificacion'];
  private obligatoriosDigital: string[] = ['formatoArchivo', 'tamanio', 'hashVerificacion'];

  /**
   * ActivatedRoute lee datos de la URL; Router cambia de pantalla;
   * FormBuilder arma el formulario; ArchiveludoApiService habla con el back;
   * ChangeDetectorRef avisa a Angular que hay que redibujar.
   */
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private constructorFormulario: FormBuilder,
    private api: ArchiveludoApiService,
    private cambios: ChangeDetectorRef
  ) { }

  /** Pide el videojuego para mostrar su título: GET /api/videojuegos/{id} */
  private cargarJuego(id: number): void {
    this.api.obtenerVideojuego(id).subscribe({
      next: (videojuego) => this.alRecibirJuego(videojuego),
      error: () => this.alFallarJuego()
    });
  }

  private alRecibirJuego(videojuego: Videojuego): void {
    this.videojuego = videojuego;
    this.cargando = false;

    this.cambios.markForCheck();
  }

  /** Si el back responde error (por ejemplo 404), el HTML muestra "No encontramos ese videojuego". */
  private alFallarJuego(): void {
    this.videojuego = undefined;
    this.cargando = false;

    this.cambios.markForCheck();
  }

  /**
   * Envía la copia: POST /api/videojuegos/{id}/copias (nueva) o
   * PUT /api/copias/{id} (edición, con el mismo tipoCopia que tiene guardado).
   */
  private enviarCopia(copia: CopiaNueva): void {
    if (this.esEdicion) {
      this.api.actualizarCopia(this.idCopia, copia).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallarGuardado(error)
      });
    } else {
      this.api.crearCopia(this.idVideojuego, copia).subscribe({
        next: () => this.alGuardar(),
        error: (error) => this.alFallarGuardado(error)
      });
    }
  }

  private alGuardar(): void {
    this.guardando = false;
    this.volverALaFicha();
  }

  /**
   * Si falla, el formulario se queda como estaba y se muestra un mensaje:
   *  - 400: el back explica qué dato está mal (viene en error.error.mensaje)
   *  - 403: el rol de consulta no puede registrar copias
   *  - otro: mensaje general
   */
  private alFallarGuardado(error: HttpErrorResponse): void {
    console.error('No se pudo registrar la copia', error);
    this.guardando = false;

    if (error.error && error.error.mensaje) {
      this.errorEnvio = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorEnvio = 'Tu rol no permite guardar copias.';
    } else {
      this.errorEnvio = 'No se pudo guardar la copia. Revisa los datos e inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }



  /**
   * ngOnInit se ejecuta una vez, cuando el componente ya está listo.
   * Arma el formulario y empieza a escuchar el id de la URL.
   */
  ngOnInit(): void {
    this.crearFormulario();

    this.route.paramMap.subscribe({
      next: (parametros) => this.alCambiarParametros(parametros)
    });
  }

  /**
   * Lee la URL. En /videojuegos/8/copias/nueva el parámetro "id" vale '8'
   * (como TEXTO; Number() lo convierte) y se pide ese juego. En
   * /copias/11/editar viene "idCopia": se pide la copia, y de ella sale el juego.
   */
  private alCambiarParametros(parametros: ParamMap): void {
    this.cargando = true;

    if (parametros.has('idCopia')) {
      this.idCopia = Number(parametros.get('idCopia'));
      this.cargarCopia(this.idCopia);
      return;
    }

    this.idCopia = 0;
    this.idVideojuego = Number(parametros.get('id'));

    this.cargarJuego(this.idVideojuego);
  }

  /** Edición: GET /api/copias/{id}. Si no existe (404), se muestra "No encontramos...". */
  private cargarCopia(idCopia: number): void {
    this.api.obtenerCopia(idCopia).subscribe({
      next: (copia) => this.alRecibirCopia(copia),
      error: () => this.alFallarJuego()
    });
  }

  /** Llega la copia: se rellena el formulario, se bloquea el tipo y se pide su juego. */
  private alRecibirCopia(copia: Copia): void {
    this.idVideojuego = copia.idVideojuego;

    // Primero el tipo (activa los campos que le corresponden) y después el resto.
    this.formulario.controls['tipoCopia'].setValue(copia.tipoCopia);
    this.aplicarValidadores(copia.tipoCopia);

    const tamanio = this.separarTamanio(copia.tamanio);

    this.formulario.patchValue({
      estadoIntegridad: copia.estadoIntegridad,
      tipoSoporte: copia.tipoSoporte || '',
      numeroSerie: copia.numeroSerie || '',
      ubicacionFisica: copia.ubicacionFisica || '',
      formatoArchivo: copia.formatoArchivo || '',
      tamanio: tamanio.valor,
      unidadTamanio: tamanio.unidad,
      hashVerificacion: copia.hashVerificacion || ''
    });

    // El tipo no se puede cambiar al editar.
    this.formulario.controls['tipoCopia'].disable();

    this.cargarJuego(copia.idVideojuego);
  }

  /**
   * Convierte bytes a la unidad más grande que dé un número de 1 o más
   * (GB, MB o KB). Ejemplo: 734003200 -> { valor: 700, unidad: 'MB' }.
   */
  private separarTamanio(bytes?: number): { valor: number | string; unidad: string } {
    if (bytes === undefined || bytes === null) {
      return { valor: '', unidad: 'MB' };
    }

    // De la unidad más grande a la más chica; se queda con la primera que aplique.
    for (let posicion = this.unidadesTamanio.length - 1; posicion >= 0; posicion--) {
      const unidad = this.unidadesTamanio[posicion];

      if (bytes >= unidad.factor) {
        return { valor: Math.round((bytes / unidad.factor) * 100) / 100, unidad: unidad.valor };
      }
    }

    // Menos de 1 KB: se muestra en KB con decimales.
    return { valor: Math.round((bytes / 1024) * 1000) / 1000, unidad: 'KB' };
  }

  /**
   * Arma el formulario con todos los campos (físicos y digitales) y un valor
   * inicial para cada uno. Después ajusta cuáles son obligatorios según el
   * tipo elegido, y vuelve a ajustarlos cada vez que el usuario cambia de tipo.
   */
  private crearFormulario(): void {
    this.formulario = this.constructorFormulario.group({
      tipoCopia: ['digital', Validators.required],
      estadoIntegridad: ['buena', Validators.required],
      tipoSoporte: [''],
      numeroSerie: [''],
      ubicacionFisica: [''],
      formatoArchivo: [''],
      tamanio: [''],
      unidadTamanio: ['MB'],
      hashVerificacion: ['']
    });

    this.aplicarValidadores('digital');

    this.formulario.controls['tipoCopia'].valueChanges.subscribe({
      next: (tipo) => this.aplicarValidadores(tipo)
    });
  }

  /**
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

      if (nombre === 'tamanio') {
        // El tamaño debe ser un número mayor que 0
        control.setValidators([Validators.required, Validators.min(0.000001)]);
      } else if (obligatorios.includes(nombre)) {
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

  /** true si la copia que se está registrando es física. */
  get esFisica(): boolean {
    return this.formulario.controls['tipoCopia'].value === 'fisica';
  }

  /** true si la copia que se está registrando es digital. */
  get esDigital(): boolean {
    return this.formulario.controls['tipoCopia'].value === 'digital';
  }

  /**
   * Dice si hay que mostrar el error de un campo: solo si tiene un error Y
   * (el usuario ya pasó por él O ya intentó enviar el formulario).
   */
  mostrarError(nombre: string): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || this.enviado);
  }

  /** Texto del error de un campo, según qué validación falló. */
  textoError(nombre: string): string {
    const control = this.formulario.controls[nombre];

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }
    if (control.hasError('min')) {
      return 'Debe ser un número mayor que 0.';
    }
    if (control.hasError('maxlength')) {
      return 'Máximo 255 caracteres.';
    }
    return '';
  }

  /**
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

  /**
   * Arma el cuerpo que se envía al back (CopiaNueva). Solo incluye los campos
   * del tipo elegido, sin espacios sobrantes; el número de serie, que es
   * opcional, se omite si está vacío. La fecha y el usuario NO se envían:
   * los pone el back.
   */
  private armarCopia(): CopiaNueva {
    const datos = this.formulario.getRawValue();

    const copia: CopiaNueva = {
      tipoCopia: datos.tipoCopia,
      estadoIntegridad: datos.estadoIntegridad
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
      copia.tamanio = this.convertirABytes(Number(datos.tamanio), datos.unidadTamanio);
      copia.hashVerificacion = datos.hashVerificacion.trim();
    }

    return copia;
  }

  /** Se ejecuta al pulsar "Cancelar": vuelve a la ficha sin guardar nada. */
  cancelar(): void {
    this.volverALaFicha();
  }

  /** Navega a /videojuegos/:id, la ficha del juego. */
  private volverALaFicha(): void {
    this.router.navigate(['/videojuegos', this.idVideojuego]);
  }

  /**
   * Convierte el tamaño escrito por el usuario a bytes.
   * Busca la unidad elegida (KB, MB o GB) y multiplica por su factor.
   * Ejemplo: 700 MB -> 700 x 1048576 = 734003200.
   */
  private convertirABytes(valor: number, unidad: string): number {
    let factor = 1;

    for (const opcion of this.unidadesTamanio) {
      if (opcion.valor === unidad) {
        factor = opcion.factor;
      }
    }

    return Math.round(valor * factor);
  }
}
