# 📋 Guía de Revisión de Archivos - Orden y Razones

## ✅ Errores Corregidos

### 1. Error en `JwtService.java`
**Problema:** `Cannot resolve method 'parserBuilder' in 'Jwts'`  
**Causa:** La versión 0.12.3 de jjwt cambió la API. Ya no existe `parserBuilder()`, ahora es `parser()`.  
**Solución:** ✅ Actualizado el método `extractAllClaims()` para usar la nueva API.

---

## 🔍 Orden de Revisión de Archivos

### **FASE 1: Configuración Base** 🔧

#### 1. `pom.xml`
**¿Por qué revisar primero?**
- Contiene todas las dependencias del proyecto
- Si falta alguna dependencia, nada funcionará
- Verifica que tenga las 3 dependencias JWT:
  ```xml
  <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>0.12.3</version>
  </dependency>
  <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>0.12.3</version>
      <scope>runtime</scope>
  </dependency>
  <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>0.12.3</version>
      <scope>runtime</scope>
  </dependency>
  ```

**Qué verificar:**
- ✅ Versión de Spring Boot: 3.5.7
- ✅ Java version: 25
- ✅ Dependencias JWT presentes
- ✅ Spring Security presente
- ✅ Lombok presente
- ✅ Spring Validation presente

---

#### 2. `application.yml`
**¿Por qué revisar segundo?**
- Contiene la configuración de conexión a MySQL
- Si la BD no está configurada, la app no arranca
- Tiene la configuración JWT

**Qué verificar:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cotizador_db  # ✅ BD existe?
    username: root                                   # ✅ Usuario correcto?
    password: tu_contraseña                          # ✅ Contraseña correcta?
  jpa:
    hibernate:
      ddl-auto: update                               # ✅ Crea tablas automático

jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  expiration: 86400000  # 24 horas
```

**Acción requerida:**
- Verifica que la base de datos `cotizador_db` exista
- Si no existe, créala: `CREATE DATABASE cotizador_db;`

---

### **FASE 2: Modelos (Entidades)** 🗃️

#### 3. `models/Cliente.java`
**¿Por qué revisar?**
- Modelo principal del CRUD que ya tenías funcionando
- Debe tener relación con `SolicitudPrestamo`

**Qué verificar:**
```java
@OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
private List<SolicitudPrestamo> solicitudes;
```

---

#### 4. `models/SolicitudPrestamo.java`
**¿Por qué revisar?**
- Segundo modelo del CRUD
- Debe tener relación con `Cliente`

**Qué verificar:**
```java
@ManyToOne
@JoinColumn(name = "cliente_id", nullable = false)
private Cliente cliente;
```

---

#### 5. `models/User.java`
**¿Por qué revisar?**
- Modelo de usuarios para autenticación
- Implementa `UserDetails` de Spring Security
- Tiene relación Many-to-Many con `Role`

**Qué verificar:**
```java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();
```

---

#### 6. `models/Role.java`
**¿Por qué revisar?**
- Define los roles (ADMIN, USER, MANAGER)
- Tiene relación Many-to-Many con `Permission`

**Qué verificar:**
```java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "role_permissions",
    joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id")
)
private Set<Permission> permissions = new HashSet<>();
```

---

#### 7. `models/Permission.java`
**¿Por qué revisar?**
- Define permisos granulares (READ_CLIENTES, WRITE_CLIENTES, etc.)
- Es la unidad más pequeña de autorización

---

### **FASE 3: Seguridad (JWT)** 🔐

#### 8. `security/JwtService.java` ⚠️ **YA CORREGIDO**
**¿Por qué revisar?**
- Genera y valida tokens JWT
- **TENÍA ERROR**: `parserBuilder()` no existe en jjwt 0.12.3
- **YA CORREGIDO**: Ahora usa `parser()` con la nueva API

**Qué verificar:**
```java
private Claims extractAllClaims(String token) {
    return Jwts
            .parser()  // ✅ Correcto para 0.12.3
            .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
}
```

---

#### 9. `security/CustomUserDetailsService.java`
**¿Por qué revisar?**
- Carga los datos del usuario desde la BD
- Spring Security lo usa para validar credenciales

**Qué verificar:**
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
}
```

---

#### 10. `security/JwtAuthenticationFilter.java`
**¿Por qué revisar?**
- Intercepta TODAS las peticiones HTTP
- Extrae el token JWT del header `Authorization`
- Valida el token con `JwtService`

**Qué verificar:**
```java
@Override
protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }
    // ... resto del código
}
```

---

#### 11. `config/SecurityConfig.java` ⚠️ **ACTUALIZADO CON MODO DESARROLLO**
**¿Por qué revisar?**
- Configuración principal de Spring Security
- Define qué rutas son públicas y cuáles requieren autenticación
- **AHORA TIENE DOS MODOS**: DESARROLLO (sin JWT) y PRODUCCIÓN (con JWT)

