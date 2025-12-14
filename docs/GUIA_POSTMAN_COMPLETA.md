# 📮 GUÍA COMPLETA DE POSTMAN - API Cotizador

## 🚀 Configuración Inicial

### 1. Crear Colección
1. Abre Postman
2. Click en **Collections** → **New Collection**
3. Nombre: `Cotizador API`
4. Guarda

### 2. Crear Ambiente (Environment)
1. Click en **Environments** → **Create Environment**
2. Nombre: `Cotizador Local`
3. Agrega estas variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `base_url` | `http://localhost:8080` | `http://localhost:8080` |
| `token` | (vacío) | (vacío) |

4. Guarda y selecciona este ambiente (esquina superior derecha)

---

## 🔐 AUTENTICACIÓN

### 1. REGISTRO DE USUARIO
```
POST {{base_url}}/api/auth/register
```

**Headers:**
| Key | Value |
|-----|-------|
| Content-Type | application/json |

**Body (raw - JSON):**
```json
{
    "username": "juan123",
    "email": "juan@email.com",
    "password": "123456"
}
```

**Respuesta Exitosa (201 Created):**
```json
{
    "success": true,
    "message": "Usuario registrado exitosamente",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFuQGVtYWlsLmNvbSIsImlhdCI6MTczMjc1...",
        "type": "Bearer",
        "username": "juan123",
        "email": "juan@email.com",
        "roles": ["USER"]
    }
}
```

**Errores posibles:**
| Código | Mensaje | Causa |
|--------|---------|-------|
| 400 | "El username ya está en uso" | Username duplicado |
| 400 | "El email ya está registrado" | Email duplicado |
| 400 | "El email es obligatorio" | Campo vacío |

**⚡ Script para guardar token automáticamente:**
En la pestaña **Tests** agrega:
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.data.token);
    console.log("Token guardado!");
}
```

---

### 2. LOGIN (INICIAR SESIÓN)
```
POST {{base_url}}/api/auth/login
```

**Headers:**
| Key | Value |
|-----|-------|
| Content-Type | application/json |

**Body (raw - JSON):**
```json
{
    "email": "juan@email.com",
    "password": "123456"
}
```

**Respuesta Exitosa (200 OK):**
```json
{
    "success": true,
    "message": "Login exitoso",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFuQGVtYWlsLmNvbSIsImlhdCI6MTczMjc1...",
        "type": "Bearer",
        "username": "juan123",
        "email": "juan@email.com",
        "roles": ["USER"]
    }
}
```

**Errores posibles:**
| Código | Mensaje | Causa |
|--------|---------|-------|
| 401 | "Credenciales incorrectas" | Email o password mal |
| 400 | "El email es obligatorio" | Campo vacío |

**⚡ Script para guardar token automáticamente:**
En la pestaña **Tests** agrega:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.data.token);
    console.log("Token guardado: " + jsonData.data.token.substring(0, 20) + "...");
}
```

---

## 👥 CLIENTES (CRUD)

> ⚠️ **IMPORTANTE:** Estas rutas requieren autenticación (cuando actives el modo PRODUCCIÓN)

### Configurar Authorization para toda la carpeta

1. Crea una carpeta llamada `Clientes` dentro de tu colección
2. Click derecho → **Edit**
3. Pestaña **Authorization**
4. Type: **Bearer Token**
5. Token: `{{token}}`
6. Todas las peticiones dentro heredarán este token

---

### 3. CREAR CLIENTE
```
POST {{base_url}}/api/clientes
```

**Headers:**
| Key | Value |
|-----|-------|
| Content-Type | application/json |
| Authorization | Bearer {{token}} |

**Body (raw - JSON):**
```json
{
    "nombre": "Pedro García",
    "correo": "pedro@email.com",
    "telefono": "555-1234",
    "direccion": "Calle 123 #45-67"
}
```

**Respuesta Exitosa (200 OK):**
```json
{
    "success": true,
    "message": "Cliente creado exitosamente",
    "data": null
}
```

---

### 4. OBTENER TODOS LOS CLIENTES
```
GET {{base_url}}/api/clientes
```

**Headers:**
| Key | Value |
|-----|-------|
| Authorization | Bearer {{token}} |

**Query Params (opcional):**
| Key | Value | Descripción |
|-----|-------|-------------|
| query | Pedro | Buscar por nombre |

**Respuesta Exitosa (200 OK):**
```json
{
    "success": true,
    "message": "Clientes obtenidos exitosamente",
    "data": [
        {
            "id": 1,
            "nombre": "Pedro García",
            "correo": "pedro@email.com",
            "telefono": "555-1234",
            "direccion": "Calle 123 #45-67"
        },
        {
            "id": 2,
            "nombre": "María López",
            "correo": "maria@email.com",
            "telefono": "555-5678",
            "direccion": "Avenida 789"
        }
    ]
}
```

