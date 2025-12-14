# 📊 EXPLICACIÓN DETALLADA - MODELADO DE BASE DE DATOS

## 🎯 Objetivo

Este documento explica el **PORQUÉ** de cada tabla, relación y decisión de diseño en el modelo de base de datos. Te ayudará a entender el modelado para que puedas aplicarlo en tus propios proyectos.

---

## 📚 Conceptos Básicos de Relaciones

### 1. One-to-One (Uno a Uno)
**Ejemplo**: Persona ↔ Pasaporte
- Una persona tiene UN pasaporte
- Un pasaporte pertenece a UNA persona

**Implementación SQL**:
```sql
CREATE TABLE personas (
    id INT PRIMARY KEY,
    nombre VARCHAR(100)
);

CREATE TABLE pasaportes (
    id INT PRIMARY KEY,
    numero VARCHAR(20),
    persona_id INT UNIQUE, -- UNIQUE garantiza One-to-One
    FOREIGN KEY (persona_id) REFERENCES personas(id)
);
```

**¿Cuándo usar?**
- Cuando necesitas separar información por organización
- Cuando algunos campos son opcionales y grandes

---

### 2. One-to-Many (Uno a Muchos)
**Ejemplo**: Cliente ↔ Solicitudes
- Un cliente tiene MUCHAS solicitudes
- Una solicitud pertenece a UN cliente

**Implementación SQL**:
```sql
CREATE TABLE clientes (
    id INT PRIMARY KEY,
    nombre VARCHAR(100)
);

CREATE TABLE solicitudes (
    id INT PRIMARY KEY,
    monto DECIMAL(10,2),
    cliente_id INT, -- SIN UNIQUE permite Many
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);
```

**¿Cuándo usar?**
- Cuando un registro "padre" tiene múltiples "hijos"
- Es la relación MÁS COMÚN en bases de datos

---

### 3. Many-to-Many (Muchos a Muchos)
**Ejemplo**: Estudiantes ↔ Cursos
- Un estudiante está en MUCHOS cursos
- Un curso tiene MUCHOS estudiantes

**Implementación SQL**:
```sql
CREATE TABLE estudiantes (
    id INT PRIMARY KEY,
    nombre VARCHAR(100)
);

CREATE TABLE cursos (
    id INT PRIMARY KEY,
    nombre VARCHAR(100)
);

-- TABLA INTERMEDIA (junction table)
CREATE TABLE estudiante_curso (
    estudiante_id INT,
    curso_id INT,
    PRIMARY KEY (estudiante_id, curso_id),
    FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id),
    FOREIGN KEY (curso_id) REFERENCES cursos(id)
);
```

**¿Cuándo usar?**
- Cuando AMBOS lados pueden tener múltiples relaciones
- Requiere una **tabla intermedia** (junction table)

---

## 🏗️ NUESTRO MODELO - ANÁLISIS DETALLADO

---

## 1️⃣ TABLA: clientes

```sql
CREATE TABLE clientes (
    id BIGINT PRIMARY KEY,
    nombre_completo VARCHAR(255),
    documento_identidad VARCHAR(20),
    email VARCHAR(255),
    -- otros campos...
);
```

### ¿Por qué existe?

Necesitamos almacenar la información de personas que solicitan préstamos. Un cliente es una **entidad independiente** que puede existir sin solicitudes.

### Decisiones de diseño:

- **id BIGINT**: Identificador único, permite hasta 9,223,372,036,854,775,807 registros
- **documento_identidad VARCHAR(20)**: DNI puede tener letras (ej: DNI-12345678)
- **email VARCHAR(255)**: Estándar RFC 5321 para emails
- **ingreso_mensual DECIMAL(10,2)**: 10 dígitos totales, 2 decimales
  - Ejemplo: 99,999,999.99 (casi 100 millones)

### Índices:

```sql
INDEX idx_documento (documento_identidad)
INDEX idx_email (email)
```

**¿Por qué?** Aceleran búsquedas frecuentes:
- Buscar cliente por DNI: `WHERE documento_identidad = '12345678'`
- Buscar cliente por email: `WHERE email = 'juan@example.com'`

**Costo**: Los índices ocupan espacio en disco pero mejoran MUCHO la velocidad.

---

## 2️⃣ TABLA: solicitudes_prestamo

```sql
CREATE TABLE solicitudes_prestamo (
    id BIGINT PRIMARY KEY,
    cliente_id BIGINT,
    monto DECIMAL(10,2),
    -- cálculos...
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT
);
```

### ¿Por qué existe?

