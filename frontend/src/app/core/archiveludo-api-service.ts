import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { Copia, CopiaNueva } from '../shared/models/copia-model';
import { Custodia, CustodiaNueva, Institucion, InstitucionNueva } from '../shared/models/custodia-model';
import { Experto, ExpertoNuevo } from '../shared/models/experto-model';
import { Participacion, ParticipacionEditada, ParticipacionNueva } from '../shared/models/participacion-model';
import { ProyectoDeRestauracion, ProyectoEditado, ProyectoNuevo } from '../shared/models/proyecto-model';
import { InformeFinal, InformeFinalSolicitud } from '../shared/models/informe-final-model';
import { EstatusLegal, EstatusLegalNuevo } from '../shared/models/estatus-legal-model';
import { EventoRiesgo, EventoRiesgoNuevo } from '../shared/models/evento-riesgo-model';
import { CompaniaEditora, EditoraNueva } from '../shared/models/compania-editora-model';
import { Estudio, EstudioNuevo, Videojuego, VideojuegoNuevo } from '../shared/models/videojuego-model';
import { FiltrosReporte, Reporte, TipoReporte } from '../shared/models/reporte-model';
import { CredencialesLogin, UsuarioLogin } from '../shared/models/sesion-model';
import { Usuario, UsuarioNuevo } from '../shared/models/usuario-model';
import {
  DatosFicha,
  ListaProyectos,
  PaginaVideojuegos
} from '../shared/models/api-model';
import { ResumenInicio } from '../shared/models/resumen-inicio-model';

/**
 * Servicio que concentra TODAS las llamadas a la API de Spring Boot.
 *
 * Cada función devuelve un Observable. Un Observable es "una respuesta que
 * todavía no llegó": la petición no se envía hasta que alguien hace
 * .subscribe() sobre él, y ahí se reciben los datos cuando el back responde.
 *
 * "providedIn: 'root'" hace que Angular cree una sola instancia para toda
 * la app y la entregue a quien la pida en su constructor.
 */
@Injectable({
  providedIn: 'root'
})
export class ArchiveludoApiService {

  /**
   * Dirección base de la API. Ajusta el puerto si tu Spring Boot corre en otro.
   * Si más adelante hay un servidor de producción, solo se cambia esta línea.
   */
  private urlBase: string = 'http://localhost:8080/api';

  /** Angular entrega solo el HttpClient, por inyección de dependencias. */
  constructor(private http: HttpClient) { }

  /**
   * POST /api/auth/login: inicia sesión.
   * Envía correo y contraseña. El back responde con los datos del usuario en
   * el cuerpo y con el token JWT en la CABECERA "Authorization"; por eso se
   * pide la respuesta completa ({ observe: 'response' }) y no solo el cuerpo.
   * Con credenciales malas responde 401.
   */
  iniciarSesion(credenciales: CredencialesLogin): Observable<HttpResponse<UsuarioLogin>> {
    return this.http.post<UsuarioLogin>(this.urlBase + '/auth/login', credenciales, { observe: 'response' });
  }

  /** GET /api/inicio/resumen: contadores, prioridad de intervención y las tres listas del Inicio. */
  obtenerResumenInicio(): Observable<ResumenInicio> {
    return this.http.get<ResumenInicio>(this.urlBase + '/inicio/resumen');
  }

  /**
   * GET /api/videojuegos?estado=en riesgo&limite=10
   * Para los carruseles del Inicio. Es la misma llamada que buscarVideojuegos,
   * pidiendo la primera página de "limite" juegos.
   */
  obtenerVideojuegosPorEstado(estado: string, limite: number): Observable<PaginaVideojuegos> {
    return this.buscarVideojuegos(estado, '', 0, limite);
  }

  /**
   * GET /api/videojuegos?pagina=0&limite=20&estado=en riesgo&q=torre
   * Búsqueda del Catálogo (y de En riesgo):
   *   - "estado" es opcional: si viene vacío, no se manda y trae todos
   *   - "q" es opcional: texto que debe contener el título
   *   - "pagina" empieza en 0 y "limite" es el tamaño de cada página
   *
   * El back responde con un ARREGLO de videojuegos en el cuerpo y el total
   * de coincidencias en la cabecera X-Total-Count. Por eso se pide la
   * respuesta completa ({ observe: 'response' }) y aquí se juntan las dos
   * cosas en un objeto { total, juegos }.
   */
  buscarVideojuegos(estado: string, texto: string, pagina: number, tamano: number): Observable<PaginaVideojuegos> {
    // HttpParams es inmutable: cada .set() devuelve un objeto nuevo,
    // por eso se vuelve a guardar en la misma variable.
    let parametros = new HttpParams()
      .set('pagina', pagina)
      .set('limite', tamano);

    if (estado) {
      parametros = parametros.set('estado', estado);
    }

    if (texto) {
      parametros = parametros.set('q', texto);
    }

    return this.http
      .get<Videojuego[]>(this.urlBase + '/videojuegos', { params: parametros, observe: 'response' })
      .pipe(map((respuesta) => this.armarPagina(respuesta)));
  }

