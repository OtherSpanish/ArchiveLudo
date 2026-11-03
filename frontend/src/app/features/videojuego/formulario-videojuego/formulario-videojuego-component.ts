import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { CompaniaEditora } from '../../../shared/models/compania-editora-model';
import {
  EstadoConservacion,
  Estudio,
  Videojuego,
  VideojuegoNuevo
} from '../../../shared/models/videojuego-model';

/** Una opción del selector de estado: el valor que se envía y el texto que se ve. */
interface OpcionEstado {
  valor: EstadoConservacion;
  etiqueta: string;
}

/**
 * Formulario para CREAR o EDITAR un videojuego. Es una sola pantalla con dos rutas:
 *   - /videojuegos/nuevo        -> modo crear  (no hay :id en la URL)
 *   - /videojuegos/:id/editar   -> modo editar (carga el juego y llena el formulario)
 *
 * Pide al back los estudios y las editoras para los selectores:
 *   GET /api/estudios y GET /api/editoras
 * y al guardar envía:
 *   POST /api/videojuegos          (crear)
 *   PUT  /api/videojuegos/{id}     (editar; idsEditoras REEMPLAZA la lista anterior)
 * Solo curador y administrador llegan aquí (lo exige roleGuard en la ruta).
 */
@Component({
  selector: 'app-formulario-videojuego',
  templateUrl: './formulario-videojuego-component.html',
  styleUrl: './formulario-videojuego-component.css',
  standalone: false
})
export class FormularioVideojuegoComponent implements OnInit {

  /** Año mínimo y máximo que acepta el back para anioLanzamiento. */
  readonly anioMinimo: number = 1950;
  readonly anioMaximo: number = new Date().getFullYear();

  /** Id del juego que se edita. Vale 0 en modo crear. */
  idVideojuego: number = 0;

  /** true si se está editando un juego existente. */
  modoEdicion: boolean = false;

  /** Título del juego que se edita, para las migas y el subtítulo. */
  tituloOriginal: string = '';

  /** true mientras se esperan los datos iniciales (estudios, editoras y, al editar, el juego). */
  cargando: boolean = true;

  /** true si no se pudo cargar lo necesario (por ejemplo, el id no existe). */
  hayErrorCarga: boolean = false;

  /** Datos de los selectores. */
  estudios: Estudio[] = [];
  editoras: CompaniaEditora[] = [];

  /** Ids de las editoras marcadas (casillas). */
  editorasSeleccionadas: number[] = [];

  /** Opciones del estado de conservación, con los valores EXACTOS del back (con espacios). */
  opcionesEstado: OpcionEstado[] = [
    { valor: 'activo', etiqueta: 'Activo' },
    { valor: 'en riesgo', etiqueta: 'En riesgo' },
    { valor: 'parcialmente perdido', etiqueta: 'Parcialmente perdido' },
    { valor: 'totalmente perdido', etiqueta: 'Totalmente perdido' },
    { valor: 'restaurado', etiqueta: 'Restaurado' }
  ];

  /** El formulario. */
  formulario: FormGroup;

  /** true después del primer intento de guardar. */
  enviado: boolean = false;

  /** true mientras se espera la respuesta del back. */
  guardando: boolean = false;

  /** Mensaje si el back rechaza el guardado. */
  errorEnvio: string = '';

