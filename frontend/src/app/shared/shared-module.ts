import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { BarraSuperior } from './components/barra-superior/barra-superior';
import { EstadoBadge } from './components/estado-badge/estado-badge';
import { JuegoCard } from './components/juego-card/juego-card';
import { StatCard } from './components/stat-card/stat-card';

@NgModule({
  declarations: [BarraSuperior, EstadoBadge, JuegoCard, StatCard],
  imports: [CommonModule, RouterModule],
  exports: [BarraSuperior, EstadoBadge, JuegoCard, StatCard],
})
export class SharedModule {}
