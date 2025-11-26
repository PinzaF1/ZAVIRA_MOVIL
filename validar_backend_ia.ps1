# 🧪 SCRIPT DE VALIDACIÓN - Backend OpenAI/IA
# Validar que el backend está generando preguntas con OpenAI correctamente

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🧪 VALIDACIÓN BACKEND - OPENAI/IA" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Configuración
$backendUrl = "https://churnable-nimbly-norbert.ngrok-free.dev"
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2wiOiJlc3R1ZGlhbnRlIiwiaWRfdXN1YXJpbyI6IjMyNSIsImlkX2luc3RpdHVjaW9uIjoiMiIsImlhdCI6MTc2NDE5NTI4MiwiZXhwIjoxNzY0MjgxNjgyfQ.-NPsW3ZeAa9hdpoXiB9g9QBs5H07nMHixgL-UJHkgsY"

# Headers
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
    "Accept" = "application/json"
    "ngrok-skip-browser-warning" = "true"
}

# Casos de prueba
$testCases = @(
    @{
        Nombre = "Prueba 1: Inglés - Verb to be"
        Body = @{
            area = "Ingles"
            subtema = "Verb to be (am, is, are)"
            nivel_orden = 1
            usa_estilo_kolb = $true
            intento_actual = 1
        }
    },
    @{
        Nombre = "Prueba 2: Matemáticas - Números enteros"
        Body = @{
            area = "Matematicas"
            subtema = "Operaciones con números enteros"
            nivel_orden = 1
            usa_estilo_kolb = $true
            intento_actual = 1
        }
    },
    @{
        Nombre = "Prueba 3: Ciencias - Indagación científica"
        Body = @{
            area = "Ciencias"
            subtema = "Indagación científica (variables, control e interpretación de datos)"
            nivel_orden = 1
            usa_estilo_kolb = $true
            intento_actual = 1
        }
    }
)

$totalTests = $testCases.Count
$passedTests = 0
$failedTests = 0

foreach ($test in $testCases) {
    Write-Host "----------------------------------------" -ForegroundColor Yellow
    Write-Host "📝 $($test.Nombre)" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Yellow

    $body = $test.Body | ConvertTo-Json

    try {
        Write-Host "📤 Enviando request..." -ForegroundColor Gray

        # Hacer request
        $response = Invoke-RestMethod -Uri "$backendUrl/sesion/parada" `
            -Method Post `
            -Headers $headers `
            -Body $body `
            -TimeoutSec 30

        # Analizar respuesta
        $preguntas = $response.preguntas
        $totalPreguntas = $preguntas.Count

        Write-Host "✅ Respuesta recibida: $totalPreguntas preguntas" -ForegroundColor Green

        # Verificar que todas tienen id_pregunta = null
        $todasConNull = $true
        $preguntasIA = 0
        $preguntasBanco = 0

        foreach ($pregunta in $preguntas) {
            if ($pregunta.id_pregunta -eq $null) {
                $preguntasIA++
            } else {
                $preguntasBanco++
                $todasConNull = $false
            }
        }

        Write-Host "`n📊 Resultados:" -ForegroundColor Cyan
        Write-Host "  • Total preguntas: $totalPreguntas" -ForegroundColor White
        Write-Host "  • Preguntas IA (id_pregunta=null): $preguntasIA" -ForegroundColor $(if ($preguntasIA -eq $totalPreguntas) { "Green" } else { "Yellow" })
        Write-Host "  • Preguntas Banco (id_pregunta=número): $preguntasBanco" -ForegroundColor $(if ($preguntasBanco -eq 0) { "Green" } else { "Red" })

        # Mostrar primera pregunta como ejemplo
        if ($totalPreguntas -gt 0) {
            $primera = $preguntas[0]
            Write-Host "`n📝 Ejemplo (Pregunta #1):" -ForegroundColor Cyan
            Write-Host "  • id_pregunta: $($primera.id_pregunta)" -ForegroundColor White
            Write-Host "  • enunciado: $($primera.enunciado.Substring(0, [Math]::Min(80, $primera.enunciado.Length)))..." -ForegroundColor White
        }

        if ($todasConNull) {
            Write-Host "`n✅ PRUEBA EXITOSA: Todas las preguntas son de IA/OpenAI" -ForegroundColor Green
            $passedTests++
        } else {
            Write-Host "`n❌ PRUEBA FALLIDA: $preguntasBanco preguntas son del banco local" -ForegroundColor Red
            $failedTests++
        }

    } catch {
        Write-Host "`n❌ ERROR en la prueba" -ForegroundColor Red
        Write-Host "Detalles: $($_.Exception.Message)" -ForegroundColor Red
        $failedTests++
    }

    Write-Host ""
}

# Resumen final
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📊 RESUMEN DE VALIDACIÓN" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Total de pruebas: $totalTests" -ForegroundColor White
Write-Host "✅ Pruebas exitosas: $passedTests" -ForegroundColor Green
Write-Host "❌ Pruebas fallidas: $failedTests" -ForegroundColor $(if ($failedTests -eq 0) { "Green" } else { "Red" })

if ($failedTests -eq 0) {
    Write-Host "`n🎉 ¡VALIDACIÓN COMPLETA!" -ForegroundColor Green
    Write-Host "✅ El backend está generando preguntas con OpenAI/IA correctamente" -ForegroundColor Green
    Write-Host "✅ Todas las preguntas tienen id_pregunta = null" -ForegroundColor Green
    Write-Host "`n📝 Siguiente paso:" -ForegroundColor Yellow
    Write-Host "  • Probar desde la app móvil Android" -ForegroundColor White
    Write-Host "  • Verificar que las preguntas se muestren correctamente" -ForegroundColor White
    Write-Host "  • Confirmar al equipo backend que todo funciona" -ForegroundColor White
} else {
    Write-Host "`n⚠️ VALIDACIÓN INCOMPLETA" -ForegroundColor Yellow
    Write-Host "❌ Algunas pruebas fallaron" -ForegroundColor Red
    Write-Host "`n📝 Acciones requeridas:" -ForegroundColor Yellow
    Write-Host "  • Verificar logs del backend" -ForegroundColor White
    Write-Host "  • Confirmar que las variables de entorno estén configuradas" -ForegroundColor White
    Write-Host "  • Reiniciar el servicio backend si es necesario" -ForegroundColor White
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🏁 VALIDACIÓN COMPLETADA" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

