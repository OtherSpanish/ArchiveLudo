-- ArchiveLudo - Bases de Datos 1
-- 05: vistas (opcional)
-- Una vista es una consulta guardada; no guarda datos ni cambia tablas.
-- Se usan como tabla: SELECT * FROM nombre_vista;
-- Ejecutar con adminbd.

USE archiveludo;

-- estatus legal vigente de cada juego = el de fecha mas reciente
CREATE OR REPLACE VIEW v_estatus_legal_vigente AS
SELECT e.id_videojuego, v.titulo, v.plataforma_original,
       e.id_estatus, e.estatus, e.fecha_determinacion, e.observacion
FROM ESTATUS_LEGAL e
JOIN VIDEOJUEGO v ON v.id_videojuego = e.id_videojuego
WHERE e.id_estatus = (
  SELECT e2.id_estatus
  FROM ESTATUS_LEGAL e2
  WHERE e2.id_videojuego = e.id_videojuego
  ORDER BY e2.fecha_determinacion DESC, e2.id_estatus DESC
  LIMIT 1
);

-- donde esta hoy cada copia (custodia con fecha_fin NULL)
CREATE OR REPLACE VIEW v_custodia_vigente AS
SELECT c.id_copia, c.tipo_copia, v.titulo,
       i.id_institucion, i.nombre AS institucion, i.tipo AS tipo_institucion,
       cu.fecha_inicio, cu.condicion_almacenamiento
FROM CUSTODIA cu
JOIN COPIA c ON c.id_copia = cu.id_copia
JOIN VIDEOJUEGO v ON v.id_videojuego = c.id_videojuego
JOIN INSTITUCION i ON i.id_institucion = cu.id_institucion
WHERE cu.fecha_fin IS NULL;

-- RF-08: juegos sin ninguna copia
CREATE OR REPLACE VIEW v_titulos_sin_copias AS
SELECT v.id_videojuego, v.titulo, v.plataforma_original, v.estado_conservacion
FROM VIDEOJUEGO v
WHERE NOT EXISTS (SELECT 1 FROM COPIA c WHERE c.id_videojuego = v.id_videojuego);

-- RF-08: estudios desaparecidos con juegos que no tienen proyecto
CREATE OR REPLACE VIEW v_estudios_desaparecidos_sin_proyecto AS
SELECT es.id_estudio, es.nombre AS estudio, es.anio_cierre,
       v.id_videojuego, v.titulo, v.estado_conservacion
FROM ESTUDIO es
JOIN VIDEOJUEGO v ON v.id_estudio = es.id_estudio
WHERE es.estado_actual = 'desaparecido'
  AND NOT EXISTS (SELECT 1 FROM PROYECTO_DE_RESTAURACION p
                  WHERE p.id_videojuego = v.id_videojuego);

-- RF-08: piezas en riesgo por institucion
-- cuenta copias con custodia vigente que estan deterioradas, daniadas o
-- irrecuperables, o cuyo juego no esta activo ni restaurado
CREATE OR REPLACE VIEW v_instituciones_piezas_en_riesgo AS
SELECT i.id_institucion, i.nombre AS institucion, i.tipo,
       COUNT(*) AS piezas_en_riesgo
FROM CUSTODIA cu
JOIN INSTITUCION i ON i.id_institucion = cu.id_institucion
JOIN COPIA c ON c.id_copia = cu.id_copia
JOIN VIDEOJUEGO v ON v.id_videojuego = c.id_videojuego
WHERE cu.fecha_fin IS NULL
  AND (c.estado_integridad IN ('deteriorada', 'dañada', 'irrecuperable')
       OR v.estado_conservacion NOT IN ('activo', 'restaurado'))
GROUP BY i.id_institucion, i.nombre, i.tipo;

-- ejemplos
SELECT * FROM v_estatus_legal_vigente ORDER BY id_videojuego;
SELECT * FROM v_instituciones_piezas_en_riesgo ORDER BY piezas_en_riesgo DESC;
