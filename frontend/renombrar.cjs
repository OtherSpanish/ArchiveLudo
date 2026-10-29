/*
 * renombrar.cjs
 * Pasa los archivos de src/app/core, src/app/features y src/app/shared
 * al estilo con sufijo:
 *
 *   banner-prioridad.ts       -> banner-prioridad-component.ts
 *   banner-prioridad.html     -> banner-prioridad-component.html
 *   banner-prioridad.css      -> banner-prioridad-component.css
 *   banner-prioridad.spec.ts  -> banner-prioridad-component.spec.ts
 *   videojuego.model.ts       -> videojuego-model.ts
 *   archiveludo-api.ts        -> archiveludo-api-service.ts
 *
 * Tambien actualiza: los imports, templateUrl, styleUrl y los nombres
 * de las clases (Inicio -> InicioComponent, ArchiveludoApi -> ArchiveludoApiService).
 *
 * USO (desde la raiz del proyecto, donde esta package.json):
 *   node renombrar.cjs             -> SIMULA: solo muestra que cambiaria
 *   node renombrar.cjs --aplicar   -> hace los cambios de verdad
 */

const fs = require('fs');
const path = require('path');

const aplicar = process.argv.includes('--aplicar');
const carpetaApp = path.join(process.cwd(), 'src', 'app');
const carpetasARenombrar = ['core', 'features', 'shared'];

// ---------------------------------------------------------------
// 1. Utilidades sencillas
// ---------------------------------------------------------------

// Devuelve la lista de TODOS los archivos dentro de una carpeta (recursivo).
function listarArchivos(carpeta) {
  const resultado = [];
  const nombres = fs.readdirSync(carpeta);

  for (const nombre of nombres) {
    const ruta = path.join(carpeta, nombre);

    if (fs.statSync(ruta).isDirectory()) {
      const internos = listarArchivos(ruta);
      for (const interno of internos) {
        resultado.push(interno);
      }
    } else {
      resultado.push(ruta);
    }
  }
  return resultado;
}

// Dice si una linea es un comentario (para no tocar texto de explicacion).
function esComentario(linea) {
  const texto = linea.trim();
  return texto.startsWith('//') || texto.startsWith('/*') || texto.startsWith('*');
}

// Quita la extension .ts: 'a/b/inicio.ts' -> 'a/b/inicio'
function sinTs(ruta) {
  return ruta.substring(0, ruta.length - 3);
}

// ---------------------------------------------------------------
// 2. Calcular el nombre nuevo de cada archivo
// ---------------------------------------------------------------

// Devuelve el nombre nuevo, o '' si ese archivo no se renombra.
// 'tipo' queda en 'component', 'model' o 'service' para usarlo despues.
function calcularNuevo(ruta) {
  const nombre = path.basename(ruta);
  const carpetaPadre = path.basename(path.dirname(ruta));
  const primeraCarpeta = path.relative(carpetaApp, ruta).split(path.sep)[0];

  // Modelos: videojuego.model.ts -> videojuego-model.ts
  if (nombre.endsWith('.model.ts')) {
    return { nombre: nombre.replace('.model.ts', '-model.ts'), tipo: 'model' };
  }
  if (nombre.endsWith('.model.spec.ts')) {
    return { nombre: nombre.replace('.model.spec.ts', '-model.spec.ts'), tipo: 'model' };
  }

  // Servicios: lo que este directamente en core/
  if (primeraCarpeta === 'core' && path.dirname(ruta) === path.join(carpetaApp, 'core')) {
    if (nombre.endsWith('.spec.ts')) {
      const base = nombre.replace('.spec.ts', '');
      if (!base.endsWith('-service')) {
        return { nombre: base + '-service.spec.ts', tipo: 'service' };
      }
    } else if (nombre.endsWith('.ts')) {
      const base = nombre.replace('.ts', '');
      if (!base.endsWith('-service')) {
        return { nombre: base + '-service.ts', tipo: 'service' };
      }
    }
    return null;
  }

  // Componentes: el archivo se llama igual que su carpeta (inicio/inicio.ts)
  const base = nombre.split('.')[0];
  const resto = nombre.substring(base.length);
  if (base === carpetaPadre && !base.endsWith('-component')) {
    return { nombre: base + '-component' + resto, tipo: 'component' };
  }

  return null;
}

// ---------------------------------------------------------------
// 3. Armar el plan
// ---------------------------------------------------------------

const plan = [];                 // { desde, hacia, tipo }
const mapaImports = {};          // ruta vieja sin .ts -> ruta nueva sin .ts
const datosComponente = {};      // ruta del .ts del componente -> { baseVieja, baseNueva }
const clasesComponente = [];     // nombres de clases de componentes a renombrar

