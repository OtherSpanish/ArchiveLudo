-- ArchiveLudo - Bases de Datos 1
-- 04: restricciones extra sacadas del documento de supuestos
-- (2 UNIQUE y 11 CHECK). Los datos del 02 ya las cumplen.
-- Si se corre dos veces, MySQL avisa que la restriccion ya existe.
-- Ejecutar con adminbd.

USE archiveludo;

-- nombres opcionales vacios ('') pasan a NULL antes de crear los CHECK
UPDATE USUARIO SET segundo_nombre = NULL WHERE TRIM(segundo_nombre) = '';
UPDATE USUARIO SET segundo_apellido = NULL WHERE TRIM(segundo_apellido) = '';

-- ---------- UNIQUE ----------

-- S-V6: no se repite el mismo juego (titulo + anio + plataforma)
ALTER TABLE VIDEOJUEGO
  ADD CONSTRAINT uq_videojuego_titulo_anio_plataforma
  UNIQUE (titulo, anio_lanzamiento, plataforma_original);

-- S-C6: numero de serie unico (MySQL deja varios NULL)
ALTER TABLE COPIA_FISICA
  ADD CONSTRAINT uq_copia_fisica_numero_serie
  UNIQUE (numero_serie);

-- ---------- CHECK ----------

-- anios razonables
ALTER TABLE VIDEOJUEGO
  ADD CONSTRAINT ck_videojuego_anio
  CHECK (anio_lanzamiento BETWEEN 1950 AND 2100);

ALTER TABLE ESTUDIO
  ADD CONSTRAINT ck_estudio_anio_fundacion
  CHECK (anio_fundacion BETWEEN 1950 AND 2100);

-- S-C7: tamanio en bytes mayor que cero
ALTER TABLE COPIA_DIGITAL
  ADD CONSTRAINT ck_copia_digital_tamanio
  CHECK (tamanio > 0);

-- el hash va en hexadecimal
ALTER TABLE COPIA_DIGITAL
  ADD CONSTRAINT ck_copia_digital_hash_hex
  CHECK (REGEXP_LIKE(hash_verificacion, '^[0-9A-Fa-f]+$'));

-- solo un proyecto finalizado puede tener informe
ALTER TABLE PROYECTO_DE_RESTAURACION
  ADD CONSTRAINT ck_proyecto_informe_finalizado
  CHECK (url_informe IS NULL OR estado = 'finalizado');

-- S-CU6: la condicion de almacenamiento no puede ir vacia
ALTER TABLE CUSTODIA
  ADD CONSTRAINT ck_custodia_condicion
  CHECK (CHAR_LENGTH(TRIM(condicion_almacenamiento)) > 0);

-- telefonos positivos (se guardan sin + ni espacios)
ALTER TABLE INST_TELEFONO
  ADD CONSTRAINT ck_inst_telefono_positivo
  CHECK (numero_telefono > 0);

-- formato basico de correo: algo@dominio.ext
ALTER TABLE USUARIO
  ADD CONSTRAINT ck_usuario_correo
  CHECK (correo LIKE '_%@_%._%');

ALTER TABLE EXPERTO
  ADD CONSTRAINT ck_experto_correo
  CHECK (correo LIKE '_%@_%._%');

ALTER TABLE INST_CORREO
  ADD CONSTRAINT ck_inst_correo_formato
  CHECK (correo LIKE '_%@_%._%');

-- segundo nombre / apellido: NULL o con texto, nunca ''
ALTER TABLE USUARIO
  ADD CONSTRAINT ck_usuario_nombres_opcionales
  CHECK ((segundo_nombre IS NULL OR CHAR_LENGTH(TRIM(segundo_nombre)) > 0)
     AND (segundo_apellido IS NULL OR CHAR_LENGTH(TRIM(segundo_apellido)) > 0));

-- lista de CHECK y UNIQUE que quedaron en la base
SELECT table_name, constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_schema = 'archiveludo'
  AND constraint_type IN ('CHECK', 'UNIQUE')
ORDER BY table_name, constraint_type, constraint_name;
