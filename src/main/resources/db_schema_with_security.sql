-- ===================================================================
-- SCRIPT SQL - MODELO DE BASE DE DATOS
-- Sistema de Cotización de Préstamos Bancarios con Autenticación
-- ===================================================================

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS cotizador_db;
USE cotizador_db;

-- ===================================================================
-- TABLA: permissions
-- ===================================================================
-- Permisos del sistema - Define ACCIONES específicas
-- Ejemplos: READ_CLIENTS, CREATE_LOAN, APPROVE_LOAN
-- VENTAJA: Control granular sobre qué puede hacer cada usuario
-- ===================================================================
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Nombre del permiso (ej: READ_CLIENTS, CREATE_LOAN)',
    description VARCHAR(255) COMMENT 'Descripción legible del permiso',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Permisos del sistema';

-- ===================================================================
-- TABLA: roles
-- ===================================================================
-- Roles del sistema - Define TIPOS de usuario
-- Ejemplos: ADMIN, USER, MANAGER, AUDITOR
-- Un rol agrupa varios permisos
-- ===================================================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Nombre del rol (ej: ADMIN, USER)',
    description VARCHAR(255) COMMENT 'Descripción del rol',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Roles de usuarios';

-- ===================================================================
-- TABLA: users
-- ===================================================================
-- Usuarios del sistema
-- Implementa UserDetails de Spring Security
-- RELACIÓN: Many-to-Many con roles (un usuario puede tener varios roles)
-- ===================================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Nombre de usuario único',
    password VARCHAR(255) NOT NULL COMMENT 'Contraseña encriptada con BCrypt',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Correo electrónico único',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Usuario activo/inactivo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de registro',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Última actualización',
    
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Usuarios del sistema';

-- ===================================================================
-- TABLA INTERMEDIA: user_roles
-- ===================================================================
-- Relación Many-to-Many entre usuarios y roles
-- 
-- ¿POR QUÉ MANY-TO-MANY?
-- - Un usuario puede tener VARIOS roles (ej: ADMIN y MANAGER)
-- - Un rol puede estar en VARIOS usuarios (ej: muchos usuarios con rol USER)
-- 
-- ESTRUCTURA:
-- - user_id: FK a users.id (el usuario)
-- - role_id: FK a roles.id (el rol)
-- 
-- EJEMPLO:
-- user_id=1, role_id=1  →  Usuario "juan" tiene rol "ADMIN"
-- user_id=1, role_id=2  →  Usuario "juan" tiene rol "MANAGER"
-- user_id=2, role_id=2  →  Usuario "maria" tiene rol "MANAGER"
-- ===================================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL COMMENT 'ID del usuario',
    role_id BIGINT NOT NULL COMMENT 'ID del rol',
    
    PRIMARY KEY (user_id, role_id),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Relación usuarios-roles';

-- ===================================================================
-- TABLA INTERMEDIA: role_permissions
-- ===================================================================
-- Relación Many-to-Many entre roles y permisos
-- 
-- ¿POR QUÉ MANY-TO-MANY?
-- - Un rol puede tener VARIOS permisos (ej: ADMIN tiene READ_CLIENTS, CREATE_CLIENTS, DELETE_CLIENTS)
-- - Un permiso puede estar en VARIOS roles (ej: READ_CLIENTS está en ADMIN y USER)
-- 
-- ESTRUCTURA:
-- - role_id: FK a roles.id (el rol)
-- - permission_id: FK a permissions.id (el permiso)
-- 
-- EJEMPLO:
-- role_id=1, permission_id=1  →  Rol "ADMIN" tiene permiso "READ_CLIENTS"
-- role_id=1, permission_id=2  →  Rol "ADMIN" tiene permiso "CREATE_CLIENTS"
-- role_id=2, permission_id=1  →  Rol "USER" tiene permiso "READ_CLIENTS"
-- 
-- VENTAJA: Si cambias permisos del rol ADMIN, TODOS los usuarios
-- con rol ADMIN automáticamente obtienen los cambios
-- ===================================================================
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL COMMENT 'ID del rol',
    permission_id BIGINT NOT NULL COMMENT 'ID del permiso',
    
    PRIMARY KEY (role_id, permission_id),
    
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Relación roles-permisos';

