# Guía y Documentación de Arquitectura - API Microservicio Beerhouse (Categorías)

Este documento describe la arquitectura, configuración, estructura de paquetes y endpoints del microservicio Spring Boot desarrollado para la gestión de **Categorías**, conectado a base de datos **MySQL** vía **JDBC (HikariCP)**.

---

## 1. Información General del Proyecto

- **Nombre del Proyecto:** API Microservicio Beerhouse (`api`)
- **Framework:** Spring Boot 4.2.0 (Spring Framework 7.x)
- **Lenguaje:** Java 21 (LTS)
- **Manejador de Dependencias:** Maven
- **Driver de Base de Datos:** MySQL Connector/J 9.x
- **Capa de Persistencia:** Spring Data JPA / Hibernate con Pool de Conexiones HikariCP (JDBC)
- **Seguridad / Acceso:** Configuración global de CORS para permitir solicitudes externas.
- **Control de Logs:** Logback con archivos rotativos (`logs/beerhouse-api.log`).

---

## 2. Configuración de Base de Datos (MySQL)

Conexión configurada en `src/main/resources/application.properties`:

- **Host:** `localhost`
- **Puerto:** `3306`
- **Base de Datos:** `beerhouse_db` (creada automáticamente si no existe)
- **Usuario:** `root`
- **Contraseña:** *(vacía/sin contraseña)*
- **Pool de Conexiones (JDBC / HikariCP):**
  - Nombre: `BeerhouseHikariCP`
  - Maximum Pool Size: `10`
  - Minimum Idle: `5`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/beerhouse_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.datasource.hikari.pool-name=BeerhouseHikariCP
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

---

## 3. Estructura de Paquetes y Clases

Basado en la clase existente `com.microserviciobeerhouse.api.model.categoria`, se construyeron los paquetes siguiendo la arquitectura limpia en capas:

```
src/main/java/com/microserviciobeerhouse/api/
├── ApiApplication.java                       # Clase principal de arranque de Spring Boot
├── config/
│   └── CorsConfig.java                       # Configuración global de CORS (WebMvcConfigurer)
├── controller/
│   └── CategoriaController.java              # @RestController con endpoints de consulta y eliminación
├── dto/
│   ├── ApiResponseDto.java                   # Envoltorio genérico estándar para respuestas HTTP exitosas
│   ├── ErrorResponseDto.java                 # Formato estructurado para respuestas de error (400 y 500)
│   └── PaginatedResponseDto.java             # DTO para transporte limpio de datos paginados
├── exception/
│   ├── BadRequestException.java              # Excepción personalizada para errores 400
│   ├── ResourceNotFoundException.java        # Excepción personalizada para recursos no encontrados (400)
│   └── GlobalExceptionHandler.java           # @RestControllerAdvice para control centralizado de errores
├── model/
│   └── categoria.java                        # Entidad JPA asignada a la tabla 'categoria'
├── repository/
│   └── CategoriaRepository.java              # Repositorio JPA para operaciones CRUD y paginación
└── service/
    ├── ICategoriaService.java                # Interfaz de lógica de negocio
    └── impl/
        └── CategoriaServiceImpl.java         # Implementación con manejo try/catch y registro de logs
```

---

## 4. Endpoints de la API REST (`/api/categorias`)

El controlador `CategoriaController` implementa la anotación `@RestController` y expone los siguientes métodos:

### 4.1. Consultar Categorías con Paginación
- **Método HTTP:** `GET`
- **Ruta:** `/api/categorias`
- **Parámetros Query (opcionales):**
  - `page` (int, default: `0`): Número de página (base 0).
  - `size` (int, default: `10`): Cantidad de elementos por página (entre 1 y 100).
  - `sortBy` (String, default: `id_categoria`): Campo de ordenación.
  - `sortDir` (String, default: `asc`): Dirección del ordenamiento (`asc` o `desc`).
- **Respuesta Exitosa (200 OK):**
```json
{
  "timestamp": "2026-11-23T16:00:58.123",
  "status": 200,
  "message": "Categorías consultadas exitosamente con paginación",
  "data": {
    "content": [
      {
        "id_categoria": 1,
        "nombre_categoria": "Cerveza Rubia",
        "descripcion": "Cerveza artesanal ligera",
        "imagen": "https://example.com/rubia.png",
        "imagen_public_id": "pub_01"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "isFirst": true,
    "isLast": true,
    "isEmpty": false
  }
}
```

### 4.2. Consultar Categoría por ID
- **Método HTTP:** `GET`
- **Ruta:** `/api/categorias/{id}`
- **Respuesta Exitosa (200 OK):**
```json
{
  "timestamp": "2026-11-23T16:00:58.123",
  "status": 200,
  "message": "Categoría encontrada con éxito",
  "data": {
    "id_categoria": 1,
    "nombre_categoria": "Cerveza Rubia",
    "descripcion": "Cerveza artesanal ligera",
    "imagen": "https://example.com/rubia.png",
    "imagen_public_id": "pub_01"
  }
}
```

