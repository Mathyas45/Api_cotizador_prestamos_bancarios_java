# 🔐 Guía Spring Security + JWT - Paso a Paso

## 📚 ¿Qué es Spring Security?

Spring Security es como un **guardia de seguridad** para tu aplicación. 
Controla **quién puede entrar** (autenticación) y **qué puede hacer** (autorización).

---

## 🎯 Conceptos Básicos

### 1. **Autenticación** = ¿QUIÉN ERES?
- Verificar que eres quien dices ser
- Usas **usuario + contraseña** para demostrarlo
- Como mostrar tu cédula al entrar a un edificio

### 2. **Autorización** = ¿QUÉ PUEDES HACER?
- Una vez autenticado, ¿qué acciones tienes permitidas?
- Depende de tus **roles** (ADMIN, USER, MANAGER)
- Como tener llave de ciertas oficinas pero no de todas

### 3. **JWT (JSON Web Token)** = TU CREDENCIAL DIGITAL
- Es un "pase" que el servidor te da cuando haces login
- Lo guardas y lo muestras en cada petición
- Contiene tu información (username, roles) codificada
- Tiene fecha de expiración (24 horas en nuestra app)

---

## 🗂️ ORDEN DE ARCHIVOS PARA ENTENDER

Lee los archivos en **ESTE ORDEN** para entender todo:

```
📁 ORDEN DE LECTURA (de lo básico a lo complejo)
│
├── 1️⃣ MODELOS (La base de datos)
│   ├── models/User.java          → El usuario del sistema
│   ├── models/Role.java          → Los roles (ADMIN, USER)
│   └── models/Permission.java    → Los permisos específicos
│
├── 2️⃣ CONFIGURACIÓN
│   ├── application.yml           → Variables de entorno
│   └── config/SecurityConfig.java → Configuración de seguridad
│
├── 3️⃣ SEGURIDAD JWT
│   ├── security/JwtService.java              → Crea y valida tokens
│   ├── security/CustomUserDetailsService.java → Carga usuario de BD
│   └── security/JwtAuthenticationFilter.java  → Intercepta peticiones
│
├── 4️⃣ LÓGICA DE NEGOCIO
│   └── services/AuthService.java  → Login y registro
│
└── 5️⃣ API REST
    └── controllers/AuthController.java → Endpoints públicos
```

---

## 1️⃣ MODELOS - La Base de Datos

### 📄 `models/User.java`
**¿Qué hace?** Define cómo se guarda un usuario en la BD.

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {  // 👈 Implementa UserDetails
    
    private Long id;
    private String username;    // Nombre de usuario único
    private String password;    // Contraseña ENCRIPTADA (nunca texto plano)
    private String email;
    private Boolean enabled;    // ¿Está activo?
    
    @ManyToMany
    private Set<Role> roles;    // 👈 Un usuario tiene varios roles
}
```

**¿Por qué implementa `UserDetails`?**
- Es una interfaz de Spring Security
- Define métodos que Spring necesita para autenticar:
  - `getUsername()` - devuelve el nombre de usuario
  - `getPassword()` - devuelve la contraseña encriptada
  - `getAuthorities()` - devuelve roles y permisos
  - `isEnabled()` - ¿está activo el usuario?

### 📄 `models/Role.java`
**¿Qué hace?** Define los tipos de usuario (ADMIN, USER, MANAGER).

```java
@Entity
@Table(name = "roles")
public class Role {
    
    private Long id;
    private String name;            // "ADMIN", "USER", "MANAGER"
    private String description;
    
    @ManyToMany
    private Set<Permission> permissions;  // 👈 Cada rol tiene permisos
}
```

**Ejemplo de relación:**
```
ADMIN → [CREATE_CLIENT, READ_CLIENT, DELETE_CLIENT, CREATE_LOAN...]
USER  → [READ_CLIENT, CREATE_LOAN]
```

---

## 2️⃣ CONFIGURACIÓN

### 📄 `application.yml`
**¿Qué hace?** Configura la aplicación usando variables de entorno.

```yaml
spring:
  datasource:
    # ${VARIABLE:valor_default} = Si no existe la variable, usa el default
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:cotizador_db}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:12345678}