  /** Junta el cuerpo (arreglo) y la cabecera X-Total-Count en un solo objeto. */
  private armarPagina(respuesta: HttpResponse<Videojuego[]>): PaginaVideojuegos {
    const juegos = respuesta.body || [];
    const cabecera = respuesta.headers.get('X-Total-Count');
    // Si por algún motivo falta la cabecera, se usa la cantidad recibida.
    const total = cabecera !== null ? Number(cabecera) : juegos.length;

    return { total: total, juegos: juegos };
  }

  /** GET /api/videojuegos/{id}: datos base del videojuego (con idEstudio e idsEditoras). */
  obtenerVideojuego(idVideojuego: number): Observable<Videojuego> {
    return this.http.get<Videojuego>(this.urlBase + '/videojuegos/' + idVideojuego);
  }

  /**
   * POST /api/videojuegos/{id}/imagen: sube (o reemplaza) la portada del videojuego.
   * La petición NO es JSON: es multipart/form-data y el archivo va en el campo "imagen".
   * No se pone Content-Type a mano: con un FormData el navegador lo arma solo.
   * Curador o administrador. Formatos JPG, PNG o WEBP, máximo 5 MB.
   * Responde con el videojuego completo, ya con la urlImagen nueva.
   */
  subirImagenVideojuego(idVideojuego: number, archivo: File): Observable<Videojuego> {
    const datos = new FormData();
    datos.append('imagen', archivo);
    return this.http.post<Videojuego>(this.urlBase + '/videojuegos/' + idVideojuego + '/imagen', datos);
  }

  /** DELETE /api/videojuegos/{id}/imagen: quita la portada. Solo administrador. */
  eliminarImagenVideojuego(idVideojuego: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/videojuegos/' + idVideojuego + '/imagen');
  }

  /** GET /api/estudios: todos los estudios (para el selector del formulario). */
  obtenerEstudios(): Observable<Estudio[]> {
    return this.http.get<Estudio[]>(this.urlBase + '/estudios');
  }

  /** POST /api/editoras (curador o administrador): responde 201 con la editora creada. */
  crearEditora(editora: EditoraNueva): Observable<CompaniaEditora> {
    return this.http.post<CompaniaEditora>(this.urlBase + '/editoras', editora);
  }

  /** PUT /api/editoras/{id}: el cuerpo es igual al del POST. */
  actualizarEditora(idEditora: number, editora: EditoraNueva): Observable<CompaniaEditora> {
    return this.http.put<CompaniaEditora>(this.urlBase + '/editoras/' + idEditora, editora);
  }

  /** DELETE /api/editoras/{id} (solo administrador): 204, o 409 si publica algún videojuego. */
  eliminarEditora(idEditora: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/editoras/' + idEditora);
  }

  /** POST /api/estudios (curador o administrador): responde 201 con el estudio creado. */
  crearEstudio(estudio: EstudioNuevo): Observable<Estudio> {
    return this.http.post<Estudio>(this.urlBase + '/estudios', estudio);
  }

  /** PUT /api/estudios/{id}: el cuerpo es igual al del POST. */
  actualizarEstudio(idEstudio: number, estudio: EstudioNuevo): Observable<Estudio> {
    return this.http.put<Estudio>(this.urlBase + '/estudios/' + idEstudio, estudio);
  }

  /** DELETE /api/estudios/{id} (solo administrador): 204, o 409 si algún videojuego lo usa. */
  eliminarEstudio(idEstudio: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/estudios/' + idEstudio);
  }

  /** POST /api/videojuegos (curador o administrador): responde 201 con el videojuego creado. */
  crearVideojuego(videojuego: VideojuegoNuevo): Observable<Videojuego> {
    return this.http.post<Videojuego>(this.urlBase + '/videojuegos', videojuego);
  }

