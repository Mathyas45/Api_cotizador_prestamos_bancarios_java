# 📊 DIAGRAMA VISUAL - MODELO DE BASE DE DATOS

## 🎨 Diagrama Completo del Sistema

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SISTEMA DE SEGURIDAD                               │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────────────┐
                    │       USERS          │
                    │──────────────────────│
                    │ id (PK)              │
                    │ username (UNIQUE)    │
                    │ password (BCrypt)    │
                    │ email (UNIQUE)       │
                    │ enabled (BOOLEAN)    │
                    │ created_at           │
                    │ updated_at           │
                    └──────────┬───────────┘
                               │
                               │ Many-to-Many
                               │
                    ┌──────────▼───────────┐
                    │    USER_ROLES        │  ← Tabla Intermedia
                    │──────────────────────│
                    │ user_id (FK)         │
                    │ role_id (FK)         │
                    │ PK(user_id, role_id) │
                    └──────────┬───────────┘
                               │
                               │
                    ┌──────────▼───────────┐
                    │       ROLES          │
                    │──────────────────────│
                    │ id (PK)              │
                    │ name (UNIQUE)        │
                    │ description          │
                    │ created_at           │
                    └──────────┬───────────┘
                               │
                               │ Many-to-Many
                               │
                    ┌──────────▼───────────┐
                    │ ROLE_PERMISSIONS     │  ← Tabla Intermedia
                    │──────────────────────│
                    │ role_id (FK)         │
                    │ permission_id (FK)   │
                    │ PK(role_id, perm_id) │
                    └──────────┬───────────┘
                               │
                               │
                    ┌──────────▼───────────┐
                    │    PERMISSIONS       │
                    │──────────────────────│
                    │ id (PK)              │
                    │ name (UNIQUE)        │
                    │ description          │
                    │ created_at           │
                    └──────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                         SISTEMA DE NEGOCIO                                   │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────────────┐
                    │      CLIENTES        │
                    │──────────────────────│
                    │ id (PK)              │
                    │ nombre_completo      │
                    │ documento_identidad  │
                    │ email                │
                    │ telefono             │
                    │ ingreso_mensual      │
                    │ reg_estado           │
                    │ created_at           │
                    │ updated_at           │
                    └──────────┬───────────┘
                               │
                               │ One-to-Many
                               │ (Un cliente tiene muchas solicitudes)
                               │
                    ┌──────────▼───────────┐
                    │ SOLICITUDES_PRESTAMO │
                    │──────────────────────│
                    │ id (PK)              │
                    │ cliente_id (FK) ─────┘
                    │ monto                │
                    │ porcentaje_cuota_ini │
                    │ monto_cuota_inicial  │
                    │ monto_financiar      │
                    │ plazo_anios          │
                    │ tasa_interes         │
                    │ tcea                 │
                    │ cuota_mensual        │
                    │ motivo_rechazo       │
                    │ riesgo_cliente       │
                    │ estado               │
                    │ created_at           │
                    │ updated_at           │
                    └──────────────────────┘
```

---

## 🔗 EXPLICACIÓN DE RELACIONES

### 1️⃣ User ↔ Role (Many-to-Many)

```
┌─────────┐         ┌──────────────┐         ┌────────┐
│  User   │────────►│  user_roles  │◄────────│  Role  │
│         │  N:M    │              │  N:M    │        │
│ id: 1   │         │ user_id: 1   │         │ id: 1  │
│ juan    │         │ role_id: 1   │         │ ADMIN  │
└─────────┘         │              │         └────────┘
                    │ user_id: 1   │
                    │ role_id: 2   │
                    └──────────────┘

EJEMPLO:
- Juan (user_id=1) tiene rol ADMIN (role_id=1)
- Juan también tiene rol MANAGER (role_id=2)
```

### 2️⃣ Role ↔ Permission (Many-to-Many)

```
┌────────┐         ┌──────────────────┐         ┌────────────┐
│  Role  │────────►│ role_permissions │◄────────│ Permission │
│        │  N:M    │                  │  N:M    │            │
│ id: 1  │         │ role_id: 1       │         │ id: 1      │
│ ADMIN  │         │ permission_id: 1 │         │ READ_CLI.. │
└────────┘         │                  │         └────────────┘
                   │ role_id: 1       │
                   │ permission_id: 2 │
                   └──────────────────┘