---

### 5. OBTENER CLIENTE POR ID
```
GET {{base_url}}/api/clientes/1
```

**Headers:**
| Key | Value |
|-----|-------|
| Authorization | Bearer {{token}} |

**Respuesta Exitosa (200 OK):**
```json
{
    "success": true,
    "message": "Cliente encontrado",
    "data": {
        "id": 1,
        "nombre": "Pedro García",
        "correo": "pedro@email.com",
        "telefono": "555-1234",
        "direccion": "Calle 123 #45-67"
    }
}
```

**Error (404):**
```json
{
    "success": false,
    "message": "Cliente no encontrado con id: 99",
    "data": null
}
```

---

### 6. ACTUALIZAR CLIENTE
```
PUT {{base_url}}/api/clientes/1
```

**Headers:**
| Key | Value |
|-----|-------|
| Content-Type | application/json |
| Authorization | Bearer {{token}} |

**Body (raw - JSON):**
```json
{
    "nombre": "Pedro García Actualizado",
    "correo": "pedro.nuevo@email.com",
    "telefono": "555-9999",
    "direccion": "Nueva Dirección 456"
}
```

**Respuesta Exitosa (200 OK):**
```json
{
    "success": true,
    "message": "Cliente actualizado exitosamente",
    "data": null
}
```

---

### 7. ELIMINAR CLIENTE
```
DELETE {{base_url}}/api/clientes/1
```

**Headers:**
| Key | Value |
|-----|-------|
| Authorization | Bearer {{token}} |

**Respuesta Exitosa (200 OK):**
```json
{
    "success": true,
    "message": "Cliente eliminado exitosamente",
    "data": null
}
```

---

## 💰 SOLICITUDES DE PRÉSTAMO

### 8. SIMULAR PRÉSTAMO
```
POST {{base_url}}/api/solicitudesPrestamo/simulador
```

**Headers:**
| Key | Value |
|-----|-------|
| Content-Type | application/json |
| Authorization | Bearer {{token}} |

**Body (raw - JSON):**
```json
{
    "clienteId": 1,
    "monto": 50000000,
    "plazoMeses": 60,
    "tipoCredito": "HIPOTECARIO"
}
```

**Respuesta Exitosa (200 OK):**
```json
{
    "id": null,
    "clienteId": 1,
    "monto": 50000000,
    "plazoMeses": 60,
    "tipoCredito": "HIPOTECARIO",
    "tasaInteres": 12.5,
    "cuotaMensual": 1124589.45,
    "totalPagar": 67475367.0,
    "estado": "SIMULADO"
}
```

---

### 9. CREAR SOLICITUD DE PRÉSTAMO
```
POST {{base_url}}/api/solicitudesPrestamo/register
```

**Headers:**
| Key | Value |
|-----|-------|
| Content-Type | application/json |
| Authorization | Bearer {{token}} |

**Body (raw - JSON):**
```json
{
    "clienteId": 1,
    "monto": 50000000,
    "plazoMeses": 60,
    "tipoCredito": "HIPOTECARIO"
}
```

**Respuesta Exitosa (201 Created):**
```json
{
    "id": 1,
    "clienteId": 1,
    "monto": 50000000,
    "plazoMeses": 60,
    "tipoCredito": "HIPOTECARIO",
    "tasaInteres": 12.5,
    "cuotaMensual": 1124589.45,
    "totalPagar": 67475367.0,
    "estado": "PENDIENTE"
}
```

---

### 10. OBTENER TODAS LAS SOLICITUDES
```
GET {{base_url}}/api/solicitudesPrestamo
```

**Headers:**
| Key | Value |
|-----|-------|
| Authorization | Bearer {{token}} |

---

### 11. OBTENER SOLICITUD POR ID
```
GET {{base_url}}/api/solicitudesPrestamo/1
```

**Headers:**
| Key | Value |
|-----|-------|
| Authorization | Bearer {{token}} |

---

### 12. ACTUALIZAR SOLICITUD
```
PUT {{base_url}}/api/solicitudesPrestamo/update/1
```

**Headers:**
| Key | Value |
|-----|-------|
| Content-Type | application/json |
| Authorization | Bearer {{token}} |

**Body (raw - JSON):**
```json
{
    "estado": "APROBADO"
}
```

---

### 13. ELIMINAR SOLICITUD
```
DELETE {{base_url}}/api/solicitudesPrestamo/delete/1
```

**Headers:**
| Key | Value |
|-----|-------|
| Authorization | Bearer {{token}} |

---

## 🧪 ORDEN DE PRUEBAS RECOMENDADO

### Flujo completo de pruebas:

