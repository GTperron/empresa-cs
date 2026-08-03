# Scripts de Utilidad para Desarrollo

## PowerShell Scripts para tareas comunes

### 🚀 Iniciar proyecto rápidamente

```powershell
# build-and-run.ps1

# Parar Docker antiguo (si está corriendo)
Write-Host "Deteniendo contenedores anteriores..."
docker-compose down

# Iniciar Docker
Write-Host "Iniciando PostgreSQL + PgAdmin..."
docker-compose up -d

# Esperar a que PostgreSQL esté listo
Write-Host "Esperando a que PostgreSQL inicie..."
Start-Sleep -Seconds 15

# Compilar
Write-Host "Compilando proyecto..."
mvn clean package -DskipTests

# Ejecutar
Write-Host "Iniciando aplicación..."
mvn spring-boot:run
```

### 🔧 Limpiar y reiniciar base de datos

```powershell
# reset-database.ps1

Write-Host "Deteniendo contenedores..."
docker-compose down -v

Write-Host "Limpiando caché de Maven..."
mvn clean

Write-Host "Iniciando PostgreSQL..."
docker-compose up -d

Write-Host "Esperando a que PostgreSQL inicie..."
Start-Sleep -Seconds 15

Write-Host "Recompilando con migraciones..."
mvn clean package -DskipTests

Write-Host "Base de datos lista. Ejecuta: mvn spring-boot:run"
```

### 📊 Ver logs de Docker

```powershell
# logs.ps1

# PostgreSQL
Write-Host "Logs de PostgreSQL:"
docker-compose logs -f postgres

# O PgAdmin
Write-Host "Logs de PgAdmin:"
docker-compose logs -f pgadmin

# O todos
docker-compose logs -f
```

### 🧪 Ejecutar tests

```powershell
# run-tests.ps1

Write-Host "Ejecutando tests..."
mvn clean test

Write-Host "Tests completados. Ver target/surefire-reports/"
```

### 🔍 Verificar estado de servicios

```powershell
# check-status.ps1

Write-Host "=== ESTADO DE CONTENEDORES ==="
docker ps -a

Write-Host "`n=== VERIFICANDO POSTGRESQL ==="
try {
    docker exec empresa_cs_postgres pg_isready -U postgres -d empresa_cs
    Write-Host "✓ PostgreSQL respondiendo correctamente"
} catch {
    Write-Host "✗ PostgreSQL NO responde"
}

Write-Host "`n=== VERIFICANDO API ==="
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v3/api-docs" -ErrorAction Stop
    Write-Host "✓ API respondiendo en puerto 8080"
    Write-Host "  Swagger: http://localhost:8080/api/swagger-ui.html"
} catch {
    Write-Host "✗ API NO responde en puerto 8080"
}

Write-Host "`n=== VERIFICANDO PGADMIN ==="
try {
    $response = Invoke-WebRequest -Uri "http://localhost:5050" -ErrorAction Stop
    Write-Host "✓ PgAdmin accesible en puerto 5050"
} catch {
    Write-Host "✗ PgAdmin NO accesible"
}
```

### 🧹 Limpiar todo (nuclear)

```powershell
# nuclear-clean.ps1

Write-Host "⚠️  ADVERTENCIA: Esto eliminará TODOS los datos"
$confirm = Read-Host "¿Estás seguro? (s/n)"

if ($confirm -eq "s") {
    Write-Host "Eliminando contenedores y volúmenes..."
    docker-compose down -v
    
    Write-Host "Limpiando caché de Maven..."
    mvn clean
    
    Write-Host "Limpiando carpeta target..."
    Remove-Item -Path "./target" -Recurse -Force -ErrorAction SilentlyContinue
    
    Write-Host "✓ Limpieza completada"
} else {
    Write-Host "Operación cancelada"
}
```

---

## Comandos individuales útiles

### Maven

```powershell
# Compilar sin tests
mvn clean package -DskipTests

# Solo compilar
mvn compile

# Ejecutar aplicación
mvn spring-boot:run

# Ejecutar tests
mvn test

# Ver dependencias
mvn dependency:tree

# Limpiar caché
mvn clean
```

### Docker

```powershell
# Ver contenedores
docker ps -a

# Ver logs
docker-compose logs -f

# Conectar a BD
docker exec -it empresa_cs_postgres psql -U postgres -d empresa_cs

# Detener todo
docker-compose down

# Detener y limpiar
docker-compose down -v
```

### Java/Spring Boot

```powershell
# Ejecutar JAR directamente
java -jar target/inventario-1.0.0.jar

# Con perfil específico
java -Dspring.profiles.active=dev -jar target/inventario-1.0.0.jar

# Ver versión de Java
java -version

# Ver variables de entorno
$env:JAVA_HOME
```

---

## Curl/PowerShell - Probar API sin Swagger

### Registro

```powershell
$body = @{
    email = "test@ejemplo.com"
    password = "MiContra123!"
    passwordConfirmacion = "MiContra123!"
    nombre = "Juan"
    apellido = "Pérez"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/auth/registro" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $body | ConvertTo-Json
```

### Login

```powershell
$body = @{
    email = "test@ejemplo.com"
    password = "MiContra123!"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $body

$json = $response.Content | ConvertFrom-Json
Write-Host $json | ConvertTo-Json
```

### Obtener Perfil (con token)

```powershell
# Primero, hacer login y guardar accessToken de la respuesta
$accessToken = "eyJhbGciOiJIUzUxMiJ9..."

Invoke-WebRequest -Uri "http://localhost:8080/api/usuarios/perfil" `
    -Method GET `
    -Headers @{
        "Authorization" = "Bearer $accessToken"
        "Content-Type" = "application/json"
    } | ConvertTo-Json
```

---

## Configuración de Visual Studio Code

### .vscode/launch.json

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Spring Boot App",
            "request": "launch",
            "cwd": "${workspaceFolder}",
            "mainClass": "com.empresa.inventario.InventarioApplication",
            "projectName": "inventario",
            "args": "",
            "console": "integratedTerminal"
        }
    ]
}
```

### .vscode/settings.json

```json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.home": "C:\\Program Files\\Java\\jdk-17",
    "editor.formatOnSave": true,
    "[java]": {
        "editor.defaultFormatter": "redhat.java",
        "editor.formatOnSave": true
    }
}
```

---

## Configuración de IntelliJ IDEA

1. **Project Structure** → SDK: JDK 17
2. **Run** → Edit Configurations → Add Maven
   - Command: `spring-boot:run`
   - Working directory: `$PROJECT_DIR$`
3. **Maven** → enable "Skip Tests by Default"

---

## Variables de Entorno Útiles

```powershell
# Establecer para toda la sesión PowerShell

# JWT
$env:JWT_SECRET="tu-secreto-super-seguro-con-256-bits-minimo"

# Mail
$env:MAIL_USERNAME="tu-email@gmail.com"
$env:MAIL_PASSWORD="tu-contraseña-app"

# Database (si cambias docker-compose)
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="empresa_cs"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"

# Spring profile
$env:SPRING_PROFILES_ACTIVE="dev"
```

---

## Troubleshooting Rápido

```powershell
# Restart everything
docker-compose restart
mvn clean
mvn spring-boot:run

# Check ports
netstat -ano | findstr :8080
netstat -ano | findstr :5432
netstat -ano | findstr :5050

# Kill process on port
taskkill /PID 1234 /F

# See Maven dependency tree
mvn dependency:tree | less

# Force update dependencies
mvn dependency:resolve -U
```

---

**Último update**: 2026-07-14
