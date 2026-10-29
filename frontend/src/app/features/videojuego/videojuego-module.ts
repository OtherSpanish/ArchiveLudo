import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../../shared/shared-module';
import { FichaVideojuegoComponent } from './ficha-videojuego/ficha-videojuego-component';
import { RegistrarCopiaComponent } from './registrar-copia/registrar-copia-component';

/*
 * Pantallas de un videojuego: la ficha y el formulario para registrar copias.
 * Sus rutas se declaran en AppRoutingModule.
 * ReactiveFormsModule da las herramientas del formulario (formGroup, formControlName).
 */
@NgModule({
  declarations: [FichaVideojuegoComponent, RegistrarCopiaComponent],
  imports: [CommonModule, RouterModule, ReactiveFormsModule, SharedModule],
  exports: [FichaVideojuegoComponent, RegistrarCopiaComponent],
})
export class VideojuegoModule {}