EJEMPLO:
- Rol ADMIN (role_id=1) tiene permiso READ_CLIENTS (permission_id=1)
- Rol ADMIN también tiene permiso CREATE_CLIENTS (permission_id=2)
```

### 3️⃣ Cliente ↔ Solicitud (One-to-Many)

```
┌─────────────┐         ┌──────────────────────┐
│   Cliente   │────────►│ SolicitudPrestamo    │
│             │  1:N    │                      │
│ id: 1       │         │ id: 1                │
│ Juan Pérez  │         │ cliente_id: 1 ───────┘
└─────────────┘         │ monto: 50000         │
                        │ ...                  │
                        ├──────────────────────┤
                        │ id: 2                │
                        │ cliente_id: 1 ───────┘
                        │ monto: 20000         │
                        │ ...                  │
                        └──────────────────────┘

EJEMPLO:
- Juan (cliente_id=1) tiene 2 solicitudes
- Solicitud 1: $50,000
- Solicitud 2: $20,000
```

---

## 🎯 FLUJO DE AUTORIZACIÓN

```
1. Usuario inicia sesión
   │
   ▼
2. Sistema busca usuario en tabla USERS
   │
   ▼
3. Sistema carga roles desde USER_ROLES
   │
   ▼
4. Para cada rol, carga permisos desde ROLE_PERMISSIONS
   │
   ▼
5. Usuario autenticado con todos sus permisos
   │
   ▼
6. Usuario hace request a endpoint protegido
   │
   ▼
7. Spring Security verifica si tiene el permiso requerido
   │
   ├─► SÍ → Permite acceso
   │
   └─► NO → Retorna 403 Forbidden
```

---

## 📊 EJEMPLO REAL CON DATOS

### Usuario: admin

```
┌──────────────────────────────────────────────────────────┐
│ USUARIO: admin                                            │
├──────────────────────────────────────────────────────────┤
│ Email: admin@cotizador.com                               │
│ Password: ******** (BCrypt hash)                         │
│ Enabled: TRUE                                            │
└──────────────────────────────────────────────────────────┘
                        │
                        │ tiene roles
                        ▼
┌──────────────────────────────────────────────────────────┐
│ ROLES:                                                    │
│                                                           │
│  [1] ADMIN                                               │
│      ├─ READ_CLIENTS                                     │
│      ├─ CREATE_CLIENTS                                   │
│      ├─ UPDATE_CLIENTS                                   │
│      ├─ DELETE_CLIENTS                                   │
│      ├─ READ_LOANS                                       │
│      ├─ CREATE_LOANS                                     │
│      ├─ UPDATE_LOANS                                     │
│      ├─ DELETE_LOANS                                     │
│      ├─ APPROVE_LOANS                                    │
│      ├─ REJECT_LOANS                                     │
│      ├─ MANAGE_USERS                                     │
│      └─ MANAGE_ROLES                                     │
└──────────────────────────────────────────────────────────┘

RESULTADO: admin puede hacer TODO en el sistema
```

### Usuario: usuario

```
┌──────────────────────────────────────────────────────────┐
│ USUARIO: usuario                                          │
├──────────────────────────────────────────────────────────┤
│ Email: usuario@cotizador.com                             │
│ Password: ******** (BCrypt hash)                         │
│ Enabled: TRUE                                            │
└──────────────────────────────────────────────────────────┘
                        │
                        │ tiene roles
                        ▼
┌──────────────────────────────────────────────────────────┐
│ ROLES:                                                    │
│                                                           │
│  [1] USER                                                │
│      ├─ READ_CLIENTS                                     │
│      ├─ CREATE_CLIENTS                                   │
│      ├─ READ_LOANS                                       │
│      └─ CREATE_LOANS                                     │
└──────────────────────────────────────────────────────────┘

RESULTADO: usuario solo puede ver y crear (no puede eliminar ni aprobar)
```

---

## 🔐 SEGURIDAD DE CONTRASEÑAS

```
Password ingresada: "password123"
                    │
                    ▼
              BCrypt Encode
                    │
                    ▼
