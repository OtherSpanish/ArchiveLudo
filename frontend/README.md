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
| curador | Crear y editar (videojuegos, copias, estatus, estudios, editoras, instituciones) |
| administrador | Todo lo anterior, eliminar registros y gestionar usuarios |

La sesión se guarda con el token JWT que entrega el back; un interceptor lo
envía en cada petición y, si el back responde 401, cierra la sesión.
**Nunca** se guardan contraseñas ni hashes en el front.

## Estructura

```
src/app/
  core/            servicio de la API, sesión, guards, interceptor
  shared/          modelos, tarjetas, barra superior, directiva *appHasRole
  features/
    inicio/ catalogo/ en-riesgo/ videojuego/ instituciones/
    proyectos/ reportes/ login/ no-encontrado/
    administracion/   estudios, editoras, instituciones, usuarios
```

## Pantallas conectadas al back y pendientes

Ver [`PENDIENTES_BACK.md`](PENDIENTES_BACK.md).

## Comandos útiles

```powershell
ng build                               # compilar
npx ngc --noEmit -p tsconfig.app.json  # revisar tipos Y plantillas HTML
```