Un cliente puede solicitar MÚLTIPLES préstamos a lo largo del tiempo. Cada solicitud es un evento independiente con sus propios cálculos.

---

### 🔗 RELACIÓN: Cliente ↔ Solicitudes (One-to-Many)

```
Cliente (1) ─────→ (N) Solicitudes
```

**Ejemplo real**:
```
Juan Pérez (cliente_id=1)
    ├─ Solicitud #1: $50,000 para casa (2020)
    ├─ Solicitud #2: $20,000 para auto (2021)
    └─ Solicitud #3: $10,000 para estudios (2023)
```

### ¿Por qué One-to-Many y NO One-to-One?

**Si fuera One-to-One:**
```sql
-- DISEÑO MALO ❌
CREATE TABLE clientes (
    id INT PRIMARY KEY,
    nombre VARCHAR(100),
    solicitud_id INT UNIQUE -- Solo UNA solicitud
);
```

**Problemas:**
- ❌ Cliente solo podría tener 1 solicitud en toda su vida
- ❌ Para nueva solicitud, tendrías que ELIMINAR la anterior
- ❌ No hay historial

**Nuestro diseño (One-to-Many):**
```sql
-- DISEÑO BUENO ✅
CREATE TABLE solicitudes (
    id INT PRIMARY KEY,
    cliente_id INT -- Muchas solicitudes pueden tener el mismo cliente_id
);
```

**Ventajas:**
- ✅ Cliente puede tener infinitas solicitudes
- ✅ Se mantiene el historial completo
- ✅ Puedes analizar patrones de comportamiento

---

### 🔒 Foreign Key: ON DELETE RESTRICT

```sql
FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT
```

**¿Qué hace?**

Impide eliminar un cliente si tiene solicitudes asociadas.

**Ejemplo**:
```sql
-- Intentas eliminar cliente con solicitudes
DELETE FROM clientes WHERE id = 1;

-- ERROR: Cannot delete or update a parent row: 
-- a foreign key constraint fails
```

**¿Por qué RESTRICT?**
- ✅ Protege integridad de datos
- ✅ Evita solicitudes "huérfanas" (sin cliente)
- ✅ Obliga a tomar decisiones conscientes

**Alternativas**:

```sql
-- ON DELETE CASCADE: Elimina cliente Y todas sus solicitudes
FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE

-- ON DELETE SET NULL: Elimina cliente, deja solicitudes con cliente_id = NULL
FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL
```

**¿Cuál usar?**
- **RESTRICT**: Datos financieros (nuestro caso) - NO queremos perder solicitudes
- **CASCADE**: Datos temporales (ej: carritos de compra)
- **SET NULL**: Logs/auditoría (mantener el registro pero sin relación)

---

## 3️⃣ TABLA: users

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    email VARCHAR(100) UNIQUE,
    enabled BOOLEAN
);
```

### ¿Por qué existe?

Necesitamos usuarios que puedan **autenticarse** en el sistema. Un usuario representa a una persona que usa la aplicación (empleados del banco).

**IMPORTANTE**: User ≠ Cliente
- **User**: Empleado del banco que usa el sistema
- **Cliente**: Persona que solicita préstamos

---

## 4️⃣ TABLA: roles

```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) UNIQUE,
    description VARCHAR(255)
);
```

### ¿Por qué existe?

Necesitamos **agrupar usuarios por tipo** para asignar permisos fácilmente.

**Ejemplo sin roles** (MALO ❌):
```sql
-- Tendríamos que asignar permisos UNO POR UNO a cada usuario
INSERT INTO user_permissions (user_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), ...; -- 20 permisos para usuario 1
(2, 1), (2, 2), (2, 3), (2, 4), ...; -- Repetir para usuario 2
(3, 1), (3, 2), (3, 3), (3, 4), ...; -- Repetir para usuario 3
```

**Con roles (BUENO ✅)**:
```sql
-- Definir rol una vez
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3); -- Rol ADMIN tiene 3 permisos

