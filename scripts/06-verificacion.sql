-- ArchiveLudo - Bases de Datos 1
-- 06: verificacion (solo lectura, no cambia nada)
-- Las secciones 4 a 8 deben salir vacias.

USE archiveludo;

-- 1. version y codificacion
SELECT VERSION() AS version_mysql,
       @@character_set_database AS charset_bd,
       @@collation_database AS collation_bd;

-- 2. tablas (deben ser 17, todas InnoDB)
SELECT table_name, engine
FROM information_schema.tables
WHERE table_schema = 'archiveludo' AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- 3. registros por tabla
SELECT 'USUARIO' AS tabla, COUNT(*) AS registros FROM USUARIO
UNION ALL SELECT 'ESTUDIO', COUNT(*) FROM ESTUDIO
UNION ALL SELECT 'COMPANIA_EDITORA', COUNT(*) FROM COMPANIA_EDITORA
UNION ALL SELECT 'INSTITUCION', COUNT(*) FROM INSTITUCION
UNION ALL SELECT 'EXPERTO', COUNT(*) FROM EXPERTO
UNION ALL SELECT 'VIDEOJUEGO', COUNT(*) FROM VIDEOJUEGO
UNION ALL SELECT 'COMPED_VID', COUNT(*) FROM COMPED_VID
UNION ALL SELECT 'COPIA', COUNT(*) FROM COPIA
UNION ALL SELECT 'COPIA_FISICA', COUNT(*) FROM COPIA_FISICA
UNION ALL SELECT 'COPIA_DIGITAL', COUNT(*) FROM COPIA_DIGITAL
UNION ALL SELECT 'INST_CORREO', COUNT(*) FROM INST_CORREO
UNION ALL SELECT 'INST_TELEFONO', COUNT(*) FROM INST_TELEFONO
UNION ALL SELECT 'CUSTODIA', COUNT(*) FROM CUSTODIA
UNION ALL SELECT 'ESTATUS_LEGAL', COUNT(*) FROM ESTATUS_LEGAL
UNION ALL SELECT 'EVENTO_DE_RIESGO', COUNT(*) FROM EVENTO_DE_RIESGO
UNION ALL SELECT 'PROYECTO_DE_RESTAURACION', COUNT(*) FROM PROYECTO_DE_RESTAURACION
UNION ALL SELECT 'PARTICIPACION', COUNT(*) FROM PARTICIPACION;

-- 4. especializacion de COPIA (S-C3)
-- 4a. copias sin la fila de su subtipo
SELECT c.id_copia, c.tipo_copia
FROM COPIA c
LEFT JOIN COPIA_FISICA f ON f.id_copia = c.id_copia
LEFT JOIN COPIA_DIGITAL d ON d.id_copia = c.id_copia
WHERE (c.tipo_copia = 'fisica' AND f.id_copia IS NULL)
   OR (c.tipo_copia = 'digital' AND d.id_copia IS NULL);

-- 4b. copias que estan en los dos subtipos
SELECT f.id_copia FROM COPIA_FISICA f
JOIN COPIA_DIGITAL d ON d.id_copia = f.id_copia;

-- 4c. subtipo que no coincide con tipo_copia
SELECT c.id_copia, c.tipo_copia, 'esta en COPIA_DIGITAL' AS problema
FROM COPIA c JOIN COPIA_DIGITAL d ON d.id_copia = c.id_copia
WHERE c.tipo_copia <> 'digital'
UNION ALL
SELECT c.id_copia, c.tipo_copia, 'esta en COPIA_FISICA'
FROM COPIA c JOIN COPIA_FISICA f ON f.id_copia = c.id_copia
WHERE c.tipo_copia <> 'fisica';

-- 5. copias con mas de una custodia vigente (S-CU3)
SELECT id_copia, COUNT(*) AS custodias_vigentes
FROM CUSTODIA
WHERE fecha_fin IS NULL
GROUP BY id_copia
HAVING COUNT(*) > 1;

-- 6. custodias de la misma copia que se cruzan en fechas (S-CU4)
SELECT a.id_copia, a.id_custodia AS custodia_a, b.id_custodia AS custodia_b
FROM CUSTODIA a
JOIN CUSTODIA b ON a.id_copia = b.id_copia AND a.id_custodia < b.id_custodia
WHERE a.fecha_inicio < COALESCE(b.fecha_fin, '9999-12-31')
  AND b.fecha_inicio < COALESCE(a.fecha_fin, '9999-12-31');

-- 7. participaciones sin fecha de salida en proyectos ya finalizados
SELECT pa.id_participacion, pa.id_proyecto, pr.estado, pr.fecha_fin
FROM PARTICIPACION pa
JOIN PROYECTO_DE_RESTAURACION pr ON pr.id_proyecto = pa.id_proyecto
WHERE pr.estado = 'finalizado' AND pa.fecha_salida IS NULL;

-- 8. copias sin ninguna custodia (S-CU7)
SELECT c.id_copia, c.tipo_copia, c.id_videojuego
FROM COPIA c
WHERE NOT EXISTS (SELECT 1 FROM CUSTODIA cu WHERE cu.id_copia = c.id_copia);

-- 9. cuantas restricciones hay de cada tipo
SELECT constraint_type, COUNT(*) AS cantidad
FROM information_schema.table_constraints
WHERE table_schema = 'archiveludo'
GROUP BY constraint_type
ORDER BY constraint_type;
