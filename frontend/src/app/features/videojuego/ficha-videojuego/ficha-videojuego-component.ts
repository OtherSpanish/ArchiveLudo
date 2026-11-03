import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Estudio, Videojuego } from '../../../shared/models/videojuego-model';
import { Custodia, Institucion } from '../../../shared/models/custodia-model';
import { Copia } from '../../../shared/models/copia-model';
import { EstatusLegal, EstatusLegalNuevo } from '../../../shared/models/estatus-legal-model';
import { EventoRiesgo } from '../../../shared/models/evento-riesgo-model';
import { CompaniaEditora } from '../../../shared/models/compania-editora-model';
import { ArchiveludoApiService } from '../../../core/archiveludo-api-service';
import { InstitucionConCopias } from '../../../shared/models/api-model';
import { Experto } from '../../../shared/models/experto-model';
import { ProyectoDeRestauracion } from '../../../shared/models/proyecto-model';

/*
 * Las cuatro pestañas de la ficha. Cada valor es el identificador interno
 * de la pestaña; el texto que ve el usuario se arma en el HTML.
 */
type Pestana = 'copias' | 'proyectos' | 'legal' | 'eventos';

/* Los cuatro estatus legales que acepta el back (texto legible). */
const ESTATUS_OPCIONES: string[] = ['Protegido', 'Dominio público', 'Huérfano', 'Abandonware'];

/*
 * Pantalla de la ficha de un videojuego (/videojuegos/:id).
 * Incluye: cabecera, pestañas, pestaña Copias, historial de custodia,
 * pestaña Proyectos, pestaña Estatus legal y pestaña Eventos de riesgo.
 */
@Component({
  selector: 'app-ficha-videojuego',
  templateUrl: './ficha-videojuego-component.html',
  styleUrl: './ficha-videojuego-component.css',
  standalone: false
})
export class FichaVideojuegoComponent implements OnInit {

  /*
   * El juego que se está mostrando. Empieza sin valor (undefined) y se llena
   * cuando se carga. El "?" indica que puede no tener valor: si el id de la URL
   * no existe, se queda así y el HTML muestra un mensaje de "no encontrado".
   */
  videojuego?: Videojuego;

  /* Nombres de las editoras del juego (se cruzan idsEditoras con GET /api/editoras). */
  nombresEditoras: string[] = [];

  /* true cuando el back respondió que el juego no existe o falló la carga. */
  hayError: boolean = false;

  numeroCopias: number = 0;

  numeroProyectos: number = 0;

  /*
   * Copias del juego (pestaña Copias, columna izquierda). Cada una trae
   * idInstitucionActual y nombreInstitucionActual si tiene custodia vigente.
   */
  copias: Copia[] = [];

  /*
   * Instituciones que custodian copias de este juego (columna derecha).
   * Se arman en el front: se cuentan las copias por idInstitucionActual y se
   * cruzan con GET /api/instituciones (ver agruparInstituciones).
   */
  instituciones: InstitucionConCopias[] = [];

  /*
   * Copia cuyo historial de custodia se muestra abajo. Al cargar es la
   * primera de la lista; el botón "Ver historial de custodia" de cada tarjeta
   * la cambia. Vale 0 mientras el juego no tenga copias.
   */
  idCopiaHistorial: number = 0;

  /* Periodos de custodia de esa copia, para la línea de tiempo. */
  custodiasHistorial: Custodia[] = [];

  /* Proyectos de restauración del juego, con sus participaciones (pestaña Proyectos). */
  proyectos: ProyectoDeRestauracion[] = [];

  /* Catálogo de expertos: sirve para mostrar el nombre de cada participación. */
  expertos: Experto[] = [];

  /*
   * Historial de estatus legal (pestaña Estatus legal), del más reciente al
   * más antiguo. El primero es el vigente: el HTML lo marca con $first.
   */
  historialLegal: EstatusLegal[] = [];

  /* Bitácora de eventos de riesgo (pestaña Eventos de riesgo), del más reciente al más antiguo. */
  eventos: EventoRiesgo[] = [];

  // ---- Copias: eliminar (editar lleva a otra pantalla) ----

  /* Id de la copia que pide confirmación para eliminarse (0 = ninguna). */
  idConfirmandoEliminarCopia: number = 0;

  /* Mensaje si el back rechaza eliminar una copia (por ejemplo, 409 si tiene custodias). */
  errorCopia: string = '';

  // ---- Historial de custodia: registrar, editar y eliminar ----

  /* Catálogo completo de instituciones (selector del formulario de custodia). */
  catalogoInstituciones: Institucion[] = [];

  /* true cuando el formulario de custodia está desplegado. */
  mostrarFormularioCustodia: boolean = false;

  /* Custodia que se edita. Undefined cuando el formulario registra una nueva. */
  custodiaEditando?: Custodia;

