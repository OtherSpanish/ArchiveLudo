-- ArchiveLudo - Bases de Datos 1
-- 02: datos de demostracion (168 registros)
-- Los juegos, estudios y editoras son reales. Las instituciones, los
-- expertos, las copias y los proyectos son inventados pero coherentes.
-- Los ids van explicitos para que las llaves foraneas cuadren.
-- Se ejecuta despues del 01, con la base vacia.

SET NAMES utf8mb4;
USE archiveludo;

START TRANSACTION;


-- USUARIO (3) - uno por rol, con su hash de siempre
INSERT INTO USUARIO (id_usuario, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, correo, hash_contrasenia, rol, fecha_creacion) VALUES
  (1, 'Tomas', NULL, 'Triana', 'Galvis', 'tomas@gmail.com', '$2a$10$O2Wj/eEGjlTWkmLuQFjFG.KLcyUD6voWOK2AQZAkHpyfFNgx0mMDa', 'administrador', '2026-11-02'),
  (2, 'Esteban', NULL, 'Puerto', 'Rojas', 'Esteban@gmail.com', '$2a$10$GYlnyQvbDA//k44mzKYXRehV./B4rAXnScGjNDZ0m4652ldX1U7x6', 'curador', '2026-11-01'),
  (3, 'Nataly', 'Vanessa', 'Rengifo', 'Bautista', 'natalyrengifo707@gmail.com', '$2a$10$G4Z3dOUyvWNL1WdEu36DueLTHfhHnDwFd/HyjQxO6f1kkA7kKBNaq', 'consulta', '2026-11-01');

-- ESTUDIO (10)
INSERT INTO ESTUDIO (id_estudio, nombre, pais, anio_fundacion, anio_cierre, estado_actual) VALUES
  (1, 'Looking Glass Studios', 'Estados Unidos', 1990, 2000, 'desaparecido'),
  (2, 'Westwood Studios', 'Estados Unidos', 1985, 2003, 'desaparecido'),
  (3, 'Black Isle Studios', 'Estados Unidos', 1996, 2003, 'desaparecido'),
  (4, 'Ion Storm', 'Estados Unidos', 1996, 2005, 'desaparecido'),
  (5, 'Pandemic Studios', 'Estados Unidos', 1998, 2009, 'desaparecido'),
  (6, 'CD Projekt Red', 'Polonia', 2002, NULL, 'activo'),
  (7, 'Sega AM2', 'Japón', 1985, NULL, 'activo'),
  (8, 'Rare', 'Reino Unido', 1985, NULL, 'activo'),
  (9, 'Nintendo EAD', 'Japón', 1983, 2015, 'desaparecido'),
  (10, 'Estudio Pixel Andino', 'Colombia', 2003, 2011, 'desaparecido');

-- COMPANIA_EDITORA (10)
INSERT INTO COMPANIA_EDITORA (id_editora, nombre, pais) VALUES
  (1, 'Origin Systems', 'Estados Unidos'),
  (2, 'Eidos Interactive', 'Reino Unido'),
  (3, 'Virgin Interactive', 'Reino Unido'),
  (4, 'Interplay Entertainment', 'Estados Unidos'),
  (5, 'LucasArts', 'Estados Unidos'),
  (6, 'Atari', 'Francia'),
  (7, 'CD Projekt', 'Polonia'),
  (8, 'Electronic Arts', 'Estados Unidos'),
  (9, 'Sega', 'Japón'),
  (10, 'Nintendo', 'Japón');

-- INSTITUCION (5)
INSERT INTO INSTITUCION (id_institucion, nombre, tipo, pais) VALUES
  (1, 'Museo Nacional del Videojuego', 'museo', 'Colombia'),
  (2, 'Archivo Digital Universitario', 'universidad', 'Colombia'),
  (3, 'Archivo Ibérico del Videojuego', 'archivo', 'España'),
  (4, 'Colección Retro Andina', 'coleccion privada', 'Colombia'),
  (5, 'Museo del Píxel', 'museo', 'México');