**⚠️ MODO DESARROLLO - Para probar sin login:**
```java
// Descomenta este bloque:
/*
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll() // ⚠️ TODAS las rutas públicas
    );
*/

// Y comenta el bloque de PRODUCCIÓN
```

**✅ MODO PRODUCCIÓN - Con JWT habilitado:**
```java
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/clientes/**").authenticated()
            .requestMatchers("/api/solicitudesPrestamo/**").authenticated()
            .anyRequest().authenticated()
    )
    .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    )
    .authenticationProvider(authenticationProvider())
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

---

### **FASE 4: Repositorios** 💾

#### 12. `repositories/ClienteRepository.java`
**¿Por qué revisar?**
- Interface para acceso a BD de clientes

**Qué verificar:**
```java
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCorreo(String correo);
}
```

---

#### 13. `repositories/SolicitudPrestamoRepository.java`
**¿Por qué revisar?**
- Interface para acceso a BD de solicitudes

---

#### 14-16. `repositories/UserRepository.java`, `RoleRepository.java`, `PermissionRepository.java`
**¿Por qué revisar?**
- Repositorios para el sistema de autenticación

---

### **FASE 5: DTOs** 📦

#### 17. `dto/ApiResponse.java` ⚠️ **YA ACTUALIZADO**
**¿Por qué revisar?**
- Wrapper genérico para todas las respuestas de la API
- **YA CORREGIDO**: Ahora es genérico con `<T>` y tiene `@Builder`

**Qué verificar:**
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
```

---

#### 18-22. DTOs de Cliente, SolicitudPrestamo, Auth
**¿Por qué revisar?**
- Request y Response para cada endpoint
- Mappers para convertir entre Entity y DTO

---

### **FASE 6: Servicios (Lógica de Negocio)** 🧠

#### 23. `services/ClienteService.java`
**¿Por qué revisar?**
- Lógica de negocio del CRUD de clientes
- Valida duplicados por correo

---

#### 24. `services/SolicitudPrestamoService.java`
**¿Por qué revisar?**
- Lógica de negocio del CRUD de solicitudes
- Llama a la API externa de tasas de interés

---

#### 25. `services/AuthService.java`
**¿Por qué revisar?**
- Lógica de registro y login
- Genera tokens JWT
- Asigna roles por defecto

**Qué verificar:**
```java
public AuthResponse register(RegisterRequest request) {
    // Encripta contraseña con BCrypt
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    
    // Asigna rol USER por defecto
    Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
    user.setRoles(Set.of(userRole));
    
    // Genera token JWT
    String jwt = jwtService.generateToken(user);
}
```

---

### **FASE 7: Controladores (API REST)** 🌐

#### 26. `controllers/ClienteController.java` ⚠️ **YA ACTUALIZADO**
**¿Por qué revisar?**
- Endpoints del CRUD de clientes
- **YA ACTUALIZADO**: Usa la nueva `ApiResponse<T>` genérica

**Qué verificar:**
```java
@PostMapping
public ResponseEntity<ApiResponse<Void>> create(@RequestBody ClienteRequest request) {
    clienteService.create(request);
    return ResponseEntity.ok(ApiResponse.<Void>builder()
            .success(true)
            .message("Cliente creado exitosamente")
            .data(null)
            .build());
}
```

---

#### 27. `controllers/SolicitudPrestamoController.java`
**¿Por qué revisar?**
- Endpoints del CRUD de solicitudes
- Endpoint de simulador

**⚠️ NOTA:** Este controlador NO usa `ApiResponse` todavía, usa `ResponseEntity<?>` directo.
Puedes actualizarlo después si quieres consistencia.

---

#### 28. `controllers/AuthController.java`
**¿Por qué revisar?**
- Endpoints de login y registro
- `/api/auth/login` (POST)
- `/api/auth/register` (POST)

**Qué verificar:**
```java
@PostMapping("/login")
public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
            .success(true)
            .message("Login exitoso")
            .data(response)
            .build());
}
```

---

### **FASE 8: Cliente HTTP** 🌐

#### 29. `Client/TasaInteresApiClient.java`
**¿Por qué revisar?**
- Llama a API externa para obtener tasas de interés
- Usa `RestTemplate`

---

### **FASE 9: Tests (Opcional pero Recomendado)** 🧪

#### 30. `test/.../ClienteServiceTest.java`
**¿Por qué revisar?**
- 10 tests unitarios del CRUD de clientes
- Usa Mockito

**Cómo ejecutar:**
```bash
mvn test
```

---