### 4.3. Eliminar Categoría
- **Método HTTP:** `DELETE`
- **Ruta:** `/api/categorias/{id}`
- **Respuesta Exitosa (200 OK):**
```json
{
  "timestamp": "2026-11-23T16:00:58.123",
  "status": 200,
  "message": "Categoría eliminada exitosamente",
  "data": "La categoría con ID 1 ha sido eliminada del sistema."
}
```

---

## 5. Manejo de Errores y Códigos de Estado (400 y 500)

Se implementó el componente `GlobalExceptionHandler` (`@RestControllerAdvice`) para interceptar y estandarizar las respuestas ante situaciones anómalas:

### 5.1. Código de Estado 400 - Solicitud Incorrecta (Bad Request)
Se dispara ante:
- Número de página menor a 0.
- Tamaño de página menor a 1 o mayor a 100.
- ID de categoría menor o igual a 0.
- Intento de eliminar o consultar una categoría inexistente.
- Tipos de datos incompatibles en la URL (ej: enviar texto en lugar de un entero).

**Ejemplo de Respuesta 400:**
```json
{
  "timestamp": "2026-11-23T16:00:59.315",
  "status": 400,
  "error": "Bad Request",
  "message": "El número de página no puede ser menor a cero",
  "path": "/api/categorias"
}
```

### 5.2. Código de Estado 500 - Error Interno del Servidor (Internal Server Error)
Se dispara ante fallos de base de datos, caídas de conexión o excepciones imprevistas en el sistema.

**Ejemplo de Respuesta 500:**
```json
{
  "timestamp": "2026-11-23T16:00:59.500",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Ocurrió un error interno en el servidor: Fallo crítico de conexión",
  "path": "/api/categorias"
}
```

---

## 6. Registro de Errores con Try-Catch y Logs

Cada método en la capa de servicios (`CategoriaServiceImpl`) y controladores (`CategoriaController`) implementa bloques `try-catch` con registro pormenorizado mediante **SLF4J**:

```java
try {
    // Lógica y validaciones
    log.info("Petición procesada con éxito");
} catch (BadRequestException e) {
    log.warn("Validación 400 detectada: {}", e.getMessage());
    throw e;
} catch (Exception e) {
    log.error("Fallo crítico 500: {}", e.getMessage(), e);
    throw new RuntimeException("Error interno: " + e.getMessage(), e);
}
```

---

## 7. Gestión de Archivos de Log del Sistema

Se configuró un sistema robusto mediante `logback-spring.xml` y `application.properties`:

- **Ubicación de logs:** Directorio `logs/` en la raíz del proyecto.
- **Log General:** `logs/beerhouse-api.log` (registra trazas de niveles DEBUG, INFO, WARN, ERROR).
- **Log de Errores:** `logs/beerhouse-error.log` (filtro exclusivo para nivel ERROR).
- **Política de Rotación:**
  - Rotación diaria y por tamaño (`SizeAndTimeBasedRollingPolicy`).
  - Tamaño máximo por archivo individual: `10MB`.
  - Archivos históricos comprimidos: `logs/archived/beerhouse-api-%d{yyyy-MM-dd}.%i.log.gz`.
  - Histórico retenido: `30` días.
  - Límite total acumulado: `1GB`.

---

## 8. Configuración de CORS para Peticiones Externas

Para permitir peticiones desde aplicaciones externas (Postman, aplicaciones móviles, frontends en Angular/React/Vue), se configuraron dos niveles:

1. **Nivel Global (`CorsConfig.java`):**
   - Mapeo: `/**`
   - Orígenes permitidos: `*` (`allowedOriginPatterns("*")`)
   - Métodos permitidos: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`, `HEAD`
   - Cabeceras permitidas: `*`
   - Soporte de credenciales: `true`
   - Tiempo de caché preflight: `3600` segundos

2. **Nivel Controlador (`@CrossOrigin`):**
   - Declarado explícitamente en la cabecera de `CategoriaController`.

---

## 9. Instrucciones de Compilación y Ejecución

### Compilar y Ejecutar Pruebas Unitarias
```bash
./mvnw clean test
```

### Ejecutar la Aplicación Spring Boot
```bash
./mvnw spring-boot:run
```
El servidor iniciará en el puerto `http://localhost:8080`.

---

## 10. Interfaz Gráfica de Usuario (Frontend en `src/main/resources/static`)

Se construyó una interfaz web moderna, responsiva y conectada directamente a los endpoints del `CategoriaController`:

- **Ubicación:** `src/main/resources/static/`
  - `index.html`: Dashboard principal con vista de categorías, barra de búsqueda por ID y controles de paginación.
  - `css/styles.css`: Estilos modernos con paleta Beerhouse (ámbar, slate oscuro), estados hover, modales y animaciones.
  - `js/app.js`: Lógica del cliente con consumo `fetch` asíncrono para paginación (`GET /api/categorias`), búsqueda por ID (`GET /api/categorias/{id}`) y eliminación con modal de confirmación (`DELETE /api/categorias/{id}`).
- **Manejo Visual de Errores (400 y 500):** Notificaciones Toast flotantes que capturan y muestran los mensajes exactos del `ErrorResponseDto`.
- **Acceso:** Al arrancar la aplicación, abrir el navegador en `http://localhost:8080/`.

