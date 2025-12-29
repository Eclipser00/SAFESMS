# test-real-sms-scenarios.ps1
# Ejecuta varios escenarios de prueba comunes enviando SMS reales al sistema
# 
# Uso: .\test-real-sms-scenarios.ps1

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$sendScript = Join-Path $scriptPath "send-real-sms.ps1"

if (-not (Test-Path $sendScript)) {
    Write-Host "[ERROR] No se encontro send-real-sms.ps1 en el mismo directorio" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Ejecutando escenarios de prueba de SMS reales..." -ForegroundColor Cyan
Write-Host ""

# Escenario 1: SMS con número largo (típico de España)
Write-Host "[ESCENARIO 1] SMS con numero largo (+34)" -ForegroundColor Yellow
& $sendScript -Address "+346001234598" -Body "Hola como los llevas"
Start-Sleep -Seconds 2

# Escenario 2: SMS con número corto (código corto)
Write-Host ""
Write-Host "[ESCENARIO 2] SMS de codigo corto" -ForegroundColor Yellow
& $sendScript -Address "5554" -Body "Tu codigo de verificacion es: 789456"
Start-Sleep -Seconds 2

# Escenario 3: SMS con remitente alfanumérico - Banco (phishing típico)
Write-Host ""
Write-Host "[ESCENARIO 3] SMS con remitente alfanumerico - Banco" -ForegroundColor Yellow
& $sendScript -Address "BANCO 123" -Body "Tengo un enlace para ti http://listaspam.com"
Start-Sleep -Seconds 2

# Escenario 4: SMS con enlace sospechoso
Write-Host ""
Write-Host "[ESCENARIO 4] SMS con enlace sospechoso" -ForegroundColor Yellow
& $sendScript -Address "InfoBANCO" -Body "Urgente: Tu cuenta ha sido bloqueada. Verifica aqui: https://banco-falso.com/verificar"
Start-Sleep -Seconds 2

# Escenario 5: SMS de Telefónica
Write-Host ""
Write-Host "[ESCENARIO 5] SMS de Telefonica" -ForegroundColor Yellow
& $sendScript -Address "telefonica" -Body "Tu factura esta lista para descargar en tu area de cliente"
Start-Sleep -Seconds 2

# Escenario 6: SMS de Correos
Write-Host ""
Write-Host "[ESCENARIO 6] SMS de Correos" -ForegroundColor Yellow
& $sendScript -Address "Correos" -Body "Tienes un paquete pendiente de recoger en tu oficina mas cercana"
Start-Sleep -Seconds 2

# Escenario 7: SMS con número internacional
Write-Host ""
Write-Host "[ESCENARIO 7] SMS con numero internacional" -ForegroundColor Yellow
& $sendScript -Address "+1234567890" -Body "Mensaje de prueba desde numero internacional"
Start-Sleep -Seconds 2

# Escenario 8: SMS con mensaje largo
Write-Host ""
Write-Host "[ESCENARIO 8] SMS con mensaje largo" -ForegroundColor Yellow
& $sendScript -Address "+34123456789" -Body "Este es un mensaje de prueba mas largo que contiene multiples palabras y frases para probar como se maneja el contenido extenso en el sistema de SMS del dispositivo Android."
Start-Sleep -Seconds 2

Write-Host ""
Write-Host "[OK] Todos los escenarios completados" -ForegroundColor Green
Write-Host ""
Write-Host "[TIP] Revisa tu dispositivo/emulador para ver los SMS recibidos" -ForegroundColor Cyan
Write-Host "   Si SafeSMS esta instalada, abre la app para ver como los procesa" -ForegroundColor Cyan

