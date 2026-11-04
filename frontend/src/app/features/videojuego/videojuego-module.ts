import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../../shared/shared-module';
import { FichaVideojuegoComponent } from './ficha-videojuego/ficha-videojuego-component';
import { FormularioVideojuegoComponent } from './formulario-videojuego/formulario-videojuego-component';
import { FormularioEventoComponent } from './formulario-evento/formulario-evento-component';
import { FormularioCustodiaComponent } from './formulario-custodia/formulario-custodia-component';
import { FormularioProyectoComponent } from './formulario-proyecto/formulario-proyecto-component';
import { GestionExpertosComponent } from './gestion-expertos/gestion-expertos-component';
import { InformeFinalComponent } from './informe-final/informe-final-component';
import { RegistrarCopiaComponent } from './registrar-copia/registrar-copia-component';

/**
 * Pantallas de un videojuego: la ficha y el formulario para registrar copias.
 * Sus rutas se declaran en AppRoutingModule.
 * ReactiveFormsModule da las herramientas del formulario (formGroup, formControlName).
 */
@NgModule({
  declarations: [
    FichaVideojuegoComponent,
    RegistrarCopiaComponent,
    FormularioVideojuegoComponent,
    FormularioProyectoComponent,
    FormularioCustodiaComponent,
    FormularioEventoComponent,
    GestionExpertosComponent,
    InformeFinalComponent
  ],
  imports: [CommonModule, RouterModule, ReactiveFormsModule, SharedModule],
  exports: [FichaVideojuegoComponent, RegistrarCopiaComponent, FormularioVideojuegoComponent],
})
export class VideojuegoModule {}
