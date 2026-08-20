# Empresa CS — Sistema de Gestión de Inventario

Aplicación full-stack de gestión de inventario/stock.

- **Backend**: Java 17 + Spring Boot 3.2.3 + PostgreSQL 16 (API REST en `http://localhost:8080/api`).
- **Frontend**: Angular 22 + Angular Material (SPA en `http://localhost:4200`).

---

## Stack

| Capa | Tecnologías |
|------|-------------|
| Backend | Java 17, Spring Boot 3.2.3 (Web, Data JPA, Security), Hibernate, Flyway, JWT (jjwt 0.12.3), Bean Validation, Lombok, Springdoc OpenAPI 2.4.0 |
| Base de datos | PostgreSQL 16 (Docker) |
| Frontend | Angular 22, Angular Material, Signals, RxJS, TypeScript |
| Build / infra | Maven, npm, Docker Compose |

---

## Requisitos previos

- **Java 17+** y **Maven 3.8+** (backend)
- **Node.js 20+** y **Angular CLI 22** (frontend)
- **Docker + Docker Compose** (PostgreSQL)

---

## Puesta en marcha

### 1. Base de datos (Docker)

```bash
cd backend
docker-compose up -d      # PostgreSQL en :5432, PgAdmin en :5050
```

PgAdmin: `http://localhost:5050` (`admin@empresa.com` / `admin`). Conexión al server: host `postgres`, puerto `5432`, usuario/clave `postgres`, base `empresa_cs`.

### 2. Backend

Variables opcionales (crear `backend/.env` a partir de `backend/.env.example`, o exportarlas). En desarrollo hay valores por defecto:

```
JWT_SECRET=un-secreto-de-al-menos-256-bits
MAIL_USERNAME=tu-email@gmail.com   # solo para recuperación de contraseña
MAIL_PASSWORD=tu-app-password
```

```bash
cd backend
mvn clean package          # compilar (agregar -DskipTests para omitir tests)
mvn spring-boot:run        # levantar API
```

- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`

Flyway aplica automáticamente las migraciones `V1`–`V6` al iniciar (`ddl-auto: validate`).

### 3. Frontend

```bash
cd frontend
npm install
npm start                  # ng serve en http://localhost:4200
```

El proxy de desarrollo (`frontend/proxy.conf.json`) redirige `/api` → `http://localhost:8080`, evitando problemas de CORS.

---

## Módulos implementados

### Backend

1. **Usuarios y seguridad** — registro/login, JWT (access 15 min + refresh 30 días), roles `USER`/`ADMIN`, perfil, cambio y recuperación de contraseña, logout.
2. **Estructura de almacenamiento** — Almacén → Zona → Estantería (jerárquico, soft delete, código único por padre).
3. **Productos y stock** — productos (`ENTRADA`/`VENTA`), stock por estantería con bloqueo pesimista, movimientos `ENTRADA`/`TRASLADO`/`AJUSTE`, transformaciones y ventas (`SALIDA_VENTA`), historial de movimientos.

### Frontend

- Autenticación (login, registro, recuperar contraseña) con interceptor de refresh automático en 401.
- Shell autenticado con guardias `authGuard` / `roleGuard`.
- ABM de **Almacenes**, **Zonas** y **Estanterías** (tablas Material, diálogos de alta/edición, controles de escritura solo para ADMIN, manejo específico de errores 409).

---

## Endpoints principales

Todos bajo el prefijo `/api`. Detalle completo e interactivo en Swagger.

| Área | Endpoints |
|------|-----------|
| Auth | `POST /auth/registro`, `/auth/login`, `/auth/refresh`, `/auth/recuperar-contrasena`, `/auth/restablecer-contrasena` |
| Usuarios | `GET/PUT /usuarios/perfil`, `PUT /usuarios/cambiar-contrasena`, `POST /usuarios/logout` |
| Almacenes | `GET/POST /almacenes`, `GET/PUT /almacenes/{id}`, `PATCH /almacenes/{id}/activar` y `/desactivar` |
| Zonas | `GET/POST /almacenes/{almacenId}/zonas`, `GET/PUT /zonas/{id}`, `PATCH /zonas/{id}/activar` y `/desactivar` |
| Estanterías | `GET/POST /zonas/{zonaId}/estanterias`, `GET/PUT /estanterias/{id}`, `PATCH /estanterias/{id}/activar` y `/desactivar` |
| Productos | `GET/POST /productos`, `GET/PUT /productos/{id}`, `PATCH /productos/{id}/activar` y `/desactivar` |
| Stock | `GET /stock`, `GET /stock/producto/{productoId}` |
| Movimientos | `POST /movimientos/entrada`, `/traslado`, `/ajuste`, `GET /movimientos` |
| Transformaciones | `GET/POST /transformaciones`, `GET /transformaciones/{id}` |
| Ventas | `GET/POST /ventas`, `GET /ventas/{id}` |

Formato de respuesta uniforme: `{ exitoso, mensaje, data, timestamp }`. Los errores de validación devuelven `data` como mapa `{ campo: mensaje }`.

---

## Estructura del repositorio

```
empresa-cs/
├── backend/                                     # API Spring Boot
│   ├── src/main/java/com/empresa/inventario/    # controller, service, repository, entity, dto, security, exception
│   ├── src/main/resources/
│   │   ├── application.yml / application-dev.yml
│   │   └── db/migration/                        # V1..V6 (Flyway)
│   ├── src/test/java/...                        # tests unitarios (JUnit + Mockito)
│   ├── docker-compose.yml                       # PostgreSQL + PgAdmin
│   ├── .env.example
│   └── pom.xml
├── frontend/                                    # SPA Angular (ver frontend/README.md)
└── README.md
```

---

## Testing

```bash
cd backend && mvn test   # tests unitarios del backend
cd frontend && ng test   # tests del frontend (Vitest)
```

---

## Troubleshooting

| Problema | Solución |
|----------|----------|
| PostgreSQL no conecta | `docker ps` y, si hace falta, `docker-compose up -d` |
| Puerto 5432/8080 ocupado | Cambiar el puerto en `docker-compose.yml` / `application.yml`, o liberar el proceso con `netstat -ano` + `taskkill /PID <pid> /F` |
| Migración Flyway falla (solo dev) | `docker-compose down -v && docker-compose up -d` para recrear la base |
| CORS al llamar la API desde el front | Usar `npm start` (proxy `/api`), no abrir el build directo contra `:8080` |
| Dependencias Maven no bajan | `mvn dependency:resolve -U` |

---

## Convenciones

- Clases `PascalCase`, métodos `camelCase`, constantes `UPPER_SNAKE_CASE`.
- DTOs de request: `<Entidad><Operación>Request`.
- Servicios transaccionales (`@Transactional`), validación con `@Valid`, roles con `@PreAuthorize`.
- Excepciones mapeadas a HTTP en `GlobalExceptionHandler`.
- Finales de línea normalizados a LF vía `.gitattributes`.

---

Proyecto privado para uso interno de Empresa CS.