  /** Cuántas cargas iniciales faltan por terminar (se baja de 3 o 2 hasta 0). */
  private cargasPendientes: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ArchiveludoApiService,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) {
    this.formulario = this.constructorFormulario.group({
      titulo: ['', [Validators.required, Validators.maxLength(255)]],
      genero: ['', [Validators.required, Validators.maxLength(100)]],
      plataformaOriginal: ['', [Validators.required, Validators.maxLength(100)]],
      anioLanzamiento: [
        '',
        [Validators.required, Validators.min(this.anioMinimo), Validators.max(this.anioMaximo)]
      ],
      estadoConservacion: ['activo', Validators.required],
      idEstudio: ['', Validators.required],
      descripcion: ['', Validators.maxLength(1000)]
    });
  }

  /** Decide el modo según la URL y pide los datos necesarios. */
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id !== null) {
      this.modoEdicion = true;
      this.idVideojuego = Number(id);
      this.cargasPendientes = 3;
      this.cargarJuego();
    } else {
      this.cargasPendientes = 2;
    }

    this.cargarEstudios();
    this.cargarEditoras();
  }

  /** GET /api/videojuegos/{id}: llena el formulario con los datos actuales. */
  private cargarJuego(): void {
    this.api.obtenerVideojuego(this.idVideojuego).subscribe({
      next: (juego) => this.alRecibirJuego(juego),
      error: (error) => this.alFallarCarga(error)
    });
  }

  private alRecibirJuego(juego: Videojuego): void {
    this.tituloOriginal = juego.titulo;
    this.editorasSeleccionadas = (juego.idsEditoras || []).slice();

    this.formulario.patchValue({
      titulo: juego.titulo,
      genero: juego.genero || '',
      plataformaOriginal: juego.plataformaOriginal,
      anioLanzamiento: juego.anioLanzamiento,
      estadoConservacion: juego.estadoConservacion,
      idEstudio: juego.idEstudio !== undefined ? juego.idEstudio : '',
      descripcion: juego.descripcion || ''
    });

    this.terminoUnaCarga();
  }

  /** GET /api/estudios */
  private cargarEstudios(): void {
    this.api.obtenerEstudios().subscribe({
      next: (lista) => {
        this.estudios = lista;
        this.terminoUnaCarga();
      },
      error: (error) => this.alFallarCarga(error)
    });
  }

  /** GET /api/editoras */
  private cargarEditoras(): void {
    this.api.obtenerEditoras().subscribe({
      next: (lista) => {
        this.editoras = lista;
        this.terminoUnaCarga();
      },
      error: (error) => this.alFallarCarga(error)
    });
  }

  /** Cuando llegan todas las cargas, se muestra el formulario. */
  private terminoUnaCarga(): void {
    this.cargasPendientes--;

    if (this.cargasPendientes === 0) {
      this.cargando = false;
    }

    this.cambios.markForCheck();
  }

  private alFallarCarga(error: unknown): void {
    console.error('No se pudieron cargar los datos del formulario', error);
    this.cargando = false;
    this.hayErrorCarga = true;

    this.cambios.markForCheck();
  }

  /** true si la editora está marcada. */
  estaSeleccionada(idEditora: number): boolean {
    return this.editorasSeleccionadas.includes(idEditora);
  }

  /** Marca o desmarca una editora (se llama al pulsar su casilla). */
  alternarEditora(idEditora: number): void {
    if (this.estaSeleccionada(idEditora)) {
      this.editorasSeleccionadas = this.editorasSeleccionadas.filter((id) => id !== idEditora);
    } else {
      this.editorasSeleccionadas = this.editorasSeleccionadas.concat([idEditora]);
    }
  }

  /** Dice si hay que mostrar el error de un campo. */
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
    if (control.hasError('min') || control.hasError('max')) {
      return 'El año debe estar entre ' + this.anioMinimo + ' y ' + this.anioMaximo + '.';
    }
    if (control.hasError('maxlength')) {
      return 'Es demasiado largo.';
    }
    return '';
  }

  /** Se ejecuta al pulsar "Guardar": valida y envía POST o PUT según el modo. */
  guardar(): void {
    this.enviado = true;
    this.errorEnvio = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando = true;
    const cuerpo = this.armarCuerpo();

    if (this.modoEdicion) {
      this.api.actualizarVideojuego(this.idVideojuego, cuerpo).subscribe({
        next: (juego) => this.alGuardar(juego),
        error: (error) => this.alFallarGuardado(error)
      });
    } else {
      this.api.crearVideojuego(cuerpo).subscribe({
        next: (juego) => this.alGuardar(juego),
        error: (error) => this.alFallarGuardado(error)
      });
    }
  }

  /** Arma el cuerpo del POST/PUT con los valores del formulario. */
  private armarCuerpo(): VideojuegoNuevo {
    const datos = this.formulario.getRawValue();

    const cuerpo: VideojuegoNuevo = {
      titulo: String(datos.titulo).trim(),
      genero: String(datos.genero).trim(),
      plataformaOriginal: String(datos.plataformaOriginal).trim(),
      anioLanzamiento: Number(datos.anioLanzamiento),
      estadoConservacion: datos.estadoConservacion,
      idEstudio: Number(datos.idEstudio),
      idsEditoras: this.editorasSeleccionadas
    };

    const descripcion = String(datos.descripcion || '').trim();
    if (descripcion !== '') {
      cuerpo.descripcion = descripcion;
    }

    return cuerpo;
  }

  /** Se guardó: va a la ficha del juego (nuevo o editado). */
  private alGuardar(juego: Videojuego): void {
    this.guardando = false;
    this.router.navigate(['/videojuegos', juego.idVideojuego]);
  }

  /**
   * Si falla, el formulario se queda como estaba:
   *  - 400 / 404: el back explica qué está mal (error.error.mensaje)
   *  - 403: el rol no puede
   *  - otro: mensaje general
   */
  private alFallarGuardado(error: HttpErrorResponse): void {
    console.error('No se pudo guardar el videojuego', error);
    this.guardando = false;

    if ((error.status === 400 || error.status === 404) && error.error && error.error.mensaje) {
      this.errorEnvio = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorEnvio = 'Tu rol no permite guardar videojuegos.';
    } else {
      this.errorEnvio = 'No se pudo guardar el videojuego. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /** "Cancelar": vuelve a la ficha (si se edita) o al catálogo (si se crea). */
  cancelar(): void {
    if (this.modoEdicion) {
      this.router.navigate(['/videojuegos', this.idVideojuego]);
    } else {
      this.router.navigate(['/catalogo']);
    }
  }

  /** Título de la pantalla según el modo. */
  get titulo(): string {
    return this.modoEdicion ? 'Editar videojuego' : 'Nuevo videojuego';
  }
}
