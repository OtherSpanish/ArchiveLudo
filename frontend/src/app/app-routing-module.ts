import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Inicio } from './features/inicio/inicio';
import { FichaVideojuego } from './features/videojuego/ficha-videojuego/ficha-videojuego';

const routes: Routes = [
  { path: '', component: Inicio },
  { path: 'videojuegos/:id', component: FichaVideojuego },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