-- EXPERTO (5)
INSERT INTO EXPERTO (id_experto, primer_nombre_ex, segundo_nombre_ex, primer_apellido_ex, segundo_apellido_ex, especialidad, afiliacion, correo) VALUES
  (1, 'Mariana', NULL, 'Quintero', 'López', 'Emulación de consolas', 'Archivo Digital Universitario', 'mariana.quintero@expertos-ludo.org'),
  (2, 'Andrés', 'Felipe', 'Moreno', NULL, 'Recuperación de medios ópticos y magnéticos', 'Museo Nacional del Videojuego', 'andres.moreno@expertos-ludo.org'),
  (3, 'Valentina', NULL, 'Rincón', 'Pardo', 'Ingeniería inversa', NULL, 'valentina.rincon@expertos-ludo.org'),
  (4, 'Santiago', 'José', 'Herrera', 'Gil', 'Derecho de propiedad intelectual', 'Colección Retro Andina', 'santiago.herrera@expertos-ludo.org'),
  (5, 'Camila', NULL, 'Ortiz', NULL, 'Restauración de cartuchos', NULL, 'camila.ortiz@expertos-ludo.org');

-- VIDEOJUEGO (13) - Cronicas de Ambar y Rutas del Cafe son inventados
INSERT INTO VIDEOJUEGO (id_videojuego, titulo, genero, plataforma_original, descripcion, anio_lanzamiento, estado_conservacion, id_estudio, id_usuario, url_imagen) VALUES
  (1, 'System Shock', 'Rol y acción', 'MS-DOS', 'Juego de rol y acción en primera persona ambientado en la estación espacial Citadel, controlada por la inteligencia artificial SHODAN.', 1994, 'en riesgo', 1, 1, NULL),
  (2, 'Thief: The Dark Project', 'Sigilo', 'Windows', 'Juego de sigilo en primera persona en el que el ladrón Garrett recorre una ciudad de estética medieval y steampunk.', 1998, 'restaurado', 1, 1, NULL),
  (3, 'Dune II: The Building of a Dynasty', 'Estrategia en tiempo real', 'MS-DOS', 'Juego que sentó las bases de la estrategia en tiempo real, ambientado en el planeta Arrakis del universo de Dune.', 1992, 'en riesgo', 2, 2, NULL),
  (4, 'Command & Conquer', 'Estrategia en tiempo real', 'MS-DOS', 'Estrategia en tiempo real centrada en el conflicto entre la Iniciativa de Defensa Global y la Hermandad de Nod.', 1995, 'activo', 2, 2, NULL),
  (5, 'Planescape: Torment', 'Rol', 'Windows', 'Juego de rol de gran peso narrativo en el que el Sin Nombre busca recuperar su memoria en el multiverso de Planescape.', 1999, 'activo', 3, 2, NULL),
  (6, 'Deus Ex', 'Rol y acción', 'Windows', 'Combina disparos, sigilo y rol en un futuro distópico marcado por conspiraciones y aumentos cibernéticos.', 2000, 'activo', 4, 1, NULL),
  (7, 'Star Wars: Battlefront', 'Disparos', 'PlayStation 2', 'Juego de disparos con batallas a gran escala del universo Star Wars; su modo multijugador en línea dependía de servidores externos.', 2004, 'parcialmente perdido', 5, 2, NULL),
  (8, 'The Witcher', 'Rol y acción', 'Windows', 'Primer juego de la saga, basado en las novelas de Andrzej Sapkowski y protagonizado por el brujo Geralt de Rivia.', 2007, 'activo', 6, 1, NULL),
  (9, 'Shenmue', 'Aventura', 'Dreamcast', 'Aventura de mundo abierto con ciclo de día y noche, ambientada en Yokosuka (Japón) en 1986.', 1999, 'en riesgo', 7, 2, NULL),
  (10, 'The Legend of Zelda: Majora\'s Mask', 'Aventura', 'Nintendo 64', 'Aventura en la que Link debe evitar la caída de la luna sobre Termina en un ciclo de tres días que se repite.', 2000, 'activo', 9, 2, NULL),
  (11, 'GoldenEye 007', 'Disparos en primera persona', 'Nintendo 64', 'Juego de disparos en primera persona basado en la película de James Bond de 1995, con multijugador en pantalla dividida.', 1997, 'restaurado', 8, 1, NULL),
  (12, 'Crónicas de Ámbar', 'Rol', 'Windows', 'Juego de rol independiente colombiano del que no se conserva ninguna copia funcional.', 2006, 'totalmente perdido', 10, 2, NULL),
  (13, 'Rutas del Café', 'Aventura', 'Windows', 'Aventura gráfica independiente colombiana ambientada en el Eje Cafetero, liberada al dominio público por sus autores.', 2004, 'en riesgo', 10, 2, NULL);