jwt:
  secret: ${JWT_SECRET:clave_secreta_aqui}    # Clave para firmar tokens
  expiration: ${JWT_EXPIRATION:86400000}      # 24 horas en milisegundos
```

**¿Cómo configurar variables de entorno en Windows?**
```powershell
# Temporal (solo esta terminal)
$env:DB_PASSWORD = "mi_password_seguro"
$env:JWT_SECRET = "mi_clave_super_secreta"

# Permanente
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "mi_password", "User")
```

### 📄 `config/SecurityConfig.java`
**¿Qué hace?** Configura TODA la seguridad de Spring.

```java
@Configuration          // 👈 Es una clase de configuración
@EnableWebSecurity      // 👈 Activa Spring Security
@EnableMethodSecurity   // 👈 Permite @PreAuthorize en métodos
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())  // 👈 Deshabilitamos CSRF (usamos JWT)
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas (sin login)
                .requestMatchers("/api/auth/**").permitAll()
                
                // Rutas protegidas (requieren login)
                .requestMatchers("/api/clientes/**").authenticated()
                
                // Todo lo demás requiere login
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 👈 Sin sesiones
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**Conceptos clave:**
| Concepto | Significado |
|----------|-------------|
| `permitAll()` | Cualquiera puede acceder, sin login |
| `authenticated()` | Solo usuarios con login válido |
| `STATELESS` | No guardamos sesión en servidor (usamos JWT) |
| `addFilterBefore` | Ejecuta nuestro filtro JWT antes del de Spring |

---

## 3️⃣ SEGURIDAD JWT

### 📄 `security/JwtService.java`
**¿Qué hace?** Crea y valida tokens JWT.

```java
@Service
public class JwtService {
    
    private String SECRET_KEY;      // Clave secreta para firmar
    private long JWT_EXPIRATION;    // Tiempo de expiración
    
    // ✅ GENERA un token para un usuario
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())  // 👈 Guardamos el username
                .setIssuedAt(new Date())                 // 👈 Fecha de creación
                .setExpiration(new Date(... + JWT_EXPIRATION))  // 👈 Expira en 24h
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)  // 👈 Firmamos
                .compact();
    }
    
    // ✅ EXTRAE el username de un token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // ✅ VALIDA si un token es correcto y no expiró
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) 
               && !isTokenExpired(token);
    }
}
```

**Estructura de un JWT:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFuMTIzIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9.firma_digital
│                      │                                                                              │
└──── HEADER ──────────┴──── PAYLOAD (datos) ──────────────────────────────────────────────────────────┴── FIRMA
      (algoritmo)            (username, fechas)                                                         (verificación)
```

### 📄 `security/CustomUserDetailsService.java`
**¿Qué hace?** Carga un usuario de la BD cuando Spring lo necesita.

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring llama a este método automáticamente durante el login
    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
```

**¿Cuándo se usa?**
1. Cuando haces **login**: Spring carga el usuario y compara passwords
2. Cuando envías un **token**: El filtro carga el usuario para verificar

