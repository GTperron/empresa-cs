# Empresa CS - Sistema de Gestión de Inventario

Backend de gestión de inventario/stock para negocio genérico, construido con Java 17, Spring Boot 3.x y PostgreSQL.

## 🚀 Quick Start

### Requisitos Previos
- **Java 17+**: [Descargar](https://www.oracle.com/java/technologies/downloads/#java17)
- **Maven 3.8+**: [Descargar](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose**: [Descargar](https://www.docker.com/products/docker-desktop)
- **Git**: [Descargar](https://git-scm.com/downloads)

### Instalación

#### 1. Clonar o descargar el proyecto
```bash
cd c:\Users\gtper\empresa-cs
```

#### 2. Levantar PostgreSQL con Docker
```bash
docker-compose up -d
```

Esto levantará:
- **PostgreSQL**: `localhost:5432`
- **PgAdmin**: `localhost:5050`

**PgAdmin Credenciales:**
- Email: `admin@empresa.com`
- Password: `admin`

Para conectar a PostgreSQL en PgAdmin:
- Host: `postgres`
- Puerto: `5432`
- Usuario: `postgres`
- Contraseña: `postgres`
- Base de datos: `empresa_cs`

#### 3. Configurar variables de entorno (opcional para desarrollo)
Crear archivo `.env` en la raíz del proyecto:
```
JWT_SECRET=tu-secreto-super-seguro-minimo-256-caracteres-aleatorios
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-contraseña-app-gmail
```

#### 4. Compilar el proyecto
```bash
mvn clean package
```

#### 5. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

La API estará disponible en: `http://localhost:8080/api`

Documentación interactiva (Swagger): `http://localhost:8080/api/swagger-ui.html`

---

## 📊 Diagrama de Base de Datos

### Tablas Principales

```
┌─────────────────────┐
│       USUARIO       │
├─────────────────────┤
│ id (PK)             │
│ email (UNIQUE)      │
│ password_hash       │
│ nombre              │
│ apellido            │
│ activo              │
│ ultimo_login        │
│ created_at          │
│ updated_at          │
└─────────────────────┘
         │
         │ M2M
         │
    ┌────┴────┐
    │          │
    ▼          ▼

┌──────────────────────┐    ┌─────────────┐
│   USUARIO_ROL        │    │     ROL     │
├──────────────────────┤    ├─────────────┤
│ usuario_id (PK/FK)   │───▶│ id (PK)     │
│ rol_id (PK/FK)       │    │ nombre (UK) │
└──────────────────────┘    │ descripcion │
                            │ activo      │
                            │ created_at  │
                            │ updated_at  │
                            └─────────────┘

┌──────────────────────────┐
│   REFRESH_TOKEN          │
├──────────────────────────┤
│ id (PK)                  │
│ usuario_id (FK)          │
│ token (UNIQUE)           │
│ expiracion               │
│ revocado                 │
│ created_at               │
└──────────────────────────┘

┌──────────────────────────┐
│ PASSWORD_RESET_TOKEN     │
├──────────────────────────┤
│ id (PK)                  │
│ usuario_id (FK)          │
│ token (UNIQUE)           │
│ expiracion               │
│ utilizado                │
│ created_at               │
└──────────────────────────┘
```

---

## 🔐 Autenticación JWT

### Flujo de Autenticación

```
1. POST /api/auth/registro o /api/auth/login
   Request: { email, password }
   Response: { accessToken, refreshToken, expiresIn, usuario }

2. Guardar tokens en cliente (preferible: sessionStorage para accessToken)

3. En cada request protegido:
   Header: Authorization: Bearer <accessToken>

4. Cuando accessToken expira (15-30 min):
   POST /api/auth/refresh
   Request: { refreshToken }
   Response: { accessToken (nuevo), refreshToken, expiresIn, usuario }

5. Si refreshToken expira o se revoca:
   Redirigir a login
```

### Token Claims (JWT)
- `sub`: Email del usuario
- `id`: ID del usuario en BD
- `roles`: Roles asignados (ej: "ROLE_USER,ROLE_ADMIN")
- `nombre`: Nombre del usuario
- `apellido`: Apellido del usuario
- `iat`: Emitido en (timestamp)
- `exp`: Expira en (timestamp)

---

## 📡 Endpoints Principales (Módulo 1)

### Autenticación

```http
POST /api/auth/registro
Content-Type: application/json

{
  "email": "usuario@ejemplo.com",
  "password": "miContraseña123!",
  "passwordConfirmacion": "miContraseña123!",
  "nombre": "Juan",
  "apellido": "Pérez"
}
```

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "usuario@ejemplo.com",
  "password": "miContraseña123!"
}
```

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "token-aqui"
}
```

### Perfil de Usuario

```http
GET /api/usuarios/perfil
Authorization: Bearer <accessToken>
```

```http
PUT /api/usuarios/perfil
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "nombre": "Juan Carlos",
  "apellido": "Pérez García"
}
```

### Cambiar Contraseña

```http
PUT /api/usuarios/cambiar-contrasena
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "contrasenaActual": "vieja123",
  "nuevaContrasena": "nueva456",
  "confirmacion": "nueva456"
}
```

### Recuperación de Contraseña

```http
POST /api/auth/recuperar-contrasena
Content-Type: application/json

{
  "email": "usuario@ejemplo.com"
}
```

```http
POST /api/auth/restablecer-contrasena
Content-Type: application/json

{
  "token": "token-de-recuperacion",
  "nuevaContrasena": "nueva789",
  "confirmacion": "nueva789"
}
```

### Logout

```http
POST /api/usuarios/logout
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "refreshToken": "token-aqui"
}
```

---

## 🗂️ Estructura del Proyecto

```
empresa-cs/
├── src/
│   ├── main/
│   │   ├── java/com/empresa/inventario/
│   │   │   ├── InventarioApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AutenticacionController.java
│   │   │   │   └── UsuarioController.java
│   │   │   ├── service/
│   │   │   │   ├── AutenticacionService.java
│   │   │   │   ├── UsuarioService.java
│   │   │   │   └── RecuperacionContrasenaService.java
│   │   │   ├── entity/
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Rol.java
│   │   │   │   ├── RefreshToken.java
│   │   │   │   └── PasswordResetToken.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegistroRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── UsuarioDTO.java
│   │   │   │   └── ... más DTOs
│   │   │   ├── repository/
│   │   │   │   ├── UsuarioRepository.java
│   │   │   │   ├── RolRepository.java
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   └── PasswordResetTokenRepository.java
│   │   │   ├── security/
│   │   │   │   ├── JwtProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── UsuarioUserDetails.java
│   │   │   │   └── UsuarioUserDetailsService.java
│   │   │   ├── exception/
│   │   │   │   ├── RecursoNoEncontradoException.java
│   │   │   │   ├── RecursoYaExisteException.java
│   │   │   │   ├── AutenticacionFallidaException.java
│   │   │   │   ├── TokenInvalidoException.java
│   │   │   │   └── AccesoDenegadoException.java
│   │   │   └── validator/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── db/migration/
│   │           ├── V1__Crear_tablas_iniciales.sql
│   │           ├── V2__Crear_tabla_refresh_token.sql
│   │           └── V3__Crear_tabla_password_reset_token.sql
│   └── test/java/
├── pom.xml
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## 🔧 Configuración

### application.yml
Configuración general de la aplicación:
- Perfiles: `dev` (por defecto)
- JPA/Hibernate
- Jackson (JSON)
- Mail (SMTP)
- JWT (secreto, tiempos de expiración)

### application-dev.yml
Configuración específica para desarrollo:
- Conexión local a PostgreSQL
- Logs en DEBUG
- Flyway habilitado

### Variables de Entorno
```bash
# JWT
JWT_SECRET=tu-secreto-super-seguro

# Mail (opcional, para recuperación de contraseña)
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=contraseña-app-gmail
```

---

## 🧪 Testing

Próximamente se agregarán tests unitarios e integración.

```bash
# Ejecutar tests
mvn test

# Tests con cobertura
mvn clean test jacoco:report
```

---

## 📝 Próximos Módulos

- **Módulo 2**: Gestión de Productos (CRUD, búsqueda, filtros)
- **Módulo 3**: Gestión de Stock/Movimientos (entrada, salida, ajustes)
- **Módulo 4**: Categorías, Proveedores, Clientes
- **Módulo 5**: Reportes y Analytics
- **Módulo 6**: Auditoría y Logs

---

## 🐛 Solución de Problemas

### "Connection refused" en PostgreSQL
```bash
# Verificar que Docker está ejecutando
docker ps

# Reiniciar contenedores
docker-compose restart
```

### "JWT Secret not configured"
```bash
# Asegúrate de que application.yml tiene la configuración
# O establece la variable de entorno JWT_SECRET
export JWT_SECRET=tu-secreto-aqui
```

### Error "Flyway migration failed"
```bash
# Limpiar migraciones (solo desarrollo)
docker exec empresa_cs_postgres psql -U postgres -d empresa_cs -c "DROP SCHEMA IF EXISTS flyway_schema_history CASCADE;"
```

---

## 📚 Referencias

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security + JWT](https://spring.io/guides/gs/securing-web/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Flyway Database Migrations](https://flywaydb.org/)
- [JWT.io](https://jwt.io/)
- [Swagger/OpenAPI](https://springdoc.org/)

---

## 📄 Licencia

Este proyecto es privado y solo para uso interno de Empresa CS.

---

## ✍️ Notas de Desarrollo

### Convenciones de Código
- Nombrado de clases: `PascalCase`
- Métodos: `camelCase`
- Constantes: `UPPER_SNAKE_CASE`
- DTOs: `<Entidad><Operación>Request/Response`

### Best Practices
- Siempre usar `@Transactional` en servicios
- Validar entrada con `@Valid` en controladores
- Mapear excepciones a códigos HTTP apropiados
- Documentar endpoints con Swagger

---

**Última actualización**: 2026-07-14
**Versión**: 1.0.0