-- COMPED_VID (13) - C&C y The Witcher tienen dos editoras
INSERT INTO COMPED_VID (id_videojuego, id_editora) VALUES
  (1, 1),
  (2, 2),
  (3, 3),
  (4, 3),
  (4, 8),
  (5, 4),
  (6, 2),
  (7, 5),
  (8, 6),
  (8, 7),
  (9, 9),
  (10, 10),
  (11, 10);

-- COPIA (19) - 11 fisicas y 8 digitales
INSERT INTO COPIA (id_copia, id_videojuego, id_usuario, tipo_copia, estado_integridad, fecha_registro) VALUES
  (1, 1, 2, 'fisica', 'deteriorada', '2026-11-02'),
  (2, 1, 2, 'digital', 'buena', '2026-11-02'),
  (3, 2, 2, 'fisica', 'buena', '2026-11-02'),
  (4, 2, 2, 'digital', 'buena', '2026-11-02'),
  (5, 3, 2, 'fisica', 'dañada', '2026-11-03'),
  (6, 4, 2, 'fisica', 'buena', '2026-11-03'),
  (7, 4, 2, 'digital', 'buena', '2026-11-03'),
  (8, 5, 2, 'fisica', 'buena', '2026-11-03'),
  (9, 6, 1, 'digital', 'buena', '2026-11-04'),
  (10, 7, 1, 'fisica', 'buena', '2026-11-04'),
  (11, 8, 1, 'fisica', 'buena', '2026-11-04'),
  (12, 8, 1, 'digital', 'buena', '2026-11-04'),
  (13, 9, 2, 'fisica', 'deteriorada', '2026-11-04'),
  (14, 9, 2, 'digital', 'buena', '2026-11-04'),
  (15, 10, 2, 'fisica', 'buena', '2026-11-05'),
  (16, 11, 1, 'fisica', 'buena', '2026-11-05'),
  (17, 11, 1, 'digital', 'buena', '2026-11-05'),
  (18, 13, 2, 'fisica', 'dañada', '2026-11-05'),
  (19, 13, 2, 'digital', 'irrecuperable', '2026-11-05');

-- COPIA_FISICA (11) - la 18 no tiene numero de serie
INSERT INTO COPIA_FISICA (id_copia, numero_serie, ubicacion_fisica, tipo_soporte) VALUES
  (1, 'ORI-SS-1994-0187', 'Estante 2, caja 7', 'disquete'),
  (3, 'EID-TH-1998-0042', 'Vitrina 3, nivel 2', 'CD-ROM'),
  (5, 'VIE-D2-1992-0311', 'Bóveda climatizada, gaveta 4', 'disquete'),
  (6, 'VIE-CC-1995-0907', 'Vitrina 4, nivel 1', 'CD-ROM'),
  (8, 'INT-PT-1999-0123', 'Estante 1, caja 3', 'CD-ROM'),
  (10, 'LUC-SWBF-2004-0056', 'Vitrina 6, nivel 1', 'DVD-ROM'),
  (11, 'CDP-W1-2007-0480', 'Estante 5, caja 2', 'DVD-ROM'),
  (13, 'SEG-SH-1999-0007', 'Vitrina 2, nivel 3', 'GD-ROM'),
  (15, 'NZSE-0041872', 'Vitrina 1, nivel 2', 'cartucho'),
  (16, 'NGEE-0193344', 'Vitrina 1, nivel 1', 'cartucho'),
  (18, NULL, 'Estante 4, caja 9', 'CD-ROM');

