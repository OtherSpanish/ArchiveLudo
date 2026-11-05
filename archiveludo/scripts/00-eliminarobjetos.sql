-- =====================================================================
-- ArchiveLudo - Bases de Datos 1 - Universidad El Bosque
-- Script 00: eliminación de objetos
-- ADVERTENCIA: borra las vistas y TODAS las tablas de archiveludo con
-- sus datos. Úsese solo para reinstalar desde cero (instalación limpia)
-- y después de sacar una copia de seguridad (ver README.txt).
-- Las tablas se eliminan en orden inverso de dependencia, de modo que
-- ninguna llave foránea impide el borrado.
-- =====================================================================

USE archiveludo;

-- Vistas (script 05)
DROP VIEW IF EXISTS v_estatus_legal_vigente;
DROP VIEW IF EXISTS v_custodia_vigente;
DROP VIEW IF EXISTS v_titulos_sin_copias;
DROP VIEW IF EXISTS v_estudios_desaparecidos_sin_proyecto;
DROP VIEW IF EXISTS v_instituciones_piezas_en_riesgo;

-- Tablas dependientes
DROP TABLE IF EXISTS PARTICIPACION;
DROP TABLE IF EXISTS PROYECTO_DE_RESTAURACION;
DROP TABLE IF EXISTS EVENTO_DE_RIESGO;
DROP TABLE IF EXISTS ESTATUS_LEGAL;
DROP TABLE IF EXISTS CUSTODIA;
DROP TABLE IF EXISTS INST_TELEFONO;
DROP TABLE IF EXISTS INST_CORREO;
DROP TABLE IF EXISTS COPIA_DIGITAL;
DROP TABLE IF EXISTS COPIA_FISICA;
DROP TABLE IF EXISTS COPIA;
DROP TABLE IF EXISTS COMPED_VID;
DROP TABLE IF EXISTS VIDEOJUEGO;

-- Tablas independientes
DROP TABLE IF EXISTS EXPERTO;
DROP TABLE IF EXISTS INSTITUCION;
DROP TABLE IF EXISTS COMPANIA_EDITORA;
DROP TABLE IF EXISTS ESTUDIO;
DROP TABLE IF EXISTS USUARIO;