-- ===================================================================
-- TABLA: clientes
-- ===================================================================
-- Clientes que solicitan préstamos
-- RELACIÓN: One-to-Many con solicitudes_prestamo
-- (un cliente puede tener varias solicitudes)
-- ===================================================================
CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL COMMENT 'Nombre completo del cliente',
    documento_identidad VARCHAR(20) NOT NULL COMMENT 'DNI/RUC del cliente',
    email VARCHAR(255) NOT NULL COMMENT 'Correo electrónico',
    telefono VARCHAR(20) COMMENT 'Teléfono de contacto',
    ingreso_mensual DECIMAL(10,2) COMMENT 'Ingreso mensual del cliente',
    reg_estado INT NOT NULL DEFAULT 1 COMMENT '1=Activo, 0=Inactivo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de registro',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Última actualización',
    
    INDEX idx_documento (documento_identidad),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Clientes del sistema';

-- ===================================================================
-- TABLA: solicitudes_prestamo
-- ===================================================================
-- Solicitudes de préstamo de los clientes
-- RELACIÓN: Many-to-One con clientes
-- (muchas solicitudes pertenecen a un cliente)
-- 
-- ¿POR QUÉ MANY-TO-ONE?
-- - Una solicitud pertenece a UN SOLO cliente
-- - Un cliente puede tener VARIAS solicitudes
-- 
-- FOREIGN KEY cliente_id:
-- - Referencias clientes(id)
-- - ON DELETE RESTRICT: NO permite eliminar cliente si tiene solicitudes
--   (protege integridad de datos)
-- ===================================================================
CREATE TABLE IF NOT EXISTS solicitudes_prestamo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL COMMENT 'ID del cliente (FK)',
    
    -- Datos de la solicitud
    monto DECIMAL(10,2) COMMENT 'Monto solicitado',
    porcentaje_cuota_inicial DECIMAL(5,2) COMMENT 'Porcentaje de cuota inicial',
    monto_cuota_inicial DECIMAL(10,2) COMMENT 'Monto de cuota inicial calculado',
    monto_financiar DECIMAL(10,2) COMMENT 'Monto a financiar (monto - cuota inicial)',
    plazo_anios INT COMMENT 'Plazo del préstamo en años',
    
    -- Datos calculados
    tasa_interes DECIMAL(5,2) COMMENT 'Tasa de interés anual asignada',
    tcea DECIMAL(5,2) COMMENT 'Tasa de costo efectivo anual',
    cuota_mensual DECIMAL(10,2) COMMENT 'Cuota mensual calculada',
    
    -- Datos de validación
    motivo_rechazo VARCHAR(1000) COMMENT 'Motivo si la solicitud fue rechazada',
    riesgo_cliente INT COMMENT 'Nivel de riesgo del cliente (1=Bajo, 2=Medio, 3=Alto)',
    estado INT COMMENT '0=Rechazado, 1=Aprobado, NULL=Pendiente',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Última actualización',
    
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT,
    
    INDEX idx_cliente_id (cliente_id),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Solicitudes de préstamo';

-- ===================================================================
-- INSERCIÓN DE DATOS INICIALES
-- ===================================================================

-- -------------------------------------------------------------------
-- 1. INSERTAR PERMISOS
-- -------------------------------------------------------------------
INSERT INTO permissions (name, description) VALUES
-- Permisos de clientes
('READ_CLIENTS', 'Ver listado de clientes'),
('CREATE_CLIENTS', 'Crear nuevos clientes'),
('UPDATE_CLIENTS', 'Actualizar clientes existentes'),
('DELETE_CLIENTS', 'Eliminar clientes'),

-- Permisos de solicitudes de préstamo
('READ_LOANS', 'Ver solicitudes de préstamo'),
('CREATE_LOANS', 'Crear solicitudes de préstamo'),
('UPDATE_LOANS', 'Actualizar solicitudes de préstamo'),
('DELETE_LOANS', 'Eliminar solicitudes de préstamo'),
('SIMULATE_LOANS', 'Simular préstamos'),
('APPROVE_LOANS', 'Aprobar solicitudes de préstamo'),
('REJECT_LOANS', 'Rechazar solicitudes de préstamo'),

-- Permisos de administración
('MANAGE_USERS', 'Gestionar usuarios del sistema'),
('MANAGE_ROLES', 'Gestionar roles y permisos');

-- -------------------------------------------------------------------
-- 2. INSERTAR ROLES
-- -------------------------------------------------------------------
INSERT INTO roles (name, description) VALUES
('USER', 'Usuario regular del sistema - puede ver y crear solicitudes'),
('MANAGER', 'Gerente - puede aprobar/rechazar solicitudes'),
('ADMIN', 'Administrador - acceso total al sistema');

