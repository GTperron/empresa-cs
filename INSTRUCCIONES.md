# 🚀 Instrucciones para Ejecutar el Proyecto

## Paso 1: Verificar Requisitos Previos

### Windows PowerShell
```powershell
# Verificar Java 17+
java -version

# Verificar Maven
mvn --version

# Verificar Docker
docker --version
docker-compose --version
```

Si alguno falta, instálalo desde:
- **Java 17**: https://www.oracle.com/java/technologies/downloads/#java17
- **Maven**: https://maven.apache.org/download.cgi
- **Docker Desktop**: https://www.docker.com/products/docker-desktop

---

## Paso 2: Levantar PostgreSQL (Docker)

Desde la carpeta del proyecto:

```powershell
# Ir a la carpeta del proyecto
cd c:\Users\gtper\empresa-cs

# Iniciar contenedores (PostgreSQL + PgAdmin)
docker-compose up -d

# Verificar que están ejecutándose
docker ps
```

**Esperar ~15 segundos a que PostgreSQL esté listo.**

### Acceso a PgAdmin
- URL: `http://localhost:5050`
- Email: `admin@empresa.com`
- Password: `admin`

Para conectar PostgreSQL en PgAdmin:
1. Hacer clic en "Add New Server"
2. Nombre: `empresa_cs_local`
3. Host: `postgres`
4. Puerto: `5432`
5. Usuario: `postgres`
6. Contraseña: `postgres`
7. Database: `empresa_cs`

---

## Paso 3: Compilar el Proyecto

```powershell
# Limpiar y compilar
mvn clean package

# O solo compilar sin tests
mvn clean package -DskipTests

# Solo compilar
mvn compile
```

**Esperar ~1-2 minutos (descargará dependencias la primera vez).**

---

## Paso 4: Ejecutar la Aplicación

### Opción A: Con Maven
```powershell
mvn spring-boot:run
```

### Opción B: Ejecutar JAR
```powershell
# Primero compilar
mvn clean package -DskipTests

# Luego ejecutar
java -jar target/inventario-1.0.0.jar
```

### Opción C: En VSCode/IntelliJ
1. Abrir la clase `InventarioApplication.java`
2. Hacer clic en "Run" (o Shift + F10)

**La aplicación debería iniciar en ~20-30 segundos.**

---

## Paso 5: Verificar que Todo Funciona

### API Status
```bash
# En PowerShell
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"email":"test@example.com","password":"test123"}' | ConvertTo-Json
```

O usar Postman/Insomnia.

### Swagger/OpenAPI
```
http://localhost:8080/api/swagger-ui.html
```

---

## 📝 Primer Flujo de Prueba (Swagger)

### 1. Registrar usuario
```
POST /api/auth/registro

{
  "email": "juan@ejemplo.com",
  "password": "MiContra123!",
  "passwordConfirmacion": "MiContra123!",
  "nombre": "Juan",
  "apellido": "Pérez"
}
```

**Response:**
```json
{
  "exitoso": true,
  "mensaje": "Usuario registrado exitosamente",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716...",
    "expiresIn": 900,
    "usuario": {
      "id": 1,
      "email": "juan@ejemplo.com",
      "nombre": "Juan",
      "apellido": "Pérez",
      "activo": true,
      "roles": ["USER"]
    }
  }
}
```

### 2. Guardar el `accessToken` en Swagger
- Hacer clic en el botón de "Authorize" (candado arriba a la derecha)
- Pegar: `Bearer {accessToken}`
- Hacer clic en "Authorize"

### 3. Obtener perfil
```
GET /api/usuarios/perfil
```

*(Ahora incluye automáticamente el Authorization header)*

### 4. Editar perfil
```
PUT /api/usuarios/perfil

{
  "nombre": "Juan Carlos",
  "apellido": "García Pérez"
}
```

### 5. Refrescar token (cuando accesToken expira)
```
POST /api/auth/refresh

{
  "refreshToken": "{el-refreshToken-del-paso-1}"
}
```

---

## 🛑 Parar la Aplicación

```powershell
# En PowerShell: Presionar Ctrl + C

# O detener Docker
docker-compose down

# Si deseas eliminar volúmenes también (perderás datos)
docker-compose down -v
```

---

## 🐛 Solución de Problemas

### Error: "Connection refused" PostgreSQL
```powershell
# Verificar que Docker está corriendo
docker ps

# Si no aparecen contenedores, reiniciar
docker-compose up -d

# Ver logs
docker-compose logs postgres
```

### Error: "Port 5432 already in use"
```powershell
# Cambiar puerto en docker-compose.yml
# Cambiar "5432:5432" a "5433:5432"
# O matar el proceso en ese puerto:

# Encontrar proceso
netstat -ano | findstr :5432

# Matar proceso (reemplazar PID)
taskkill /PID 1234 /F
```

### Error: "Port 8080 already in use"
```powershell
# En application.yml cambiar:
server:
  port: 8081

# O matar proceso
netstat -ano | findstr :8080
taskkill /PID 1234 /F
```

### Error: "JWT_SECRET not configured"
```powershell
# En application.yml ya tiene un valor por defecto
# Pero para producción, establecer variable de entorno:
$env:JWT_SECRET="tu-secreto-muy-largo-y-seguro"

# O agregar a .env en la raíz del proyecto
```

### Error: "Flyway migration failed"
```powershell
# Limpiar schema de flyway (solo en desarrollo!)
docker exec empresa_cs_postgres psql -U postgres -d empresa_cs -c "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public;"

# Luego reintentar
mvn spring-boot:run
```

### Error: "Dependency download fails"
```powershell
# Limpiar caché de Maven
mvn clean

# Actualizar dependencias
mvn dependency:resolve -U

# Reintentar compilación
mvn package -DskipTests
```

---

## 📦 Estructura de Carpetas

Después de ejecutar, deberías ver:

```
c:\Users\gtper\empresa-cs\
├── src/main/java/com/empresa/inventario/
├── src/main/resources/
│   ├── db/migration/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
├── target/
│   └── inventario-1.0.0.jar
├── pom.xml
├── docker-compose.yml
├── README.md
└── INSTRUCCIONES.md (este archivo)
```

---

## 🎯 Próximos Pasos

1. ✓ Proyecto configurado y ejecutando
2. → Probar endpoints con Swagger
3. → Implementar Módulo 2 (Productos)
4. → Agregar tests unitarios
5. → Documentar API adicional

---

## 💡 Consejos

- **Siempre** mantener Docker corriendo mientras desarrollas
- Guardar los tokens en variables (Postman/Insomnia) para ahorrar tiempo
- Revisar logs en la consola para debugging
- Usar `application-dev.yml` para desarrollo local
- Cambiar `JWT_SECRET` en producción

---

## 📞 Ayuda Rápida

| Problema | Solución |
|----------|----------|
| BD no conecta | `docker-compose up -d && docker ps` |
| Token expirado | Usar endpoint `/refresh` |
| Permisos denegados | Verificar que usuario tiene rol ADMIN |
| Port 8080 ocupado | Cambiar `server.port` o matar proceso |
| Migraciones fallan | `docker-compose down -v && docker-compose up -d` |

---

**¡Todo listo! ¡A desarrollar! 🚀**