  /* Id de la custodia que pide confirmación para eliminarse (0 = ninguna). */
  idConfirmandoEliminarCustodia: number = 0;

  /* Mensaje si el back rechaza una acción de custodia. */
  errorCustodia: string = '';

  // ---- Pestaña Proyectos: crear, editar, eliminar y gestionar expertos ----

  /* true cuando el formulario de proyecto está desplegado. */
  mostrarFormularioProyecto: boolean = false;

  /* Proyecto que se edita. Undefined cuando el formulario crea uno nuevo. */
  proyectoEditando?: ProyectoDeRestauracion;

  /* Id del proyecto cuyos expertos se están gestionando (0 = ninguno). */
  idProyectoGestionando: number = 0;

  /* Id del proyecto que pide confirmación para eliminarse (0 = ninguno). */
  idConfirmandoEliminarProyecto: number = 0;

  /* Mensaje si el back rechaza eliminar un proyecto. */
  errorProyecto: string = '';

  // ---- Eliminar el videojuego (solo administrador) ----

  /* true cuando se pidió eliminar y se espera la confirmación del usuario. */
  confirmandoEliminar: boolean = false;

  /* true mientras se espera la respuesta del back. */
  eliminando: boolean = false;

  /* Mensaje si el back rechaza la eliminación (por ejemplo, 409). */
  errorEliminar: string = '';

  // ---- Formulario "Agregar estatus legal" (pestaña Estatus legal) ----

  /* Opciones del selector de estatus. */
  opcionesEstatus: string[] = ESTATUS_OPCIONES;

  /* true cuando el formulario está desplegado. */
  mostrarFormularioEstatus: boolean = false;

  /* El formulario. Se arma en el constructor. */
  formularioEstatus: FormGroup;

  /* true después del primer intento de guardar; sirve para mostrar errores de campos sin tocar. */
  estatusEnviado: boolean = false;

  /* true mientras se espera la respuesta del back. */
  guardandoEstatus: boolean = false;

  /* Mensaje de error si el back rechaza el guardado. */
  errorEstatus: string = '';

  /* Registro que se está editando. Es undefined cuando el formulario agrega uno nuevo. */
  editandoEstatus?: EstatusLegal;

  /* Id del registro que está pidiendo confirmación para eliminarse (0 = ninguno). */
  idConfirmandoEliminarEstatus: number = 0;

  /* Fecha máxima del campo de fecha: hoy. No se puede determinar un estatus en el futuro. */
  hoy: string = this.fechaHoy();

  /* Pestaña que se está viendo. Empieza en "Copias", como en el boceto. */
  pestanaActiva: Pestana = 'copias';


