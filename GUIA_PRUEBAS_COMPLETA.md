# 🧪 GUÍA DE PRUEBAS - SISTEMA COMPLETO

## 📋 Checklist de Pruebas

Esta guía te ayuda a probar CADA funcionalidad del sistema paso a paso.

---

## 🚀 PREPARACIÓN

### 1. Verificar que MySQL está corriendo

```bash
# Windows
net start MySQL80

# Linux/Mac
sudo systemctl start mysql
```

### 2. Verificar que la base de datos existe

```sql
SHOW DATABASES;
-- Deberías ver: cotizador_db
```

### 3. Verificar datos iniciales

```sql
USE cotizador_db;

-- Verificar usuarios
SELECT * FROM users;
-- Deberías ver: admin, manager, usuario

-- Verificar roles
SELECT * FROM roles;
-- Deberías ver: ADMIN, MANAGER, USER

-- Verificar permisos
SELECT * FROM permissions;
-- Deberías ver: 12 permisos

-- Verificar clientes
SELECT * FROM clientes;
-- Deberías ver: 3 clientes de prueba
```

### 4. Iniciar la aplicación

```bash
cd "c:\cotizador java\apirest"
mvn spring-boot:run
```

**Buscar en consola:**
```
Started ApirestApplication in X.XXX seconds
```

---

## 🔐 PRUEBAS DE AUTENTICACIÓN

### Test 1: Registrar Usuario Nuevo

**Request:**
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "email": "testuser@example.com"
}
```

**Respuesta esperada (201 Created):**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "testuser",
    "email": "testuser@example.com",
    "roles": ["USER"]
  }
}
```

**✅ VERIFICAR:**
- Status code: 201
- Campo `token` existe y no está vacío
- Campo `roles` contiene ["USER"]

**🔍 Verificar en BD:**
```sql
SELECT * FROM users WHERE username = 'testuser';
-- Debería existir el usuario

SELECT r.name FROM user_roles ur
JOIN roles r ON ur.role_id = r.id
WHERE ur.user_id = (SELECT id FROM users WHERE username = 'testuser');
-- Debería retornar: USER
```

---

### Test 2: Registrar Usuario con Username Duplicado

**Request:**
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "otrapassword",
  "email": "otro@example.com"
}
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "success": false,
  "message": "El username ya está en uso"
}
```

**✅ VERIFICAR:**
- Status code: 400
- Mensaje de error apropiado

---

### Test 3: Login con Usuario Válido

**Request:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "admin",
    "email": "admin@cotizador.com",
    "roles": ["ADMIN"]
  }
}
```

**✅ VERIFICAR:**
- Status code: 200
- Token es diferente cada vez que haces login
- Roles contiene ["ADMIN"]

**💾 GUARDAR EL TOKEN** para los siguientes tests

---

### Test 4: Login con Credenciales Incorrectas

**Request:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "passwordincorrecta"
}
```

**Respuesta esperada (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Credenciales incorrectas"
}
```

**✅ VERIFICAR:**
- Status code: 401
- NO retorna token

---

## 👥 PRUEBAS DE CLIENTES (Requieren Autenticación)

### Test 5: Listar Clientes (con token válido)

**Request:**
```http
GET http://localhost:8080/api/clientes
Authorization: Bearer {TOKEN_DEL_LOGIN}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "message": "Clientes encontrados",
  "data": [
    {
      "id": 1,
      "nombreCompleto": "Juan Pérez García",
      "documentoIdentidad": "12345678",
      "email": "juan.perez@example.com",
      "telefono": "987654321",
      "ingresoMensual": 3000.00
    },
    {
      "id": 2,
      "nombreCompleto": "María González López",
      ...
    }
  ]
}
```

**✅ VERIFICAR:**
- Status code: 200
- Retorna array de clientes
- Cada cliente tiene todos los campos

---

### Test 6: Listar Clientes (sin token)

**Request:**
```http
GET http://localhost:8080/api/clientes
```

