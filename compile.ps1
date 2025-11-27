# Script de compilación para EDUEXCE_MOVIL_MOVIL
# Busca Java automáticamente y compila el proyecto

Write-Host "🔍 Buscando instalación de Java..." -ForegroundColor Cyan

# Rutas comunes de Java
$javaPaths = @(
    "C:\Program Files\Android\Android Studio\jbr",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\jdk-11",
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot",
    "$env:LOCALAPPDATA\Android\Sdk\jre"
)

$javaHome = $null
foreach ($path in $javaPaths) {
    if (Test-Path "$path\bin\java.exe") {
        $javaHome = $path
        Write-Host "✅ Java encontrado en: $javaHome" -ForegroundColor Green
        break
    }
}

if ($null -eq $javaHome) {
    Write-Host "❌ No se encontró Java. Por favor, instala JDK 11 o superior." -ForegroundColor Red
    exit 1
}

$env:JAVA_HOME = $javaHome

Write-Host "🧹 Limpiando proyecto..." -ForegroundColor Yellow
.\gradlew clean

Write-Host "🔨 Compilando APK Debug..." -ForegroundColor Yellow
.\gradlew assembleDebug --warning-mode all

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ ¡Compilación exitosa!" -ForegroundColor Green
    Write-Host "📦 APK generado en: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Error en la compilación. Revisa los logs arriba." -ForegroundColor Red
    exit 1
}