#### 31. `test/.../AuthServiceTest.java`
**¿Por qué revisar?**
- 6 tests del sistema de autenticación

---

## 🚀 ¿Cómo Probar Sin Login? (MODO DESARROLLO)

### Opción 1: Deshabilitar Seguridad Temporalmente ✅ RECOMENDADO

**Paso 1:** Edita `SecurityConfig.java`

**Descomentar** este bloque (líneas ~58-65):
```java
// ==================== MODO DESARROLLO ====================
// Descomenta estas líneas para DESHABILITAR la seguridad JWT
http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // ⚠️ TODAS las rutas públicas
        );
// =========================================================
```

**Comentar** el bloque de PRODUCCIÓN (líneas ~68-93):
```java
/*
// ==================== MODO PRODUCCIÓN ====================
http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/clientes/**").authenticated()
                // ... resto del código
        )
        .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
// =========================================================
*/
```

**Paso 2:** Reinicia la aplicación
```bash
mvn spring-boot:run
```

**Paso 3:** Prueba tus endpoints SIN token
```bash
# Crear cliente (sin Authorization header)
POST http://localhost:8080/api/clientes
{
    "nombre": "Juan Pérez",
    "correo": "juan@example.com",
    ...
}
```

---

### Opción 2: Usar el Token JWT (Cuando tengas login en el frontend)

**Paso 1:** Crea un usuario (ejecuta el SQL):
```sql
-- Usuario: admin@example.com
-- Password: admin123
```

**Paso 2:** Haz login desde Postman:
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "admin@example.com",
    "password": "admin123"
}
```

**Respuesta:**
```json
{
    "success": true,
    "message": "Login exitoso",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "type": "Bearer",
        "username": "admin@example.com",
        "roles": ["ADMIN"]
    }
}
```

**Paso 3:** Copia el token y úsalo en cada request:
```http
GET http://localhost:8080/api/clientes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 📊 Resumen de Cambios Realizados

### ✅ Errores Corregidos:
1. **JwtService.java**: Actualizado `parserBuilder()` → `parser()` para jjwt 0.12.3
2. **ApiResponse.java**: Convertido a genérico con `<T>` y agregado `@Builder`
3. **ClienteController.java**: Actualizado para usar `ApiResponse<T>` genérico

### 🔧 Configuración Añadida:
1. **SecurityConfig.java**: Agregado MODO DESARROLLO para deshabilitar seguridad temporalmente

---

## ⚠️ IMPORTANTE: Antes de Producción

Cuando termines de desarrollar el frontend y tengas login funcionando:

1. **Comenta el bloque DESARROLLO**
2. **Descomenta el bloque PRODUCCIÓN** en `SecurityConfig.java`
3. **Reinicia la aplicación**
4. **Todas las rutas requerirán token JWT**

---

## 🎯 Orden de Ejecución Recomendado

```bash
# 1. Verificar que MySQL esté corriendo
# 2. Crear la base de datos
CREATE DATABASE IF NOT EXISTS cotizador_db;

# 3. Ejecutar el script SQL con usuarios de prueba
mysql -u root -p cotizador_db < src/main/resources/db_schema_with_security.sql

# 4. Compilar el proyecto
mvn clean install

# 5. Ejecutar tests (opcional)
mvn test

# 6. Iniciar la aplicación
mvn spring-boot:run

# 7. Probar endpoints con Postman
```

---

## 📞 Endpoints Disponibles

### Autenticación (siempre públicos):
- `POST /api/auth/register` - Registrar usuario
- `POST /api/auth/login` - Iniciar sesión

### Clientes (requieren auth en PRODUCCIÓN):
- `GET /api/clientes` - Listar todos
- `GET /api/clientes/{id}` - Obtener por ID
- `POST /api/clientes` - Crear
- `PUT /api/clientes/{id}` - Actualizar
- `DELETE /api/clientes/{id}` - Eliminar

### Solicitudes (requieren auth en PRODUCCIÓN):
- `GET /api/solicitudesPrestamo` - Listar todas
- `GET /api/solicitudesPrestamo/{id}` - Obtener por ID
- `POST /api/solicitudesPrestamo/simulador` - Simular préstamo
- `POST /api/solicitudesPrestamo/register` - Crear solicitud
- `PUT /api/solicitudesPrestamo/update/{id}` - Actualizar
- `DELETE /api/solicitudesPrestamo/delete/{id}` - Eliminar

---

## 🐛 Si Encuentras Más Errores

1. Ejecuta `mvn clean install` para ver errores de compilación
2. Revisa los logs en la consola cuando inicies la app
3. Verifica que la BD esté corriendo
4. Verifica las credenciales en `application.yml`

---

**Última actualización:** 22 de noviembre de 2025  
**Estado:** ✅ Listo para desarrollo
