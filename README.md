# 🍺 API Microservicio Beerhouse - Módulo de Categorías

Microservicio backend desarrollado con **Spring Boot**, conectado a **MySQL** mediante **JDBC (HikariCP)**, que implementa operaciones de consulta paginada y eliminación de categorías, junto con un frontend web integrado en HTML5, CSS3 y JavaScript.

---

## 🚀 Características Principales

- **Arquitectura Limpia en Capas:** Controladores REST, Servicios, Repositorios, DTOs y Entidades.
- **Consultar con Paginación:** `GET /api/categorias?page=0&size=10&sortBy=id_categoria&sortDir=asc`.
- **Consulta por ID:** `GET /api/categorias/{id}`.
- **Eliminar Categoría:** `DELETE /api/categorias/{id}` con validaciones de existencia.
- **Manejo Centralizado de Errores (HTTP 400 y 500):** `@RestControllerAdvice` con formato estándar JSON.
- **Registro de Logs con SLF4J / Logback:** Rotación automática por tamaño (10MB) y tiempo (30 días).
- **CORS Habilitado:** Configuración global para permitir solicitudes externas.
- **Interfaz Gráfica de Usuario (GUI):** Dashboard web responsivo ubicado en `src/main/resources/static/` para gestionar categorías visualmente.

---

## 🛠️ Tecnologías Utilizadas

- **Java 21 (LTS)**
- **Spring Boot 4.2.0** / Spring MVC
- **Spring Data JPA & Hibernate**
- **Pool de Conexiones HikariCP (JDBC)**
- **MySQL Connector/J**
- **Logback & SLF4J**
- **HTML5, CSS3 (CSS Grid & Flexbox) y JavaScript Moderno (Fetch API)**
- **JUnit 5 & Mockito** (14 pruebas unitarias automatizadas)

---

## 🗄️ Configuración de Base de Datos

En `src/main/resources/application.properties`:

- **Host:** `localhost`
- **Puerto:** `3306`
- **Base de Datos:** `beerhouse_db`
- **Usuario:** `root`
- **Contraseña:** *(vacía)*

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/beerhouse_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

---

## 📋 Endpoints de la API

| Método | Endpoint | Descripción | Parámetros Query |
|---|---|---|---|
| `GET` | `/api/categorias` | Consulta categorías con paginación | `page`, `size`, `sortBy`, `sortDir` |
| `GET` | `/api/categorias/{id}` | Consulta una categoría por ID | - |
| `DELETE` | `/api/categorias/{id}` | Elimina una categoría por ID | - |

---

## 🖥️ Interfaz Gráfica

Acceso directo en el navegador al iniciar la aplicación:
```
http://localhost:8080/
```

- Búsqueda en tiempo real por ID.
- Paginación dinámica con selector de tamaño (5, 10, 25, 50).
- Ordenamiento ascendente y descendente por campo.
- Modales para visualización detallada y confirmación de eliminación.
- Alertas *Toast* para respuestas exitosas (200), advertencias (400) y errores (500).

---

## ⚙️ Compilación y Ejecución

### Ejecutar Pruebas Unitarias
```bash
./mvnw clean test
```

### Iniciar el Servidor
```bash
./mvnw spring-boot:run
```

---

## 🚀 Sobre mí

¡Hola! 👋 Soy desarrollador de software enfocado en construir arquitecturas backend sólidas, microservicios escalables y soluciones web integrales con Java, Spring Boot y bases de datos relacionales. 

- 💻 Especialidad: Desarrollo Backend, APIs RESTful, Spring Boot, MySQL y arquitecturas basadas en capas.
- 🎯 Enfoque: Código limpio, buenas prácticas, seguridad, manejo robusto de excepciones y rendimiento.
- 📬 Conéctate conmigo en [GitHub](https://github.com/).
