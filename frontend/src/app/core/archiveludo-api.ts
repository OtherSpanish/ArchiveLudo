import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Custodia } from '../shared/models/custodia.model';
import {
  CopiaConCustodia,
  DatosFicha,
  DatosInstitucion,
  JuegosPorEstado,
  ResumenInicio,
} from '../shared/models/api.model';

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
  providedIn: 'root',
})
export class ArchiveludoApi {
  /*
   * Dirección base de la API. Ajusta el puerto si tu Spring Boot corre en otro.
   * Si más adelante hay un servidor de producción, solo se cambia esta línea.
   */
  private urlBase: string = 'http://localhost:8080/api';

  /* Angular entrega solo el HttpClient, por inyección de dependencias. */
  constructor(private http: HttpClient) {}

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
    const parametros = new HttpParams().set('estado', estado).set('limite', limite);

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
  buscarVideojuegos(
    estado: string,
    texto: string,
    pagina: number,
    tamano: number,
  ): Observable<JuegosPorEstado> {
    // HttpParams es inmutable: cada .set() devuelve un objeto nuevo,
    // por eso se vuelve a guardar en la misma variable.
    let parametros = new HttpParams().set('pagina', pagina).set('limite', tamano);

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
    return this.http.get<CopiaConCustodia[]>(
      this.urlBase + '/videojuegos/' + idVideojuego + '/copias',
    );
  }

  /* GET /api/copias/{id}/custodias: historial de custodia de una copia. */
  obtenerCustodias(idCopia: number): Observable<Custodia[]> {
    return this.http.get<Custodia[]>(this.urlBase + '/copias/' + idCopia + '/custodias');
  }

  /* GET /api/instituciones/{id}: ficha de una institución con sus contactos y copias. */
  obtenerInstitucion(idInstitucion: number): Observable<DatosInstitucion> {
    return this.http.get<DatosInstitucion>(this.urlBase + '/instituciones/' + idInstitucion);
  }
}
