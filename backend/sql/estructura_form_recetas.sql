-- Form Recetas - estructura PostgreSQL
-- Ejecuta este archivo dentro de la base de datos form_recetas.
-- Si ya creaste las tablas, NO vuelvas a ejecutar este archivo.
-- Los IDs son INT PRIMARY KEY; Spring Boot crea y sincroniza secuencias para
-- usuarios, recetas, categorías e imágenes de portada al arrancar.

CREATE TABLE roles (
    id_rol INT PRIMARY KEY,
    nombre_rol VARCHAR(255)
);

CREATE TABLE usuarios (
    id_usuario INT PRIMARY KEY,
    nombre_usuario VARCHAR(255),
    correo VARCHAR(255),
    contrasena_hash VARCHAR(255),
    nombre_completo VARCHAR(255),
    fecha_registro TIMESTAMP,
    activo BOOLEAN
);

CREATE TABLE paises (
    id_pais INT PRIMARY KEY,
    nombre_pais VARCHAR(255)
);

CREATE TABLE categorias (
    id_categoria INT PRIMARY KEY,
    nombre_categoria VARCHAR(255),
    imagen_url VARCHAR(255)
);

CREATE TABLE equipos_cocina (
    id_equipo INT PRIMARY KEY,
    nombre_equipo VARCHAR(255)
);

CREATE TABLE ingredientes (
    id_ingrediente INT PRIMARY KEY,
    nombre_ingrediente VARCHAR(255)
);

CREATE TABLE unidades_medida (
    id_unidad INT PRIMARY KEY,
    nombre_unidad VARCHAR(255),
    abreviatura VARCHAR(255)
);

CREATE TABLE usuario_rol (
    id_usuario INT,
    id_rol INT,
    PRIMARY KEY (id_usuario, id_rol),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

CREATE TABLE direcciones (
    id_direccion INT PRIMARY KEY,
    id_usuario INT,
    calle VARCHAR(255),
    numero VARCHAR(255),
    colonia VARCHAR(255),
    ciudad VARCHAR(255),
    estado VARCHAR(255),
    codigo_postal VARCHAR(255),
    es_principal BOOLEAN,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE metodos_pago (
    id_metodo_pago INT PRIMARY KEY,
    id_usuario INT,
    tipo VARCHAR(255),
    ultimos_4_digitos VARCHAR(255),
    es_principal BOOLEAN,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE carritos (
    id_carrito INT PRIMARY KEY,
    id_usuario INT,
    fecha_creacion TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE recetas (
    id_receta INT PRIMARY KEY,
    id_usuario INT,
    id_pais INT,
    titulo VARCHAR(255),
    descripcion TEXT,
    tiempo_preparacion_min INT,
    porciones INT,
    dificultad VARCHAR(255),
    precio DECIMAL(10,2),
    fecha_publicacion TIMESTAMP,
    activo BOOLEAN,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_pais) REFERENCES paises(id_pais)
);

CREATE TABLE pedidos (
    id_pedido INT PRIMARY KEY,
    id_usuario INT,
    id_direccion INT,
    id_metodo_pago INT,
    fecha_pedido TIMESTAMP,
    estado VARCHAR(255),
    total DECIMAL(10,2),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_direccion) REFERENCES direcciones(id_direccion),
    FOREIGN KEY (id_metodo_pago) REFERENCES metodos_pago(id_metodo_pago)
);

CREATE TABLE productos (
    id_producto INT PRIMARY KEY,
    id_receta INT,
    nombre_producto VARCHAR(255),
    descripcion TEXT,
    precio DECIMAL(10,2),
    stock INT,
    FOREIGN KEY (id_receta) REFERENCES recetas(id_receta)
);

CREATE TABLE valoraciones (
    id_valoracion INT PRIMARY KEY,
    id_usuario INT,
    id_receta INT,
    puntuacion INT,
    comentario_breve TEXT,
    fecha TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_receta) REFERENCES recetas(id_receta)
);

CREATE TABLE favoritos (
    id_usuario INT,
    id_receta INT,
    fecha_agregado TIMESTAMP,
    PRIMARY KEY (id_usuario, id_receta),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_receta) REFERENCES recetas(id_receta)
);

CREATE TABLE imagenes_receta (
    id_imagen INT PRIMARY KEY,
    id_receta INT,
    url_imagen VARCHAR(255),
    es_portada BOOLEAN,
    FOREIGN KEY (id_receta) REFERENCES recetas(id_receta)
);

CREATE TABLE informacion_nutricional (
    id_receta INT PRIMARY KEY,
    calorias DECIMAL(10,2),
    proteinas_g DECIMAL(10,2),
    carbohidratos_g DECIMAL(10,2),
    grasas_g DECIMAL(10,2),
    azucares_g DECIMAL(10,2),
    sodio_mg DECIMAL(10,2),
    FOREIGN KEY (id_receta) REFERENCES recetas(id_receta)
);

CREATE TABLE pasos_receta (
    id_paso INT PRIMARY KEY,
    id_receta INT,
    numero_paso INT,
    descripcion TEXT,
    tiempo_estimado_min INT,
    FOREIGN KEY (id_receta) REFERENCES recetas(id_receta)
);

CREATE TABLE receta_categoria (
    id_receta INT,
    id_categoria INT,
    PRIMARY KEY (id_receta, id_categoria),
    FOREIGN KEY (id_receta) REFERENCES recetas(id_receta),
    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
);

CREATE TABLE receta_equipo (
    id_receta INT,
    id_equipo INT,
    PRIMARY KEY (id_receta, id_equipo),
    FOREIGN KEY (id_receta) REFERENCES recetas(id_receta),
    FOREIGN KEY (id_equipo) REFERENCES equipos_cocina(id_equipo)
);

CREATE TABLE receta_ingrediente (
    id_receta INT,
    id_ingrediente INT,
    cantidad DECIMAL(10,2),
    id_unidad INT,
    PRIMARY KEY (id_receta, id_ingrediente),
    FOREIGN KEY (id_receta) REFERENCES recetas(id_receta),
    FOREIGN KEY (id_ingrediente) REFERENCES ingredientes(id_ingrediente),
    FOREIGN KEY (id_unidad) REFERENCES unidades_medida(id_unidad)
);

CREATE TABLE carrito_item (
    id_carrito INT,
    id_producto INT,
    cantidad INT,
    PRIMARY KEY (id_carrito, id_producto),
    FOREIGN KEY (id_carrito) REFERENCES carritos(id_carrito),
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

CREATE TABLE pedido_item (
    id_pedido INT,
    id_producto INT,
    cantidad INT,
    precio_unitario DECIMAL(10,2),
    PRIMARY KEY (id_pedido, id_producto),
    FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido),
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);
