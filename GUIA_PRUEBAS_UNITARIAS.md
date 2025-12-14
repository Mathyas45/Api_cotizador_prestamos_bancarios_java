# 🧪 GUÍA COMPLETA - PRUEBAS UNITARIAS CON JUNIT Y MOCKITO

## 📚 ¿Qué son las Pruebas Unitarias?

Las **pruebas unitarias** son código que **prueba otro código**. Verifican que cada "unidad" (método/función) funciona correctamente de forma AISLADA.

### Analogía

Imagina que fabricas autos:

- ❌ **Sin pruebas**: Ensamblas TODO y luego pruebas si arranca
  - Si falla, ¿cuál pieza es el problema? 🤷‍♂️
  
- ✅ **Con pruebas unitarias**: Pruebas CADA pieza antes de ensamblar
  - Motor ✓
  - Frenos ✓
  - Luces ✓
  - Si algo falla, sabes EXACTAMENTE qué es

---

## 🎯 Beneficios

1. **Detectan errores ANTES de producción**
   - Mejor encontrar un bug en desarrollo que en el cliente

2. **Documentan el código**
   - Los tests explican CÓMO debe usarse cada método

3. **Facilitan refactoring**
   - Cambias código y los tests verifican que siga funcionando

4. **Son RÁPIDOS**
   - No usan base de datos ni red
   - Se ejecutan en milisegundos

5. **Empresas los REQUIEREN**
   - Es estándar en la industria
   - Forma parte de CI/CD (integración continua)

---

## 🛠️ Herramientas

### JUnit 5

Framework para escribir y ejecutar pruebas en Java.

**Instalación** (ya incluido en tu pom.xml):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Mockito

Framework para crear "mocks" (objetos simulados) de dependencias.

**¿Por qué?** Tu servicio depende de repositorios, pero NO queremos usar la BD real en los tests.

---

## 📖 Conceptos Clave

### 1. Mock (Objeto Simulado)

Un **mock** es un objeto FALSO que simula el comportamiento de uno real.

**Ejemplo sin mock** (MALO ❌):
```java
@Test
void testBuscarCliente() {
    // ❌ Necesita base de datos MySQL corriendo
    ClienteRepository repo = new ClienteRepositoryImpl();
    Cliente cliente = repo.findById(1L); // Consulta SQL real
}
```

**Ejemplo con mock** (BUENO ✅):
```java
@Mock
private ClienteRepository repo; // Mock (falso)

@Test
void testBuscarCliente() {
    // ✅ No necesita base de datos
    when(repo.findById(1L)).thenReturn(Optional.of(cliente));
    // Simula que repo.findById retorna un cliente
}
```

---

### 2. Patrón AAA (Arrange-Act-Assert)

Estructura estándar para escribir tests:

```java
@Test
void testEjemplo() {
    // ARRANGE (Preparar)
    // Configura datos de prueba y mocks
    
    // ACT (Actuar)
    // Ejecuta el método a probar
    
    // ASSERT (Afirmar)
    // Verifica que el resultado es correcto
}
```

**Ejemplo completo**:
```java
@Test
void testBuscarCliente() {
    // ARRANGE: Preparar datos
    Cliente clienteMock = new Cliente();
    clienteMock.setId(1L);
    clienteMock.setNombre("Juan");
    
    when(clienteRepository.findById(1L))
        .thenReturn(Optional.of(clienteMock));
    
    // ACT: Ejecutar
    ClienteResponse resultado = clienteService.findById(1L);
    
    // ASSERT: Verificar
    assertEquals("Juan", resultado.getNombre());
    assertEquals(1L, resultado.getId());
}
```

---

## 🔧 Anotaciones Principales

### @ExtendWith(MockitoExtension.class)

Habilita Mockito en JUnit 5.

```java
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    // Ahora puedes usar @Mock
}
```

### @Mock

Crea un mock (objeto simulado) de una dependencia.

```java
@Mock
private ClienteRepository clienteRepository; // Repositorio FALSO

@Mock
private ClienteMapper clienteMapper; // Mapper FALSO
```