```
1️⃣  REGISTRO
    POST /api/auth/register
    ↓ (guarda token automáticamente)
    
2️⃣  LOGIN (probar que funciona)
    POST /api/auth/login
    ↓
    
3️⃣  CREAR CLIENTE
    POST /api/clientes
    ↓
    
4️⃣  LISTAR CLIENTES
    GET /api/clientes
    ↓
    
5️⃣  VER CLIENTE ESPECÍFICO
    GET /api/clientes/1
    ↓
    
6️⃣  SIMULAR PRÉSTAMO
    POST /api/solicitudesPrestamo/simulador
    ↓
    
7️⃣  CREAR SOLICITUD
    POST /api/solicitudesPrestamo/register
    ↓
    
8️⃣  VER SOLICITUDES
    GET /api/solicitudesPrestamo
    ↓
    
9️⃣  ACTUALIZAR SOLICITUD
    PUT /api/solicitudesPrestamo/update/1
    ↓
    
🔟  ACTUALIZAR CLIENTE
    PUT /api/clientes/1
    ↓
    
1️⃣1️⃣ ELIMINAR SOLICITUD
    DELETE /api/solicitudesPrestamo/delete/1
    ↓
    
1️⃣2️⃣ ELIMINAR CLIENTE
    DELETE /api/clientes/1
```

---

## 🔧 SOLUCIÓN DE PROBLEMAS

### Error 401 Unauthorized
```json
{
    "timestamp": "2024-...",
    "status": 401,
    "error": "Unauthorized"
}
```
**Solución:**
1. Verifica que el token esté en el header `Authorization: Bearer <token>`
2. El token puede haber expirado (24 horas) → Haz login de nuevo
3. El modo PRODUCCIÓN está activado pero no enviaste token

### Error 403 Forbidden
```json
{
    "timestamp": "2024-...",
    "status": 403,
    "error": "Forbidden"
}
```
**Solución:**
1. El usuario no tiene permisos para esta acción
2. Verifica los roles del usuario

### Error 400 Bad Request
```json
{
    "success": false,
    "message": "El email es obligatorio"
}
```
**Solución:**
1. Revisa el body del request
2. Campos requeridos faltantes
3. Formato de datos incorrecto

### Error 500 Internal Server Error
**Solución:**
1. Revisa la consola del backend (logs)
2. Puede ser error de base de datos
3. Error en la lógica del servidor

---

## 📱 LO QUE NECESITAS EN TU FRONTEND

### 1. Servicio de Autenticación
```typescript
// Angular - auth.service.ts
interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

interface AuthResponse {
  token: string;
  type: string;
  username: string;
  email: string;
  roles: string[];
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
```

### 2. Interceptor HTTP
```typescript
// Agregar token a todas las peticiones
intercept(req: HttpRequest<any>, next: HttpHandler) {
  const token = localStorage.getItem('token');
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  return next.handle(req);
}
```

### 3. Guard de Rutas
```typescript
// Proteger rutas que requieren login
canActivate(): boolean {
  if (localStorage.getItem('token')) {
    return true;
  }
  this.router.navigate(['/login']);
  return false;
}
```

### 4. Manejo de Errores
```typescript
// Manejar 401 (token expirado)
intercept(req, next) {
  return next.handle(req).pipe(
    catchError(error => {
      if (error.status === 401) {
        localStorage.removeItem('token');
        this.router.navigate(['/login']);
      }
      return throwError(error);
    })
  );
}
```

---

## 📊 RESUMEN DE ENDPOINTS

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Registrar usuario | ❌ |
| POST | `/api/auth/login` | Iniciar sesión | ❌ |
| GET | `/api/clientes` | Listar clientes | ✅ |
| GET | `/api/clientes/{id}` | Ver cliente | ✅ |
| POST | `/api/clientes` | Crear cliente | ✅ |
| PUT | `/api/clientes/{id}` | Actualizar cliente | ✅ |
| DELETE | `/api/clientes/{id}` | Eliminar cliente | ✅ |
| POST | `/api/solicitudesPrestamo/simulador` | Simular préstamo | ✅ |
| POST | `/api/solicitudesPrestamo/register` | Crear solicitud | ✅ |
| GET | `/api/solicitudesPrestamo` | Listar solicitudes | ✅ |
| GET | `/api/solicitudesPrestamo/{id}` | Ver solicitud | ✅ |
| PUT | `/api/solicitudesPrestamo/update/{id}` | Actualizar solicitud | ✅ |
| DELETE | `/api/solicitudesPrestamo/delete/{id}` | Eliminar solicitud | ✅ |

---

## 🎯 PRÓXIMOS PASOS

1. **Probar en modo DESARROLLO** (actual - sin token)
2. **Activar modo PRODUCCIÓN** en `SecurityConfig.java`
3. **Probar con token JWT**
4. **Implementar frontend** con los endpoints documentados

¡Éxito con tus pruebas! 🚀