-- COPIA_DIGITAL (8) - tamanio en bytes y hash SHA-256
INSERT INTO COPIA_DIGITAL (id_copia, formato_archivo, tamanio, hash_verificacion) VALUES
  (2, 'iso', 403701760, '28a7f672cd77be5c9bb855a223dddef25dd78a1e58c02e669854011396a5c21a'),
  (4, 'iso', 681574400, '6d1fb2b9b4289dbcf82b946b184555a7448bdac289f86e072de0d5edf3912d07'),
  (7, 'iso', 650117120, '194b336965354583c2ee47c950b1e84a81c0baf16603c07250b00fd7f9b13370'),
  (9, 'iso', 737280000, 'd2550ee33f70b5a38fa2fa4d46bd4175d6e563cf540e349752c5038f7cca11db'),
  (12, 'iso', 4508876800, '928263a2433c583a3025f1788026e26307f676eb65671da1bcca52779e28e8e9'),
  (14, 'gdi', 1073741824, '22b8bbde874270f07fa1071f0e34ab95cae8fbfe480961ea3a1ccb30f38f524b'),
  (17, 'z64', 12582912, '656742a42d90ad28830bef8418df5fe27634864011b10028e0727a6b79a5dc36'),
  (19, 'zip', 52428800, '5b80f115b154e86ccb2bdd8add4795aede64cf214c43737f2ae615aa1062e357');

-- INST_CORREO (6)
INSERT INTO INST_CORREO (id_institucion, correo) VALUES
  (1, 'colecciones@museovideojuego.org'),
  (1, 'contacto@museovideojuego.org'),
  (2, 'archivo.digital@universidad.edu.co'),
  (3, 'info@archivoiberico.es'),
  (4, 'coleccionretroandina@gmail.com'),
  (5, 'contacto@museodelpixel.mx');

-- INST_TELEFONO (6)
INSERT INTO INST_TELEFONO (id_institucion, numero_telefono) VALUES
  (1, 3005550102),
  (1, 6015550101),
  (2, 6015550201),
  (3, 34915550301),
  (4, 3105550401),
  (5, 525555500101);