  /*
   * ActivatedRoute lee datos de la URL actual.
   * ArchiveludoApiService habla con el back.
   * ChangeDetectorRef avisa a Angular que hay que redibujar la pantalla.
   */
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ArchiveludoApiService,
    private constructorFormulario: FormBuilder,
    private cambios: ChangeDetectorRef
  ) {
    this.formularioEstatus = this.constructorFormulario.group({
      estatus: ['Protegido', Validators.required],
      fechaDeterminacion: [this.hoy, Validators.required],
      observacion: ['', Validators.maxLength(500)]
    });
  }

  /*
   * Pide al back: el videojuego, su estatus legal y sus eventos.
   * También sus copias y sus proyectos.
   * Si el juego no existe (404) se queda sin valor y el HTML muestra
   * "No encontramos ese videojuego".
   */
  private cargarJuego(id: number): void {
    this.limpiarJuego();

    // GET /api/videojuegos/{id}
    this.api.obtenerVideojuego(id).subscribe({
      next: (juego) => this.alRecibirVideojuego(juego),
      error: (error) => this.mostrarError(error)
    });

    // GET /api/videojuegos/{id}/estatus-legal
    this.api.obtenerEstatusLegal(id).subscribe({
      next: (historial) => this.alRecibirEstatusLegal(historial),
      error: (error) => this.mostrarError(error)
    });

    // GET /api/videojuegos/{id}/eventos-riesgo
    this.api.obtenerEventosRiesgo(id).subscribe({
      next: (eventos) => this.alRecibirEventos(eventos),
      error: (error) => this.mostrarError(error)
    });

    // GET /api/videojuegos/{id}/copias
    this.api.obtenerCopias(id).subscribe({
      next: (copias) => this.alRecibirCopias(copias),
      error: (error) => this.mostrarError(error)
    });

    // GET /api/videojuegos/{id}/proyectos
    // Si el servidor aún no tiene este servicio, la pestaña queda vacía; un
    // error aquí NO debe ocultar toda la ficha (por eso no usa mostrarError).
    this.api.obtenerProyectos(id).subscribe({
      next: (proyectos) => this.alRecibirProyectos(proyectos),
      error: (error) => console.error('No se pudieron cargar los proyectos del videojuego', error)
    });

    // GET /api/expertos (para armar los nombres). Si falla, la tarjeta usa "Experto #id".
    this.api.obtenerExpertos().subscribe({
      next: (expertos) => this.alRecibirExpertos(expertos),
      error: (error) => console.error('No se pudieron cargar los expertos', error)
    });
  }

  private alRecibirProyectos(proyectos: ProyectoDeRestauracion[]): void {
    this.proyectos = proyectos;
    this.numeroProyectos = proyectos.length;
    this.cambios.markForCheck();
  }

  private alRecibirExpertos(expertos: Experto[]): void {
    this.expertos = expertos;
    this.cambios.markForCheck();
  }

  /* El proyecto cuyos expertos se gestionan, buscado por id en la lista actual. */
  get proyectoGestionando(): ProyectoDeRestauracion | undefined {
    for (const proyecto of this.proyectos) {
      if (proyecto.idProyecto === this.idProyectoGestionando) {
        return proyecto;
      }
    }
    return undefined;
  }

  /* Abre el formulario vacío para crear un proyecto. */
  abrirNuevoProyecto(): void {
    this.proyectoEditando = undefined;
    this.idProyectoGestionando = 0;
    this.idConfirmandoEliminarProyecto = 0;
    this.errorProyecto = '';
    this.mostrarFormularioProyecto = true;
  }

  /* Abre el formulario con los datos de un proyecto (editar). */
  abrirEditarProyecto(proyecto: ProyectoDeRestauracion): void {
    this.proyectoEditando = proyecto;
    this.idProyectoGestionando = 0;
    this.idConfirmandoEliminarProyecto = 0;
    this.errorProyecto = '';
    this.mostrarFormularioProyecto = true;
  }

  cerrarFormularioProyecto(): void {
    this.mostrarFormularioProyecto = false;
    this.proyectoEditando = undefined;
  }

  /* El formulario guardó: se cierra y se vuelve a pedir la lista. */
  alGuardarProyecto(): void {
    this.cerrarFormularioProyecto();
    this.recargarProyectos();
  }

  /* Abre el panel de expertos de un proyecto (cierra el formulario si estaba abierto). */
  gestionarExpertos(proyecto: ProyectoDeRestauracion): void {
    this.mostrarFormularioProyecto = false;
    this.proyectoEditando = undefined;
    this.idConfirmandoEliminarProyecto = 0;
    this.errorProyecto = '';
    this.idProyectoGestionando = proyecto.idProyecto;
  }

  cerrarGestionExpertos(): void {
    this.idProyectoGestionando = 0;
  }

  /* Vuelve a pedir los proyectos del juego (cambian los expertos, el estado, etc.). */
  recargarProyectos(): void {
    if (!this.videojuego) {
      return;
    }

    this.api.obtenerProyectos(this.videojuego.idVideojuego).subscribe({
      next: (proyectos) => this.alRecibirProyectos(proyectos),
      error: (error) => console.error('No se pudieron recargar los proyectos', error)
    });
  }

  /* Primer paso de eliminar un proyecto: confirmación debajo de su tarjeta. */
  pedirEliminarProyecto(proyecto: ProyectoDeRestauracion): void {
    this.errorProyecto = '';
    this.idConfirmandoEliminarProyecto = proyecto.idProyecto;
  }

  cancelarEliminarProyecto(): void {
    this.idConfirmandoEliminarProyecto = 0;
  }

  /* Segundo paso: DELETE /api/proyectos/{id}; borra también sus participaciones. */
  confirmarEliminarProyecto(proyecto: ProyectoDeRestauracion): void {
    this.api.eliminarProyecto(proyecto.idProyecto).subscribe({
      next: () => this.alEliminarProyecto(),
      error: (error) => this.alFallarEliminarProyecto(error)
    });
  }

  private alEliminarProyecto(): void {
    this.idConfirmandoEliminarProyecto = 0;
    this.recargarProyectos();
  }

  private alFallarEliminarProyecto(error: HttpErrorResponse): void {
    console.error('No se pudo eliminar el proyecto', error);
    this.idConfirmandoEliminarProyecto = 0;

    if (error.status === 403) {
      this.errorProyecto = 'Solo un administrador puede eliminar proyectos.';
    } else if (error.status === 404) {
      this.errorProyecto = 'Ese proyecto ya no existe.';
    } else {
      this.errorProyecto = 'No se pudo eliminar el proyecto. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /*
   * Llegan las copias. Si alguna tiene custodia vigente, se pide la lista de
   * instituciones para armar la columna derecha, y se carga el historial de
   * la primera copia.
   */
  private alRecibirCopias(copias: Copia[]): void {
    this.copias = copias;
    this.numeroCopias = copias.length;
    this.instituciones = [];
    this.cambios.markForCheck();

    // Se conserva la copia elegida si sigue existiendo; si no, se usa la primera.
    if (copias.length > 0) {
      if (this.copiaExiste(this.idCopiaHistorial, copias)) {
        this.verHistorialCustodia(this.idCopiaHistorial);
      } else {
        this.verHistorialCustodia(copias[0].idCopia);
      }

      // GET /api/instituciones (columna derecha y selector del formulario de custodia)
      this.api.obtenerInstituciones().subscribe({
        next: (lista) => this.alRecibirInstituciones(lista),
        error: (error) => console.error('No se pudieron cargar las instituciones', error)
      });
    } else {
      this.idCopiaHistorial = 0;
      this.custodiasHistorial = [];
    }
  }

  /* true si una copia con ese id está en la lista. */
  private copiaExiste(idCopia: number, copias: Copia[]): boolean {
    for (const copia of copias) {
      if (copia.idCopia === idCopia) {
        return true;
      }
    }
    return false;
  }

  /* Editar una copia: lleva a /copias/:id/editar. */
  editarCopia(idCopia: number): void {
    this.router.navigate(['/copias', idCopia, 'editar']);
  }

  /* Primer paso de eliminar una copia: confirmación sobre la lista. */
  pedirEliminarCopia(idCopia: number): void {
    this.errorCopia = '';
    this.idConfirmandoEliminarCopia = idCopia;
  }

  cancelarEliminarCopia(): void {
    this.idConfirmandoEliminarCopia = 0;
  }

  /* Segundo paso: DELETE /api/copias/{id}. 409 si la copia tiene custodias. */
  confirmarEliminarCopia(): void {
    this.api.eliminarCopia(this.idConfirmandoEliminarCopia).subscribe({
      next: () => this.alEliminarCopia(),
      error: (error) => this.alFallarEliminarCopia(error)
    });
  }

  private alEliminarCopia(): void {
    this.idConfirmandoEliminarCopia = 0;
    this.recargarCopias();
  }

  private alFallarEliminarCopia(error: HttpErrorResponse): void {
    console.error('No se pudo eliminar la copia', error);
    this.idConfirmandoEliminarCopia = 0;

    if (error.error && error.error.mensaje) {
      this.errorCopia = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorCopia = 'Solo un administrador puede eliminar copias.';
    } else {
      this.errorCopia = 'No se pudo eliminar la copia. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /* Vuelve a pedir las copias (cambian sus custodias vigentes y el historial). */
  recargarCopias(): void {
    if (!this.videojuego) {
      return;
    }

    this.api.obtenerCopias(this.videojuego.idVideojuego).subscribe({
      next: (copias) => this.alRecibirCopias(copias),
      error: (error) => this.mostrarError(error)
    });
  }

  /* Fecha de inicio de la custodia vigente del historial mostrado (la que no tiene fechaFin). */
  get inicioCustodiaVigente(): string | undefined {
    for (const custodia of this.custodiasHistorial) {
      if (!custodia.fechaFin) {
        return custodia.fechaInicio;
      }
    }
    return undefined;
  }

  /* Abre el formulario vacío para registrar (trasladar) una custodia. */
  abrirNuevaCustodia(): void {
    this.custodiaEditando = undefined;
    this.idConfirmandoEliminarCustodia = 0;
    this.errorCustodia = '';
    this.mostrarFormularioCustodia = true;
  }

  /* Abre el formulario con los datos de una custodia (editar). El id llega desde el historial. */
  abrirEditarCustodia(idCustodia: number): void {
    for (const custodia of this.custodiasHistorial) {
      if (custodia.idCustodia === idCustodia) {
        this.custodiaEditando = custodia;
        this.idConfirmandoEliminarCustodia = 0;
        this.errorCustodia = '';
        this.mostrarFormularioCustodia = true;
      }
    }
  }

  cerrarFormularioCustodia(): void {
    this.mostrarFormularioCustodia = false;
    this.custodiaEditando = undefined;
  }

  /* El formulario guardó: se cierra y se refrescan copias e historial. */
  alGuardarCustodia(): void {
    this.cerrarFormularioCustodia();
    this.recargarCopias();
  }

  /* Primer paso de eliminar una custodia: confirmación sobre el historial. */
  pedirEliminarCustodia(idCustodia: number): void {
    this.mostrarFormularioCustodia = false;
    this.custodiaEditando = undefined;
    this.errorCustodia = '';
    this.idConfirmandoEliminarCustodia = idCustodia;
  }

  cancelarEliminarCustodia(): void {
    this.idConfirmandoEliminarCustodia = 0;
  }

  /* Segundo paso: DELETE /api/custodias/{id}. Si era la vigente, la copia queda sin custodia. */
  confirmarEliminarCustodia(): void {
    this.api.eliminarCustodia(this.idConfirmandoEliminarCustodia).subscribe({
      next: () => this.alEliminarCustodia(),
      error: (error) => this.alFallarEliminarCustodia(error)
    });
  }

  private alEliminarCustodia(): void {
    this.idConfirmandoEliminarCustodia = 0;
    this.recargarCopias();
  }

  private alFallarEliminarCustodia(error: HttpErrorResponse): void {
    console.error('No se pudo eliminar la custodia', error);
    this.idConfirmandoEliminarCustodia = 0;

    if (error.status === 403) {
      this.errorCustodia = 'Solo un administrador puede eliminar custodias.';
    } else if (error.status === 404) {
      this.errorCustodia = 'Esa custodia ya no existe.';
    } else {
      this.errorCustodia = 'No se pudo eliminar la custodia. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /* true si al menos una copia tiene institución actual. */
  private hayCustodiaVigente(copias: Copia[]): boolean {
    for (const copia of copias) {
      if (copia.idInstitucionActual !== undefined) {
        return true;
      }
    }
    return false;
  }

  /* Llega el catálogo de instituciones: se guarda y se arma la columna derecha. */
  private alRecibirInstituciones(catalogo: Institucion[]): void {
    this.catalogoInstituciones = catalogo;
    this.instituciones = this.agruparInstituciones(this.copias, catalogo);
    this.cambios.markForCheck();
  }

  /*
   * Muestra el historial de custodia de una copia (botón de la tarjeta).
   * GET /api/copias/{id}/custodias. Si falla, el historial queda vacío.
   */
  verHistorialCustodia(idCopia: number): void {
    this.idCopiaHistorial = idCopia;
    this.custodiasHistorial = [];

    this.api.obtenerCustodias(idCopia).subscribe({
      next: (custodias) => this.alRecibirCustodias(idCopia, custodias),
      error: (error) => console.error('No se pudo cargar el historial de custodia', error)
    });
  }

  /* Solo se acepta la respuesta de la copia que sigue seleccionada. */
  private alRecibirCustodias(idCopia: number, custodias: Custodia[]): void {
    if (idCopia === this.idCopiaHistorial) {
      this.custodiasHistorial = custodias;
      this.cambios.markForCheck();
    }
  }

  /* Llega el juego: se muestra y se piden su estudio y las editoras. */
  private alRecibirVideojuego(juego: Videojuego): void {
    this.videojuego = juego;
    this.cambios.markForCheck();

    // GET /api/estudios/{idEstudio}
    if (juego.idEstudio !== undefined) {
      this.api.obtenerEstudio(juego.idEstudio).subscribe({
        next: (estudio) => this.alRecibirEstudio(estudio),
        error: (error) => this.mostrarError(error)
      });
    }

    // GET /api/editoras (se cruza con idsEditoras)
    const ids = juego.idsEditoras || [];
    if (ids.length > 0) {
      this.api.obtenerEditoras().subscribe({
        next: (editoras) => this.alRecibirEditoras(editoras, ids),
        error: (error) => this.mostrarError(error)
      });
    }
  }

  /* Guarda el estudio dentro del juego, para que textoEstudio lo muestre. */
  private alRecibirEstudio(estudio: Estudio): void {
    if (this.videojuego) {
      this.videojuego.estudio = estudio;
    }
    this.cambios.markForCheck();
  }

  /* Se queda solo con los nombres de las editoras cuyo id está en idsEditoras. */
  private alRecibirEditoras(editoras: CompaniaEditora[], ids: number[]): void {
    const nombres: string[] = [];

    for (const editora of editoras) {
      if (ids.includes(editora.idEditora)) {
        nombres.push(editora.nombre);
      }
    }

    this.nombresEditoras = nombres;
    this.cambios.markForCheck();
  }

  private alRecibirEstatusLegal(historial: EstatusLegal[]): void {
    this.historialLegal = historial;
    this.cambios.markForCheck();
  }

  private alRecibirEventos(eventos: EventoRiesgo[]): void {
    this.eventos = eventos;
    this.cambios.markForCheck();
  }

  /* El detalle del error queda en la consola (F12). */
  private mostrarError(error: unknown): void {
    console.error('No se pudo cargar la ficha del videojuego', error);
    this.hayError = true;
    this.cambios.markForCheck();
  }


  /*
   * ngOnInit se ejecuta una vez, cuando el componente ya está listo.
   * paramMap avisa cada vez que cambian los parámetros de la URL, así que la
   * ficha se actualiza aunque el usuario pase de /videojuegos/1 a /videojuegos/8
   * sin salir de la pantalla.
   */
  ngOnInit(): void {
    this.route.paramMap.subscribe({
      next: (parametros) => this.alCambiarParametros(parametros)
    });
  }

  /* Lee el :id de la URL y pide ese juego. */
  private alCambiarParametros(parametros: ParamMap): void {
    // En /videojuegos/8 el parámetro "id" vale '8', pero como TEXTO.
    // Number() lo convierte a número para poder compararlo con idVideojuego.
    const id = Number(parametros.get('id'));

    this.cargarJuego(id);
  }

  /* Borra el juego anterior, para no mostrar datos mezclados al cambiar de id. */
  private limpiarJuego(): void {
    this.confirmandoEliminar = false;
    this.errorEliminar = '';
    this.videojuego = undefined;
    this.nombresEditoras = [];
    this.hayError = false;
    this.numeroCopias = 0;
    this.numeroProyectos = 0;
    this.copias = [];
    this.instituciones = [];
    this.idCopiaHistorial = 0;
    this.custodiasHistorial = [];
    this.proyectos = [];
    this.catalogoInstituciones = [];
    this.idConfirmandoEliminarCopia = 0;
    this.errorCopia = '';
    this.mostrarFormularioCustodia = false;
    this.custodiaEditando = undefined;
    this.idConfirmandoEliminarCustodia = 0;
    this.errorCustodia = '';
    this.mostrarFormularioProyecto = false;
    this.proyectoEditando = undefined;
    this.idProyectoGestionando = 0;
    this.idConfirmandoEliminarProyecto = 0;
    this.errorProyecto = '';
    this.expertos = [];
    this.historialLegal = [];
    this.eventos = [];
  }

  /*
   * Arma la lista de instituciones custodias a partir de las copias.
   * Recorre las copias, y por cada una:
   *   - si no tiene institución actual, la salta
   *   - si su institución ya está en la lista, suma 1 a su contador
   *   - si no está, la busca en el catálogo y la agrega con contador 1
   * Ejemplo: copias del Museo, la Universidad y el Museo ->
   *   [ Museo (2 copias), Universidad (1 copia) ]
   */
  private agruparInstituciones(copias: Copia[], catalogo: Institucion[]): InstitucionConCopias[] {
    const lista: InstitucionConCopias[] = [];

    for (const copia of copias) {
      const idInstitucion = copia.idInstitucionActual;

      if (idInstitucion === undefined) {
        continue;
      }

      let existente: InstitucionConCopias | undefined = undefined;

      for (const entrada of lista) {
        if (entrada.institucion.idInstitucion === idInstitucion) {
          existente = entrada;
        }
      }

      if (existente) {
        existente.numeroCopias++;
      } else {
        lista.push({
          institucion: this.buscarInstitucion(idInstitucion, copia.nombreInstitucionActual, catalogo),
          numeroCopias: 1
        });
      }
    }

    return lista;
  }

  /*
   * Busca una institución por id en el catálogo. Si no aparece, arma una
   * mínima con el nombre que trae la copia, para que la tarjeta no quede vacía.
   */
  private buscarInstitucion(id: number, nombre: string | undefined, catalogo: Institucion[]): Institucion {
    for (const institucion of catalogo) {
      if (institucion.idInstitucion === id) {
        return institucion;
      }
    }

    return {
      idInstitucion: id,
      nombre: nombre || 'Institución ' + id,
      tipo: '—',
      pais: '—',
      telefonos: [],
      correos: [],
      cantidadCopiasCustodiadas: 0
    };
  }

  /* Primer paso: pide confirmación antes de eliminar. */
  pedirConfirmacionEliminar(): void {
    this.errorEliminar = '';
    this.confirmandoEliminar = true;
  }

  /* El usuario se arrepintió. */
  cancelarEliminar(): void {
    this.confirmandoEliminar = false;
  }

  /* Segundo paso: DELETE /api/videojuegos/{id}. Si sale bien, vuelve al catálogo. */
  confirmarEliminar(): void {
    if (!this.videojuego) {
      return;
    }

    this.eliminando = true;
    this.errorEliminar = '';

    this.api.eliminarVideojuego(this.videojuego.idVideojuego).subscribe({
      next: () => this.alEliminar(),
      error: (error) => this.alFallarEliminar(error)
    });
  }

  private alEliminar(): void {
    this.eliminando = false;
    this.router.navigate(['/catalogo']);
  }

  /*
   * 409: el back explica que el juego tiene datos asociados (copias, estatus
   * o eventos). 403: el rol no puede. Otro: mensaje general.
   */
  private alFallarEliminar(error: HttpErrorResponse): void {
    console.error('No se pudo eliminar el videojuego', error);
    this.eliminando = false;
    this.confirmandoEliminar = false;

    if (error.status === 409 && error.error && error.error.mensaje) {
      this.errorEliminar = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorEliminar = 'Solo un administrador puede eliminar videojuegos.';
    } else {
      this.errorEliminar = 'No se pudo eliminar el videojuego. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /* Despliega el formulario de estatus legal, limpio. */
  abrirFormularioEstatus(): void {
    this.editandoEstatus = undefined;
    this.idConfirmandoEliminarEstatus = 0;
    this.formularioEstatus.reset({ estatus: 'Protegido', fechaDeterminacion: this.hoy, observacion: '' });
    this.estatusEnviado = false;
    this.errorEstatus = '';
    this.mostrarFormularioEstatus = true;
  }

  /* Abre el formulario con los datos de un registro existente (editar). */
  abrirEditarEstatus(registro: EstatusLegal): void {
    this.editandoEstatus = registro;
    this.idConfirmandoEliminarEstatus = 0;
    this.formularioEstatus.reset({
      estatus: registro.estatus,
      fechaDeterminacion: registro.fechaDeterminacion,
      observacion: registro.observacion || ''
    });
    this.estatusEnviado = false;
    this.errorEstatus = '';
    this.mostrarFormularioEstatus = true;
  }

  /* Cierra el formulario sin guardar. */
  cancelarFormularioEstatus(): void {
    this.mostrarFormularioEstatus = false;
  }

  /* Dice si hay que mostrar el error de un campo del formulario de estatus. */
  mostrarErrorEstatus(nombre: string): boolean {
    const control = this.formularioEstatus.controls[nombre];
    return control.invalid && (control.touched || this.estatusEnviado);
  }

  /* Texto del error de un campo del formulario de estatus. */
  textoErrorEstatus(nombre: string): string {
    const control = this.formularioEstatus.controls[nombre];

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }
    if (control.hasError('maxlength')) {
      return 'Máximo 500 caracteres.';
    }
    return '';
  }

  /*
   * Guarda el estatus: valida, valida que la fecha no sea futura y envía
   * POST /api/videojuegos/{id}/estatus-legal.
   */
  guardarEstatus(): void {
    this.estatusEnviado = true;
    this.errorEstatus = '';

    if (this.formularioEstatus.invalid) {
      this.formularioEstatus.markAllAsTouched();
      return;
    }

    const datos = this.formularioEstatus.getRawValue();

    if (datos.fechaDeterminacion > this.hoy) {
      this.errorEstatus = 'La fecha no puede ser futura.';
      return;
    }

    const nuevo: EstatusLegalNuevo = {
      estatus: datos.estatus,
      fechaDeterminacion: datos.fechaDeterminacion
    };

    const observacion = String(datos.observacion || '').trim();
    if (observacion !== '' ) {
      nuevo.observacion = observacion;
    }

    if (!this.videojuego) {
      return;
    }

    this.guardandoEstatus = true;

    // Editando: PUT /api/estatus-legal/{id}. Agregando: POST del videojuego.
    if (this.editandoEstatus) {
      this.api.actualizarEstatusLegal(this.editandoEstatus.idEstatus, nuevo).subscribe({
        next: () => this.alGuardarEstatus(),
        error: (error) => this.alFallarEstatus(error)
      });
    } else {
      this.api.crearEstatusLegal(this.videojuego.idVideojuego, nuevo).subscribe({
        next: () => this.alGuardarEstatus(),
        error: (error) => this.alFallarEstatus(error)
      });
    }
  }

  /* Primer paso de eliminar un registro del historial: confirmación en la misma fila. */
  pedirEliminarEstatus(registro: EstatusLegal): void {
    this.errorEstatus = '';
    this.idConfirmandoEliminarEstatus = registro.idEstatus;
  }

  cancelarEliminarEstatus(): void {
    this.idConfirmandoEliminarEstatus = 0;
  }

  /* Segundo paso: DELETE /api/estatus-legal/{id}; luego se vuelve a pedir la lista. */
  confirmarEliminarEstatus(registro: EstatusLegal): void {
    this.api.eliminarEstatusLegal(registro.idEstatus).subscribe({
      next: () => this.alEliminarEstatus(),
      error: (error) => this.alFallarEliminarEstatus(error)
    });
  }

  /* Se eliminó: el vigente puede haber cambiado, así que se pide la lista completa. */
  private alEliminarEstatus(): void {
    this.idConfirmandoEliminarEstatus = 0;

    if (this.videojuego) {
      this.api.obtenerEstatusLegal(this.videojuego.idVideojuego).subscribe({
        next: (historial) => this.alRecibirEstatusLegal(historial),
        error: (error) => this.mostrarError(error)
      });
    }

    this.cambios.markForCheck();
  }

  private alFallarEliminarEstatus(error: HttpErrorResponse): void {
    console.error('No se pudo eliminar el estatus legal', error);
    this.idConfirmandoEliminarEstatus = 0;

    if (error.status === 403) {
      this.errorEstatus = 'Solo un administrador puede eliminar estatus legales.';
    } else if (error.status === 404) {
      this.errorEstatus = 'Ese registro ya no existe.';
    } else {
      this.errorEstatus = 'No se pudo eliminar el estatus. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /*
   * Se guardó. Se vuelve a pedir la lista completa porque el registro nuevo
   * puede haber cambiado cuál es el vigente.
   */
  private alGuardarEstatus(): void {
    this.guardandoEstatus = false;
    this.mostrarFormularioEstatus = false;

    if (this.videojuego) {
      this.api.obtenerEstatusLegal(this.videojuego.idVideojuego).subscribe({
        next: (historial) => this.alRecibirEstatusLegal(historial),
        error: (error) => this.mostrarError(error)
      });
    }

    this.cambios.markForCheck();
  }

  /* 400: el back explica qué está mal. 403: el rol no puede. Otro: mensaje general. */
  private alFallarEstatus(error: HttpErrorResponse): void {
    console.error('No se pudo guardar el estatus legal', error);
    this.guardandoEstatus = false;

    if (error.status === 400 && error.error && error.error.mensaje) {
      this.errorEstatus = error.error.mensaje;
    } else if (error.status === 403) {
      this.errorEstatus = 'Tu rol no permite guardar estatus legales.';
    } else {
      this.errorEstatus = 'No se pudo guardar el estatus. Inténtalo de nuevo.';
    }

    this.cambios.markForCheck();
  }

  /* Fecha de hoy 'yyyy-MM-dd' en hora local (toISOString daría la fecha en UTC). */
  private fechaHoy(): string {
    const ahora = new Date();
    const mes = String(ahora.getMonth() + 1).padStart(2, '0');
    const dia = String(ahora.getDate()).padStart(2, '0');

    return ahora.getFullYear() + '-' + mes + '-' + dia;
  }

  /* Cambia la pestaña activa cuando el usuario hace clic en una. */
  seleccionarPestana(pestana: Pestana): void {
    this.pestanaActiva = pestana;
  }

  /*
   * Convierte '2022-01-20' en '20/01/2022'.
   * split('-') parte el texto en tres pedazos: año, mes y día.
   */
  formatearFecha(fecha: string): string {
    const partes = fecha.split('-');
    if (partes.length !== 3) {
      return fecha;
    }
    return partes[2] + '/' + partes[1] + '/' + partes[0];
  }

  /*
   * Cuántos colores de relleno hay definidos en styles.css
   * (--portada-1 hasta --portada-6).
   */
  private totalColoresPortada: number = 6;

  /*
   * Color de la portada según el id del juego, así cada juego conserva
   * siempre el mismo color. Los colores NO están aquí: viven en styles.css
   * como variables. Este código solo elige el número y devuelve el nombre
   * de la variable, por ejemplo 'var(--portada-3)'.
   * El "%" hace que vuelva a empezar después del sexto color.
   */
  get colorPortada(): string {
    if (!this.videojuego) {
      return 'var(--portada-1)';
    }
    const numero = (this.videojuego.idVideojuego % this.totalColoresPortada) + 1;
    return 'var(--portada-' + numero + ')';
  }

  /* Iniciales del título: "Furia Quark" -> "FQ" */
  get iniciales(): string {
    if (!this.videojuego) {
      return '';
    }

    const palabras = this.videojuego.titulo.trim().split(' ');
    let resultado = '';

    for (const palabra of palabras) {
      if (palabra.length > 0 && resultado.length < 2) {
        resultado += palabra.charAt(0).toUpperCase();
      }
    }

    return resultado;
  }

  /* Línea de plataforma y año: "Arcade · 1994" */
  get plataformaYAnio(): string {
    if (!this.videojuego) {
      return '';
    }
    return this.videojuego.plataformaOriginal + ' · ' + this.videojuego.anioLanzamiento;
  }

  /*
   * Estatus legal vigente: el registro del historial con vigente === true.
   * Si no hay ninguno (lista vacía), el juego está "Sin determinar".
   */
  get estatusLegal(): string {
    for (const registro of this.historialLegal) {
      if (registro.vigente) {
        return registro.estatus;
      }
    }
    return 'Sin determinar';
  }

  /* Línea de editoras: "Editoras: A, B" o "Sin editora registrada". */
  get textoEditoras(): string {
    if (this.nombresEditoras.length === 0) {
      return 'Sin editora registrada';
    }
    return 'Editoras: ' + this.nombresEditoras.join(', ');
  }

  /* Una fuente que empieza por "http" se muestra como enlace. */
  esEnlace(fuente: string): boolean {
    return fuente.startsWith('http');
  }

  /* Línea del estudio: "Estudio: Quark Interactive (desaparecido)" */
  get textoEstudio(): string {
    if (!this.videojuego || !this.videojuego.estudio) {
      return '';
    }
    const estudio = this.videojuego.estudio;
    return 'Estudio: ' + estudio.nombre + ' (' + estudio.estadoActual + ')';
  }
}
