import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { HasRoleDirective } from './directives/has-role-directive';
import { BarraSuperiorComponent } from './components/barra-superior/barra-superior-component';
import { EstadoBadgeComponent } from './components/estado-badge/estado-badge-component';
import { JuegoCardComponent } from './components/juego-card/juego-card-component';
import { StatCardComponent } from './components/stat-card/stat-card-component';
import { BannerPrioridadComponent } from './components/banner-prioridad/banner-prioridad-component';
import { CopiaCardComponent } from './components/copia-card/copia-card-component';
import { InstitucionCardComponent } from './components/institucion-card/institucion-card-component';
import { TimelineCustodiaComponent } from './components/timeline-custodia/timeline-custodia-component';
import { ProyectoCardComponent } from './components/proyecto-card/proyecto-card-component';

/*
 * Módulo de piezas compartidas. Declara cada componente y directiva
 * reutilizable y los exporta, para que cualquier otro módulo que importe
 * SharedModule pueda usar sus etiquetas (<app-estado-badge>,
 * <app-proyecto-card>, etc.) y la directiva *appHasRole.
 *
 * RouterModule se importa porque algunas tarjetas llevan enlaces con routerLink.
 */
@NgModule({
  declarations: [
    HasRoleDirective,
    BarraSuperiorComponent,
    EstadoBadgeComponent,
    JuegoCardComponent,
    StatCardComponent,
    BannerPrioridadComponent,
    CopiaCardComponent,
    InstitucionCardComponent,
    TimelineCustodiaComponent,
    ProyectoCardComponent,
  ],
  imports: [CommonModule, RouterModule],
  exports: [
    HasRoleDirective,
    BarraSuperiorComponent,
    EstadoBadgeComponent,
    JuegoCardComponent,
    StatCardComponent,
    BannerPrioridadComponent,
    CopiaCardComponent,
    InstitucionCardComponent,
    TimelineCustodiaComponent,
    ProyectoCardComponent,
  ],
})
export class SharedModule {}