**Lo que hace**:
- Crea un objeto que NO hace nada por defecto
- Tú defines su comportamiento con `when(...).thenReturn(...)`

### @InjectMocks

Crea la instancia del servicio e inyecta los mocks automáticamente.

```java
@Mock
private ClienteRepository clienteRepository;

@InjectMocks
private ClienteService clienteService;
// clienteService recibe clienteRepository como dependencia
```

**Equivalente manual**:
```java
ClienteRepository repo = mock(ClienteRepository.class);
ClienteService service = new ClienteService(repo);
```

### @BeforeEach

Se ejecuta ANTES de cada test. Útil para inicializar datos.

```java
private Cliente cliente;

@BeforeEach
void setUp() {
    cliente = new Cliente();
    cliente.setId(1L);
    cliente.setNombre("Juan");
    // Ahora 'cliente' está disponible en todos los tests
}
```

### @Test

Marca un método como prueba unitaria.

```java
@Test
void testCrearCliente() {
    // Este método es una prueba
}
```

### @DisplayName

Nombre legible de la prueba (aparece en reportes).

```java
@Test
@DisplayName("Buscar cliente por ID - Cliente encontrado")
void testBuscarClienteExistente() {
    // ...
}
```

---

## 🎓 Métodos de Mockito

### when(...).thenReturn(...)

Define el comportamiento de un mock.

```java
// "Cuando se llame a findById(1L), retorna este cliente"
when(clienteRepository.findById(1L))
    .thenReturn(Optional.of(cliente));
```

**Sintaxis**:
```java
when(mock.metodo(argumentos)).thenReturn(valorRetorno);
```

### when(...).thenThrow(...)

Simula que un método lanza una excepción.

```java
// "Cuando se llame a findById(999L), lanza excepción"
when(clienteRepository.findById(999L))
    .thenThrow(new RuntimeException("No encontrado"));
```

### verify(...)

Verifica que un método del mock fue llamado.

```java
// Verifica que save() fue llamado exactamente 1 vez
verify(clienteRepository, times(1)).save(any(Cliente.class));

// Verifica que delete() NUNCA fue llamado
verify(clienteRepository, never()).delete(any());
```

### any(...)

Coincide con cualquier argumento de ese tipo.

```java
// Cualquier objeto Cliente
verify(clienteRepository).save(any(Cliente.class));

// Cualquier String
when(clienteRepository.findByDocumento(anyString()))
    .thenReturn(Optional.of(cliente));
```

---

## 🎓 Métodos de Assertions (JUnit)

### assertEquals(expected, actual)

Verifica que dos valores son iguales.

```java
assertEquals("Juan", cliente.getNombre());
assertEquals(1L, cliente.getId());
```

### assertNotNull(object)

Verifica que un objeto NO es null.

```java
assertNotNull(resultado);
```

### assertTrue / assertFalse

Verifica condiciones booleanas.

```java
assertTrue(cliente.isEnabled());
assertFalse(cliente.isBlocked());
```

### assertThrows(exception, lambda)

Verifica que un código lanza una excepción.

```java
RuntimeException ex = assertThrows(RuntimeException.class, () -> {
    clienteService.findById(999L);
});

assertEquals("Cliente no encontrado", ex.getMessage());
```

---

## 📝 EJEMPLO COMPLETO COMENTADO

