import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { SharedModule } from '../../shared/shared-module';
import { FichaVideojuego } from './ficha-videojuego/ficha-videojuego';

@NgModule({
  declarations: [FichaVideojuego],
  imports: [CommonModule, RouterModule, SharedModule],
  exports: [FichaVideojuego],
})
export class VideojuegoModule {}
