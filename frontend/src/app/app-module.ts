import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { SharedModule } from './shared/shared-module';
import { InicioModule } from './features/inicio/inicio-module';
import { VideojuegoModule } from './features/videojuego/videojuego-module';

@NgModule({
  declarations: [App],
  imports: [BrowserModule, AppRoutingModule, SharedModule, InicioModule, VideojuegoModule],
  providers: [
    // Habilita HttpClient en toda la app, para poder llamar a la API
    provideHttpClient(),
  ],
  bootstrap: [App],
})
export class AppModule {}
