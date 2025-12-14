# 🏦 Sistema de Cotización de Préstamos Bancarios con Spring Security

## 📋 Descripción

Sistema completo de gestión de préstamos bancarios con autenticación JWT, autorización por roles y permisos, y pruebas unitarias con JUnit y Mockito.

## 🔐 Características de Seguridad

- ✅ **Autenticación con JWT**: Tokens seguros con firma digital
- ✅ **Roles**: ADMIN, MANAGER, USER
- ✅ **Permisos granulares**: Control fino sobre cada acción
- ✅ **BCrypt**: Encriptación de contraseñas
- ✅ **CORS configurado**: Listo para Angular/React/Kotlin

## 🧪 Pruebas Unitarias

- ✅ **JUnit 5**: Framework de pruebas moderno
- ✅ **Mockito**: Simulación de dependencias
- ✅ **Cobertura**: ClienteService, AuthService
- ✅ **Buenas prácticas**: AAA (Arrange-Act-Assert)

## 🗄️ Modelo de Base de Datos

### Tablas de Seguridad

#### **users**
- Usuarios del sistema
- Implementa `UserDetails` de Spring Security
- Contraseñas encriptadas con BCrypt

#### **roles**
- Define tipos de usuario (ADMIN, MANAGER, USER)
- Un rol agrupa varios permisos

#### **permissions**
- Permisos específicos (READ_CLIENTS, CREATE_LOAN, etc.)
- Control granular sobre acciones

#### **user_roles** (Many-to-Many)
- Relaciona usuarios con roles
- Un usuario puede tener varios roles
- Ejemplo: Un usuario puede ser ADMIN y MANAGER

#### **role_permissions** (Many-to-Many)
- Relaciona roles con permisos
- Un rol puede tener varios permisos
- Si cambias permisos del rol, todos los usuarios con ese rol se actualizan

### Tablas de Negocio

#### **clientes**
- Clientes que solicitan préstamos
- Relación One-to-Many con solicitudes_prestamo

#### **solicitudes_prestamo**
- Solicitudes de préstamo
- Relación Many-to-One con clientes
- Cálculos financieros: tasa, cuota mensual, TCEA

### Diagrama de Relaciones

```
SEGURIDAD:
User ←→ user_roles ←→ Role ←→ role_permissions ←→ Permission

NEGOCIO:
Cliente ←→ SolicitudPrestamo (One-to-Many)
```

## 🚀 Instalación y Configuración

### 1. Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA / Eclipse / VS Code)

### 2. Configurar Base de Datos

```sql
-- Ejecutar en MySQL
CREATE DATABASE cotizador_db;
```

### 3. Ejecutar Script SQL

```bash
# Ejecutar el script con datos iniciales
mysql -u root -p cotizador_db < src/main/resources/db_schema_with_security.sql
```

Este script crea:
- Todas las tablas
- Permisos del sistema
- Roles (ADMIN, MANAGER, USER)
- Usuarios de prueba
- Clientes de ejemplo

### 4. Configurar application.yml

Edita `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cotizador_db
    username: root
    password: TU_PASSWORD_AQUI
```

### 5. Compilar el Proyecto

```bash
mvn clean install
```

### 6. Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

La aplicación inicia en: `http://localhost:8080`

## 🧪 Ejecutar Pruebas Unitarias

### Ejecutar todas las pruebas

```bash
mvn test
```

### Ejecutar una prueba específica

```bash
mvn test -Dtest=ClienteServiceTest
mvn test -Dtest=AuthServiceTest
```

### Ver reporte de cobertura

```bash
mvn test
# Los resultados se muestran en consola
```

### ¿Qué prueban los tests?

**ClienteServiceTest.java**
- ✅ Crear cliente
- ✅ Buscar cliente por ID (encontrado/no encontrado)
- ✅ Listar todos los clientes
- ✅ Buscar clientes con query
- ✅ Actualizar cliente
- ✅ Eliminar cliente

**AuthServiceTest.java**
- ✅ Registrar usuario exitosamente
- ✅ Validar username duplicado
- ✅ Validar email duplicado
- ✅ Login exitoso
- ✅ Login con credenciales incorrectas
- ✅ Manejo de errores

## 🔑 API Endpoints

### Autenticación (Públicos)

#### Registrar Usuario
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "juan123",
  "password": "mipassword123",
  "email": "juan@example.com"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "juan123",
    "email": "juan@example.com",
    "roles": ["USER"]
  }
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

**Respuesta:**
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

### Clientes (Requieren Autenticación)

#### Crear Cliente
```http
POST /api/clientes
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombreCompleto": "Juan Pérez",
  "documentoIdentidad": "12345678",
  "email": "juan@example.com",
  "telefono": "987654321",
  "ingresoMensual": 3000.00
}
```

#### Listar Clientes
```http
GET /api/clientes
Authorization: Bearer {token}
```

#### Buscar Cliente por ID
```http
GET /api/clientes/1
Authorization: Bearer {token}
```

### Solicitudes de Préstamo (Requieren Autenticación)

#### Crear Solicitud
```http
POST /api/solicitudes
Authorization: Bearer {token}
Content-Type: application/json

{
  "clienteId": 1,
  "monto": 50000.00,
  "porcentajeCuotaInicial": 20.0,
  "plazoAnios": 10
}
```

