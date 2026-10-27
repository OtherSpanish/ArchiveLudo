import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { SharedModule } from '../../shared/shared-module';
import { Inicio } from './inicio';

@NgModule({
  declarations: [
    Inicio
  ],
  imports: [
    CommonModule,
    RouterModule,
    SharedModule
  ],
  exports: [
    Inicio
  ]
})
export class InicioModule { }