```java
package com.optic.apirest.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 1️⃣ Habilitar Mockito
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - ClienteService")
class ClienteServiceTest {

    // 2️⃣ Crear mocks de dependencias
    @Mock
    private ClienteRepository clienteRepository; // Repositorio FALSO
    
    @Mock
    private ClienteMapper clienteMapper; // Mapper FALSO

    // 3️⃣ Crear instancia del servicio con mocks inyectados
    @InjectMocks
    private ClienteService clienteService;

    // 4️⃣ Variables de datos de prueba
    private Cliente clienteMock;
    private ClienteResponse clienteResponse;

    // 5️⃣ Inicializar datos ANTES de cada test
    @BeforeEach
    void setUp() {
        // Crear cliente mock
        clienteMock = new Cliente();
        clienteMock.setId(1L);
        clienteMock.setNombre("Juan Pérez");
        
        // Crear response mock
        clienteResponse = new ClienteResponse();
        clienteResponse.setId(1L);
        clienteResponse.setNombre("Juan Pérez");
    }

    // 6️⃣ TEST: Buscar cliente que existe
    @Test
    @DisplayName("Buscar cliente por ID - Encontrado")
    void testFindById_ClienteEncontrado() {
        // ===== ARRANGE (Preparar) =====
        // Configurar mock: cuando se llame a findById(1L), retorna clienteMock
        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(clienteMock));
        
        // Configurar mock: cuando se llame a toResponse, retorna clienteResponse
        when(clienteMapper.toResponse(clienteMock))
            .thenReturn(clienteResponse);

        // ===== ACT (Actuar) =====
        // Ejecutar el método a probar
        ClienteResponse resultado = clienteService.findById(1L);

        // ===== ASSERT (Afirmar) =====
        // Verificar que el resultado es correcto
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan Pérez", resultado.getNombre());
        
        // Verificar que los mocks fueron llamados correctamente
        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteMapper, times(1)).toResponse(clienteMock);
    }

    // 7️⃣ TEST: Buscar cliente que NO existe
    @Test
    @DisplayName("Buscar cliente por ID - No encontrado")
    void testFindById_ClienteNoEncontrado() {
        // ===== ARRANGE =====
        // Simular que el cliente no existe
        when(clienteRepository.findById(999L))
            .thenReturn(Optional.empty());

        // ===== ACT & ASSERT =====
        // Verificar que lanza excepción
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            clienteService.findById(999L);
        });
        
        // Verificar el mensaje de error
        assertEquals("Cliente no encontrado con ID: 999", ex.getMessage());
        
        // Verificar que el repository fue llamado
        verify(clienteRepository, times(1)).findById(999L);
        
        // Verificar que el mapper NUNCA fue llamado (porque no hay cliente)
        verify(clienteMapper, never()).toResponse(any());
    }
}
```

---

## 🚀 EJECUTAR LAS PRUEBAS

### Desde la Terminal

```bash
# Ejecutar TODAS las pruebas
mvn test

# Ejecutar una clase específica
mvn test -Dtest=ClienteServiceTest

# Ejecutar un test específico
mvn test -Dtest=ClienteServiceTest#testFindById_ClienteEncontrado
```

### Desde IntelliJ IDEA

1. Click derecho en la clase de test
2. "Run 'ClienteServiceTest'"

O:

1. Click en el ícono verde ▶️ junto al método
2. "Run testFindById..."

### Desde VS Code

1. Instalar extensión "Test Runner for Java"
2. Click en "Run Test" sobre el método

---

## 📊 INTERPRETAR RESULTADOS

### Éxito ✅

```
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Significado**:
- ✅ Todas las pruebas pasaron
- ✅ El código funciona como se espera

### Fallo ❌

```
[ERROR] Failures: 
[ERROR]   ClienteServiceTest.testFindById:45
    Expected :Juan Pérez
    Actual   :null
```

**Significado**:
- ❌ La prueba `testFindById` falló
- ❌ En la línea 45
- ❌ Se esperaba "Juan Pérez" pero se obtuvo null

**Solución**: Revisa el código y arregla el bug

---

## 🎯 BUENAS PRÁCTICAS

### 1. Nombra tests descriptivamente

```java
// ❌ MALO
@Test
void test1() { }

// ✅ BUENO
@Test
@DisplayName("Crear cliente con datos válidos debe guardar en BD")
void testCrearCliente_DatosValidos_DebeGuardar() { }
```

### 2. Un test, un concepto

```java
// ❌ MALO: Un test que prueba TODO
@Test
void testClienteService() {
    // Prueba crear
    // Prueba buscar
    // Prueba actualizar
    // Prueba eliminar
}

// ✅ BUENO: Tests separados
@Test
void testCrearCliente() { }

@Test
void testBuscarCliente() { }

@Test
void testActualizarCliente() { }

@Test
void testEliminarCliente() { }
```

### 3. Tests independientes

Cada test debe poder ejecutarse SOLO, sin depender de otros.

```java
// ❌ MALO: Test2 depende de Test1
@Test
void test1_crearCliente() {
    cliente = service.create(...);
}

