-- ArchiveLudo - Bases de Datos 1
-- 01: creacion de la base de datos y de las 17 tablas
-- Las tablas van en orden: primero las que no dependen de otras.
-- Ejecutar con adminbd.

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS archiveludo
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE archiveludo;

-- USUARIO: usuarios de la aplicacion
CREATE TABLE USUARIO (
  id_usuario bigint NOT NULL AUTO_INCREMENT,
  primer_nombre varchar(50) NOT NULL,
  segundo_nombre varchar(50) DEFAULT NULL,
  primer_apellido varchar(50) NOT NULL,
  segundo_apellido varchar(50) DEFAULT NULL,
  correo varchar(120) NOT NULL,
  hash_contrasenia varchar(100) NOT NULL,
  rol enum('administrador','curador','consulta') NOT NULL,
  fecha_creacion date NOT NULL DEFAULT (curdate()),
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uq_usuario_correo (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ESTUDIO: estudios desarrolladores
CREATE TABLE ESTUDIO (
  id_estudio bigint NOT NULL AUTO_INCREMENT,
  nombre varchar(150) NOT NULL,
  pais varchar(80) NOT NULL,
  anio_fundacion int NOT NULL,
  anio_cierre int DEFAULT NULL,
  estado_actual enum('activo','desaparecido') NOT NULL,
  PRIMARY KEY (id_estudio),
  CONSTRAINT ck_estudio_anios CHECK (((anio_cierre is null) or (anio_cierre >= anio_fundacion))),
  CONSTRAINT ck_estudio_cierre_estado CHECK ((((estado_actual = 'activo') and (anio_cierre is null)) or ((estado_actual = 'desaparecido') and (anio_cierre is not null))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- COMPANIA_EDITORA: editoras
CREATE TABLE COMPANIA_EDITORA (
  id_editora bigint NOT NULL AUTO_INCREMENT,
  nombre varchar(150) NOT NULL,
  pais varchar(80) NOT NULL,
  PRIMARY KEY (id_editora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- INSTITUCION: instituciones que guardan copias
CREATE TABLE INSTITUCION (
  id_institucion bigint NOT NULL AUTO_INCREMENT,
  nombre varchar(150) NOT NULL,
  tipo enum('museo','universidad','archivo','coleccion privada') NOT NULL,
  pais varchar(80) NOT NULL,
  PRIMARY KEY (id_institucion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- EXPERTO: expertos de los proyectos
CREATE TABLE EXPERTO (
  id_experto bigint NOT NULL AUTO_INCREMENT,
  primer_nombre_ex varchar(60) NOT NULL,
  segundo_nombre_ex varchar(60) DEFAULT NULL,
  primer_apellido_ex varchar(60) NOT NULL,
  segundo_apellido_ex varchar(60) DEFAULT NULL,
  especialidad varchar(100) NOT NULL,
  afiliacion varchar(150) DEFAULT NULL,
  correo varchar(150) NOT NULL,
  PRIMARY KEY (id_experto),
  UNIQUE KEY uq_experto_correo (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- VIDEOJUEGO: catalogo de juegos
CREATE TABLE VIDEOJUEGO (
  id_videojuego bigint NOT NULL AUTO_INCREMENT,
  titulo varchar(200) NOT NULL,
  genero varchar(80) NOT NULL,
  plataforma_original varchar(80) NOT NULL,
  descripcion text,
  anio_lanzamiento int NOT NULL,
  estado_conservacion enum('activo','en riesgo','parcialmente perdido','totalmente perdido','restaurado') NOT NULL,
  id_estudio bigint NOT NULL,
  id_usuario bigint NOT NULL,
  url_imagen varchar(500) DEFAULT NULL,
  PRIMARY KEY (id_videojuego),
  KEY fk_videojuego_estudio (id_estudio),
  KEY fk_videojuego_usuario (id_usuario),
  CONSTRAINT fk_videojuego_estudio FOREIGN KEY (id_estudio) REFERENCES ESTUDIO (id_estudio),
  CONSTRAINT fk_videojuego_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- COMPED_VID: relacion N:M juego - editora
CREATE TABLE COMPED_VID (
  id_videojuego bigint NOT NULL,
  id_editora bigint NOT NULL,
  PRIMARY KEY (id_videojuego,id_editora),
  KEY fk_comped_vid_editora (id_editora),
  CONSTRAINT fk_comped_vid_editora FOREIGN KEY (id_editora) REFERENCES COMPANIA_EDITORA (id_editora) ON DELETE CASCADE,
  CONSTRAINT fk_comped_vid_videojuego FOREIGN KEY (id_videojuego) REFERENCES VIDEOJUEGO (id_videojuego) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- COPIA: datos comunes de la copia (supertipo)
CREATE TABLE COPIA (
  id_copia bigint NOT NULL AUTO_INCREMENT,
  id_videojuego bigint NOT NULL,
  id_usuario bigint NOT NULL,
  tipo_copia varchar(10) NOT NULL,
  estado_integridad varchar(20) NOT NULL,
  fecha_registro date NOT NULL,
  PRIMARY KEY (id_copia),
  KEY fk_copia_videojuego (id_videojuego),
  KEY fk_copia_usuario (id_usuario),
  CONSTRAINT fk_copia_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario),
  CONSTRAINT fk_copia_videojuego FOREIGN KEY (id_videojuego) REFERENCES VIDEOJUEGO (id_videojuego),
  CONSTRAINT ck_copia_integridad CHECK ((estado_integridad in ('buena','deteriorada','dañada','irrecuperable'))),
  CONSTRAINT ck_copia_tipo CHECK ((tipo_copia in ('fisica','digital')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- COPIA_FISICA: subtipo fisico
CREATE TABLE COPIA_FISICA (
  id_copia bigint NOT NULL,
  numero_serie varchar(60) DEFAULT NULL,
  ubicacion_fisica varchar(200) NOT NULL,
  tipo_soporte varchar(40) NOT NULL,
  PRIMARY KEY (id_copia),
  CONSTRAINT fk_copia_fisica_copia FOREIGN KEY (id_copia) REFERENCES COPIA (id_copia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- COPIA_DIGITAL: subtipo digital
CREATE TABLE COPIA_DIGITAL (
  id_copia bigint NOT NULL,
  formato_archivo varchar(20) NOT NULL,
  tamanio bigint NOT NULL,
  hash_verificacion varchar(128) NOT NULL,
  PRIMARY KEY (id_copia),
  CONSTRAINT fk_copia_digital_copia FOREIGN KEY (id_copia) REFERENCES COPIA (id_copia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- INST_CORREO: correos de la institucion
CREATE TABLE INST_CORREO (
  id_institucion bigint NOT NULL,
  correo varchar(150) NOT NULL,
  PRIMARY KEY (id_institucion,correo),
  CONSTRAINT fk_inst_correo_institucion FOREIGN KEY (id_institucion) REFERENCES INSTITUCION (id_institucion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- INST_TELEFONO: telefonos de la institucion
CREATE TABLE INST_TELEFONO (
  id_institucion bigint NOT NULL,
  numero_telefono decimal(15,0) NOT NULL,
  PRIMARY KEY (id_institucion,numero_telefono),
  CONSTRAINT fk_inst_telefono_institucion FOREIGN KEY (id_institucion) REFERENCES INSTITUCION (id_institucion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- CUSTODIA: historial de custodias de cada copia
CREATE TABLE CUSTODIA (
  id_custodia bigint NOT NULL AUTO_INCREMENT,
  id_copia bigint NOT NULL,
  id_institucion bigint NOT NULL,
  fecha_inicio date NOT NULL,
  fecha_fin date DEFAULT NULL,
  condicion_almacenamiento varchar(200) NOT NULL,
  PRIMARY KEY (id_custodia),
  KEY fk_custodia_copia (id_copia),
  KEY fk_custodia_institucion (id_institucion),
  CONSTRAINT fk_custodia_copia FOREIGN KEY (id_copia) REFERENCES COPIA (id_copia),
  CONSTRAINT fk_custodia_institucion FOREIGN KEY (id_institucion) REFERENCES INSTITUCION (id_institucion),
  CONSTRAINT ck_custodia_fechas CHECK (((fecha_fin is null) or (fecha_fin >= fecha_inicio)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ESTATUS_LEGAL: historial del estatus legal
CREATE TABLE ESTATUS_LEGAL (
  id_estatus bigint NOT NULL AUTO_INCREMENT,
  id_videojuego bigint NOT NULL,
  id_usuario bigint NOT NULL,
  estatus varchar(30) NOT NULL,
  fecha_determinacion date NOT NULL,
  observacion varchar(500) DEFAULT NULL,
  PRIMARY KEY (id_estatus),
  KEY fk_estatus_legal_videojuego (id_videojuego),
  KEY fk_estatus_legal_usuario (id_usuario),
  CONSTRAINT fk_estatus_legal_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario),
  CONSTRAINT fk_estatus_legal_videojuego FOREIGN KEY (id_videojuego) REFERENCES VIDEOJUEGO (id_videojuego),
  CONSTRAINT ck_estatus_legal_estatus CHECK ((estatus in ('protegido','dominio publico','huerfano','abandonware')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- EVENTO_DE_RIESGO: eventos de riesgo
CREATE TABLE EVENTO_DE_RIESGO (
  id_evento bigint NOT NULL AUTO_INCREMENT,
  id_videojuego bigint NOT NULL,
  id_usuario bigint NOT NULL,
  tipo_evento varchar(40) NOT NULL,
  fecha_evento date NOT NULL,
  descripcion varchar(500) NOT NULL,
  fuente varchar(200) DEFAULT NULL,
  PRIMARY KEY (id_evento),
  KEY fk_evento_de_riesgo_videojuego (id_videojuego),
  KEY fk_evento_de_riesgo_usuario (id_usuario),
  CONSTRAINT fk_evento_de_riesgo_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario),
  CONSTRAINT fk_evento_de_riesgo_videojuego FOREIGN KEY (id_videojuego) REFERENCES VIDEOJUEGO (id_videojuego),
  CONSTRAINT ck_evento_de_riesgo_tipo CHECK ((tipo_evento in ('cierre de servidores','perdida de codigo fuente','retiro de tienda digital','deterioro de medios','disputa legal')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- PROYECTO_DE_RESTAURACION: proyectos de restauracion
CREATE TABLE PROYECTO_DE_RESTAURACION (
  id_proyecto bigint NOT NULL AUTO_INCREMENT,
  id_videojuego bigint NOT NULL,
  id_usuario bigint NOT NULL,
  nombre varchar(150) NOT NULL,
  descripcion varchar(500) DEFAULT NULL,
  herramienta_emulador varchar(100) DEFAULT NULL,
  estado enum('activo','finalizado') NOT NULL,
  fecha_inicio date NOT NULL,
  fecha_fin date DEFAULT NULL,
  url_informe varchar(500) DEFAULT NULL,
  PRIMARY KEY (id_proyecto),
  KEY fk_proyecto_videojuego (id_videojuego),
  KEY fk_proyecto_usuario (id_usuario),
  CONSTRAINT fk_proyecto_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario),
  CONSTRAINT fk_proyecto_videojuego FOREIGN KEY (id_videojuego) REFERENCES VIDEOJUEGO (id_videojuego),
  CONSTRAINT ck_proyecto_estado_fin CHECK ((((estado = 'activo') and (fecha_fin is null)) or ((estado = 'finalizado') and (fecha_fin is not null)))),
  CONSTRAINT ck_proyecto_fechas CHECK (((fecha_fin is null) or (fecha_fin >= fecha_inicio)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- PARTICIPACION: expertos en cada proyecto
CREATE TABLE PARTICIPACION (
  id_participacion bigint NOT NULL AUTO_INCREMENT,
  id_proyecto bigint NOT NULL,
  id_experto bigint NOT NULL,
  rol_experto varchar(80) NOT NULL,
  fecha_incorporacion date NOT NULL,
  fecha_salida date DEFAULT NULL,
  PRIMARY KEY (id_participacion),
  UNIQUE KEY uq_participacion_experto_proyecto (id_proyecto,id_experto),
  KEY fk_participacion_experto (id_experto),
  CONSTRAINT fk_participacion_experto FOREIGN KEY (id_experto) REFERENCES EXPERTO (id_experto),
  CONSTRAINT fk_participacion_proyecto FOREIGN KEY (id_proyecto) REFERENCES PROYECTO_DE_RESTAURACION (id_proyecto),
  CONSTRAINT ck_participacion_fechas CHECK (((fecha_salida is null) or (fecha_salida >= fecha_incorporacion)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
