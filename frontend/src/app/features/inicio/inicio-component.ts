import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Videojuego } from '../../shared/models/videojuego-model';
import { JuegoConMeta, PaginaVideojuegos } from '../../shared/models/api-model';
import { Copia } from '../../shared/models/copia-model';
import { Estudio } from '../../shared/models/videojuego-model';
import { ArchiveludoApiService } from '../../core/archiveludo-api-service';

/*
 * Un contador de la parte de arriba: etiqueta, número y color del punto.
 * Es un tipo solo del front: se arma con los totales que entrega el back.
 */
interface Contador {
  etiqueta: string;
  valor: number;
  color: 'info' | 'ok' | 'warn' | 'bad';
}

/*
 * Una fila del Inicio: "En riesgo crítico", "Totalmente perdidos" o "Restaurados con éxito".
 *  - color: el color del punto junto al título (coincide con las clases del CSS)
 *  - estado: valor de la base de datos, para pedirlo a la API y para el enlace "Ver todos"
 *  - total: cuántos juegos hay en esa categoría en total (no solo los visibles)
 */
interface FilaJuegos {
  titulo: string;
  color: 'ok' | 'warn' | 'bad';
  estado: string;
  total: number;
  juegos: JuegoConMeta[];
}

/*
 * Pantalla de Inicio. El back no tiene un endpoint de "resumen", así que
 * todo se arma con GET /api/videojuegos (la cabecera X-Total-Count da los
 * totales) y, para el banner, con las copias y el estudio del primer juego
 * en riesgo.
 */
@Component({
  selector: 'app-inicio',
  templateUrl: './inicio-component.html',
  styleUrl: './inicio-component.css',
  standalone: false,
})
export class InicioComponent implements OnInit {

  /* Total de videojuegos del catálogo (primer contador). */
  totalTitulos: number = 0;

  /* El "?" indica que al principio no tiene valor. El HTML ya revisa que exista antes de dibujar el banner. */
  juegoPrioritario?: Videojuego;

  descripcionPrioritario: string = '';

  criteriosPrioritario: string[] = [];

  /* true si alguna petición falló; se muestra un aviso en la pantalla. */
  hayError: boolean = false;

  /* Las tres filas existen desde el inicio (con total 0 y sin juegos); cada una se llena con su llamada. */
  filas: FilaJuegos[] = [
    { titulo: 'En riesgo crítico', color: 'warn', estado: 'en riesgo', total: 0, juegos: [] },
    { titulo: 'Totalmente perdidos', color: 'bad', estado: 'totalmente perdido', total: 0, juegos: [] },
    { titulo: 'Restaurados con éxito', color: 'ok', estado: 'restaurado', total: 0, juegos: [] }
  ];

  /* ChangeDetectorRef avisa a Angular que hay datos nuevos para redibujar. */
  constructor(private api: ArchiveludoApiService, private cambios: ChangeDetectorRef) { }

  /* Los contadores salen de los totales de las filas y del total del catálogo. */
  get contadores(): Contador[] {
    return [
      { etiqueta: 'Títulos catalogados', valor: this.totalTitulos, color: 'info' },
      { etiqueta: 'En riesgo', valor: this.filas[0].total, color: 'warn' },
      { etiqueta: 'Totalmente perdidos', valor: this.filas[1].total, color: 'bad' },
      { etiqueta: 'Restaurados', valor: this.filas[2].total, color: 'ok' }
    ];
  }

  /* Angular llama a ngOnInit una vez, cuando la pantalla está lista. */
  ngOnInit(): void {
    this.cargarTotal();

    for (const fila of this.filas) {
      this.cargarFila(fila);
    }
  }

  /* Total del catálogo: se pide un solo juego y se lee X-Total-Count. */
  private cargarTotal(): void {
    this.api.buscarVideojuegos('', '', 0, 1).subscribe({
      next: (pagina) => this.alRecibirTotal(pagina),
      error: (error) => this.mostrarError(error)
    });
  }

  private alRecibirTotal(pagina: PaginaVideojuegos): void {
    this.totalTitulos = pagina.total;
    this.cambios.markForCheck();
  }

  /* Pide los juegos de una fila: GET /api/videojuegos?estado=...&limite=10 */
  private cargarFila(fila: FilaJuegos): void {
    this.api.obtenerVideojuegosPorEstado(fila.estado, 10).subscribe({
      next: (pagina) => this.llenarFila(fila, pagina),
      error: (error) => this.mostrarError(error)
    });
  }

  private llenarFila(fila: FilaJuegos, pagina: PaginaVideojuegos): void {
    const juegos: JuegoConMeta[] = [];

    for (const juego of pagina.juegos) {
      juegos.push({ videojuego: juego, meta: '' });
    }

    fila.total = pagina.total;
    fila.juegos = juegos;

    // El banner usa el primer juego en riesgo.
    if (fila.estado === 'en riesgo' && pagina.juegos.length > 0) {
      this.prepararBanner(pagina.juegos[0]);
    }

    this.cambios.markForCheck();
  }

  /*
   * Arma el banner con el primer juego en riesgo. Los criterios salen de
   * datos reales: si no tiene copias (GET /videojuegos/{id}/copias) y si su
   * estudio desapareció (GET /estudios/{id}).
   */
  private prepararBanner(juego: Videojuego): void {
    this.juegoPrioritario = juego;
    this.descripcionPrioritario = juego.descripcion || '';
    this.criteriosPrioritario = ['Estado de conservación: en riesgo'];

    this.api.obtenerCopias(juego.idVideojuego).subscribe({
      next: (copias) => this.alRecibirCopiasBanner(copias),
      error: (error) => this.mostrarError(error)
    });

    if (juego.idEstudio !== undefined) {
      this.api.obtenerEstudio(juego.idEstudio).subscribe({
        next: (estudio) => this.alRecibirEstudioBanner(juego, estudio),
        error: (error) => this.mostrarError(error)
      });
    }
  }

  private alRecibirCopiasBanner(copias: Copia[]): void {
    if (copias.length === 0) {
      this.criteriosPrioritario = [...this.criteriosPrioritario, 'Sin copias registradas'];
      this.cambios.markForCheck();
    }
  }

  private alRecibirEstudioBanner(juego: Videojuego, estudio: Estudio): void {
    // Se crea una copia del juego con su estudio, para que el banner lo muestre en el subtítulo.
    this.juegoPrioritario = { ...juego, estudio: estudio };

    if (estudio.estadoActual === 'desaparecido') {
      this.criteriosPrioritario = [...this.criteriosPrioritario, 'Estudio desarrollador desaparecido'];
    }

    this.cambios.markForCheck();
  }

  /* El detalle queda en la consola (F12) y la pantalla muestra un aviso. */
  private mostrarError(error: unknown): void {
    console.error('No se pudo cargar la información del Inicio', error);
    this.hayError = true;
    this.cambios.markForCheck();
  }
}
