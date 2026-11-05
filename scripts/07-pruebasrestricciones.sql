-- ArchiveLudo - Bases de Datos 1
-- 07: pruebas negativas
-- Cada sentencia rompe una restriccion a proposito y TIENE que fallar
-- con el error que dice su comentario. Al final hay ROLLBACK, asi que
-- no queda nada guardado.
-- Correr con --force para que mysqlsh siga despues de cada error.

USE archiveludo;

START TRANSACTION;

-- P1. id de estudio repetido -> ERROR 1062
INSERT INTO ESTUDIO (id_estudio, nombre, pais, anio_fundacion, anio_cierre, estado_actual)
VALUES (1, 'Duplicado', 'Colombia', 2000, NULL, 'activo');

-- P2. copia de un juego que no existe -> ERROR 1452
INSERT INTO COPIA (id_videojuego, id_usuario, tipo_copia, estado_integridad, fecha_registro)
VALUES (9999, 1, 'fisica', 'buena', '2026-11-05');

-- P3. borrar un juego que tiene copias -> ERROR 1451
DELETE FROM VIDEOJUEGO WHERE id_videojuego = 1;

-- P4. estado de integridad que no esta en la lista -> ERROR 3819
INSERT INTO COPIA (id_videojuego, id_usuario, tipo_copia, estado_integridad, fecha_registro)
VALUES (1, 1, 'fisica', 'regular', '2026-11-05');

-- P5. rol que no existe -> ERROR 1265
INSERT INTO USUARIO (primer_nombre, primer_apellido, correo, hash_contrasenia, rol)
VALUES ('Prueba', 'Rol', 'prueba.rol@gmail.com', '$2a$10$x', 'superusuario');

-- P6. correo repetido -> ERROR 1062
INSERT INTO USUARIO (primer_nombre, primer_apellido, correo, hash_contrasenia, rol)
VALUES ('Otra', 'Persona', 'tomas@gmail.com', '$2a$10$x', 'consulta');

-- P7. estudio activo con anio de cierre (S-E3) -> ERROR 3819
INSERT INTO ESTUDIO (nombre, pais, anio_fundacion, anio_cierre, estado_actual)
VALUES ('Estudio Incoherente', 'Colombia', 2000, 2010, 'activo');

-- P8. cierre antes de la fundacion (S-E5) -> ERROR 3819
INSERT INTO ESTUDIO (nombre, pais, anio_fundacion, anio_cierre, estado_actual)
VALUES ('Estudio Al Reves', 'Colombia', 2010, 2000, 'desaparecido');

-- P9. custodia que termina antes de empezar (S-CU5) -> ERROR 3819
INSERT INTO CUSTODIA (id_copia, id_institucion, fecha_inicio, fecha_fin, condicion_almacenamiento)
VALUES (1, 1, '2026-10-10', '2026-01-01', 'Vitrina');

-- P10. proyecto finalizado sin fecha de fin (S-P3) -> ERROR 3819
INSERT INTO PROYECTO_DE_RESTAURACION (id_videojuego, id_usuario, nombre, estado, fecha_inicio, fecha_fin)
VALUES (1, 1, 'Proyecto incoherente', 'finalizado', '2026-01-01', NULL);

-- P11. mismo experto dos veces en un proyecto (S-X5) -> ERROR 1062
INSERT INTO PARTICIPACION (id_proyecto, id_experto, rol_experto, fecha_incorporacion)
VALUES (3, 1, 'Repetido', '2026-01-01');

-- P12. copia digital de tamanio 0 (script 04) -> ERROR 3819
UPDATE COPIA_DIGITAL SET tamanio = 0 WHERE id_copia = 2;

-- P13. juego repetido: mismo titulo, anio y plataforma (script 04) -> ERROR 1062
INSERT INTO VIDEOJUEGO (titulo, genero, plataforma_original, anio_lanzamiento, estado_conservacion, id_estudio, id_usuario)
VALUES ('System Shock', 'Rol y acción', 'MS-DOS', 1994, 'activo', 1, 1);

-- P14. numero de serie repetido (script 04) -> ERROR 1062
INSERT INTO COPIA_FISICA (id_copia, numero_serie, ubicacion_fisica, tipo_soporte)
VALUES (2, 'ORI-SS-1994-0187', 'Estante 1', 'disquete');

ROLLBACK;