@Test
void test2_buscarCliente() {
    // Depende de que test1 se ejecute primero ❌
    result = service.findById(cliente.getId());
}

// ✅ BUENO: Cada test es independiente
@Test
void testCrearCliente() {
    // Mock todo lo necesario aquí
}

@Test
void testBuscarCliente() {
    // Mock todo lo necesario aquí
}
```

### 4. Prueba casos normales Y errores

```java
@Test
void testBuscarCliente_Exitoso() { }

@Test
void testBuscarCliente_NoEncontrado() { }

@Test
void testBuscarCliente_IdNulo() { }
```

---

## 🎓 EJERCICIO PRÁCTICO

### Tarea

Crea un test para este método:

```java
public class CalculadoraService {
    public int sumar(int a, int b) {
        return a + b;
    }
}
```

<details>
<summary>Ver Solución</summary>

```java
@ExtendWith(MockitoExtension.class)
class CalculadoraServiceTest {

    @InjectMocks
    private CalculadoraService calculadora;

    @Test
    @DisplayName("Sumar dos números positivos")
    void testSumar_NumerosPositivos() {
        // ARRANGE
        int a = 5;
        int b = 3;
        
        // ACT
        int resultado = calculadora.sumar(a, b);
        
        // ASSERT
        assertEquals(8, resultado);
    }

    @Test
    @DisplayName("Sumar número positivo y negativo")
    void testSumar_PositivoYNegativo() {
        // ARRANGE
        int a = 10;
        int b = -3;
        
        // ACT
        int resultado = calculadora.sumar(a, b);
        
        // ASSERT
        assertEquals(7, resultado);
    }

    @Test
    @DisplayName("Sumar cero")
    void testSumar_ConCero() {
        // ARRANGE
        int a = 5;
        int b = 0;
        
        // ACT
        int resultado = calculadora.sumar(a, b);
        
        // ASSERT
        assertEquals(5, resultado);
    }
}
```
</details>

---

## 📚 RESUMEN

### ¿Qué aprendiste?

- ✅ Qué son las pruebas unitarias y por qué son importantes
- ✅ Cómo usar JUnit 5 para escribir tests
- ✅ Cómo usar Mockito para crear mocks
- ✅ El patrón AAA (Arrange-Act-Assert)
- ✅ Anotaciones principales: @Test, @Mock, @InjectMocks
- ✅ Métodos de Mockito: when(), verify(), any()
- ✅ Assertions: assertEquals(), assertThrows(), etc.
- ✅ Cómo ejecutar y leer resultados

### Próximos pasos

1. **Ejecuta los tests existentes** en tu proyecto
   ```bash
   mvn test
   ```

2. **Lee los tests** como documentación del código

3. **Crea tus propios tests** para SolicitudPrestamoService

4. **Practica TDD** (Test-Driven Development):
   - Escribe el test PRIMERO
   - Escribe el código que hace pasar el test
   - Refactoriza

---

## 🎯 HOJA DE REFERENCIA RÁPIDA

```java
// Configuración de clase
@ExtendWith(MockitoExtension.class)
class MiTest {
    @Mock
    private MiRepositorio repo;
    
    @InjectMocks
    private MiServicio servicio;
    
    @BeforeEach
    void setUp() {
        // Inicializar datos
    }
}

// Mockito
when(repo.metodo(args)).thenReturn(valor);
when(repo.metodo(args)).thenThrow(excepcion);
verify(repo, times(n)).metodo(args);
verify(repo, never()).metodo(args);
any(Clase.class)
anyString(), anyInt(), anyLong()

// JUnit Assertions
assertEquals(esperado, actual);
assertNotNull(objeto);
assertTrue(condicion);
assertFalse(condicion);
assertThrows(Excepcion.class, () -> { codigo });

// Patrón AAA
@Test
void testAlgo() {
    // ARRANGE: Preparar
    // ACT: Ejecutar
    // ASSERT: Verificar
}
```

---

¡Felicidades! 🎉 Ahora dominas las pruebas unitarias con JUnit y Mockito.