-- Asignar rol a usuarios
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- Usuario 1 es ADMIN
(2, 1), -- Usuario 2 es ADMIN
(3, 1); -- Usuario 3 es ADMIN
```

**Ventajas**:
- ✅ Si cambias permisos del rol, TODOS los usuarios se actualizan
- ✅ Fácil de mantener
- ✅ Escalable

---

## 5️⃣ TABLA: permissions

```sql
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) UNIQUE,
    description VARCHAR(255)
);
```

### ¿Por qué existe?

Necesitamos **acciones específicas** que se pueden realizar en el sistema.

**Ejemplo**:
```sql
INSERT INTO permissions (name, description) VALUES
('READ_CLIENTS', 'Ver listado de clientes'),
('CREATE_CLIENTS', 'Crear nuevos clientes'),
('DELETE_CLIENTS', 'Eliminar clientes');
```

### ¿Por qué separar permisos de roles?

**Sin separación (MALO ❌)**:
```sql
CREATE TABLE roles (
    id INT PRIMARY KEY,
    name VARCHAR(50),
    can_read_clients BOOLEAN,
    can_create_clients BOOLEAN,
    can_delete_clients BOOLEAN,
    can_read_loans BOOLEAN,
    can_create_loans BOOLEAN,
    -- 50 columnas más...
);
```

**Problemas**:
- ❌ Tabla gigante e inflexible
- ❌ Agregar nuevo permiso = alterar tabla
- ❌ No se pueden combinar permisos fácilmente

**Con tabla separada (BUENO ✅)**:
```sql
CREATE TABLE permissions (
    id INT PRIMARY KEY,
    name VARCHAR(100)
);

-- Agregar nuevo permiso = INSERT (no altera estructura)
INSERT INTO permissions (name) VALUES ('EXPORT_REPORTS');
```

---

## 6️⃣ TABLA INTERMEDIA: user_roles

```sql
CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### 🔗 RELACIÓN: User ↔ Role (Many-to-Many)

```
User (N) ←────→ (N) Role
```

**¿Por qué Many-to-Many?**

**Escenario 1**: Un usuario puede tener VARIOS roles
```
Juan (user_id=1)
    ├─ Rol ADMIN (puede administrar sistema)
    └─ Rol MANAGER (puede aprobar préstamos)
```

**Escenario 2**: Un rol puede estar en VARIOS usuarios
```
Rol MANAGER (role_id=2)
    ├─ Usuario Juan
    ├─ Usuario María
    └─ Usuario Carlos
```

**Ambos escenarios son válidos simultáneamente** → Many-to-Many

---

### ¿Por qué necesitamos tabla intermedia?

**Sin tabla intermedia (IMPOSIBLE)**:
```sql
CREATE TABLE users (
    id INT PRIMARY KEY,
    role_id INT -- ❌ Solo permite UN rol
);
```

**Con tabla intermedia (CORRECTO)**:
```sql
-- Tabla intermedia permite múltiples combinaciones
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- Juan es ADMIN
(1, 2), -- Juan es MANAGER
(2, 2), -- María es MANAGER
(3, 3); -- Carlos es USER
```

---

### Clave Primaria Compuesta

```sql
PRIMARY KEY (user_id, role_id)
```

**¿Por qué ambas columnas?**

Previene duplicados:
```sql
-- PERMITIDO ✅
INSERT INTO user_roles VALUES (1, 1); -- Juan es ADMIN
INSERT INTO user_roles VALUES (1, 2); -- Juan es MANAGER

-- RECHAZADO ❌ (duplicado)
INSERT INTO user_roles VALUES (1, 1); -- Juan es ADMIN (ya existe)
-- ERROR: Duplicate entry '1-1' for key 'PRIMARY'
```

---

## 7️⃣ TABLA INTERMEDIA: role_permissions

```sql
CREATE TABLE role_permissions (
    role_id BIGINT,
    permission_id BIGINT,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);
```

### 🔗 RELACIÓN: Role ↔ Permission (Many-to-Many)

```
Role (N) ←────→ (N) Permission
```

**¿Por qué Many-to-Many?**

**Escenario 1**: Un rol tiene VARIOS permisos
```
Rol ADMIN (role_id=1)
    ├─ READ_CLIENTS
    ├─ CREATE_CLIENTS
    ├─ DELETE_CLIENTS
    └─ APPROVE_LOANS
```

**Escenario 2**: Un permiso está en VARIOS roles
```
Permiso READ_CLIENTS (permission_id=1)
    ├─ Rol ADMIN
    ├─ Rol MANAGER
    └─ Rol USER
```

**Ejemplo práctico**:
```sql
-- Rol ADMIN: Todos los permisos
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4);

-- Rol USER: Solo lectura
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 1); -- Solo READ_CLIENTS

-- Permiso READ_CLIENTS está en ADMIN y USER
-- Ambos roles tienen el permiso
```

---

## 🎯 RESUMEN DEL MODELO COMPLETO

### Cadena de Autorización

```
Usuario → tiene → Roles → tienen → Permisos
```

**Ejemplo completo**:

