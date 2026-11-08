# ArchiveLudo — Frontend

Plataforma de preservación de videojuegos (proyecto final de Bases de Datos 1,
Universidad El Bosque). Este repositorio es el **frontend**: Angular 22 con
NgModules (componentes `standalone: false`). El back es Spring Boot + MySQL.

## Requisitos

- Node.js 20 o superior y npm
- Angular CLI (`npm install -g @angular/cli`)
- El back de ArchiveLudo corriendo en `http://localhost:8080`

## Puesta en marcha (PowerShell)

```powershell
npm install
ng serve
```

Abre `http://localhost:4200/`. La dirección del back está en
`src/app/core/archiveludo-api-service.ts` (propiedad `urlBase`).

## Sin el back: servidor de prueba (mock)

Para desarrollar sin la base de datos hay un servidor falso que imita el
contrato real del back. **No está en el repositorio** (está en `.gitignore`);
cada integrante lo tiene en su máquina en `mock-back/server.js`.

```powershell
node mock-back/server.js
ng serve
```

Los datos viven en memoria y se reinician al apagarlo. Usuarios de prueba
(contraseña `1234`): `admin@archiveludo.com`, `curador@archiveludo.com`,
`consulta@archiveludo.com`. Con el back real, apaga el mock (Ctrl + C) y no
cambies nada del front.

## Roles

| Rol | Puede |
| --- | --- |
| consulta | Ver todo |
| curador | Crear y editar: videojuegos, copias, custodias, estatus legal, eventos de riesgo, proyectos y sus expertos, y en Administración estudios, editoras, instituciones y expertos |
| administrador | Todo lo del curador, más eliminar registros y gestionar usuarios |

Los botones se muestran u ocultan con la directiva `*appHasRole`, y las rutas
se protegen con `roleGuard` (`data.roles`). Es solo comodidad visual: quien
decide si una acción se permite es el back (responde 403).

La sesión se guarda con el token JWT que entrega el back; un interceptor lo
envía en cada petición y, si el back responde 401, cierra la sesión.
**Nunca** se guardan contraseñas ni hashes en el front.

## Regla de arquitectura: el front no maneja lógica

El front **solo presenta**. Las reglas de negocio (validar fechas, calcular
totales, agrupar, ordenar, decidir prioridades) viven en el back y sus modelos.
Los formularios solo exigen los campos obligatorios, envían los datos tal cual
y muestran el `mensaje` del error que devuelve el back (`{estado, mensaje}`).
Lo que todavía se calcula en el cliente está listado en el documento de
pedidos al back (se entrega aparte, no está en el repositorio) y se quitará cuando el
back entregue los endpoints.

## Estructura

```
src/app/
  core/            servicio de la API, sesión, guards, interceptor
  shared/          modelos, tarjetas, barra superior, directiva *appHasRole
    components/    copia-card, timeline-custodia, proyecto-card,
                   institucion-card, estado-badge, ...
  features/
    inicio/ catalogo/ en-riesgo/ proyectos/ reportes/ login/ no-encontrado/
    videojuego/       ficha (copias, proyectos, estatus, eventos), formulario
                      del juego, registrar/editar copia, formulario de
                      custodia, de proyecto, de evento y gestión de expertos
                      del proyecto
    instituciones/    listado y ficha de institución
    administracion/   estudios, editoras, instituciones, expertos, usuarios
```

## Pantallas conectadas al back y pendientes

El front consume el contrato v2 del back: copias con su custodia vigente,
historial de custodias, expertos, proyectos con participaciones, eventos de
riesgo y el conteo de copias por institución.

Pendiente del back (el front hoy lo calcula o lo muestra como "no disponible"):
resumen de Inicio, reportes de proyectos, custodias y expertos, detalle de
copias por institución, informe final en PDF. El detalle y el contrato
propuesto están en el documento de pedidos al back (se entrega aparte).

## Comandos útiles

```powershell
ng build                               # compilar
npx ngc --noEmit -p tsconfig.app.json  # revisar tipos Y plantillas HTML
npm run docs                           # generar la documentación del código (carpeta documentation/)
```
