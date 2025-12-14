# 🔐 GUÍA COMPLETA: SPRING SECURITY, JWT Y SISTEMA DE PERMISOS

## Índice
1. [¿Qué pasaba con el error?](#1-qué-pasaba-con-el-error)
2. [Conceptos Fundamentales de Seguridad](#2-conceptos-fundamentales-de-seguridad)
3. [JWT (JSON Web Tokens)](#3-jwt-json-web-tokens)
4. [Spring Security - Arquitectura](#4-spring-security---arquitectura)
5. [Sistema de Roles y Permisos (RBAC)](#5-sistema-de-roles-y-permisos-rbac)
6. [Flujo Completo de Autenticación](#6-flujo-completo-de-autenticación)
7. [Cómo Proteger Endpoints](#7-cómo-proteger-endpoints)
8. [Preguntas de Entrevista](#8-preguntas-de-entrevista)

---

## 1. ¿Qué pasaba con el error?

### El Problema: `ConcurrentModificationException`

```
User → roles → Role → users → User → roles → ... (bucle infinito)
```

**Causa:** Las entidades `User` y `Role` tenían relaciones bidireccionales con `FetchType.EAGER`:

```java
// En User.java
@ManyToMany(fetch = FetchType.EAGER)
private Set<Role> roles;

// En Role.java  
@ManyToMany(mappedBy = "roles") // Por defecto es LAZY, pero Lombok causaba problemas
private Set<User> users;
```

**¿Qué pasaba?**
1. Hibernate cargaba un `User`
2. Hibernate cargaba sus `roles` (EAGER)
3. Para cada `Role`, Hibernate intentaba cargar sus `users`
4. Para cada `User`, Hibernate intentaba cargar sus `roles`
5. **¡BUCLE INFINITO!** → `ConcurrentModificationException`

### La Solución

```java
// En Role.java - Hacer LAZY y excluir de Lombok
@ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
@JsonIgnore           // Evita serialización circular
@ToString.Exclude     // Evita bucle en toString()
@EqualsAndHashCode.Exclude  // Evita bucle en equals/hashCode
private Set<User> users;
```

**Lección aprendida:** En relaciones bidireccionales, siempre:
- Hacer el lado "inverso" (mappedBy) LAZY
- Excluirlo de serialización (@JsonIgnore)
- Excluirlo de Lombok (@ToString.Exclude, @EqualsAndHashCode.Exclude)

---

## 2. Conceptos Fundamentales de Seguridad

### 🔑 Autenticación vs Autorización

| Concepto | Definición | Pregunta que responde | Ejemplo |
|----------|------------|----------------------|---------|
| **Autenticación** | Verificar QUIÉN eres | "¿Eres quien dices ser?" | Login con email/password |
| **Autorización** | Verificar QUÉ puedes hacer | "¿Tienes permiso para esto?" | ¿Puedes eliminar usuarios? |

```
Usuario hace login → Autenticación ✓
Usuario intenta borrar cliente → Autorización (¿tiene permiso DELETE_CLIENTS?)
```

### 🔐 Tipos de Autenticación

1. **Session-Based (Tradicional)**
   - Servidor guarda sesión en memoria
   - Cliente envía cookie de sesión
   - ❌ No escala bien (problemas con múltiples servidores)

2. **Token-Based (JWT) - Lo que usamos**
   - Servidor genera token al hacer login
   - Cliente guarda token (localStorage, cookies)
   - Cliente envía token en cada request
   - ✅ Stateless, escalable

### 📋 Stateless vs Stateful

| Stateful (Sesiones) | Stateless (JWT) |
|---------------------|-----------------|
| Servidor recuerda quién eres | Servidor NO recuerda nada |
| Necesita almacenar sesiones | No almacena nada |
| Problemas con load balancers | Funciona con cualquier servidor |
| Cookie de sesión | Token JWT en header |

---

## 3. JWT (JSON Web Tokens)

### ¿Qué es JWT?

JWT es un **token codificado** que contiene información del usuario. Es como un "pase de acceso" firmado digitalmente.

### Estructura del JWT

Un JWT tiene 3 partes separadas por puntos:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c3VhcmlvQGVtYWlsLmNvbSIsImlhdCI6MTcwMTQ4ODAwMCwiZXhwIjoxNzAxNTc0NDAwfQ.abc123signature
     ↑ HEADER                              ↑ PAYLOAD                                                              ↑ SIGNATURE
```

| Parte | Contenido | Ejemplo decodificado |
|-------|-----------|---------------------|
| **Header** | Tipo de token y algoritmo | `{"alg": "HS256", "typ": "JWT"}` |
| **Payload** | Datos del usuario (claims) | `{"sub": "usuario@email.com", "iat": 1701488000, "exp": 1701574400}` |
| **Signature** | Firma digital | `HMACSHA256(header + payload, SECRET_KEY)` |

### Claims (Datos en el Payload)

```json
{
  "sub": "usuario@email.com",  // Subject - identificador del usuario
  "iat": 1701488000,           // Issued At - cuándo se creó
  "exp": 1701574400,           // Expiration - cuándo expira
  "roles": ["USER", "MANAGER"] // Custom claim - datos personalizados
}
```

### Flujo del JWT

```
┌─────────────┐                              ┌─────────────┐
│   CLIENTE   │                              │   SERVIDOR  │
│  (Angular)  │                              │  (Spring)   │
└──────┬──────┘                              └──────┬──────┘
       │                                            │
       │  1. POST /api/auth/login                   │
       │     {email, password}                      │
       │ ────────────────────────────────────────►  │
       │                                            │
       │                              2. Valida credenciales
       │                              3. Genera JWT
       │                                            │
       │  4. Response: {token: "eyJ..."}            │
       │ ◄────────────────────────────────────────  │
       │                                            │
       │  5. Guarda token en localStorage           │
       │                                            │
       │  6. GET /api/clientes                      │
       │     Header: Authorization: Bearer eyJ...   │
       │ ────────────────────────────────────────►  │
       │                                            │
       │                              7. Valida JWT (JwtAuthFilter)
       │                              8. Extrae usuario del token
       │                              9. Verifica permisos
       │                                            │
       │  10. Response: [{cliente1}, {cliente2}]    │
       │ ◄────────────────────────────────────────  │
       │                                            │
```

### Código del JwtService

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;  // Clave secreta para firmar

    @Value("${jwt.expiration}")
    private long JWT_EXPIRATION;  // 24 horas en milisegundos

    // Genera un token para el usuario
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())  // Email del usuario
                .setIssuedAt(new Date())                // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)  // Firma
                .compact();
    }

    // Extrae el email del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Valida si el token es válido
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
```

---

## 4. Spring Security - Arquitectura

### Componentes Principales

```
┌─────────────────────────────────────────────────────────────────────┐
│                         SPRING SECURITY                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐ │
│  │ SecurityConfig  │    │   JwtService    │    │ JwtAuthFilter   │ │
│  │                 │    │                 │    │                 │ │
│  │ - Rutas públicas│    │ - Genera tokens │    │ - Intercepta    │ │
│  │ - Rutas protect.│    │ - Valida tokens │    │   requests      │ │
│  │ - CORS config   │    │ - Extrae claims │    │ - Valida JWT    │ │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘ │
│           │                     │                      │            │
│           └─────────────────────┼──────────────────────┘            │
│                                 │                                   │
│  ┌──────────────────────────────┴───────────────────────────────┐  │
│  │                    CustomUserDetailsService                   │  │
│  │                                                               │  │
│  │  loadUserByUsername(email) → Carga User de la BD             │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                 │                                   │
│  ┌──────────────────────────────┴───────────────────────────────┐  │
│  │                    UserRepository                             │  │
│  │                                                               │  │
│  │  findByEmail(email) → Consulta a la base de datos            │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Cadena de Filtros (Filter Chain)

Cada request HTTP pasa por una cadena de filtros:

```
Request HTTP
    │
    ▼
┌───────────────────────────┐
│ 1. CorsFilter             │  ← Verifica CORS
└───────────────────────────┘
    │
    ▼
┌───────────────────────────┐
│ 2. JwtAuthenticationFilter│  ← ¡Nuestro filtro! Valida JWT
└───────────────────────────┘
    │
    ▼
┌───────────────────────────┐
│ 3. AuthorizationFilter    │  ← Verifica permisos/roles
└───────────────────────────┘
    │
    ▼
┌───────────────────────────┐
│ 4. Controller             │  ← Tu código
└───────────────────────────┘
```

### Código del JwtAuthenticationFilter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        
        // 1. Extraer header "Authorization"
        final String authHeader = request.getHeader("Authorization");
        
        // 2. Si no hay token, continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (quitar "Bearer ")
        String jwt = authHeader.substring(7);
        
        // 4. Extraer email del token
        String email = jwtService.extractUsername(jwt);

        // 5. Si hay email y usuario no está autenticado
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 6. Cargar usuario de la BD
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 7. Validar token
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // 8. Crear autenticación
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()  // Roles y permisos
                    );
                
                // 9. Establecer en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
```

---

## 5. Sistema de Roles y Permisos (RBAC)

### ¿Qué es RBAC?

**RBAC = Role-Based Access Control** (Control de Acceso Basado en Roles)

Es un modelo donde:
1. Los **usuarios** tienen **roles**
2. Los **roles** tienen **permisos**
3. Los **permisos** definen qué acciones se pueden realizar

### Modelo de Datos

```
┌─────────┐       ┌──────────────┐       ┌──────────┐       ┌─────────────────┐       ┌────────────┐
│  User   │──────>│  user_roles  │<──────│   Role   │──────>│role_permissions │<──────│ Permission │
├─────────┤  M:N  ├──────────────┤  M:N  ├──────────┤  M:N  ├─────────────────┤  M:N  ├────────────┤
│ id      │       │ user_id      │       │ id       │       │ role_id         │       │ id         │
│ username│       │ role_id      │       │ name     │       │ permission_id   │       │ name       │
│ email   │       └──────────────┘       │ desc     │       └─────────────────┘       │ desc       │
│ password│                              └──────────┘                                 └────────────┘
└─────────┘
```

### Tus Roles y Permisos Actuales

#### Permisos Disponibles:
| Permiso | Descripción |
|---------|-------------|
| `READ_CLIENTS` | Ver listado de clientes |
| `CREATE_CLIENTS` | Crear nuevos clientes |
| `UPDATE_CLIENTS` | Actualizar clientes |
| `DELETE_CLIENTS` | Eliminar clientes |
| `READ_LOANS` | Ver solicitudes de préstamo |
| `CREATE_LOANS` | Crear solicitudes |
| `UPDATE_LOANS` | Actualizar solicitudes |
| `DELETE_LOANS` | Eliminar solicitudes |
| `APPROVE_LOANS` | Aprobar solicitudes |
| `REJECT_LOANS` | Rechazar solicitudes |
| `MANAGE_USERS` | Gestionar usuarios |
| `MANAGE_ROLES` | Gestionar roles y permisos |

#### Roles y sus Permisos:

| Rol | Permisos |
|-----|----------|
| **USER** | `READ_CLIENTS`, `CREATE_CLIENTS`, `READ_LOANS`, `CREATE_LOANS` |
| **MANAGER** | Todo de USER + `UPDATE_CLIENTS`, `UPDATE_LOANS`, `APPROVE_LOANS`, `REJECT_LOANS` |
| **ADMIN** | **Todos los permisos** |

### Cómo se Cargan los Permisos

En la entidad `User`:

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList<>();
    
    for (Role role : roles) {
        // Agregar el ROL (con prefijo ROLE_)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        // Ejemplo: ROLE_ADMIN, ROLE_USER, ROLE_MANAGER
        
        // Agregar cada PERMISO del rol
        for (Permission permission : role.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getName()));
            // Ejemplo: READ_CLIENTS, CREATE_LOANS, APPROVE_LOANS
        }
    }
    
    return authorities;
}
```

**Resultado para usuario con rol MANAGER:**
```
[ROLE_MANAGER, READ_CLIENTS, CREATE_CLIENTS, UPDATE_CLIENTS, 
 READ_LOANS, CREATE_LOANS, UPDATE_LOANS, APPROVE_LOANS, REJECT_LOANS]
```

---

## 7. Cómo Proteger Endpoints

### Método 1: En SecurityConfig (Configuración Global)

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.authorizeHttpRequests(auth -> auth
        // Rutas públicas
        .requestMatchers("/api/auth/**").permitAll()
        
        // Por ROL
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/manager/**").hasRole("MANAGER")
        
        // Por PERMISO
        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**")
            .hasAuthority("DELETE_CLIENTS")
        
        // Todo lo demás requiere autenticación
        .anyRequest().authenticated()
    );
    return http.build();
}
```

### Método 2: Con Anotaciones en Controllers (Más Granular)

```java
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    // Cualquier usuario autenticado puede ver
    @GetMapping
    @PreAuthorize("hasAuthority('READ_CLIENTS')")
    public List<Cliente> listar() {
        return clienteService.findAll();
    }

    // Solo usuarios con permiso CREATE_CLIENTS
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_CLIENTS')")
    public Cliente crear(@RequestBody ClienteRequest request) {
        return clienteService.create(request);
    }

    // Solo usuarios con permiso UPDATE_CLIENTS
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_CLIENTS')")
    public Cliente actualizar(@PathVariable Long id, @RequestBody ClienteRequest request) {
        return clienteService.update(id, request);
    }

    // Solo usuarios con permiso DELETE_CLIENTS (normalmente ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_CLIENTS')")
    public void eliminar(@PathVariable Long id) {
        clienteService.delete(id);
    }
}
```

### Método 3: Por Rol

```java
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    // Solo MANAGER o ADMIN pueden aprobar
    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public void aprobar(@PathVariable Long id) {
        solicitudService.aprobar(id);
    }

    // Solo ADMIN puede eliminar
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        solicitudService.delete(id);
    }
}
```

### Método 4: Combinaciones Complejas

```java
// Debe tener el permiso O ser admin
@PreAuthorize("hasAuthority('APPROVE_LOANS') or hasRole('ADMIN')")
public void aprobarSolicitud(Long id) { }

// Debe tener AMBOS permisos
@PreAuthorize("hasAuthority('READ_LOANS') and hasAuthority('UPDATE_LOANS')")
public void actualizarSolicitud(Long id) { }

// Verificar que el usuario sea el dueño del recurso
@PreAuthorize("#username == authentication.principal.username")
public void actualizarPerfil(String username) { }
```

---

## 8. Preguntas de Entrevista

### Nivel Básico

**1. ¿Cuál es la diferencia entre autenticación y autorización?**
> - **Autenticación**: Verificar la identidad (quién eres) - login con credenciales
> - **Autorización**: Verificar permisos (qué puedes hacer) - acceso a recursos

**2. ¿Qué es un JWT y cuáles son sus partes?**
> JWT es un token codificado con 3 partes: Header (algoritmo), Payload (datos/claims), Signature (firma digital). Permite autenticación stateless.

**3. ¿Por qué usamos tokens en lugar de sesiones?**
> - **Escalabilidad**: No necesita almacenar estado en servidor
> - **Microservicios**: Funciona con múltiples servidores
> - **Mobile/SPA**: Mejor para apps modernas
> - **Stateless**: Cada request es independiente

**4. ¿Qué es CORS y por qué es necesario?**
> CORS (Cross-Origin Resource Sharing) permite que un frontend en un dominio (localhost:4200) haga peticiones a un backend en otro dominio (localhost:8080). Sin CORS configurado, el navegador bloquea las peticiones.

### Nivel Intermedio

**5. ¿Qué es la cadena de filtros (Filter Chain) en Spring Security?**
> Es una secuencia de filtros que procesan cada request HTTP. Incluye:
> - CorsFilter (CORS)
> - JwtAuthenticationFilter (validar token)
> - AuthorizationFilter (verificar permisos)
> Cada filtro decide si el request continúa o se rechaza.

**6. ¿Cómo funciona @PreAuthorize?**
> Es una anotación de Spring Security que verifica permisos ANTES de ejecutar un método. Usa SpEL (Spring Expression Language):
> - `hasRole('ADMIN')` - tiene rol
> - `hasAuthority('DELETE_CLIENTS')` - tiene permiso
> - `#id == authentication.principal.id` - verifica parámetros

**7. ¿Qué es RBAC?**
> Role-Based Access Control. Modelo de seguridad donde:
> - Usuarios tienen Roles
> - Roles tienen Permisos
> - Permisos definen acciones permitidas
> Ventaja: Cambiar permisos de un rol afecta a todos los usuarios con ese rol.

**8. ¿Cuál es la diferencia entre hasRole() y hasAuthority()?**
> - `hasRole('ADMIN')` busca la autoridad `ROLE_ADMIN` (agrega prefijo automáticamente)
> - `hasAuthority('DELETE_CLIENTS')` busca exactamente `DELETE_CLIENTS`
> Los roles son un tipo de autoridad con prefijo ROLE_.

### Nivel Avanzado

**9. ¿Cómo manejarías la renovación de tokens (refresh tokens)?**
> 1. Generar dos tokens: Access Token (corta duración) y Refresh Token (larga duración)
> 2. Access Token para requests normales (15-60 min)
> 3. Refresh Token para obtener nuevos Access Tokens
> 4. Guardar Refresh Token en HttpOnly cookie (más seguro)
> 5. Invalidar Refresh Token en logout

**10. ¿Cómo protegerías contra ataques JWT?**
> - **Signature verification**: Siempre verificar la firma
> - **Expiration**: Tokens de corta duración
> - **HTTPS**: Siempre usar HTTPS
> - **No guardar datos sensibles**: El payload es decodificable
> - **Blacklist**: Para invalidar tokens antes de expiración
> - **Secret rotation**: Rotar la clave secreta periódicamente

**11. ¿Qué pasa si el secret key de JWT se compromete?**
> Cualquiera podría crear tokens válidos. Soluciones:
> - Rotar inmediatamente el secret
> - Todos los tokens existentes se invalidan
> - Usuarios deben re-autenticarse
> - Implementar JWT blacklist para casos específicos

**12. ¿Cómo implementarías permisos dinámicos?**
> 1. Cargar permisos de BD en cada request (o cachear)
> 2. Usar `MethodSecurityExpressionHandler` personalizado
> 3. Crear un `PermissionEvaluator` custom
> 4. Ejemplo: `@PreAuthorize("@securityService.hasPermission(#id, 'EDIT')")`

### Preguntas de Código

**13. ¿Qué hace este código?**
```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```
> Establece el usuario autenticado en el contexto de seguridad de Spring. Después de esto, Spring Security sabe quién es el usuario y cuáles son sus permisos para el request actual.

**14. ¿Por qué usamos `OncePerRequestFilter`?**
> Garantiza que el filtro se ejecute exactamente UNA VEZ por request HTTP, incluso si hay forwards internos o includes. Un `Filter` normal podría ejecutarse múltiples veces.

**15. ¿Qué problema tiene este código?**
```java
@ManyToMany(fetch = FetchType.EAGER)
private Set<Role> roles;

@ManyToMany(mappedBy = "roles")  // En Role.java
private Set<User> users;
```
> Puede causar `ConcurrentModificationException` o `StackOverflowError` por relación bidireccional. Hibernate intenta cargar en bucle: User→Role→User→Role...
> Solución: Hacer el lado inverso LAZY y usar `@JsonIgnore`, `@ToString.Exclude`.

---

## Resumen Visual

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           FLUJO COMPLETO                                 │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1. LOGIN                                                                │
│     POST /api/auth/login {email, password}                               │
│         │                                                                │
│         ▼                                                                │
│     AuthService.login()                                                  │
│         │                                                                │
│         ├─► Busca usuario por email                                      │
│         ├─► Valida password (BCrypt)                                     │
│         ├─► Genera JWT (JwtService)                                      │
│         │                                                                │
│         ▼                                                                │
│     Response: {token: "eyJ...", roles: ["USER"]}                         │
│                                                                          │
│  2. REQUEST CON TOKEN                                                    │
│     GET /api/clientes                                                    │
│     Header: Authorization: Bearer eyJ...                                 │
│         │                                                                │
│         ▼                                                                │
│     JwtAuthenticationFilter                                              │
│         │                                                                │
│         ├─► Extrae token del header                                      │
│         ├─► Valida firma y expiración                                    │
│         ├─► Extrae email del token                                       │
│         ├─► Carga User de BD                                             │
│         ├─► Carga authorities (roles + permisos)                         │
│         ├─► Establece SecurityContext                                    │
│         │                                                                │
│         ▼                                                                │
│     Controller                                                           │
│         │                                                                │
│         ├─► @PreAuthorize verifica permisos                              │
│         ├─► Si tiene permiso → ejecuta método                            │
│         ├─► Si NO tiene permiso → 403 Forbidden                          │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Usuarios de Prueba

Para probar el sistema, ejecuta el script SQL y usa:

| Email | Password | Rol | Permisos |
|-------|----------|-----|----------|
| admin@cotizador.com | password123 | ADMIN | Todos |
| manager@cotizador.com | password123 | MANAGER | Gestión (sin delete) |
| usuario@cotizador.com | password123 | USER | Básicos (read, create) |

---

**¡Ahora tienes toda la información para entender Spring Security, JWT y prepararte para entrevistas!** 🚀
