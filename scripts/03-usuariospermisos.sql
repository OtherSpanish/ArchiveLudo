-- ArchiveLudo - Bases de Datos 1
-- 03: usuarios de MySQL para la aplicacion
-- archiveludo_app: el que usa el back (lee y escribe datos, no toca tablas)
-- archiveludo_lectura: solo consulta
-- Cambiar las contrasenas antes de ejecutar y no subirlas al repo.
-- Ejecutar con adminbd.

CREATE USER IF NOT EXISTS 'archiveludo_app'@'%'
  IDENTIFIED BY 'CambiarEstaClave#App2026';

GRANT SELECT, INSERT, UPDATE, DELETE
  ON archiveludo.* TO 'archiveludo_app'@'%';

CREATE USER IF NOT EXISTS 'archiveludo_lectura'@'%'
  IDENTIFIED BY 'CambiarEstaClave#Lec2026';

GRANT SELECT
  ON archiveludo.* TO 'archiveludo_lectura'@'%';

-- para revisar que quedaron bien
SHOW GRANTS FOR 'archiveludo_app'@'%';
SHOW GRANTS FOR 'archiveludo_lectura'@'%';