### 📄 `security/JwtAuthenticationFilter.java`
**¿Qué hace?** Intercepta TODAS las peticiones HTTP y valida el token.

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        
        // 1️⃣ Extraer el header "Authorization"
        String authHeader = request.getHeader("Authorization");
        
        // 2️⃣ ¿Tiene el formato "Bearer <token>"?
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // Continuar sin autenticar
            return;
        }
        
        // 3️⃣ Extraer el token (quitar "Bearer ")
        String jwt = authHeader.substring(7);
        
        // 4️⃣ Extraer el username del token
        String username = jwtService.extractUsername(jwt);
        
        // 5️⃣ Cargar el usuario de la BD
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        // 6️⃣ ¿El token es válido?
        if (jwtService.isTokenValid(jwt, userDetails)) {
            // 7️⃣ ¡Autenticación exitosa! Informar a Spring Security
            SecurityContextHolder.getContext().setAuthentication(...);
        }
        
        // 8️⃣ Continuar con la petición
        filterChain.doFilter(request, response);
    }
}
```

---

## 4️⃣ LÓGICA DE NEGOCIO

### 📄 `services/AuthService.java`
**¿Qué hace?** Contiene la lógica de registro y login.

#### Registro de usuario:
```java
public AuthResponse register(RegisterRequest request) {
    // 1️⃣ Verificar que username no exista
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new RuntimeException("Username ya en uso");
    }
    
    // 2️⃣ Crear usuario con password ENCRIPTADA
    User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))  // 👈 BCrypt
            .email(request.getEmail())
            .roles(Set.of(rolUser))  // 👈 Rol USER por defecto
            .build();
    
    // 3️⃣ Guardar en BD
    userRepository.save(user);
    
    // 4️⃣ Generar token JWT
    String token = jwtService.generateToken(user);
    
    // 5️⃣ Retornar respuesta con token
    return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .build();
}
```

#### Login:
```java
public AuthResponse login(LoginRequest request) {
    // 1️⃣ Autenticar con Spring Security
    // Si el password es incorrecto, lanza BadCredentialsException
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()
        )
    );
    
    // 2️⃣ Cargar usuario de BD
    User user = userRepository.findByUsername(request.getUsername()).get();
    
    // 3️⃣ Generar token JWT
    String token = jwtService.generateToken(user);
    
    // 4️⃣ Retornar respuesta
    return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .build();
}
```

---

## 5️⃣ API REST

### 📄 `controllers/AuthController.java`
**¿Qué hace?** Expone los endpoints de autenticación.

```java
@RestController
@RequestMapping("/api/auth")  // 👈 Ruta base: /api/auth
public class AuthController {

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(201).body(
            ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Usuario registrado")
                .data(response)
                .build()
        );
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
            ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login exitoso")
                .data(response)
                .build()
        );
    }
}
```

---

## 🔄 FLUJO COMPLETO

### Flujo de REGISTRO:
```
┌─────────────┐    POST /api/auth/register     ┌─────────────────┐
│   FRONTEND  │ ──────────────────────────────►│ AuthController  │
│  (Angular)  │   {username, password, email}  │                 │
└─────────────┘                                └────────┬────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │   AuthService   │
                                               │  register()     │
                                               └────────┬────────┘
                                                        │
                          ┌─────────────────────────────┼─────────────────────────────┐
                          │                             │                             │
                          ▼                             ▼                             ▼
                 ┌─────────────────┐         ┌─────────────────┐           ┌─────────────────┐
                 │ UserRepository  │         │ PasswordEncoder │           │   JwtService    │
                 │    save()       │         │    encode()     │           │ generateToken() │
                 └─────────────────┘         └─────────────────┘           └─────────────────┘
                 (Guarda en BD)              (Encripta password)           (Genera token)
                                                        │
                                                        ▼
┌─────────────┐    { token: "eyJ...", username: "juan" }  ┌─────────────────┐
│   FRONTEND  │ ◄─────────────────────────────────────────│ AuthController  │
│  (Angular)  │   Guarda token en localStorage            │                 │
└─────────────┘                                           └─────────────────┘
```

### Flujo de LOGIN:
```
┌─────────────┐    POST /api/auth/login        ┌─────────────────┐
│   FRONTEND  │ ──────────────────────────────►│ AuthController  │
│  (Angular)  │   {username, password}         │                 │
└─────────────┘                                └────────┬────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │   AuthService   │
                                               │    login()      │
                                               └────────┬────────┘
                                                        │
                                                        ▼
                                          ┌─────────────────────────┐
                                          │  AuthenticationManager  │
                                          │     authenticate()      │
                                          └────────────┬────────────┘
                                                       │
                                    ┌──────────────────┴──────────────────┐
                                    ▼                                     ▼
                          ┌─────────────────┐                   ┌─────────────────┐
                          │UserDetailsService│                   │ PasswordEncoder │
                          │loadUserByUsername│                   │    matches()    │
                          └─────────────────┘                   └─────────────────┘
                          (Carga de BD)                         (Compara passwords)
                                    │                                     │
                                    └──────────────────┬──────────────────┘
                                                       │
                                                       ▼
                                               ¿Coincide password?
                                                   │      │
                                              NO   │      │  SÍ
                                                   ▼      ▼
                                        BadCredentialsException    Genera JWT
                                                                      │
                                                                      ▼