-- CUSTODIA (22) - cada copia tiene una vigente (fecha_fin NULL)
INSERT INTO CUSTODIA (id_custodia, id_copia, id_institucion, fecha_inicio, fecha_fin, condicion_almacenamiento) VALUES
  (1, 1, 3, '2019-03-01', '2023-08-31', 'Caja sellada a temperatura ambiente'),
  (2, 1, 4, '2023-09-01', NULL, 'Caja antiestática con control de humedad'),
  (3, 2, 2, '2026-11-02', NULL, 'Servidor con respaldo diario y verificación de hash'),
  (4, 3, 4, '2018-05-10', '2022-02-14', 'Estante cerrado con control de humedad'),
  (5, 3, 1, '2022-02-15', NULL, 'Vitrina con control de humedad'),
  (6, 4, 2, '2026-11-02', NULL, 'Servidor con respaldo diario y verificación de hash'),
  (7, 5, 3, '2021-04-12', NULL, 'Bóveda climatizada a 18 °C'),
  (8, 6, 1, '2024-03-01', NULL, 'Vitrina con control de humedad'),
  (9, 7, 2, '2026-11-03', NULL, 'Servidor con respaldo diario y verificación de hash'),
  (10, 8, 4, '2020-09-20', NULL, 'Caja sellada con desecante'),
  (11, 9, 2, '2026-11-04', NULL, 'Servidor con respaldo diario y verificación de hash'),
  (12, 10, 1, '2025-05-15', NULL, 'Vitrina con control de humedad'),
  (13, 11, 3, '2023-10-01', NULL, 'Estante con protección UV'),
  (14, 12, 2, '2026-11-04', NULL, 'Almacenamiento en frío con doble copia'),
  (15, 13, 3, '2020-01-10', '2024-06-30', 'Caja antiestática en bodega'),
  (16, 13, 1, '2024-07-01', NULL, 'Vitrina con control de humedad'),
  (17, 14, 2, '2026-11-04', NULL, 'Servidor con respaldo diario y verificación de hash'),
  (18, 15, 4, '2019-11-11', NULL, 'Caja antiestática con control de humedad'),
  (19, 16, 1, '2024-02-20', NULL, 'Vitrina con control de humedad'),
  (20, 17, 2, '2026-11-05', NULL, 'Almacenamiento en frío con doble copia'),
  (21, 18, 4, '2022-06-30', NULL, 'Caja sellada con desecante'),
  (22, 19, 2, '2026-11-05', NULL, 'Disco de respaldo externo');

-- ESTATUS_LEGAL (17) - el vigente es el de fecha mas reciente
INSERT INTO ESTATUS_LEGAL (id_estatus, id_videojuego, id_usuario, estatus, fecha_determinacion, observacion) VALUES
  (1, 1, 1, 'protegido', '2025-02-10', 'Derechos vigentes; el título se sigue comercializando en ediciones posteriores.'),
  (2, 2, 1, 'protegido', '2025-02-10', 'Derechos vigentes de su titular actual.'),
  (3, 3, 2, 'protegido', '2025-03-04', 'Derechos vigentes de su titular actual.'),
  (4, 4, 2, 'protegido', '2015-06-01', 'Derechos de Electronic Arts tras la adquisición de Westwood Studios en 1998.'),
  (5, 4, 2, 'protegido', '2020-06-05', 'Verificado de nuevo con el relanzamiento remasterizado del título.'),
  (6, 5, 2, 'protegido', '2025-03-04', 'Derechos vigentes; existe una edición mejorada comercial.'),
  (7, 6, 1, 'protegido', '2025-02-10', 'Derechos vigentes de su titular actual.'),
  (8, 7, 2, 'protegido', '2025-03-18', 'Derechos vigentes; el modo en línea original no está disponible.'),
  (9, 8, 1, 'protegido', '2025-02-10', 'Derechos vigentes de CD Projekt.'),
  (10, 9, 2, 'protegido', '2025-03-18', 'Derechos vigentes de Sega.'),
  (11, 10, 2, 'protegido', '2025-03-18', 'Derechos vigentes de Nintendo.'),
  (12, 11, 1, 'protegido', '2025-02-10', 'Derechos vigentes; licencia de la franquicia James Bond de por medio.'),
  (13, 12, 2, 'protegido', '2006-08-01', 'Derechos del estudio desarrollador al momento del lanzamiento.'),
  (14, 12, 2, 'huerfano', '2012-03-20', 'El estudio cerró en 2011 y no se identifica al titular de los derechos.'),
  (15, 12, 2, 'abandonware', '2019-05-10', 'Sin distribución comercial ni titular identificable desde hace más de diez años.'),
  (16, 13, 2, 'protegido', '2004-11-01', 'Derechos del estudio desarrollador al momento del lanzamiento.'),
  (17, 13, 2, 'dominio publico', '2015-02-02', 'Liberado al dominio público por sus autores.');

