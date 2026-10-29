import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { SharedModule } from './shared/shared-module';
import { AuthModule } from './features/auth/auth-module';
import { InicioModule } from './features/inicio/inicio-module';
import { VideojuegoModule } from './features/videojuego/videojuego-module';
import { CatalogoModule } from './features/catalogo/catalogo-module';
import { InstitucionesModule } from './features/instituciones/instituciones-module';
import { EnRiesgoModule } from './features/en-riesgo/en-riesgo-module';
import { ProyectosModule } from './features/proyectos/proyectos-module';
import { ReportesModule } from './features/reportes/reportes-module';
import { NoEncontradoModule } from './features/no-encontrado/no-encontrado-module';

@NgModule({
  declarations: [
    App
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    SharedModule,
    AuthModule,
    InicioModule,
    VideojuegoModule,
    CatalogoModule,
    InstitucionesModule,
    EnRiesgoModule,
    ProyectosModule,
    ReportesModule,
    // Siempre el último: su ruta "**" acepta cualquier URL.
    NoEncontradoModule
  ],
  providers: [
    provideHttpClient()
  ],
  bootstrap: [App]
})
export class AppModule { }
