-- PetCare Store: ejecutar con MySQL 8.0.16 o superior.
-- No elimina tablas ni datos existentes. Los ejemplos se insertan solo si no existen.
CREATE DATABASE IF NOT EXISTS petcare_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE petcare_db;

CREATE TABLE IF NOT EXISTS usuario (
 id_usuario INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 correo VARCHAR(150) NOT NULL UNIQUE,
 password VARCHAR(100) NOT NULL,
 rol VARCHAR(20) NOT NULL,
 estado BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMINISTRADOR','EMPLEADO'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categoria (
 id_categoria INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL UNIQUE,
 descripcion VARCHAR(500),
 estado BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS producto (
 id_producto INT AUTO_INCREMENT PRIMARY KEY,
 id_categoria INT NOT NULL,
 nombre VARCHAR(150) NOT NULL,
 descripcion VARCHAR(1000),
 precio DECIMAL(10,2) NOT NULL,
 stock INT NOT NULL DEFAULT 0,
 imagen VARCHAR(255),
 estado BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria),
 CONSTRAINT ck_producto_precio CHECK (precio >= 0),
 CONSTRAINT ck_producto_stock CHECK (stock >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cliente (
 id_cliente INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 apellido VARCHAR(100) NOT NULL,
 documento VARCHAR(20) NOT NULL UNIQUE,
 telefono VARCHAR(20) NOT NULL,
 correo VARCHAR(150) NOT NULL,
 direccion VARCHAR(250) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventario (
 id_inventario INT AUTO_INCREMENT PRIMARY KEY,
 id_producto INT NOT NULL UNIQUE,
 cantidad INT NOT NULL DEFAULT 0,
 stock_minimo INT NOT NULL DEFAULT 10,
 fecha_actualizacion DATETIME(6) NOT NULL,
 CONSTRAINT fk_inventario_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
 CONSTRAINT ck_inventario_cantidad CHECK (cantidad >= 0),
 CONSTRAINT ck_inventario_minimo CHECK (stock_minimo >= 0)
) ENGINE=InnoDB;
-- ============================================================================
-- CARGA DE DATOS INICIALES (SEMILLA)
-- Se usa START TRANSACTION / COMMIT para que toda la carga sea atómica
-- (se guarda todo junto o nada si ocurre un fallo).
-- ============================================================================
START TRANSACTION;

-- 1. Usuarios iniciales: contraseñas cifradas con BCrypt (nunca texto plano)
--    Se comprueba por 'correo' (UNIQUE) que no existan antes de insertarlos.
INSERT INTO usuario (nombre, correo, password, rol, estado)
SELECT 'Administrador PetCare', 'admin@petcarestore.com', '$2a$10$/XfW0zU.yHY7MkTAGlrAK.EPK3iyU9MYCYPwn4KKcUZjubUkXA342', 'ADMINISTRADOR', TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE correo = 'admin@petcarestore.com');

INSERT INTO usuario (nombre, correo, password, rol, estado)
SELECT 'Empleado PetCare', 'empleado@petcarestore.com', '$2a$10$mcz.TSavccJW.SDnFT5zKuT1rGa4ec8GT3VozNXfSfJRT4wWUivQ6', 'EMPLEADO', TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE correo = 'empleado@petcarestore.com');

-- 2. Categorías base: se insertan primero porque los productos dependen de ellas (FK).
--    Se verifica por 'nombre' (UNIQUE) para no duplicarlas.
INSERT INTO categoria (nombre, descripcion, estado)
SELECT 'Alimentos', 'Nutrición para perros y gatos', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Alimentos');

INSERT INTO categoria (nombre, descripcion, estado)
SELECT 'Accesorios', 'Collares, correas y accesorios', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Accesorios');

INSERT INTO categoria (nombre, descripcion, estado)
SELECT 'Juguetes', 'Diversión para mascotas', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Juguetes');

INSERT INTO categoria (nombre, descripcion, estado)
SELECT 'Higiene', 'Limpieza y cuidado diario', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Higiene');

INSERT INTO categoria (nombre, descripcion, estado)
SELECT 'Salud', 'Productos de cuidado y bienestar', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Salud');

-- 3. Productos de demostración: se obtiene 'c.id_categoria' consultando la tabla
--    'categoria' por nombre en lugar de usar un ID fijo (1, 2, etc.) que podría variar.
INSERT INTO producto (id_categoria, nombre, descripcion, precio, stock, estado)
SELECT c.id_categoria, 'Alimento para perros', 'Bolsa de alimento balanceado para perros adultos', 45.00, 25, TRUE
FROM categoria c
WHERE c.nombre = 'Alimentos'
  AND NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Alimento para perros');

INSERT INTO producto (id_categoria, nombre, descripcion, precio, stock, estado)
SELECT c.id_categoria, 'Collar ajustable', 'Collar cómodo con ajuste para perro', 25.00, 5, TRUE
FROM categoria c
WHERE c.nombre = 'Accesorios'
  AND NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Collar ajustable');

INSERT INTO producto (id_categoria, nombre, descripcion, precio, stock, estado)
SELECT c.id_categoria, 'Pelota para mascotas', 'Pelota resistente para juegos', 18.00, 18, TRUE
FROM categoria c
WHERE c.nombre = 'Juguetes'
  AND NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Pelota para mascotas');

INSERT INTO producto (id_categoria, nombre, descripcion, precio, stock, estado)
SELECT c.id_categoria, 'Champú para perros', 'Champú suave para el cuidado del pelaje', 32.50, 8, TRUE
FROM categoria c
WHERE c.nombre = 'Higiene'
  AND NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Champú para perros');

INSERT INTO producto (id_categoria, nombre, descripcion, precio, stock, estado)
SELECT c.id_categoria, 'Cepillo para gatos', 'Cepillo para retirar pelo suelto', 22.00, 0, TRUE
FROM categoria c
WHERE c.nombre = 'Accesorios'
  AND NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Cepillo para gatos');

-- 4. Inventario inicial: sincroniza automáticamente cada producto con su fila 1:1
--    en 'inventario', copiando su stock inicial y fijando stock_minimo = 10.
INSERT INTO inventario (id_producto, cantidad, stock_minimo, fecha_actualizacion)
SELECT p.id_producto, p.stock, 10, NOW()
FROM producto p
WHERE NOT EXISTS (SELECT 1 FROM inventario i WHERE i.id_producto = p.id_producto);

-- 5. Clientes de demostración: se valida por 'documento' (UNIQUE) para evitar duplicados.
INSERT INTO cliente (nombre, apellido, documento, telefono, correo, direccion)
SELECT 'María', 'Torres', '45678901', '987654321', 'maria.torres@example.com', 'Av. Las Flores 123, Lima'
WHERE NOT EXISTS (SELECT 1 FROM cliente WHERE documento = '45678901');

INSERT INTO cliente (nombre, apellido, documento, telefono, correo, direccion)
SELECT 'Carlos', 'Rojas', '45678902', '987654322', 'carlos.rojas@example.com', 'Jr. Los Olivos 456, Lima'
WHERE NOT EXISTS (SELECT 1 FROM cliente WHERE documento = '45678902');

-- Confirma y guarda permanentemente todos los registros insertados en la transacción.
COMMIT;
