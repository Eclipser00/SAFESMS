# setup-sms-testing.ps1
# Script de configuración inicial para testing de SMS
# Configura los permisos de ejecución de PowerShell si es necesario

Write-Host "🔧 Configurando entorno para testing de SMS..." -ForegroundColor Cyan
Write-Host ""

# Verificar política de ejecución de PowerShell
$executionPolicy = Get-ExecutionPolicy

if ($executionPolicy -eq "Restricted") {
    Write-Host "⚠️  La política de ejecución de PowerShell está en 'Restricted'" -ForegroundColor Yellow
    Write-Host "   Esto puede impedir la ejecución de scripts" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "💡 Para permitir la ejecución de scripts en este directorio:" -ForegroundColor Cyan
    Write-Host "   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser" -ForegroundColor Gray
    Write-Host ""
    $response = Read-Host "¿Deseas configurar la política ahora? (S/N)"
    if ($response -eq "S" -or $response -eq "s") {
        try {
            Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
            Write-Host "✅ Política de ejecución configurada correctamente" -ForegroundColor Green
        } catch {
            Write-Host "❌ Error al configurar la política: $_" -ForegroundColor Red
            Write-Host "   Puedes ejecutar el comando manualmente como administrador" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "✅ Política de ejecución: $executionPolicy" -ForegroundColor Green
}

Write-Host ""
Write-Host "📋 Verificando requisitos..." -ForegroundColor Cyan

# Verificar ADB
try {
    $adbVersion = adb version 2>&1 | Select-String -Pattern "Android Debug Bridge"
    if ($adbVersion) {
        Write-Host "✅ ADB encontrado" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ ADB no encontrado" -ForegroundColor Red
    Write-Host "   Descarga Android SDK Platform Tools:" -ForegroundColor Yellow
    Write-Host "   https://developer.android.com/studio/releases/platform-tools" -ForegroundColor Gray
}

# Verificar dispositivos conectados
Write-Host ""
$devices = adb devices 2>&1 | Select-String -Pattern "device$"
if ($devices) {
    Write-Host "✅ Dispositivos conectados:" -ForegroundColor Green
    $devices | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
} else {
    Write-Host "⚠️  No hay dispositivos conectados" -ForegroundColor Yellow
    Write-Host "   Ejecuta 'adb devices' para verificar" -ForegroundColor Gray
}

Write-Host ""
Write-Host "📚 Archivos de scripts disponibles:" -ForegroundColor Cyan
Write-Host "   - send-real-sms.ps1 (enviar SMS individual)" -ForegroundColor Gray
Write-Host "   - test-real-sms-scenarios.ps1 (ejecutar todos los escenarios)" -ForegroundColor Gray
Write-Host ""
Write-Host "💡 Ejemplo de uso:" -ForegroundColor Cyan
Write-Host "   .\send-real-sms.ps1 -Address `"+346001234598`" -Body `"Hola como los llevas`"" -ForegroundColor Gray
Write-Host ""

