import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Copia, CopiaNueva } from '../shared/models/copia-model';
import { Custodia } from '../shared/models/custodia-model';
import { EstatusLegal } from '../shared/models/estatus-legal-model';
import { EventoRiesgo } from '../shared/models/evento-riesgo-model';
import { ProyectoConJuego } from '../shared/models/proyecto-con-juego-model';
import { ReporteTabla, TipoReporte } from '../shared/models/reporte-model';
import { CredencialesLogin, RespuestaLogin } from '../shared/models/sesion-model';
import {
  CopiaConCustodia,
  DatosFicha,
  DatosInstitucion,
  InstitucionConCopias,
  JuegosPorEstado,
  ProyectoConDetalle,
  ResumenInicio
} from '../shared/models/api-model';

/*
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

  /*
   * Dirección base de la API. Ajusta el puerto si tu Spring Boot corre en otro.
   * Si más adelante hay un servidor de producción, solo se cambia esta línea.
   */
  private urlBase: string = 'http://localhost:8080/api';

  /* Angular entrega solo el HttpClient, por inyección de dependencias. */
  constructor(private http: HttpClient) { }

  /*
   * POST /api/auth/login: inicia sesión.
   * Envía usuario y contraseña; el back responde con el token JWT y los
   * datos del usuario, o con 401 si las credenciales no son válidas.
   */
  iniciarSesion(credenciales: CredencialesLogin): Observable<RespuestaLogin> {
    return this.http.post<RespuestaLogin>(this.urlBase + '/auth/login', credenciales);
  }

  /* GET /api/inicio/resumen: contadores y banner de prioridad. */
  obtenerResumenInicio(): Observable<ResumenInicio> {
    return this.http.get<ResumenInicio>(this.urlBase + '/inicio/resumen');
  }

  /*
   * GET /api/videojuegos?estado=en_riesgo&limite=10
   * "limite" es cuántos juegos trae para el carrusel; el total de la
   * categoría llega aparte, dentro de la respuesta.
   */
  obtenerVideojuegosPorEstado(estado: string, limite: number): Observable<JuegosPorEstado> {
    const parametros = new HttpParams()
      .set('estado', estado)
      .set('limite', limite);

    return this.http.get<JuegosPorEstado>(this.urlBase + '/videojuegos', { params: parametros });
  }

  /*
   * GET /api/videojuegos?pagina=0&limite=20&estado=en_riesgo&q=torre
   * Búsqueda del Catálogo. Usa la misma ruta que la anterior, pero aquí:
   *   - "estado" es opcional: si viene vacío, no se manda y trae todos los estados
   *   - "q" es opcional: texto que debe contener el título
   *   - "pagina" empieza en 0 y "limite" es el tamaño de cada página
   * La respuesta tiene la misma forma: { total, juegos }, donde "total" es
   * la cantidad de coincidencias en toda la base, no solo en esta página.
   */
  buscarVideojuegos(estado: string, texto: string, pagina: number, tamano: number): Observable<JuegosPorEstado> {
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

    return this.http.get<JuegosPorEstado>(this.urlBase + '/videojuegos', { params: parametros });
  }

  /* GET /api/videojuegos/{id}: cabecera de la ficha. */
  obtenerFicha(idVideojuego: number): Observable<DatosFicha> {
    return this.http.get<DatosFicha>(this.urlBase + '/videojuegos/' + idVideojuego);
  }

  /* GET /api/videojuegos/{id}/copias: copias de un videojuego con su custodia vigente. */
  obtenerCopias(idVideojuego: number): Observable<CopiaConCustodia[]> {
    return this.http.get<CopiaConCustodia[]>(this.urlBase + '/videojuegos/' + idVideojuego + '/copias');
  }

  /*
   * POST /api/videojuegos/{id}/copias: registra una copia nueva.
   * El cuerpo es una CopiaNueva; el back responde 201 con la Copia ya creada.
   */
  crearCopia(idVideojuego: number, copia: CopiaNueva): Observable<Copia> {
    return this.http.post<Copia>(this.urlBase + '/videojuegos/' + idVideojuego + '/copias', copia);
  }

  /* GET /api/videojuegos/{id}/proyectos: proyectos de restauración con sus expertos. */
  obtenerProyectos(idVideojuego: number): Observable<ProyectoConDetalle[]> {
    return this.http.get<ProyectoConDetalle[]>(this.urlBase + '/videojuegos/' + idVideojuego + '/proyectos');
  }

  /*
   * GET /api/proyectos: TODOS los proyectos de restauración, de cualquier
   * videojuego, cada uno con sus expertos y el videojuego al que pertenece.
   * Del más reciente al más antiguo (por fechaInicio). Es para la pantalla
   * global de Proyectos; el filtro por estado se hace en el front.
   */
  obtenerTodosLosProyectos(): Observable<ProyectoConJuego[]> {
    return this.http.get<ProyectoConJuego[]>(this.urlBase + '/proyectos');
  }

  /*
   * GET /api/videojuegos/{id}/estatus-legal: historial de estatus legal,
   * del más reciente al más antiguo. El primero es el vigente.
   */
  obtenerEstatusLegal(idVideojuego: number): Observable<EstatusLegal[]> {
    return this.http.get<EstatusLegal[]>(this.urlBase + '/videojuegos/' + idVideojuego + '/estatus-legal');
  }

  /*
   * GET /api/videojuegos/{id}/eventos-riesgo: bitácora de eventos de riesgo,
   * del más reciente al más antiguo.
   */
  obtenerEventosRiesgo(idVideojuego: number): Observable<EventoRiesgo[]> {
    return this.http.get<EventoRiesgo[]>(this.urlBase + '/videojuegos/' + idVideojuego + '/eventos-riesgo');
  }

  /* GET /api/copias/{id}/custodias: historial de custodia de una copia. */
  obtenerCustodias(idCopia: number): Observable<Custodia[]> {
    return this.http.get<Custodia[]>(this.urlBase + '/copias/' + idCopia + '/custodias');
  }

  /* GET /api/instituciones: lista de instituciones con su número de copias. */
  obtenerInstituciones(): Observable<InstitucionConCopias[]> {
    return this.http.get<InstitucionConCopias[]>(this.urlBase + '/instituciones');
  }

  /* GET /api/instituciones/{id}: ficha de una institución con sus contactos y copias. */
  obtenerInstitucion(idInstitucion: number): Observable<DatosInstitucion> {
    return this.http.get<DatosInstitucion>(this.urlBase + '/instituciones/' + idInstitucion);
  }

  /*
   * GET /api/reportes/{tipo}?idVideojuego=8&desde=2025-01-01&hasta=2025-12-31
   * Reporte de trazabilidad de un videojuego, a partir de las tablas de
   * historial del back. "desde" y "hasta" son opcionales (yyyy-MM-dd): si
   * vienen vacíos no se mandan y el reporte sale completo.
   */
  obtenerReporte(tipo: TipoReporte, idVideojuego: number, desde: string, hasta: string): Observable<ReporteTabla> {
    let parametros = new HttpParams().set('idVideojuego', idVideojuego);

    if (desde) {
      parametros = parametros.set('desde', desde);
    }

    if (hasta) {
      parametros = parametros.set('hasta', hasta);
    }

    return this.http.get<ReporteTabla>(this.urlBase + '/reportes/' + tipo, { params: parametros });
  }
}
