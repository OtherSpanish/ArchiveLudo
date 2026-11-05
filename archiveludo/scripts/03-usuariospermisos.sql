-- =====================================================================
-- ArchiveLudo - Bases de Datos 1 - Universidad El Bosque
-- Script 03: usuarios del motor y privilegios (mínimo privilegio)
-- Crea dos usuarios de MySQL, distintos del administrador adminbd:
--   archiveludo_app     -> usado por el backend Spring Boot (lee y escribe datos)
--   archiveludo_lectura -> solo consulta (revisión, sustentación, reportes)
-- Ninguno puede crear, alterar ni borrar tablas.
-- ANTES DE EJECUTAR: reemplace las contraseñas de ejemplo por contraseñas
-- robustas y NO las suba al repositorio.
-- Ejecutar con adminbd (tiene CREATE USER y GRANT OPTION).
-- =====================================================================

CREATE USER IF NOT EXISTS 'archiveludo_app'@'%'
  IDENTIFIED BY 'CambiarEstaClave#App2026';

GRANT SELECT, INSERT, UPDATE, DELETE
  ON archiveludo.* TO 'archiveludo_app'@'%';

CREATE USER IF NOT EXISTS 'archiveludo_lectura'@'%'
  IDENTIFIED BY 'CambiarEstaClave#Lec2026';

GRANT SELECT
  ON archiveludo.* TO 'archiveludo_lectura'@'%';

-- Verificación
SHOW GRANTS FOR 'archiveludo_app'@'%';
SHOW GRANTS FOR 'archiveludo_lectura'@'%';