for (const carpeta of carpetasARenombrar) {
  const rutaCarpeta = path.join(carpetaApp, carpeta);
  if (!fs.existsSync(rutaCarpeta)) {
    console.log('No existe la carpeta ' + rutaCarpeta + ', se salta.');
    continue;
  }

  const archivos = listarArchivos(rutaCarpeta);
  for (const archivo of archivos) {
    const nuevo = calcularNuevo(archivo);
    if (nuevo === null) {
      continue;
    }

    const destino = path.join(path.dirname(archivo), nuevo.nombre);
    plan.push({ desde: archivo, hacia: destino, tipo: nuevo.tipo });

    const esTs = archivo.endsWith('.ts') && !archivo.endsWith('.spec.ts');
    if (esTs) {
      mapaImports[sinTs(archivo)] = sinTs(destino);
    }

    if (esTs && nuevo.tipo === 'component') {
      const baseVieja = path.basename(archivo).replace('.ts', '');
      const baseNueva = path.basename(destino).replace('.ts', '');
      datosComponente[archivo] = { baseVieja: baseVieja, baseNueva: baseNueva };

      // Busca el nombre de la clase: "export class Inicio"
      const contenido = fs.readFileSync(archivo, 'utf8');
      const coincidencia = contenido.match(/export class (\w+)/);
      if (coincidencia && !coincidencia[1].endsWith('Component')) {
        clasesComponente.push(coincidencia[1]);
      }
    }
  }
}

// ---------------------------------------------------------------
// 4. Reescribir el contenido de los .ts (imports, clases, templateUrl)
// ---------------------------------------------------------------

// Cambia un import si apunta a un archivo renombrado.
function cambiarImport(linea, rutaArchivo) {
  const coincidencia = linea.match(/from\s+(['"])(\.[^'"]*)\1/);
  if (!coincidencia) {
    return linea;
  }

  const especificador = coincidencia[2];
  const absoluta = path.resolve(path.dirname(rutaArchivo), especificador);
  const destino = mapaImports[absoluta];
  if (!destino) {
    return linea;
  }

  const corte = especificador.lastIndexOf('/');
  const nuevoEspecificador = especificador.substring(0, corte + 1) + path.basename(destino);
  return linea.replace(especificador, nuevoEspecificador);
}

// Cambia './inicio.html' por './inicio-component.html' (y lo mismo con css).
function cambiarUrls(linea, datos) {
  const extensiones = ['html', 'css', 'scss'];
  const comillas = ["'", '"'];

  for (const ext of extensiones) {
    for (const c of comillas) {
      const viejo = c + './' + datos.baseVieja + '.' + ext + c;
      const nuevo = c + './' + datos.baseNueva + '.' + ext + c;
      linea = linea.split(viejo).join(nuevo);
    }
  }
  return linea;
}

// Cambia el nombre de una clase por otro, sin tocar textos entre comillas.
function cambiarClase(linea, claseVieja, claseNueva) {
  const patron = new RegExp("(?<![\\w'\"`/.-])" + claseVieja + "(?![\\w'\"`-])", 'g');
  return linea.replace(patron, claseNueva);
}

const todosLosTs = listarArchivos(carpetaApp).filter(function (r) { return r.endsWith('.ts'); });
let archivosModificados = 0;

for (const archivo of todosLosTs) {
  const contenido = fs.readFileSync(archivo, 'utf8');
  const lineas = contenido.split('\n');
  const mostrar = [];

  for (let i = 0; i < lineas.length; i++) {
    const original = lineas[i];
    let linea = original;

    linea = cambiarImport(linea, archivo);

    if (datosComponente[archivo]) {
      linea = cambiarUrls(linea, datosComponente[archivo]);
    }

    // El servicio se renombra en todo lado, incluso en los bloques BACK comentados.
    linea = cambiarClase(linea, 'ArchiveludoApi', 'ArchiveludoApiService');

    // Las clases de componentes: se salta los comentarios.
    if (!esComentario(linea)) {
      for (const clase of clasesComponente) {
        linea = cambiarClase(linea, clase, clase + 'Component');
      }
    }

    if (linea !== original) {
      lineas[i] = linea;
      mostrar.push('   linea ' + (i + 1) + ': ' + original.trim() + '\n        -> ' + linea.trim());
    }
  }

  if (mostrar.length > 0) {
    archivosModificados++;
    console.log('\nCONTENIDO  ' + path.relative(process.cwd(), archivo));
    for (const m of mostrar) {
      console.log(m);
    }
    if (aplicar) {
      fs.writeFileSync(archivo, lineas.join('\n'), 'utf8');
    }
  }
}

// ---------------------------------------------------------------
// 5. Renombrar los archivos
// ---------------------------------------------------------------

console.log('\n----- ARCHIVOS A RENOMBRAR -----');
for (const paso of plan) {
  const textoDesde = path.relative(process.cwd(), paso.desde);
  const textoHacia = path.basename(paso.hacia);
  console.log(textoDesde + '  ->  ' + textoHacia);

  if (aplicar) {
    if (fs.existsSync(paso.hacia)) {
      console.log('   OJO: ya existe ' + textoHacia + ', no se renombra.');
    } else {
      fs.renameSync(paso.desde, paso.hacia);
    }
  }
}

console.log('\nClases de componentes: ' + clasesComponente.join(', '));
console.log('Archivos renombrados: ' + plan.length + ' | archivos con contenido cambiado: ' + archivosModificados);

if (aplicar) {
  console.log('\nListo. Cambios aplicados.');
} else {
  console.log('\nESTO FUE UNA SIMULACION. No se cambio nada.');
  console.log('Si todo se ve bien, corre:  node renombrar.cjs --aplicar');
}