#### Listar Solicitudes
```http
GET /api/solicitudes
Authorization: Bearer {token}
```

## 👥 Usuarios de Prueba

Después de ejecutar el script SQL, tendrás estos usuarios:

| Username | Password | Rol | Permisos |
|----------|----------|-----|----------|
| admin | password123 | ADMIN | Todos los permisos |
| manager | password123 | MANAGER | Gestión de clientes y préstamos |
| usuario | password123 | USER | Ver y crear solicitudes |

## 🔐 ¿Cómo funciona JWT?

### 1. Login
```
Cliente → POST /api/auth/login
Backend → Valida credenciales
Backend → Genera JWT token
Backend → Retorna token
```

### 2. Usar Token en Requests
```
Cliente → GET /api/clientes
Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Backend → Valida token
Backend → Verifica permisos
Backend → Retorna datos
```

### 3. Estructura JWT

```
header.payload.signature
```

**Header**: Tipo y algoritmo
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload**: Datos del usuario
```json
{
  "sub": "admin",
  "iat": 1700000000,
  "exp": 1700086400
}
```

**Signature**: Firma digital (verifica autenticidad)

## 🧪 Conceptos de Pruebas Unitarias

### ¿Qué es JUnit?

Framework para escribir y ejecutar pruebas automáticas en Java.

```java
@Test
void testCrearCliente() {
    // Prueba que crear cliente funcione correctamente
}
```

### ¿Qué es Mockito?

Framework para crear "mocks" (objetos simulados) de dependencias.

```java
@Mock
private ClienteRepository clienteRepository; // Repositorio falso

when(clienteRepository.findById(1L))
    .thenReturn(Optional.of(cliente)); // Simula comportamiento
```

### ¿Por qué usar Mocks?

- ✅ **Rapidez**: No usa base de datos real
- ✅ **Aislamiento**: Prueba solo una unidad de código
- ✅ **Control**: Simulas cualquier escenario (errores, casos edge)

### Patrón AAA (Arrange-Act-Assert)

```java
@Test
void testBuscarCliente() {
    // ARRANGE: Preparar datos de prueba
    when(repository.findById(1L)).thenReturn(Optional.of(cliente));
    
    // ACT: Ejecutar el método a probar
    ClienteResponse result = service.findById(1L);
    
    // ASSERT: Verificar el resultado
    assertEquals("Juan Pérez", result.getNombreCompleto());
}
```

## 📱 Consumir desde Frontend

### Angular

```typescript
// auth.service.ts
login(username: string, password: string) {
  return this.http.post('http://localhost:8080/api/auth/login', {
    username,
    password
  });
}

// Guardar token
localStorage.setItem('token', response.data.token);

// Usar token en requests
const headers = new HttpHeaders({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
});

this.http.get('http://localhost:8080/api/clientes', { headers });
```

### Kotlin (Android)

```kotlin
// Retrofit interface
interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @GET("api/clientes")
    suspend fun getClientes(
        @Header("Authorization") token: String
    ): List<Cliente>
}

// Uso
val response = apiService.login(LoginRequest("admin", "password123"))
val token = "Bearer ${response.data.token}"

val clientes = apiService.getClientes(token)
```

## 🔒 Seguridad en Producción

### Variables de Entorno

**NO guardes el JWT secret en código**. Usa variables de entorno:

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}
```

```bash
# Linux/Mac
export JWT_SECRET=tu-clave-secreta-super-segura-de-256-bits

# Windows PowerShell
$env:JWT_SECRET="tu-clave-secreta-super-segura-de-256-bits"
```

### Generar Clave Secreta

```bash
# Generar clave aleatoria de 256 bits en Base64
openssl rand -base64 32
```

## 📚 Recursos de Aprendizaje

### Spring Security
- [Documentación Oficial](https://spring.io/projects/spring-security)
- [Baeldung - Spring Security](https://www.baeldung.com/security-spring)

### JWT
- [jwt.io](https://jwt.io/) - Decodificador de tokens
- [RFC 7519](https://tools.ietf.org/html/rfc7519) - Especificación JWT

### JUnit y Mockito
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

## 🎯 Próximos Pasos

1. **Ejecutar las pruebas** para entender cómo funcionan
2. **Probar los endpoints** con Postman o Insomnia
3. **Crear tu propio test** para SolicitudPrestamoService
4. **Agregar nuevos permisos** según tus necesidades
5. **Implementar frontend** con Angular o Kotlin

## 💡 Tips

- Las contraseñas se encriptan con BCrypt (irreversible)
- Los tokens JWT expiran en 24 horas (configurable)
- Los permisos se verifican automáticamente por Spring Security
- Usa `@PreAuthorize("hasRole('ADMIN')")` en controladores para proteger endpoints específicos

## ❓ Solución de Problemas

### Error: "Usuario no encontrado con username: USER"
El rol USER no existe en la BD. Ejecuta el script SQL de datos iniciales.

### Error: "Access Denied"
El usuario no tiene permisos para esa acción. Verifica sus roles y permisos en la BD.

### Tests fallan
Verifica que todas las dependencias estén en el pom.xml y ejecuta `mvn clean install`.

---

**¡Felicidades!** 🎉 Ahora tienes un sistema completo de autenticación profesional con Spring Security.