  /**
   * DELETE /api/videojuegos/{id} (solo administrador): responde 204 sin cuerpo.
   * Responde 409 si el juego tiene copias, estatus legales o eventos de riesgo.
   */
  eliminarVideojuego(idVideojuego: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/videojuegos/' + idVideojuego);
  }

  /** PUT /api/videojuegos/{id}: el cuerpo es igual al del POST; idsEditoras reemplaza la lista. */
  actualizarVideojuego(idVideojuego: number, videojuego: VideojuegoNuevo): Observable<Videojuego> {
    return this.http.put<Videojuego>(this.urlBase + '/videojuegos/' + idVideojuego, videojuego);
  }

  /** GET /api/estudios/{id}: el estudio del juego (la ficha solo trae su id). */
  obtenerEstudio(idEstudio: number): Observable<Estudio> {
    return this.http.get<Estudio>(this.urlBase + '/estudios/' + idEstudio);
  }

  /** GET /api/editoras: todas las editoras; la ficha las cruza con idsEditoras. */
  obtenerEditoras(): Observable<CompaniaEditora[]> {
    return this.http.get<CompaniaEditora[]>(this.urlBase + '/editoras');
  }

  /**
   * GET /api/videojuegos/{id}/copias: copias de un videojuego.
   * El back todavía no manda la custodia vigente (llega con las custodias, C9).
   */
  obtenerCopias(idVideojuego: number): Observable<Copia[]> {
    return this.http.get<Copia[]>(this.urlBase + '/videojuegos/' + idVideojuego + '/copias');
  }

  /**
   * POST /api/videojuegos/{id}/copias: registra una copia nueva.
   * El cuerpo es una CopiaNueva; el back responde 201 con la Copia ya creada.
   */
  crearCopia(idVideojuego: number, copia: CopiaNueva): Observable<Copia> {
    return this.http.post<Copia>(this.urlBase + '/videojuegos/' + idVideojuego + '/copias', copia);
  }

  /** GET /api/videojuegos/{id}/proyectos: proyectos de restauración con sus expertos. */
  obtenerProyectos(idVideojuego: number): Observable<ProyectoDeRestauracion[]> {
    return this.http.get<ProyectoDeRestauracion[]>(this.urlBase + '/videojuegos/' + idVideojuego + '/proyectos');
  }

  /**
   * GET /api/proyectos?estado=activo: los proyectos de restauración de todos los
   * videojuegos, del más reciente al más antiguo, cada uno con sus participaciones,
   * el título y la plataforma de su juego. El back hace el filtro:
   *   - "estado" es opcional (activo, pausado o finalizado): si viene vacío no se
   *     manda y salen todos.
   * La cantidad llega en la cabecera X-Total-Count; aquí se junta con el cuerpo.
   */
  obtenerTodosLosProyectos(estado: string): Observable<ListaProyectos> {
    let parametros = new HttpParams();

    if (estado) {
      parametros = parametros.set('estado', estado);
    }

    return this.http
      .get<ProyectoDeRestauracion[]>(this.urlBase + '/proyectos', { params: parametros, observe: 'response' })
      .pipe(map((respuesta) => this.armarListaProyectos(respuesta)));
  }

  /** Junta el cuerpo (arreglo) y la cabecera X-Total-Count en un solo objeto. */
  private armarListaProyectos(respuesta: HttpResponse<ProyectoDeRestauracion[]>): ListaProyectos {
    const proyectos = respuesta.body || [];
    const cabecera = respuesta.headers.get('X-Total-Count');
    // Si por algún motivo falta la cabecera, se usa la cantidad recibida.
    const total = cabecera !== null ? Number(cabecera) : proyectos.length;

    return { total: total, proyectos: proyectos };
  }

  /**
   * GET /api/videojuegos/{id}/estatus-legal: historial de estatus legal,
   * del más reciente al más antiguo. El primero es el vigente.
   */
  obtenerEstatusLegal(idVideojuego: number): Observable<EstatusLegal[]> {
    return this.http.get<EstatusLegal[]>(this.urlBase + '/videojuegos/' + idVideojuego + '/estatus-legal');
  }

  /**
   * POST /api/videojuegos/{id}/estatus-legal (curador o administrador).
   * El back responde 201. Después hay que volver a pedir la lista, porque el
   * registro nuevo puede ser (o no) el nuevo vigente.
   */
  crearEstatusLegal(idVideojuego: number, estatus: EstatusLegalNuevo): Observable<EstatusLegal> {
    return this.http.post<EstatusLegal>(this.urlBase + '/videojuegos/' + idVideojuego + '/estatus-legal', estatus);
  }

