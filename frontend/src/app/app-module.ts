import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { tokenInterceptor } from './core/token-interceptor';
import { SharedModule } from './shared/shared-module';
import { AuthModule } from './features/auth/auth-module';
import { InicioModule } from './features/inicio/inicio-module';
import { AdministracionModule } from './features/administracion/administracion-module';
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
    AdministracionModule,
    CatalogoModule,
    InstitucionesModule,
    EnRiesgoModule,
    ProyectosModule,
    ReportesModule,
    // Siempre el último: su ruta "**" acepta cualquier URL.
    NoEncontradoModule
  ],
  providers: [
    // withInterceptors registra el interceptor del token en todas las peticiones.
    provideHttpClient(withInterceptors([tokenInterceptor]))
  ],
  bootstrap: [App]
})
export class AppModule { }
