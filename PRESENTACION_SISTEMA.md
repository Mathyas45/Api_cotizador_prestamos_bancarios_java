# 📊 Sistema de Cotización de Préstamos - API REST

## Guía para Presentación / Diapositivas

---

## 🎯 ¿Qué es este Sistema?

### Descripción General
Sistema **API REST** para la gestión y cotización de préstamos vehiculares, desarrollado con **Spring Boot**. Permite:

- ✅ **Registrar clientes** con sus datos personales
- ✅ **Cotizar préstamos** calculando cuotas, tasas e intereses
- ✅ **Validar clientes** contra servicios externos (buró de crédito)
- ✅ **Gestionar solicitudes** de préstamo (CRUD completo)
- ✅ **Autenticación segura** con JWT y roles

---

## 🏗️ Arquitectura del Sistema

### Arquitectura en Capas (Layered Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                    📱 CLIENTE (Frontend)                     │
│                   Postman / App Web / Móvil                  │
└────────────────────────────┬────────────────────────────────┘
                             │ HTTP/REST
┌────────────────────────────▼────────────────────────────────┐
│                   🌐 CAPA CONTROLADORES                      │
│    AuthController, ClienteController, SolicitudController    │
│                   (Recibe peticiones HTTP)                   │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                    ⚙️ CAPA DE SERVICIOS                      │
│     AuthService, ClienteService, SolicitudPrestamoService    │
│                   (Lógica de negocio)                        │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                   💾 CAPA DE REPOSITORIOS                    │
│    ClienteRepository, SolicitudRepository, UserRepository    │
│                   (Acceso a datos - JPA)                     │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                    🗄️ BASE DE DATOS                          │
│                      MySQL / PostgreSQL                      │
└─────────────────────────────────────────────────────────────┘
```

### ¿Por qué Arquitectura en Capas?

| Ventaja | Descripción |
|---------|-------------|
| 🔄 **Separación de responsabilidades** | Cada capa tiene una función específica |
| 🧪 **Facilita testing** | Se pueden probar capas de forma independiente |
| 🔧 **Mantenibilidad** | Cambios en una capa no afectan a otras |
| 📈 **Escalabilidad** | Fácil de agregar nuevas funcionalidades |

---

## 🔷 Principios SOLID Aplicados

### ¿Qué es SOLID?

5 principios de diseño para crear código **limpio, mantenible y escalable**:

### 1️⃣ **S**ingle Responsibility Principle (SRP)
> "Una clase debe tener una sola razón para cambiar"

**Aplicación en el sistema:**

```java
// ✅ TasaInteresConfig - SOLO maneja configuración de tasas
@Component
public class TasaInteresConfig {
    public BigDecimal obtenerTasaPorRiesgo(Integer riesgo) {
        return switch (riesgo) {
            case 1 -> TASA_RIESGO_BAJO;    // 7.5%
            case 2 -> TASA_RIESGO_MEDIO;   // 8.5%
            default -> TASA_RIESGO_ALTO;   // 9.5%
        };
    }
}

// ✅ CalculadoraFinanciera - SOLO hace cálculos financieros
public class CalculadoraFinanciera {
    public static BigDecimal calcularCuotaMensual(...) { }
    public static BigDecimal calcularTCEA(...) { }
}

// ✅ ClienteService - SOLO maneja lógica de clientes
// ✅ SolicitudPrestamoService - SOLO maneja solicitudes
```

**🎁 Beneficio:** Si las tasas cambian, solo modificas `TasaInteresConfig`. No tocas más archivos.

---

### 2️⃣ **O**pen/Closed Principle (OCP)
> "Abierto para extensión, cerrado para modificación"

**Aplicación en el sistema:**

```java
// Configuración centralizada permite extender sin modificar
public class TasaInteresConfig {
    // Si se agregan nuevos niveles de riesgo,
    // solo se modifica ESTE archivo
    // Los servicios que usan estas tasas NO cambian
}
```

---

### 3️⃣ **L**iskov Substitution Principle (LSP)
> "Las clases derivadas deben poder sustituir a sus clases base"

**Aplicación:** Los mappers y DTOs siguen este principio al convertir entidades.

---

### 4️⃣ **I**nterface Segregation Principle (ISP)
> "Los clientes no deben depender de interfaces que no usan"

**Aplicación en el sistema:**

```java
// Interfaces específicas para cada servicio
public interface IClienteService {
    ClienteResponse create(ClienteRequest request);
    ClienteResponse findById(Long id);
    List<ClienteResponse> findAll(String query);
    void update(Long id, ClienteRequest request);
    void delete(Long id);
}