  /**
   * GET /api/videojuegos/{id}/eventos-riesgo: bitácora de eventos de riesgo,
   * del más reciente al más antiguo.
   */
  obtenerEventosRiesgo(idVideojuego: number): Observable<EventoRiesgo[]> {
    return this.http.get<EventoRiesgo[]>(this.urlBase + '/videojuegos/' + idVideojuego + '/eventos-riesgo');
  }

  /** GET /api/copias/{id}/custodias: historial de custodia de una copia. */
  obtenerCustodias(idCopia: number): Observable<Custodia[]> {
    return this.http.get<Custodia[]>(this.urlBase + '/copias/' + idCopia + '/custodias');
  }

  /** GET /api/instituciones: lista de instituciones (con teléfonos y correos). */
  obtenerInstituciones(): Observable<Institucion[]> {
    return this.http.get<Institucion[]>(this.urlBase + '/instituciones');
  }

  /**
   * GET /api/videojuegos/{id}/instituciones: las instituciones que HOY custodian al menos
   * una copia del videojuego, en orden alfabético. Cada una trae cantidadCopiasDelVideojuego.
   * Si ninguna copia tiene custodia vigente llega []; si el juego no existe, 404.
   */
  obtenerInstitucionesDelVideojuego(idVideojuego: number): Observable<Institucion[]> {
    return this.http.get<Institucion[]>(this.urlBase + '/videojuegos/' + idVideojuego + '/instituciones');
  }

  /**
   * GET /api/instituciones/{id}/copias: las copias que la institución custodia HOY, de la
   * custodia más reciente a la más antigua. Cada una trae tituloVideojuego,
   * fechaInicioCustodiaActual y condicionAlmacenamientoActual.
   * Si no custodia nada llega []; si la institución no existe, 404.
   */
  obtenerCopiasDeInstitucion(idInstitucion: number): Observable<Copia[]> {
    return this.http.get<Copia[]>(this.urlBase + '/instituciones/' + idInstitucion + '/copias');
  }

  /** POST /api/instituciones (curador o administrador): responde 201 con la institución creada. */
  crearInstitucion(institucion: InstitucionNueva): Observable<Institucion> {
    return this.http.post<Institucion>(this.urlBase + '/instituciones', institucion);
  }

  /** PUT /api/instituciones/{id}: el cuerpo es igual al del POST; las listas reemplazan las anteriores. */
  actualizarInstitucion(idInstitucion: number, institucion: InstitucionNueva): Observable<Institucion> {
    return this.http.put<Institucion>(this.urlBase + '/instituciones/' + idInstitucion, institucion);
  }

  /** DELETE /api/instituciones/{id} (solo administrador): 204, o 409 si tiene custodias. */
  eliminarInstitucion(idInstitucion: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/instituciones/' + idInstitucion);
  }

  /** PUT /api/estatus-legal/{id}: mismo cuerpo que el POST; puede cambiar cuál es el vigente. */
  actualizarEstatusLegal(idEstatus: number, estatus: EstatusLegalNuevo): Observable<EstatusLegal> {
    return this.http.put<EstatusLegal>(this.urlBase + '/estatus-legal/' + idEstatus, estatus);
  }

  /** DELETE /api/estatus-legal/{id}: 204; nunca da 409. Si era el vigente, pasa a serlo el siguiente. */
  eliminarEstatusLegal(idEstatus: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/estatus-legal/' + idEstatus);
  }

