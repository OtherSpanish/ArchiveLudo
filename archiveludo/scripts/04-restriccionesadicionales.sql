-- =====================================================================
-- ArchiveLudo - Bases de Datos 1 - Universidad El Bosque
-- Script 04: restricciones adicionales (CHECK y UNIQUE)
-- Complementa las restricciones del script 01 con reglas de los
-- supuestos que todavía no estaban declaradas en el motor.
-- Los registros del script 02 cumplen todas estas reglas. El paso 1
-- normaliza nombres opcionales vacíos ('') a NULL, por si se aplica
-- sobre una base con datos cargados por otros medios.
-- Cada ALTER TABLE es independiente: si una restricción ya existe,
-- MySQL lo informa y las demás se pueden seguir ejecutando.
-- Ejecutar con adminbd (requiere privilegio ALTER).
-- =====================================================================

USE archiveludo;

-- ---------------------------------------------------------------------
-- Paso 1. Corrección de datos previa
-- Un nombre opcional que no se conoce debe ser NULL, no texto vacío.
-- ---------------------------------------------------------------------
UPDATE USUARIO SET segundo_nombre = NULL WHERE TRIM(segundo_nombre) = '';
UPDATE USUARIO SET segundo_apellido = NULL WHERE TRIM(segundo_apellido) = '';

-- ---------------------------------------------------------------------
-- Paso 2. Restricciones de unicidad
-- ---------------------------------------------------------------------

-- S-V6: título, año de lanzamiento y plataforma original identifican
-- de forma razonable a un videojuego.
ALTER TABLE VIDEOJUEGO
  ADD CONSTRAINT uq_videojuego_titulo_anio_plataforma
  UNIQUE (titulo, anio_lanzamiento, plataforma_original);

-- S-C6: el número de serie de una copia física es único entre las copias
-- que lo tienen (MySQL permite varios NULL en una columna UNIQUE).
ALTER TABLE COPIA_FISICA
  ADD CONSTRAINT uq_copia_fisica_numero_serie
  UNIQUE (numero_serie);

-- ---------------------------------------------------------------------
-- Paso 3. Restricciones de dominio (CHECK)
-- ---------------------------------------------------------------------

-- S-G3: años en un rango razonable para la industria del videojuego.
ALTER TABLE VIDEOJUEGO
  ADD CONSTRAINT ck_videojuego_anio
  CHECK (anio_lanzamiento BETWEEN 1950 AND 2100);

ALTER TABLE ESTUDIO
  ADD CONSTRAINT ck_estudio_anio_fundacion
  CHECK (anio_fundacion BETWEEN 1950 AND 2100);

-- S-C7: el tamaño de una copia digital, en bytes, es mayor que cero.
ALTER TABLE COPIA_DIGITAL
  ADD CONSTRAINT ck_copia_digital_tamanio
  CHECK (tamanio > 0);

-- El hash de verificación se guarda en hexadecimal.
ALTER TABLE COPIA_DIGITAL
  ADD CONSTRAINT ck_copia_digital_hash_hex
  CHECK (REGEXP_LIKE(hash_verificacion, '^[0-9A-Fa-f]+$'));

-- S-IF: solo un proyecto finalizado puede tener enlace al informe final.
ALTER TABLE PROYECTO_DE_RESTAURACION
  ADD CONSTRAINT ck_proyecto_informe_finalizado
  CHECK (url_informe IS NULL OR estado = 'finalizado');

-- S-CU6: las condiciones de almacenamiento no pueden quedar vacías.
ALTER TABLE CUSTODIA
  ADD CONSTRAINT ck_custodia_condicion
  CHECK (CHAR_LENGTH(TRIM(condicion_almacenamiento)) > 0);

-- Teléfonos positivos (se guardan sin signos ni espacios).
ALTER TABLE INST_TELEFONO
  ADD CONSTRAINT ck_inst_telefono_positivo
  CHECK (numero_telefono > 0);

-- Formato mínimo de correo electrónico: texto@dominio.ext
ALTER TABLE USUARIO
  ADD CONSTRAINT ck_usuario_correo
  CHECK (correo LIKE '_%@_%._%');

ALTER TABLE EXPERTO
  ADD CONSTRAINT ck_experto_correo
  CHECK (correo LIKE '_%@_%._%');

ALTER TABLE INST_CORREO
  ADD CONSTRAINT ck_inst_correo_formato
  CHECK (correo LIKE '_%@_%._%');

-- Nombres opcionales: NULL o con contenido, nunca texto vacío.
ALTER TABLE USUARIO
  ADD CONSTRAINT ck_usuario_nombres_opcionales
  CHECK ((segundo_nombre IS NULL OR CHAR_LENGTH(TRIM(segundo_nombre)) > 0)
     AND (segundo_apellido IS NULL OR CHAR_LENGTH(TRIM(segundo_apellido)) > 0));

-- ---------------------------------------------------------------------
-- Verificación: restricciones CHECK y UNIQUE declaradas en el esquema
-- ---------------------------------------------------------------------
SELECT table_name, constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_schema = 'archiveludo'
  AND constraint_type IN ('CHECK', 'UNIQUE')
ORDER BY table_name, constraint_type, constraint_name;
