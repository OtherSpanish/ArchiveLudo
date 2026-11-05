=====================================================================
 ArchiveLudo - Scripts de implementación de la base de datos
 Bases de Datos 1 - Universidad El Bosque
 Juan Carlos Morales Melo, Tomás Triana Galvis, Nataly Vanessa Rengifo Bautista
=====================================================================

1. DESCRIPCIÓN
---------------------------------------------------------------------
Esta carpeta contiene los scripts que transforman el modelo lógico
(relacional) de ArchiveLudo en el modelo físico sobre MySQL.

  RDBMS ............ MySQL (servicio MySQL HeatWave de Oracle Cloud)
  Versión .......... 26.10.0-cloud
  Base de datos .... archiveludo
  Codificación ..... utf8mb4 / utf8mb4_0900_ai_ci
  Tablas ........... 17 (motor InnoDB)
  Registros ........ 168 (datos de demostración coherentes)

2. CONTENIDO
---------------------------------------------------------------------
  Archivo                           Tipo        Qué hace
  --------------------------------  ----------  -------------------------------------------
  00-eliminarobjetos.sql           Varios      Borra vistas y tablas (reinstalación limpia)
  01-creacionobjetos.sql           Creación    Crea la BD y las 17 tablas con PK, FK,
                                                UNIQUE, CHECK y ENUM
  02-insercionregistros.sql        Inserción   Inserta los 168 registros de demostración
  03-usuariospermisos.sql          Varios      Crea usuarios del motor con mínimo privilegio
  04-restriccionesadicionales.sql  Varios      Agrega CHECK y UNIQUE de los supuestos
  05-vistas.sql                     Varios      Vistas de consulta (OPCIONAL)
  06-verificacion.sql               Varios      Verificación de solo lectura
  07-pruebasrestricciones.sql      Varios      Pruebas negativas (deben fallar; ROLLBACK)

3. REQUISITOS
---------------------------------------------------------------------
  a) MySQL Shell 8.4 o superior en el equipo:
       winget install Oracle.MySQLShell
  b) La llave SSH autorizada en la máquina virtual de OCI.
  c) El túnel SSH abierto en una ventana de PowerShell (dejarla abierta):
       ssh -i "RUTA\A\SU\LLAVE" -N -L 3307:10.0.0.61:3306 opc@<IP_PUBLICA_VM>
  d) Un usuario de MySQL con privilegios de administración (adminbd)
     para los scripts 00 a 05.

4. CÓMO EJECUTAR UN SCRIPT
---------------------------------------------------------------------
Abra otra ventana de PowerShell, ubíquese en esta carpeta y ejecute:

  mysqlsh --sql -u adminbd -h 127.0.0.1 -P 3307 --file=01_creacion_objetos.sql

MySQL Shell pide la contraseña y ejecuta el archivo completo.
También puede ejecutarse desde una sesión abierta de mysqlsh:

  \source C:/ruta/a/la/carpeta/01_creacion_objetos.sql

(use / en lugar de \ en la ruta).

5. ORDEN DE EJECUCIÓN
---------------------------------------------------------------------
5.1 Instalación limpia (servidor nuevo o reinstalación)
    [00] -> 01 -> 02 -> 03 -> 04 -> [05] -> 06 -> [07]
    00 solo si la base ya existe y se quiere empezar de cero.
    05 y 07 son opcionales.

5.2 Reemplazar los datos de la base actual en OCI
    Detenga el backend, saque un respaldo (sección 7) y ejecute
    00 -> 01 -> 02 -> 03 -> 04 -> 05 -> 06 -> [07].
    00 borra TODAS las tablas y sus datos; 01 y 02 las vuelven a crear
    con los datos de demostración. Los usuarios de Tomás Triana, Esteban
    Puerto y Nataly Rengifo se conservan con la misma contraseña.

6. RESULTADOS ESPERADOS
---------------------------------------------------------------------
  01  17 tablas creadas sin errores.
  02  168 registros insertados (una sola transacción).
  03  Usuarios archiveludo_app (SELECT, INSERT, UPDATE, DELETE) y
      archiveludo_lectura (SELECT). CAMBIE las contraseñas de ejemplo.
  04  Al final muestra 26 restricciones (21 CHECK y 5 UNIQUE).
  05  Cinco vistas; muestra el estatus legal vigente y las instituciones
      con piezas en riesgo.
  06  17 tablas InnoDB y los conteos por tabla. Las secciones 4 a 8
      no deben devolver filas.
  07  Cada sentencia falla con el error indicado en su comentario
      (1062, 1452, 1451, 3819 o 1265). Ejecútelo con --force para que
      MySQL Shell continúe después de cada error:
        mysqlsh --sql -u adminbd -h 127.0.0.1 -P 3307 --force --file=07-pruebasrestricciones.sql
      Termina con ROLLBACK: no deja cambios.

7. COPIA DE SEGURIDAD
---------------------------------------------------------------------
Además de las copias automáticas del servicio (diarias), se puede sacar
un volcado desde la máquina virtual:

  ssh -i "RUTA\A\SU\LLAVE" opc@<IP_PUBLICA_VM>
  sudo dnf install -y mysql
  mysqldump -h 10.0.0.61 -u adminbd -p --no-tablespaces --set-gtid-purged=OFF --routines --triggers --skip-extended-insert archiveludo > archiveludo.sql
  exit
  scp -i "RUTA\A\SU\LLAVE" opc@<IP_PUBLICA_VM>:~/archiveludo.sql .

Para restaurarlo:
  mysqlsh --sql -u adminbd -h 127.0.0.1 -P 3307 -D archiveludo --file=archiveludo.sql

8. PROBLEMAS FRECUENTES
---------------------------------------------------------------------
  Can't connect / se queda esperando   El túnel SSH (requisito c) no está abierto.
  Access denied for user               Usuario o contraseña incorrectos.
  ERROR 1050 Table already exists      Se ejecutó 01 sobre una base con tablas
                                       (ver 5.2).
  ERROR 1062 Duplicate entry en 02     Los registros ya existen: ejecute
                                       antes 00 y 01 (ver 5.2).
  ERROR 3822 / ERROR 1061 en 04        El script 04 ya se había ejecutado;
                                       esa restricción ya existe. Ejecútelo
                                       con --force para aplicar solo las que
                                       falten.
  Tildes o eñes mal mostradas          Los archivos están en UTF-8; ábralos y
                                       guárdelos con esa codificación.

9. NOTAS
---------------------------------------------------------------------
  - Ningún script contiene contraseñas reales. Las del script 03 son de
    ejemplo y deben cambiarse antes de ejecutarlo.
  - Las reglas que no se pueden expresar con CHECK (una custodia vigente
    por copia, periodos sin solapamiento, copia física o digital pero no
    ambas) se validan en el backend y se verifican con el script 06.
  - Las contraseñas de los usuarios de ArchiveLudo se guardan como hash
    BCrypt generado por la aplicación, nunca en texto plano.