public interface ISolicitudPrestamoService {
    SolicitudPrestamoResponse create(...);
    SolicitudPrestamoResponse simulador(...);
    // métodos específicos de solicitudes
}
```

---

### 5️⃣ **D**ependency Inversion Principle (DIP)
> "Depender de abstracciones, no de implementaciones"

**Aplicación en el sistema:**

```java
@Service
public class SolicitudPrestamoService implements ISolicitudPrestamoService {
    
    // ✅ Inyección de dependencias por constructor
    // ✅ Depende de abstracciones (interfaces/repositorios)
    public SolicitudPrestamoService(
            SolicitudPrestamoRepository repository,
            TasaInteresConfig tasaConfig,
            ClienteRepository clienteRepo) {
        // Spring inyecta las implementaciones
    }
}
```

---

## 📁 Estructura del Proyecto

```
src/main/java/com/optic/apirest/
│
├── 📂 config/                 # Configuraciones
│   ├── SecurityConfig.java    # Seguridad JWT
│   └── TasaInteresConfig.java # Tasas centralizadas (SOLID)
│
├── 📂 controllers/            # Endpoints REST
│   ├── AuthController.java
│   ├── ClienteController.java
│   └── SolicitudPrestamoController.java
│
├── 📂 services/               # Lógica de negocio
│   ├── interfaces/            # Contratos (SOLID: DIP)
│   │   ├── IClienteService.java
│   │   └── ISolicitudPrestamoService.java
│   ├── ClienteService.java
│   └── SolicitudPrestamoService.java
│
├── 📂 models/                 # Entidades JPA
│   ├── Cliente.java
│   ├── SolicitudPrestamo.java
│   └── User.java
│
├── 📂 dto/                    # Objetos de transferencia
│   ├── cliente/
│   └── SolicitudPrestamo/
│
├── 📂 repositories/           # Acceso a datos
│
├── 📂 security/               # Seguridad JWT
│
└── 📂 utils/                  # Utilidades
    └── CalculadoraFinanciera.java # Cálculos (SOLID: SRP)
```

---

## 🔐 Seguridad Implementada

### Spring Security + JWT

```
┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│   Usuario    │────▶│   Login     │────▶│  Token JWT   │
│  (Credencial)│     │  /auth/login│     │  (Respuesta) │
└──────────────┘     └─────────────┘     └──────────────┘
                                                │
                                                ▼
┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│   Recurso    │◀────│   Filtro    │◀────│   Petición   │
│  Protegido   │     │    JWT      │     │ + Header JWT │
└──────────────┘     └─────────────┘     └──────────────┘
```

**Características:**
- 🔑 Autenticación con usuario y contraseña
- 🎫 Generación de tokens JWT
- 👥 Roles: ADMIN, USER
- 🛡️ Permisos granulares

---

## 💰 Flujo de Cotización de Préstamo

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUJO DE COTIZACIÓN                       │
└─────────────────────────────────────────────────────────────┘

1️⃣ SOLICITUD                2️⃣ VALIDACIÓN
   ─────────                    ──────────
   Cliente envía:               API externa valida:
   • Monto                      • Historial crediticio
   • Plazo (años)               • Nivel de riesgo (1-3)
   • % Cuota inicial            • Aprobado/Rechazado
         │                              │
         ▼                              ▼
   ┌───────────┐               ┌───────────────┐
   │  Request  │──────────────▶│  MockAPI      │
   │  POST     │               │  (Validación) │
   └───────────┘               └───────────────┘
                                       │
                                       ▼
3️⃣ CÁLCULO                           │
   ────────                           │
   TasaInteresConfig:                 │
   • Riesgo 1 → 7.5%                  │
   • Riesgo 2 → 8.5%          ◀───────┘
   • Riesgo 3 → 9.5%
         │
         ▼
   CalculadoraFinanciera:
   • Cuota inicial
   • Monto a financiar
   • Cuota mensual (Fórmula Francesa)
   • TCEA
         │
         ▼
4️⃣ RESPUESTA
   ──────────
   ┌─────────────────────┐
   │  SolicitudResponse  │
   │  • Tasa: 8.5%       │
   │  • Cuota: $XXX      │
   │  • TCEA: X.XX%      │
   │  • Estado: APROBADO │
   └─────────────────────┘
```