```
Usuario: Juan (id=1)
    ↓
    tiene roles:
        ├─ ADMIN
        │   ├─ READ_CLIENTS
        │   ├─ CREATE_CLIENTS
        │   └─ DELETE_CLIENTS
        │
        └─ MANAGER
            ├─ APPROVE_LOANS
            └─ REJECT_LOANS
    
    Permisos finales de Juan:
    ✅ READ_CLIENTS (de ADMIN)
    ✅ CREATE_CLIENTS (de ADMIN)
    ✅ DELETE_CLIENTS (de ADMIN)
    ✅ APPROVE_LOANS (de MANAGER)
    ✅ REJECT_LOANS (de MANAGER)
```

---

## 📊 DIAGRAMA COMPLETO

```
                    SEGURIDAD
┌─────────┐      ┌─────────────┐      ┌────────┐
│  User   │◄─────┤ user_roles  ├─────►│  Role  │
└─────────┘      └─────────────┘      └────────┘
                                           │
                                           │
                                      ┌────▼──────────────┐      ┌────────────┐
                                      │ role_permissions  ├─────►│ Permission │
                                      └───────────────────┘      └────────────┘

                    NEGOCIO
┌─────────┐      ┌──────────────────────┐
│ Cliente │◄─────┤ solicitudes_prestamo │
└─────────┘      └──────────────────────┘
   (1)                    (N)
```

---

## 🎓 LECCIONES DE MODELADO

### 1. Identificar Entidades

**Pregunta**: ¿Qué "cosas" necesito almacenar?
- Cliente
- Solicitud
- Usuario
- Rol
- Permiso

### 2. Identificar Relaciones

**Preguntas**:
- ¿Un A puede tener muchos B? → One-to-Many
- ¿Un B puede tener muchos A? → Many-to-One
- ¿Ambos pueden tener muchos del otro? → Many-to-Many

### 3. Normalización

**Regla**: No repetir datos

**MALO ❌**:
```sql
CREATE TABLE solicitudes (
    id INT,
    cliente_nombre VARCHAR(100), -- ❌ Repetido para cada solicitud
    cliente_email VARCHAR(100),  -- ❌ Repetido
    cliente_dni VARCHAR(20)      -- ❌ Repetido
);
```

**BUENO ✅**:
```sql
CREATE TABLE solicitudes (
    id INT,
    cliente_id INT -- ✅ Referencia al cliente
);
```

### 4. Integridad Referencial

Usa **Foreign Keys** siempre:
- Protege datos
- Documenta relaciones
- Facilita joins

---

## 💡 EJERCICIO PRÁCTICO

### Escenario: Sistema de Biblioteca

Diseña el modelo para:
- **Libros**: Múltiples copias del mismo libro
- **Autores**: Un libro puede tener varios autores
- **Préstamos**: Un usuario puede tener varios libros prestados

<details>
<summary>Ver Solución</summary>

```sql
-- Entidades principales
CREATE TABLE libros (
    id INT PRIMARY KEY,
    titulo VARCHAR(200),
    isbn VARCHAR(13)
);

CREATE TABLE autores (
    id INT PRIMARY KEY,
    nombre VARCHAR(100)
);

CREATE TABLE copias_libro (
    id INT PRIMARY KEY,
    libro_id INT,
    codigo_barras VARCHAR(50),
    estado ENUM('disponible', 'prestado', 'reparacion'),
    FOREIGN KEY (libro_id) REFERENCES libros(id)
);
-- One-to-Many: Un libro tiene muchas copias

CREATE TABLE usuarios (
    id INT PRIMARY KEY,
    nombre VARCHAR(100),
    email VARCHAR(100)
);

-- Many-to-Many: Libro ↔ Autor
CREATE TABLE libro_autor (
    libro_id INT,
    autor_id INT,
    PRIMARY KEY (libro_id, autor_id),
    FOREIGN KEY (libro_id) REFERENCES libros(id),
    FOREIGN KEY (autor_id) REFERENCES autores(id)
);

-- Many-to-Many: Usuario ↔ Copias (préstamos)
CREATE TABLE prestamos (
    id INT PRIMARY KEY,
    usuario_id INT,
    copia_id INT,
    fecha_prestamo DATE,
    fecha_devolucion DATE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (copia_id) REFERENCES copias_libro(id)
);
```
</details>

---

## 🚀 ¡Felicidades!

Ahora entiendes:
- ✅ One-to-One, One-to-Many, Many-to-Many
- ✅ Por qué usar tablas intermedias
- ✅ Cómo modelar autenticación y autorización
- ✅ Foreign Keys y su importancia
- ✅ Normalización básica

**Siguiente paso**: Practica modelando tus propios sistemas!