Hash guardado en BD: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

IMPORTANTE:
- Cada vez que encriptas la misma password, obtienes un hash DIFERENTE
- No se puede desencriptar (one-way hash)
- Para validar, BCrypt compara internamente
```

---

## 📱 EJEMPLO DE USO EN FRONTEND

### 1. Login

```javascript
// Usuario ingresa credenciales
username: "admin"
password: "password123"

// POST /api/auth/login
{
  "username": "admin",
  "password": "password123"
}

// Respuesta del backend
{
  "token": "eyJhbGc...",
  "username": "admin",
  "roles": ["ADMIN"]
}

// Guardar token
localStorage.setItem('token', token);
```

### 2. Hacer Request Autenticado

```javascript
// GET /api/clientes
Headers: {
  "Authorization": "Bearer eyJhbGc..."
}

// Backend:
1. JwtAuthenticationFilter intercepta el request
2. Extrae el token del header
3. Valida el token con JwtService
4. Si es válido, carga el usuario de la BD
5. Spring Security verifica permisos
6. Si tiene acceso, ejecuta el controlador
```

---

## 🎯 CASOS DE USO

### Caso 1: Usuario Nuevo

```
1. Frontend envía POST /api/auth/register
   {
     "username": "nuevo",
     "password": "pass123",
     "email": "nuevo@example.com"
   }

2. Backend:
   ├─ Verifica que username y email no existen
   ├─ Encripta password con BCrypt
   ├─ Busca rol USER en BD
   ├─ Crea usuario en tabla users
   ├─ Asocia usuario con rol USER en user_roles
   ├─ Genera token JWT
   └─ Retorna token al frontend

3. Frontend:
   ├─ Guarda token
   └─ Redirige a dashboard
```

### Caso 2: Crear Solicitud de Préstamo

```
1. Usuario autenticado hace POST /api/solicitudes
   Headers: { "Authorization": "Bearer token..." }
   Body: { clienteId: 1, monto: 50000, ... }

2. Backend:
   ├─ JwtAuthenticationFilter valida token
   ├─ Spring Security verifica permiso CREATE_LOANS
   ├─ Si tiene permiso:
   │  ├─ SolicitudPrestamoController recibe request
   │  ├─ Llama a TasaInteresApiClient (MockAPI)
   │  ├─ Calcula cuota mensual, TCEA, etc.
   │  ├─ Guarda solicitud en BD
   │  └─ Retorna respuesta
   └─ Si NO tiene permiso:
      └─ Retorna 403 Forbidden

3. Frontend:
   ├─ Recibe respuesta
   └─ Muestra solicitud creada
```

---

## 📚 ÍNDICES Y OPTIMIZACIÓN

```sql
-- Índices creados automáticamente por PRIMARY KEY y UNIQUE

USERS:
├─ PK: id
├─ UNIQUE: username
└─ UNIQUE: email

ROLES:
├─ PK: id
└─ UNIQUE: name

PERMISSIONS:
├─ PK: id
└─ UNIQUE: name

USER_ROLES:
└─ PK: (user_id, role_id)

ROLE_PERMISSIONS:
└─ PK: (role_id, permission_id)

CLIENTES:
├─ PK: id
├─ INDEX: documento_identidad
└─ INDEX: email

SOLICITUDES_PRESTAMO:
├─ PK: id
├─ INDEX: cliente_id
└─ INDEX: estado
```

**¿Por qué índices?**
- ✅ Búsquedas MÁS RÁPIDAS (especialmente con WHERE)
- ✅ JOINs más eficientes
- ❌ Ocupan espacio en disco
- ❌ Inserciones/actualizaciones ligeramente más lentas

---

## 🎓 CONCLUSIÓN

Este modelo implementa:

1. **Seguridad robusta**: Autenticación JWT + Autorización por roles/permisos
2. **Escalabilidad**: Fácil agregar nuevos roles y permisos
3. **Flexibilidad**: Un usuario puede tener múltiples roles
4. **Integridad**: Foreign Keys protegen datos
5. **Rendimiento**: Índices en campos clave

**¡Listo para producción empresarial!** ✅