-- EVENTO_DE_RIESGO (7)
INSERT INTO EVENTO_DE_RIESGO (id_evento, id_videojuego, id_usuario, tipo_evento, fecha_evento, descripcion, fuente) VALUES
  (1, 7, 2, 'cierre de servidores', '2014-05-31', 'El cierre del servicio GameSpy deja sin modo multijugador en línea a la versión original.', 'Anuncio de cierre de GameSpy Technology'),
  (2, 12, 2, 'perdida de codigo fuente', '2011-09-30', 'Al cerrar el estudio se pierden el código fuente y los instaladores del juego.', 'Testimonio de un exintegrante del estudio'),
  (3, 12, 2, 'deterioro de medios', '2014-07-07', 'Los CD de la única edición conocida se vuelven ilegibles.', 'Informe de la Colección Retro Andina'),
  (4, 13, 2, 'deterioro de medios', '2023-04-18', 'El CD-ROM de la copia conocida presenta delaminación de la capa reflectiva.', 'Informe de inspección de la Colección Retro Andina'),
  (5, 1, 2, 'deterioro de medios', '2025-11-03', 'Los disquetes de la copia física presentan errores de lectura.', 'Informe de inspección de la Colección Retro Andina'),
  (6, 3, 2, 'deterioro de medios', '2026-05-12', 'El disquete de la copia física presenta pérdida de magnetización en varios sectores.', 'Informe del Archivo Ibérico del Videojuego'),
  (7, 9, 2, 'deterioro de medios', '2025-08-20', 'El GD-ROM de la copia física presenta rayones y desgaste en la superficie de lectura.', 'Informe de inspección del Museo Nacional del Videojuego');

-- PROYECTO_DE_RESTAURACION (5)
INSERT INTO PROYECTO_DE_RESTAURACION (id_proyecto, id_videojuego, id_usuario, nombre, descripcion, herramienta_emulador, estado, fecha_inicio, fecha_fin, url_informe) VALUES
  (1, 2, 1, 'Restauración de Thief: The Dark Project', 'Creación y verificación de una imagen completa del CD-ROM original y pruebas de ejecución en un entorno emulado.', 'PCem', 'finalizado', '2023-02-01', '2024-05-30', NULL),
  (2, 11, 1, 'Restauración de GoldenEye 007', 'Volcado del cartucho original, verificación del volcado y pruebas en emulador.', 'Project64', 'finalizado', '2023-01-10', '2024-08-30', NULL),
  (3, 9, 2, 'Preservación de Shenmue', 'Volcado del GD-ROM y documentación de las funciones en línea que ya no están disponibles.', 'Flycast', 'activo', '2025-09-01', NULL, NULL),
  (4, 1, 2, 'Recuperación de System Shock en disquete', 'Lectura sector a sector de los disquetes deteriorados para reconstruir una imagen íntegra.', 'DOSBox', 'activo', '2026-02-15', NULL, NULL),
  (5, 3, 2, 'Rescate de Dune II', 'Recuperación de los sectores desmagnetizados del disquete original.', 'DOSBox', 'activo', '2026-06-01', NULL, NULL);

-- PARTICIPACION (8) - en proyectos finalizados ya tienen fecha de salida
INSERT INTO PARTICIPACION (id_participacion, id_proyecto, id_experto, rol_experto, fecha_incorporacion, fecha_salida) VALUES
  (1, 1, 2, 'Líder de recuperación', '2023-02-01', '2024-05-30'),
  (2, 1, 3, 'Ingeniera inversa', '2023-03-01', '2024-04-15'),
  (3, 2, 1, 'Líder técnica', '2023-01-10', '2024-08-30'),
  (4, 2, 5, 'Especialista en cartuchos', '2023-02-01', '2024-07-31'),
  (5, 3, 1, 'Especialista en emulación', '2025-09-01', NULL),
  (6, 3, 4, 'Asesor legal', '2025-10-10', NULL),
  (7, 4, 2, 'Recuperación de medios magnéticos', '2026-02-15', NULL),
  (8, 5, 3, 'Analista de datos recuperados', '2026-06-01', NULL);

COMMIT;