**Respuesta esperada (403 Forbidden):**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/clientes"
}
```

**✅ VERIFICAR:**
- Status code: 403
- No retorna datos

---

### Test 7: Crear Cliente

**Request:**
```http
POST http://localhost:8080/api/clientes
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "nombreCompleto": "Pedro Ramírez",
  "documentoIdentidad": "99887766",
  "email": "pedro.ramirez@example.com",
  "telefono": "987654999",
  "ingresoMensual": 4500.00
}
```

**Respuesta esperada (201 Created):**
```json
{
  "success": true,
  "message": "Cliente creado exitosamente",
  "data": null
}
```

**✅ VERIFICAR:**
- Status code: 201

**🔍 Verificar en BD:**
```sql
SELECT * FROM clientes WHERE documento_identidad = '99887766';
-- Debería existir el cliente
```

---

### Test 8: Buscar Cliente por ID

**Request:**
```http
GET http://localhost:8080/api/clientes/1
Authorization: Bearer {TOKEN}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "message": "Cliente encontrado",
  "data": {
    "id": 1,
    "nombreCompleto": "Juan Pérez García",
    "documentoIdentidad": "12345678",
    ...
  }
}
```

**✅ VERIFICAR:**
- Status code: 200
- Retorna el cliente correcto

---

### Test 9: Buscar Cliente que No Existe

**Request:**
```http
GET http://localhost:8080/api/clientes/999
Authorization: Bearer {TOKEN}
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "success": false,
  "message": "Cliente no encontrado con ID: 999"
}
```

**✅ VERIFICAR:**
- Status code: 400
- Mensaje de error apropiado

---

### Test 10: Actualizar Cliente

**Request:**
```http
PUT http://localhost:8080/api/clientes/1
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "nombreCompleto": "Juan Pérez García Actualizado",
  "documentoIdentidad": "12345678",
  "email": "juan.perez.nuevo@example.com",
  "telefono": "987654321",
  "ingresoMensual": 3500.00
}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "message": "Cliente actualizado exitosamente"
}
```

**✅ VERIFICAR:**
- Status code: 200

**🔍 Verificar en BD:**
```sql
SELECT * FROM clientes WHERE id = 1;
-- Debería tener los datos actualizados
```

---

### Test 11: Eliminar Cliente (sin solicitudes)

**Crear cliente temporal:**
```http
POST http://localhost:8080/api/clientes
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "nombreCompleto": "Temporal",
  "documentoIdentidad": "00000001",
  "email": "temp@example.com",
  "ingresoMensual": 1000.00
}
```

**Eliminar:**
```http
DELETE http://localhost:8080/api/clientes/{ID_DEL_CLIENTE_TEMPORAL}
Authorization: Bearer {TOKEN}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "message": "Cliente eliminado exitosamente"
}
```

**✅ VERIFICAR:**
- Status code: 200

**🔍 Verificar en BD:**
```sql
SELECT * FROM clientes WHERE documento_identidad = '00000001';
-- NO debería existir
```

---

## 💰 PRUEBAS DE SOLICITUDES DE PRÉSTAMO

### Test 12: Crear Solicitud de Préstamo

**Request:**
```http
POST http://localhost:8080/api/solicitudes
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "clienteId": 1,
  "monto": 50000.00,
  "porcentajeCuotaInicial": 20.0,
  "plazoAnios": 10
}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "message": "Solicitud creada exitosamente",
  "data": {
    "id": 1,
    "monto": 50000.00,
    "porcentajeCuotaInicial": 20.0,
    "montoCuotaInicial": 10000.00,
    "montoFinanciar": 40000.00,
    "plazoAnios": 10,
    "tasaInteres": 7.5,  // Depende del riesgo del cliente
    "tcea": 7.76,
    "cuotaMensual": 475.39,
    "estado": 1  // Aprobado
  }
}
```

**✅ VERIFICAR:**
- Status code: 200
- Cálculos financieros son correctos
- Estado es 1 (Aprobado)

**🔍 Verificar en BD:**
```sql
SELECT * FROM solicitudes_prestamo WHERE cliente_id = 1;
-- Debería existir la solicitud con los cálculos
```

---

### Test 13: Simular Préstamo (sin guardar)

**Request:**
```http
POST http://localhost:8080/api/solicitudes/simulador
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "clienteId": 1,
  "monto": 30000.00,
  "porcentajeCuotaInicial": 15.0,
  "plazoAnios": 5
}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "message": "Simulación exitosa",
  "data": {
    "monto": 30000.00,
    "porcentajeCuotaInicial": 15.0,
    "montoCuotaInicial": 4500.00,
    "montoFinanciar": 25500.00,
    "plazoAnios": 5,
    "tasaInteres": 7.5,
    "tcea": 7.76,
    "cuotaMensual": 507.29
  }
}
```

**✅ VERIFICAR:**
- Status code: 200
- Retorna cálculos pero NO crea registro en BD

**🔍 Verificar en BD:**
```sql
SELECT COUNT(*) FROM solicitudes_prestamo;
-- El conteo NO debe aumentar (simulación no guarda)
```

---

### Test 14: Listar Solicitudes

**Request:**
```http
GET http://localhost:8080/api/solicitudes
Authorization: Bearer {TOKEN}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "message": "Solicitudes encontradas",
  "data": [
    {
      "id": 1,
      "monto": 50000.00,
      "cliente": {
        "id": 1,
        "nombreCompleto": "Juan Pérez García"
      },
      ...
    }
  ]
}
```

**✅ VERIFICAR:**
- Status code: 200
- Retorna array de solicitudes

---

### Test 15: Actualizar Solicitud

**Request:**
```http
PUT http://localhost:8080/api/solicitudes/1
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "monto": 55000.00,
  "porcentajeCuotaInicial": 25.0,
  "plazoAnios": 12
}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "message": "Solicitud actualizada exitosamente",
  "data": {
    "id": 1,
    "monto": 55000.00,
    "porcentajeCuotaInicial": 25.0,
    "montoCuotaInicial": 13750.00,
    "montoFinanciar": 41250.00,
    "plazoAnios": 12,
    "tasaInteres": 7.5,
    "tcea": 7.76,
    "cuotaMensual": 370.89
  }
}
```

**✅ VERIFICAR:**
- Status code: 200
- Cálculos recalculados correctamente

---

## 🧪 PRUEBAS UNITARIAS

### Ejecutar Todas las Pruebas

```bash
mvn test
```

**Salida esperada:**
```
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**✅ VERIFICAR:**
- Todos los tests pasan (0 Failures, 0 Errors)

