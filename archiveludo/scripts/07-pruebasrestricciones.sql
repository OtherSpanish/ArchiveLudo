-- =====================================================================
-- ArchiveLudo - Bases de Datos 1 - Universidad El Bosque
-- Script 07: pruebas negativas de integridad
-- Cada sentencia viola una restricción a propósito y DEBE FALLAR con el
-- error indicado. Sirven para demostrar que la integridad la garantiza
-- el motor y no solo la aplicación.
-- Todo se ejecuta dentro de una transacción que termina en ROLLBACK, así
-- que, aunque alguna sentencia no fallara, nada queda guardado.
-- En MySQL Shell se recomienda ejecutar las sentencias una a una
-- (copiar y pegar) para observar cada error.
-- =====================================================================

USE archiveludo;

START TRANSACTION;

-- P1. Llave primaria duplicada -> ERROR 1062 (Duplicate entry)
INSERT INTO ESTUDIO (id_estudio, nombre, pais, anio_fundacion, anio_cierre, estado_actual)
VALUES (1, 'Duplicado', 'Colombia', 2000, NULL, 'activo');

-- P2. Llave foránea inexistente -> ERROR 1452 (foreign key constraint fails)
INSERT INTO COPIA (id_videojuego, id_usuario, tipo_copia, estado_integridad, fecha_registro)
VALUES (9999, 1, 'fisica', 'buena', '2026-11-05');

-- P3. Borrar un videojuego que tiene copias -> ERROR 1451 (Cannot delete or update a parent row)
DELETE FROM VIDEOJUEGO WHERE id_videojuego = 1;

-- P4. Valor fuera de la lista cerrada (CHECK) -> ERROR 3819 (Check constraint is violated)
INSERT INTO COPIA (id_videojuego, id_usuario, tipo_copia, estado_integridad, fecha_registro)
VALUES (1, 1, 'fisica', 'regular', '2026-11-05');

-- P5. Valor fuera del dominio ENUM -> ERROR 1265 (Data truncated for column 'rol')
INSERT INTO USUARIO (primer_nombre, primer_apellido, correo, hash_contrasenia, rol)
VALUES ('Prueba', 'Rol', 'prueba.rol@gmail.com', '$2a$10$x', 'superusuario');

-- P6. Correo de usuario repetido (UNIQUE) -> ERROR 1062
INSERT INTO USUARIO (primer_nombre, primer_apellido, correo, hash_contrasenia, rol)
VALUES ('Otra', 'Persona', 'tomas@gmail.com', '$2a$10$x', 'consulta');

-- P7. Estudio activo con año de cierre (S-E3) -> ERROR 3819
INSERT INTO ESTUDIO (nombre, pais, anio_fundacion, anio_cierre, estado_actual)
VALUES ('Estudio Incoherente', 'Colombia', 2000, 2010, 'activo');

-- P8. Año de cierre anterior a la fundación (S-E5) -> ERROR 3819
INSERT INTO ESTUDIO (nombre, pais, anio_fundacion, anio_cierre, estado_actual)
VALUES ('Estudio Al Reves', 'Colombia', 2010, 2000, 'desaparecido');

-- P9. Custodia que termina antes de empezar (S-CU5) -> ERROR 3819
INSERT INTO CUSTODIA (id_copia, id_institucion, fecha_inicio, fecha_fin, condicion_almacenamiento)
VALUES (1, 1, '2026-10-10', '2026-01-01', 'Vitrina');

-- P10. Proyecto finalizado sin fecha de fin (S-P3) -> ERROR 3819
INSERT INTO PROYECTO_DE_RESTAURACION (id_videojuego, id_usuario, nombre, estado, fecha_inicio, fecha_fin)
VALUES (1, 1, 'Proyecto incoherente', 'finalizado', '2026-01-01', NULL);

-- P11. Mismo experto dos veces en el mismo proyecto (S-X5) -> ERROR 1062
INSERT INTO PARTICIPACION (id_proyecto, id_experto, rol_experto, fecha_incorporacion)
VALUES (3, 1, 'Repetido', '2026-01-01');

-- P12. Tamaño de copia digital no positivo (script 04) -> ERROR 3819
UPDATE COPIA_DIGITAL SET tamanio = 0 WHERE id_copia = 2;

-- P13. Videojuego repetido: mismo título, año y plataforma (script 04) -> ERROR 1062
INSERT INTO VIDEOJUEGO (titulo, genero, plataforma_original, anio_lanzamiento, estado_conservacion, id_estudio, id_usuario)
VALUES ('System Shock', 'Rol y acción', 'MS-DOS', 1994, 'activo', 1, 1);

-- P14. Número de serie de copia física repetido (script 04) -> ERROR 1062
INSERT INTO COPIA_FISICA (id_copia, numero_serie, ubicacion_fisica, tipo_soporte)
VALUES (2, 'ORI-SS-1994-0187', 'Estante 1', 'disquete');

ROLLBACK;