-- -------------------------------------------------------------------
-- 3. ASIGNAR PERMISOS A ROLES
-- -------------------------------------------------------------------

-- ROL: USER (permisos básicos)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'USER'),
    id
FROM permissions
WHERE name IN (
    'READ_CLIENTS',
    'CREATE_CLIENTS',
    'READ_LOANS',
    'CREATE_LOANS',
    'SIMULATE_LOANS'
);

-- ROL: MANAGER (permisos de gestión + básicos)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'MANAGER'),
    id
FROM permissions
WHERE name IN (
    'READ_CLIENTS',
    'CREATE_CLIENTS',
    'UPDATE_CLIENTS',
    'READ_LOANS',
    'CREATE_LOANS',
    'UPDATE_LOANS',
    'SIMULATE_LOANS',
    'APPROVE_LOANS',
    'REJECT_LOANS'
);

-- ROL: ADMIN (todos los permisos)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    id
FROM permissions;

-- -------------------------------------------------------------------
-- 4. CREAR USUARIOS DE PRUEBA
-- -------------------------------------------------------------------
-- Contraseña para todos: "password123" (encriptada con BCrypt)
-- $2a$10$... es el hash BCrypt de "password123"

INSERT INTO users (username, password, email, enabled) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@cotizador.com', TRUE),
('manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'manager@cotizador.com', TRUE),
('usuario', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'usuario@cotizador.com', TRUE);

-- -------------------------------------------------------------------
-- 5. ASIGNAR ROLES A USUARIOS
-- -------------------------------------------------------------------

-- Usuario "admin" → Rol ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM users WHERE username = 'admin'),
    (SELECT id FROM roles WHERE name = 'ADMIN');

-- Usuario "manager" → Rol MANAGER
INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM users WHERE username = 'manager'),
    (SELECT id FROM roles WHERE name = 'MANAGER');

-- Usuario "usuario" → Rol USER
INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM users WHERE username = 'usuario'),
    (SELECT id FROM roles WHERE name = 'USER');

-- -------------------------------------------------------------------
-- 6. INSERTAR CLIENTES DE PRUEBA (OPCIONAL)
-- -------------------------------------------------------------------
INSERT INTO clientes (nombre_completo, documento_identidad, email, telefono, ingreso_mensual, reg_estado) VALUES
('Juan Pérez García', '12345678', 'juan.perez@example.com', '987654321', 3000.00, 1),
('María González López', '87654321', 'maria.gonzalez@example.com', '987654322', 4500.00, 1),
('Carlos Rodríguez Sánchez', '11223344', 'carlos.rodriguez@example.com', '987654323', 5000.00, 1);

-- ===================================================================
-- RESUMEN DEL MODELO DE DATOS
-- ===================================================================

/*
DIAGRAMA DE RELACIONES:

1. SEGURIDAD (Autenticación y Autorización):

   User ←→ user_roles ←→ Role ←→ role_permissions ←→ Permission
   
   - User ←→ Role: Many-to-Many
     Un usuario puede tener varios roles
     Un rol puede estar en varios usuarios
   
   - Role ←→ Permission: Many-to-Many
     Un rol puede tener varios permisos
     Un permiso puede estar en varios roles

2. NEGOCIO (Clientes y Préstamos):

   Cliente ←→ SolicitudPrestamo
   
   - Cliente → SolicitudPrestamo: One-to-Many
     Un cliente puede tener varias solicitudes
     Una solicitud pertenece a un solo cliente

FLUJO DE AUTENTICACIÓN:
1. Usuario hace POST /api/auth/login con username y password
2. Backend valida credenciales
3. Backend genera token JWT
4. Frontend guarda token
5. Frontend envía token en header "Authorization: Bearer <token>"
6. Backend valida token y permite acceso

FLUJO DE AUTORIZACIÓN:
1. Usuario autenticado hace request a endpoint protegido
2. Spring Security verifica roles/permisos del usuario
3. Si tiene el permiso requerido → permite acceso
4. Si no tiene permiso → retorna 403 Forbidden

USUARIOS DE PRUEBA:
- admin / password123    → Rol: ADMIN (acceso total)
- manager / password123  → Rol: MANAGER (gestión)
- usuario / password123  → Rol: USER (básico)
*/