┌─────────────┐    { token: "eyJ...", username: "juan" }  ┌─────────────────┐
│   FRONTEND  │ ◄─────────────────────────────────────────│ AuthController  │
└─────────────┘                                           └─────────────────┘
```

### Flujo de PETICIÓN PROTEGIDA:
```
┌─────────────┐    GET /api/clientes                      ┌─────────────────────────┐
│   FRONTEND  │ ─────────────────────────────────────────►│ JwtAuthenticationFilter │
│  (Angular)  │   Header: Authorization: Bearer eyJ...    │     doFilterInternal()  │
└─────────────┘                                           └───────────┬─────────────┘
                                                                      │
                                           ┌──────────────────────────┤
                                           ▼                          │
                                  ┌─────────────────┐                 │
                                  │   JwtService    │                 │
                                  │extractUsername()│                 │
                                  │ isTokenValid()  │                 │
                                  └────────┬────────┘                 │
                                           │                          │
                                           ▼                          │
                                  ¿Token válido?                      │
                                    │      │                          │
                               NO   │      │  SÍ                      │
                                    ▼      ▼                          │
                             401 Unauthorized    SecurityContext      │
                                             .setAuthentication()     │
                                                       │              │
                                                       ▼              │
                                              ┌─────────────────┐     │
                                              │ SecurityConfig  │◄────┘
                                              │ ¿Tiene acceso?  │
                                              └────────┬────────┘
                                                       │
                                           ┌───────────┴───────────┐
                                           ▼                       ▼
                                    403 Forbidden          ┌─────────────────┐
                                                           │ClienteController│
                                                           │   findAll()     │
                                                           └────────┬────────┘
                                                                    │
                                                                    ▼
┌─────────────┐    { success: true, data: [...clientes] }  ┌─────────────────┐
│   FRONTEND  │ ◄──────────────────────────────────────────│ClienteController│
└─────────────┘                                            └─────────────────┘
```

---

## 🧪 CÓMO PROBAR

### 1. Registrar usuario (Postman):
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "juan123",
  "password": "miPassword123",
  "email": "juan@email.com"
}
```

### 2. Login (Postman):
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "juan123",
  "password": "miPassword123"
}
```

### 3. Acceder a ruta protegida:
```http
GET http://localhost:8080/api/clientes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...  👈 El token del login
```

---

## 📝 RESUMEN

| Archivo | Responsabilidad |
|---------|-----------------|
| `User.java` | Define usuario en BD + métodos para Spring Security |
| `Role.java` | Define roles (ADMIN, USER) y sus permisos |
| `SecurityConfig.java` | Configura rutas públicas/protegidas |
| `JwtService.java` | Crea y valida tokens JWT |
| `CustomUserDetailsService.java` | Carga usuario de BD |
| `JwtAuthenticationFilter.java` | Intercepta requests y valida tokens |
| `AuthService.java` | Lógica de registro y login |
| `AuthController.java` | Endpoints /register y /login |

---

## 🎓 PARA SEGUIR APRENDIENDO

1. **Primero entiende** el flujo de LOGIN (más simple)
2. **Luego** cómo se valida el token en cada petición
3. **Después** cómo se asignan roles y permisos
4. **Finalmente** cómo proteger endpoints por rol

¡Cualquier duda, pregúntame! 🚀