---

## 🧮 Fórmulas Financieras

### Fórmula Francesa (Cuota Mensual)

$$Cuota = \frac{M \times i}{1 - (1 + i)^{-n}}$$

Donde:
- **M** = Monto a financiar
- **i** = Tasa de interés mensual
- **n** = Número de cuotas (meses)

### TCEA (Tasa de Costo Efectivo Anual)

$$TCEA = ((1 + i_{mensual})^{12} - 1) \times 100$$

---

## 📊 Endpoints Principales

### 🔐 Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/register` | Registrar usuario |
| POST | `/auth/login` | Iniciar sesión |

### 👤 Clientes
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/clientes` | Listar clientes |
| GET | `/clientes/{id}` | Obtener cliente |
| POST | `/clientes` | Crear cliente |
| PUT | `/clientes/{id}` | Actualizar cliente |
| DELETE | `/clientes/{id}` | Eliminar cliente |

### 💰 Solicitudes de Préstamo
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/solicitudes` | Listar solicitudes |
| GET | `/solicitudes/{id}` | Obtener solicitud |
| POST | `/solicitudes` | Crear solicitud |
| POST | `/solicitudes/simulador` | Simular (sin guardar) |
| PUT | `/solicitudes/{id}` | Actualizar solicitud |
| DELETE | `/solicitudes/{id}` | Eliminar solicitud |

---

## ✅ Ventajas del Sistema

| Característica | Beneficio |
|----------------|-----------|
| **Spring Boot** | Desarrollo rápido, configuración mínima |
| **JPA/Hibernate** | ORM potente para BD |
| **JWT** | Autenticación stateless y segura |
| **Principios SOLID** | Código mantenible y escalable |
| **Arquitectura en capas** | Separación de responsabilidades |
| **DTOs** | Separación entre modelo y vista |
| **Inyección de dependencias** | Bajo acoplamiento |

---

## 🛠️ Tecnologías Utilizadas

```
┌────────────────────────────────────────────┐
│           STACK TECNOLÓGICO                │
├────────────────────────────────────────────┤
│  ☕ Java 17+                               │
│  🍃 Spring Boot 3.x                        │
│  🔐 Spring Security + JWT                  │
│  💾 Spring Data JPA                        │
│  🗄️ MySQL / PostgreSQL                     │
│  📦 Maven                                  │
│  🧪 JUnit 5 + Mockito                      │
└────────────────────────────────────────────┘
```

---

## 🎯 Resumen para Diapositivas

### Slide 1: Introducción
- Nombre del sistema
- Qué problema resuelve
- Tecnologías principales

### Slide 2: Arquitectura
- Diagrama de capas
- Explicación breve de cada capa

### Slide 3: Principios SOLID
- Lista de los 5 principios
- Ejemplo de 1-2 aplicados

### Slide 4: Flujo Principal
- Diagrama del flujo de cotización
- Paso a paso simplificado

### Slide 5: Seguridad
- JWT + Spring Security
- Roles y permisos

### Slide 6: Demo / Endpoints
- Mostrar endpoints principales
- Demo con Postman (opcional)

### Slide 7: Conclusiones
- Ventajas del sistema
- Escalabilidad futura

---

## 📝 Notas para el Presentador

1. **Enfatizar SOLID:** Es el diferenciador técnico del proyecto
2. **Mostrar código real:** `TasaInteresConfig` es un buen ejemplo
3. **Demo práctica:** Si es posible, hacer una cotización en vivo
4. **Mencionar testing:** El código es fácil de testear gracias a SOLID

---

*Documento generado para presentación académica/profesional del Sistema de Cotización de Préstamos*