---

### Ejecutar Pruebas de ClienteService

```bash
mvn test -Dtest=ClienteServiceTest
```

**Tests ejecutados:**
- testCreateCliente_Success
- testFindById_ClienteFound
- testFindById_ClienteNotFound
- testFindAll_AllClientes
- testFindAll_WithQuery
- testUpdateCliente_Success
- testDeleteCliente_Success
- testDeleteCliente_NotFound

**Resultado esperado:**
```
Tests run: 10, Failures: 0, Errors: 0
```

---

### Ejecutar Pruebas de AuthService

```bash
mvn test -Dtest=AuthServiceTest
```

**Tests ejecutados:**
- testRegister_Success
- testRegister_UsernameExists
- testRegister_EmailExists
- testLogin_Success
- testLogin_BadCredentials
- testLogin_UserNotFound

**Resultado esperado:**
```
Tests run: 6, Failures: 0, Errors: 0
```

---

## 🎯 CHECKLIST FINAL

### Autenticación ✅
- [ ] Registrar usuario nuevo
- [ ] Login con credenciales válidas
- [ ] Login con credenciales incorrectas
- [ ] Token se genera correctamente
- [ ] Usuarios duplicados son rechazados

### Clientes ✅
- [ ] Listar clientes con token
- [ ] Listar clientes sin token (debe fallar)
- [ ] Crear cliente nuevo
- [ ] Buscar cliente por ID
- [ ] Actualizar cliente
- [ ] Eliminar cliente

### Solicitudes ✅
- [ ] Crear solicitud con cálculos correctos
- [ ] Simular préstamo
- [ ] Listar solicitudes
- [ ] Actualizar solicitud
- [ ] Eliminar solicitud

### Pruebas Unitarias ✅
- [ ] ClienteServiceTest (10 tests pasan)
- [ ] AuthServiceTest (6 tests pasan)

### Base de Datos ✅
- [ ] Tablas creadas correctamente
- [ ] Datos iniciales insertados
- [ ] Foreign Keys funcionando
- [ ] Índices creados

---

## 📊 HERRAMIENTAS RECOMENDADAS

### Postman

**Colección de Postman:**

Crear una nueva colección con estas carpetas:

```
📁 Cotizador API
├── 📁 Auth
│   ├── Register User
│   ├── Login Admin
│   ├── Login Manager
│   └── Login User
├── 📁 Clientes
│   ├── List Clientes
│   ├── Get Cliente by ID
│   ├── Create Cliente
│   ├── Update Cliente
│   └── Delete Cliente
└── 📁 Solicitudes
    ├── List Solicitudes
    ├── Get Solicitud by ID
    ├── Create Solicitud
    ├── Simulate Solicitud
    ├── Update Solicitud
    └── Delete Solicitud
```

**Variables de entorno:**
```json
{
  "baseUrl": "http://localhost:8080",
  "token": ""
}
```

**Script de login para guardar token automáticamente:**
```javascript
// En "Tests" tab del request de login
pm.test("Login successful", function () {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.data.token);
});
```

---

### Insomnia

Similar a Postman, crea una workspace con todas las requests.

---

### MySQL Workbench

Para verificar datos en la base de datos visualmente.

**Queries útiles:**
```sql
-- Ver todos los usuarios con sus roles
SELECT u.username, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;

-- Ver permisos de un rol
SELECT r.name as role, p.name as permission
FROM roles r
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.name = 'ADMIN';

-- Ver solicitudes con cliente
SELECT 
    s.id,
    c.nombre_completo,
    s.monto,
    s.estado,
    s.created_at
FROM solicitudes_prestamo s
JOIN clientes c ON s.cliente_id = c.id;
```

---

## 🎉 ¡Felicidades!

Si todas las pruebas pasan, tu sistema está funcionando correctamente y listo para:

- ✅ Consumirse desde Angular
- ✅ Consumirse desde Kotlin/Android
- ✅ Desplegarse en producción
- ✅ Presentarse en tu maestría
- ✅ Incluirse en tu portafolio profesional

---

**¿Preguntas? Revisa:**
- `README_COMPLETO.md` - Guía general
- `GUIA_PRUEBAS_UNITARIAS.md` - Detalles de tests
- `EXPLICACION_MODELADO_BD.md` - Modelo de datos
