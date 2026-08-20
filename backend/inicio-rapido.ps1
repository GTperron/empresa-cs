#!/usr/bin/env pwsh

# ============================================
# SCRIPT DE INICIO RÁPIDO - Empresa CS
# ============================================
# Uso: pwsh inicio-rapido.ps1

$ErrorActionPreference = "Stop"

Write-Host "`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║      SISTEMA DE GESTIÓN DE INVENTARIO - EMPRESA CS              ║" -ForegroundColor Cyan
Write-Host "║               Script de Inicio Rápido v1.0.0                    ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# ============================================
# VERIFICAR REQUISITOS
# ============================================
Write-Host "🔍 Verificando requisitos previos..." -ForegroundColor Yellow

$requirementsOk = $true

# Java
Write-Host "   Verificando Java 17+..." -NoNewline
try {
    $javaVersion = java -version 2>&1
    if ($javaVersion -match "17|18|19|20|21") {
        Write-Host " ✅" -ForegroundColor Green
    } else {
        Write-Host " ⚠️  (Versión < 17)" -ForegroundColor Yellow
        $requirementsOk = $false
    }
} catch {
    Write-Host " ❌ NO INSTALADO" -ForegroundColor Red
    $requirementsOk = $false
}

# Maven
Write-Host "   Verificando Maven..." -NoNewline
try {
    mvn --version | Out-Null
    Write-Host " ✅" -ForegroundColor Green
} catch {
    Write-Host " ❌ NO INSTALADO" -ForegroundColor Red
    $requirementsOk = $false
}

# Docker
Write-Host "   Verificando Docker..." -NoNewline
try {
    docker --version | Out-Null
    Write-Host " ✅" -ForegroundColor Green
} catch {
    Write-Host " ❌ NO INSTALADO" -ForegroundColor Red
    $requirementsOk = $false
}

# Docker Compose
Write-Host "   Verificando Docker Compose..." -NoNewline
try {
    docker-compose --version | Out-Null
    Write-Host " ✅" -ForegroundColor Green
} catch {
    Write-Host " ❌ NO INSTALADO" -ForegroundColor Red
    $requirementsOk = $false
}

if (-not $requirementsOk) {
    Write-Host "`n❌ Faltan requisitos. Por favor instala los faltantes." -ForegroundColor Red
    Write-Host "   Java 17+: https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Gray
    Write-Host "   Maven: https://maven.apache.org/download.cgi" -ForegroundColor Gray
    Write-Host "   Docker Desktop: https://www.docker.com/products/docker-desktop" -ForegroundColor Gray
    exit 1
}

Write-Host "`n✅ Todos los requisitos están instalados`n" -ForegroundColor Green

# ============================================
# SELECCIONAR ACCIÓN
# ============================================
Write-Host "📋 ¿Qué deseas hacer?" -ForegroundColor Cyan
Write-Host ""
Write-Host "  1) Inicio rápido (Docker + Compilar + Ejecutar)"
Write-Host "  2) Solo iniciar Docker"
Write-Host "  3) Compilar y ejecutar"
Write-Host "  4) Limpiar y reinstalar (limpia BD)"
Write-Host "  5) Ver documentación"
Write-Host "  6) Ver logs de Docker"
Write-Host "  0) Salir"
Write-Host ""

$opcion = Read-Host "Ingresa una opción (0-6)"

# ============================================
# EJECUTAR ACCIÓN
# ============================================

switch ($opcion) {
    "1" {
        Write-Host "`n🚀 Iniciando aplicación (modo completo)...`n" -ForegroundColor Cyan
        
        # Docker
        Write-Host "📦 Iniciando PostgreSQL + PgAdmin..." -ForegroundColor Yellow
        docker-compose up -d
        Write-Host "⏳ Esperando a que PostgreSQL esté listo..." -NoNewline
        Start-Sleep -Seconds 15
        Write-Host " ✅`n" -ForegroundColor Green
        
        # Compilar
        Write-Host "🔨 Compilando proyecto..." -ForegroundColor Yellow
        mvn clean package -DskipTests
        Write-Host ""
        
        # Ejecutar
        Write-Host "🎯 Iniciando aplicación..." -ForegroundColor Cyan
        Write-Host "   API: http://localhost:8080/api" -ForegroundColor Gray
        Write-Host "   Swagger: http://localhost:8080/api/swagger-ui.html" -ForegroundColor Gray
        Write-Host "   PgAdmin: http://localhost:5050 (admin@empresa.com / admin)" -ForegroundColor Gray
        Write-Host "`n   Presiona Ctrl+C para detener`n" -ForegroundColor Yellow
        
        mvn spring-boot:run
    }
    
    "2" {
        Write-Host "`n📦 Iniciando Docker...`n" -ForegroundColor Cyan
        docker-compose up -d
        Write-Host "`n✅ Contenedores iniciados" -ForegroundColor Green
        Write-Host "   PostgreSQL: localhost:5432" -ForegroundColor Gray
        Write-Host "   PgAdmin: http://localhost:5050" -ForegroundColor Gray
        Write-Host "`n   Detener con: docker-compose down" -ForegroundColor Gray
    }
    
    "3" {
        Write-Host "`n🔨 Compilando y ejecutando...`n" -ForegroundColor Cyan
        Write-Host "   Asegúrate de que PostgreSQL está corriendo (opción 2)" -ForegroundColor Yellow
        Write-Host "`n"
        mvn spring-boot:run
    }
    
    "4" {
        Write-Host "`n⚠️  Advertencia: Esto eliminará TODOS los datos de la BD" -ForegroundColor Red
        $confirm = Read-Host "¿Estás seguro? (escriba 'si' para continuar)"
        
        if ($confirm -eq "si") {
            Write-Host "`n🧹 Limpiando y reinstalando...`n" -ForegroundColor Yellow
            
            Write-Host "   Deteniendo Docker..." -NoNewline
            docker-compose down -v
            Write-Host " ✅" -ForegroundColor Green
            
            Write-Host "   Limpiando Maven..." -NoNewline
            mvn clean | Out-Null
            Write-Host " ✅" -ForegroundColor Green
            
            Write-Host "   Iniciando PostgreSQL..." -NoNewline
            docker-compose up -d
            Write-Host " ✅" -ForegroundColor Green
            
            Write-Host "   Compilando..." 
            mvn clean package -DskipTests
            
            Write-Host "`n✅ Limpieza completada. Base de datos lista." -ForegroundColor Green
            Write-Host "   Ejecuta: mvn spring-boot:run" -ForegroundColor Gray
        } else {
            Write-Host "`n❌ Operación cancelada" -ForegroundColor Yellow
        }
    }
    
    "5" {
        Write-Host "`n📖 Documentación disponible:`n" -ForegroundColor Cyan
        Write-Host "   README.md (en la raíz del repo) - Guía completa del proyecto" -ForegroundColor Gray
        Write-Host ""

        $mostrar = Read-Host "¿Deseas abrir el README? (si/no)"

        if ($mostrar.ToLower() -eq "si") { notepad ..\README.md }
        else { Write-Host "OK" }
    }
    
    "6" {
        Write-Host "`n📊 Mostrando logs de Docker (Ctrl+C para salir)...`n" -ForegroundColor Cyan
        docker-compose logs -f
    }
    
    "0" {
        Write-Host "`n👋 Hasta luego!`n" -ForegroundColor Green
        exit 0
    }
    
    default {
        Write-Host "`n❌ Opción no válida" -ForegroundColor Red
        exit 1
    }
}