  /** GET /api/usuarios (solo administrador): lista sin contraseñas. */
  obtenerUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.urlBase + '/usuarios');
  }

  /** POST /api/usuarios (solo administrador): la contraseña es obligatoria. */
  crearUsuario(usuario: UsuarioNuevo): Observable<Usuario> {
    return this.http.post<Usuario>(this.urlBase + '/usuarios', usuario);
  }

  /** PUT /api/usuarios/{id}: si no se envía contraseña, el back conserva la actual. */
  actualizarUsuario(idUsuario: number, usuario: UsuarioNuevo): Observable<Usuario> {
    return this.http.put<Usuario>(this.urlBase + '/usuarios/' + idUsuario, usuario);
  }

  /** DELETE /api/usuarios/{id} (solo administrador): 204, o 409 si tiene registros. */
  eliminarUsuario(idUsuario: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/usuarios/' + idUsuario);
  }

  /** GET /api/instituciones/{id}: una institución (con teléfonos y correos). */
  obtenerInstitucion(idInstitucion: number): Observable<Institucion> {
    return this.http.get<Institucion>(this.urlBase + '/instituciones/' + idInstitucion);
  }

  /**
   * GET /api/reportes/{tipo}?idVideojuego=8&desde=2025-01-01&hasta=2025-12-31
   * Reporte del archivo como tabla lista para pintar ({ titulo, columnas, filas }).
   * Los tres filtros son opcionales: los que vienen vacíos no se mandan.
   * Con filtros inválidos el back responde 400 (o 404 si el videojuego no existe)
   * con { estado, mensaje }.
   */
  obtenerReporte(tipo: TipoReporte, filtros: FiltrosReporte): Observable<Reporte> {
    let parametros = new HttpParams();

    if (filtros.idVideojuego) {
      parametros = parametros.set('idVideojuego', filtros.idVideojuego);
    }

    if (filtros.desde) {
      parametros = parametros.set('desde', filtros.desde);
    }

    if (filtros.hasta) {
      parametros = parametros.set('hasta', filtros.hasta);
    }

    return this.http.get<Reporte>(this.urlBase + '/reportes/' + tipo, { params: parametros });
  }

  // ---------- Eventos de riesgo (crear, editar, eliminar) ----------

  /** POST /api/videojuegos/{id}/eventos-riesgo */
  crearEventoRiesgo(idVideojuego: number, evento: EventoRiesgoNuevo): Observable<EventoRiesgo> {
    return this.http.post<EventoRiesgo>(this.urlBase + '/videojuegos/' + idVideojuego + '/eventos-riesgo', evento);
  }

  /** PUT /api/eventos-riesgo/{id}. No cambian ni el videojuego ni el usuario. */
  actualizarEventoRiesgo(idEvento: number, evento: EventoRiesgoNuevo): Observable<EventoRiesgo> {
    return this.http.put<EventoRiesgo>(this.urlBase + '/eventos-riesgo/' + idEvento, evento);
  }

  /** DELETE /api/eventos-riesgo/{id}. Solo administrador. Nunca da 409. */
  eliminarEventoRiesgo(idEvento: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/eventos-riesgo/' + idEvento);
  }

  // ---------- Copias (por id) ----------

  /** GET /api/copias/{id} */
  obtenerCopia(idCopia: number): Observable<Copia> {
    return this.http.get<Copia>(this.urlBase + '/copias/' + idCopia);
  }

  /**
   * PUT /api/copias/{id}
   * El tipoCopia debe ser el mismo que tiene guardado la copia; si no, el back da 400.
   */
  actualizarCopia(idCopia: number, copia: CopiaNueva): Observable<Copia> {
    return this.http.put<Copia>(this.urlBase + '/copias/' + idCopia, copia);
  }

  /** DELETE /api/copias/{id}. Solo administrador. 409 si la copia tiene custodias. */
  eliminarCopia(idCopia: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/copias/' + idCopia);
  }

  // ---------- Custodias ----------

  /**
   * POST /api/copias/{id}/custodias
   * Siempre crea la custodia vigente y cierra la anterior con la fechaInicio de la nueva.
   */
  crearCustodia(idCopia: number, custodia: CustodiaNueva): Observable<Custodia> {
    return this.http.post<Custodia>(this.urlBase + '/copias/' + idCopia + '/custodias', custodia);
  }

  /** PUT /api/custodias/{id}. Aquí sí se puede enviar fechaFin. */
  actualizarCustodia(idCustodia: number, custodia: CustodiaNueva): Observable<Custodia> {
    return this.http.put<Custodia>(this.urlBase + '/custodias/' + idCustodia, custodia);
  }

  /** DELETE /api/custodias/{id}. Solo administrador. */
  eliminarCustodia(idCustodia: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/custodias/' + idCustodia);
  }

  // ---------- Expertos ----------

  /** GET /api/expertos (ya ordenados por apellido y nombre) */
  obtenerExpertos(): Observable<Experto[]> {
    return this.http.get<Experto[]>(this.urlBase + '/expertos');
  }

  /** POST /api/expertos. El correo acepta cualquier dominio y no se puede repetir. */
  crearExperto(experto: ExpertoNuevo): Observable<Experto> {
    return this.http.post<Experto>(this.urlBase + '/expertos', experto);
  }

  /** PUT /api/expertos/{id} */
  actualizarExperto(idExperto: number, experto: ExpertoNuevo): Observable<Experto> {
    return this.http.put<Experto>(this.urlBase + '/expertos/' + idExperto, experto);
  }

  /** DELETE /api/expertos/{id}. Solo administrador. 409 si participa en proyectos. */
  eliminarExperto(idExperto: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/expertos/' + idExperto);
  }

  // ---------- Proyectos de restauración (contrato nuevo) ----------

  /** GET /api/proyectos/{id} */
  obtenerProyecto(idProyecto: number): Observable<ProyectoDeRestauracion> {
    return this.http.get<ProyectoDeRestauracion>(this.urlBase + '/proyectos/' + idProyecto);
  }

  /** POST /api/videojuegos/{id}/proyectos. Debe traer al menos una participación. */
  crearProyecto(idVideojuego: number, proyecto: ProyectoNuevo): Observable<ProyectoDeRestauracion> {
    return this.http.post<ProyectoDeRestauracion>(this.urlBase + '/videojuegos/' + idVideojuego + '/proyectos', proyecto);
  }

  /** PUT /api/proyectos/{id}. Solo los datos del proyecto; las participaciones van aparte. */
  actualizarProyecto(idProyecto: number, proyecto: ProyectoEditado): Observable<ProyectoDeRestauracion> {
    return this.http.put<ProyectoDeRestauracion>(this.urlBase + '/proyectos/' + idProyecto, proyecto);
  }

  /** DELETE /api/proyectos/{id}. Solo administrador. Borra también sus participaciones y su informe final (502 si Cloudinary falla; entonces no se borra nada). */
  eliminarProyecto(idProyecto: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/proyectos/' + idProyecto);
  }

  // ---------- Informe final del proyecto (PDF) ----------

  /**
   * GET /api/proyectos/{id}/informe: el enlace del PDF (solo idProyecto y urlInforme).
   * Curador o administrador; el rol consulta recibe 403. 404 si el proyecto no tiene informe.
   */
  obtenerInformeFinal(idProyecto: number): Observable<InformeFinal> {
    return this.http.get<InformeFinal>(this.urlBase + '/proyectos/' + idProyecto + '/informe');
  }

  /**
   * POST /api/proyectos/{id}/informe: el back arma el PDF con estos 3 datos y los del
   * proyecto, lo sube a Cloudinary y devuelve el enlace. Curador o administrador.
   * 400 si el proyecto no está finalizado o algún dato no es válido; 409 si ya tiene informe.
   */
  crearInformeFinal(idProyecto: number, informe: InformeFinalSolicitud): Observable<InformeFinal> {
    return this.http.post<InformeFinal>(this.urlBase + '/proyectos/' + idProyecto + '/informe', informe);
  }

  /** PUT /api/proyectos/{id}/informe: regenera el PDF (enlace nuevo; el anterior se borra). 404 si no tiene informe. */
  actualizarInformeFinal(idProyecto: number, informe: InformeFinalSolicitud): Observable<InformeFinal> {
    return this.http.put<InformeFinal>(this.urlBase + '/proyectos/' + idProyecto + '/informe', informe);
  }

  /** DELETE /api/proyectos/{id}/informe: borra el PDF. Solo administrador. */
  eliminarInformeFinal(idProyecto: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/proyectos/' + idProyecto + '/informe');
  }

  // ---------- Participaciones ----------

  /** GET /api/proyectos/{id}/participaciones */
  obtenerParticipaciones(idProyecto: number): Observable<Participacion[]> {
    return this.http.get<Participacion[]>(this.urlBase + '/proyectos/' + idProyecto + '/participaciones');
  }

  /** POST /api/proyectos/{id}/participaciones. Un experto solo puede estar una vez por proyecto. */
  crearParticipacion(idProyecto: number, participacion: ParticipacionNueva): Observable<Participacion> {
    return this.http.post<Participacion>(this.urlBase + '/proyectos/' + idProyecto + '/participaciones', participacion);
  }

  /** PUT /api/participaciones/{id}. No cambia ni el experto ni el proyecto. */
  actualizarParticipacion(idParticipacion: number, participacion: ParticipacionEditada): Observable<Participacion> {
    return this.http.put<Participacion>(this.urlBase + '/participaciones/' + idParticipacion, participacion);
  }

  /** DELETE /api/participaciones/{id}. Solo administrador. 409 si es la única del proyecto. */
  eliminarParticipacion(idParticipacion: number): Observable<void> {
    return this.http.delete<void>(this.urlBase + '/participaciones/' + idParticipacion);
  }
}
