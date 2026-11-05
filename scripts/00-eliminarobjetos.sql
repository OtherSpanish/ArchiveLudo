-- ArchiveLudo - Bases de Datos 1
-- 00: borra las vistas y todas las tablas (con sus datos)
-- Solo para reinstalar desde cero. Sacar un respaldo antes.
-- Las tablas se borran al reves del orden de creacion por las llaves foraneas.

USE archiveludo;

-- vistas del script 05
DROP VIEW IF EXISTS v_estatus_legal_vigente;
DROP VIEW IF EXISTS v_custodia_vigente;
DROP VIEW IF EXISTS v_titulos_sin_copias;
DROP VIEW IF EXISTS v_estudios_desaparecidos_sin_proyecto;
DROP VIEW IF EXISTS v_instituciones_piezas_en_riesgo;

-- tablas que dependen de otras
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

-- tablas independientes
DROP TABLE IF EXISTS EXPERTO;
DROP TABLE IF EXISTS INSTITUCION;
DROP TABLE IF EXISTS COMPANIA_EDITORA;
DROP TABLE IF EXISTS ESTUDIO;
DROP TABLE IF EXISTS USUARIO;
