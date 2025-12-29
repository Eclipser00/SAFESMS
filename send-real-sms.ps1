# send-real-sms.ps1
# Script para enviar SMS REALES al sistema Android (funciona sin SafeSMS instalada)
# Estos SMS aparecerán en cualquier app SMS del dispositivo
# 
# Uso:
#   .\send-real-sms.ps1 -Address "+34123456789" -Body "Hola como los llevas"
#   .\send-real-sms.ps1 -Address "BANCO 123" -Body "Tengo un enlace para ti http://listaspam.com"
#   .\send-real-sms.ps1 -Address "5554" -Body "Mensaje de prueba"
#
# Requisitos:
#   - ADB instalado y en PATH
#   - Dispositivo Android conectado o emulador ejecutándose
#   - Funciona mejor en emuladores (no requiere root)

param(
    [Parameter(Mandatory=$true, HelpMessage="Número de teléfono o remitente (ej: +34123456789, BANCO 123, 5554)")]
    [string]$Address,
    
    [Parameter(Mandatory=$true, HelpMessage="Texto del mensaje SMS")]
    [string]$Body,
    
    [Parameter(Mandatory=$false, HelpMessage="Timestamp en milisegundos (opcional, por defecto usa tiempo actual)")]
    [long]$Timestamp = 0
)

# Colores para output
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# Verificar que ADB está disponible
try {
    $null = adb version 2>&1
} catch {
    Write-ColorOutput "[ERROR] ADB no encontrado en PATH" "Red"
    Write-ColorOutput "   Por favor instala Android SDK Platform Tools" "Yellow"
    Write-ColorOutput "   Descarga: https://developer.android.com/studio/releases/platform-tools" "Yellow"
    exit 1
}

# Función para normalizar el address (eliminar espacios, mantener + y letras)
function Normalize-Address {
    param([string]$address)
    
    # Si contiene letras (remitente alfanumérico), solo eliminar espacios al inicio/final
    if ($address -match '[a-zA-Z]') {
        return $address.Trim()
    }
    
    # Para números: eliminar TODOS los espacios, guiones, paréntesis, puntos
    # Mantener el + al inicio si existe
    # En PowerShell, el regex usa \s para espacios, \- para guiones, etc.
    $normalized = $address -replace '[\s\-\(\)\.]', ''
    
    return $normalized
}

# Normalizar el address antes de procesarlo
$originalAddress = $Address
$Address = Normalize-Address -address $Address

# Verificar que hay un dispositivo conectado y obtener información
$devicesOutput = adb devices 2>&1
$deviceLines = $devicesOutput | Select-String -Pattern "device$"
if ($deviceLines.Count -eq 0) {
    Write-ColorOutput "[ERROR] No hay dispositivos Android conectados" "Red"
    Write-ColorOutput "   Ejecuta 'adb devices' para verificar la conexion" "Yellow"
    Write-ColorOutput "   Asegurate de que el emulador esta ejecutandose o el dispositivo esta conectado por USB" "Yellow"
    exit 1
}

# Detectar si es emulador (por el nombre del dispositivo)
$isEmulator = $false
$devicePort = ""
$deviceId = ""
foreach ($line in $deviceLines) {
    if ($line -match "emulator-(\d+)") {
        $isEmulator = $true
        $devicePort = $matches[1]
        $deviceId = "emulator-$devicePort"
        break
    } elseif ($line -match "(\S+)\s+device") {
        $deviceId = $matches[1]
    }
}

Write-ColorOutput "[INFO] Enviando SMS REAL al sistema Android..." "Cyan"
if ($originalAddress -ne $Address) {
    Write-ColorOutput "   Remitente original: $originalAddress" "Gray"
    Write-ColorOutput "   Remitente normalizado: $Address" "Yellow"
} else {
    Write-ColorOutput "   Remitente: $Address" "Yellow"
}
Write-ColorOutput "   Mensaje: $Body" "Yellow"
if ($isEmulator) {
    Write-ColorOutput "   Dispositivo: Emulador (puerto $devicePort)" "Gray"
    Write-ColorOutput "   Metodo: Telnet (dispara broadcasts correctamente)" "Gray"
} else {
    Write-ColorOutput "   Dispositivo: Fisico" "Gray"
    Write-ColorOutput "   Metodo: Content Provider" "Gray"
}
Write-Host ""

if ($isEmulator) {
    # Método 1: Usar Telnet para emuladores (dispara broadcasts SMS_DELIVER/SMS_RECEIVED correctamente)
    Write-ColorOutput "[INFO] Usando metodo Telnet para emulador..." "Gray"
    
    # Escapar comillas y caracteres especiales para el comando
    $escapedBody = $Body -replace '"', '\"'
    $escapedBody = $escapedBody -replace '\$', '\$'
    
    # Usar adb emu sms send que es más confiable y dispara los broadcasts correctamente
    # IMPORTANTE: El address ya está normalizado (sin espacios), pero lo envolvemos en comillas por si acaso
    $telnetCommand = "adb -s $deviceId emu sms send `"$Address`" `"$escapedBody`""
    
    Write-ColorOutput "Ejecutando comando Telnet..." "Gray"
    Write-Host ""
    
    $result = Invoke-Expression $telnetCommand 2>&1
    
    if ($LASTEXITCODE -eq 0 -and $result -notmatch "error|Error|ERROR|KO|Unknown command") {
        Write-ColorOutput "[OK] SMS enviado correctamente via Telnet" "Green"
        Write-Host ""
        Write-ColorOutput "   El SMS deberia aparecer y generar notificaciones automaticamente" "Gray"
        Write-ColorOutput "   (incluida SafeSMS si esta instalada y es la app por defecto)" "Gray"
        Write-Host ""
        Write-ColorOutput "[TIP] Si SafeSMS esta instalada, deberias ver la notificacion" "Cyan"
    } else {
        Write-ColorOutput "[WARN] Error al enviar SMS via Telnet" "Yellow"
        Write-Host ""
        Write-ColorOutput "   Detalles: $result" "Gray"
        Write-Host ""
        Write-ColorOutput "   Intentando metodo alternativo (Content Provider)..." "Yellow"
        
        # Fallback al método de Content Provider
        $isEmulator = $false
    }
}

if (-not $isEmulator) {
    # Método 2: Content Provider (para dispositivos físicos o fallback)
    Write-ColorOutput "[INFO] Usando metodo Content Provider..." "Gray"
    
    # Si timestamp es 0, usar tiempo actual
    if ($Timestamp -eq 0) {
        $Timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
    }
    
    Write-ColorOutput "   Timestamp: $Timestamp" "Gray"
    Write-Host ""
    
    # Escapar comillas en el body para evitar problemas con comandos ADB
    $escapedBody = $Body -replace '"', '\"'
    $escapedAddress = $Address -replace '"', '\"'
    
    # Insertar SMS directamente en la base de datos del sistema usando ContentProvider
    # Esto simula un SMS real que llegó al dispositivo
    # Campos importantes:
    #   - address: número o nombre del remitente
    #   - body: contenido del mensaje
    #   - date: timestamp en milisegundos
    #   - read: 0 = no leído, 1 = leído
    #   - seen: 0 = no visto, 1 = visto
    #   - type: 1 = recibido, 2 = enviado
    
    if ($deviceId) {
        $adbCommand = "adb -s $deviceId shell content insert --uri content://sms/inbox --bind address:s:`"$escapedAddress`" --bind body:s:`"$escapedBody`" --bind date:l:$Timestamp --bind read:i:0 --bind seen:i:0 --bind type:i:1"
    } else {
        $adbCommand = "adb shell content insert --uri content://sms/inbox --bind address:s:`"$escapedAddress`" --bind body:s:`"$escapedBody`" --bind date:l:$Timestamp --bind read:i:0 --bind seen:i:0 --bind type:i:1"
    }
    
    Write-ColorOutput "Ejecutando comando ADB..." "Gray"
    Write-Host ""
    
    # Ejecutar comando
    $result = Invoke-Expression $adbCommand 2>&1
    
    if ($LASTEXITCODE -eq 0 -and $result -notmatch "error|Error|ERROR|Permission denied") {
        Write-ColorOutput "[OK] SMS insertado en el sistema correctamente" "Green"
        Write-Host ""
        Write-ColorOutput "   NOTA: Este metodo inserta el SMS en la BD pero puede no disparar notificaciones automaticamente" "Yellow"
        Write-ColorOutput "   El SMS aparecera en cualquier app SMS instalada" "Gray"
        Write-ColorOutput "   (incluida SafeSMS si esta instalada y es la app por defecto)" "Gray"
        Write-Host ""
        Write-ColorOutput "[TIP] Si SafeSMS esta instalada, abre la app para ver el mensaje" "Cyan"
        Write-ColorOutput "[TIP] Para notificaciones automaticas, usa un emulador con el metodo Telnet" "Cyan"
    } else {
        Write-ColorOutput "[ERROR] Error al insertar SMS" "Red"
        Write-Host ""
        Write-ColorOutput "   Detalles del error:" "Yellow"
        Write-Host $result
        Write-Host ""
        Write-ColorOutput "   Posibles soluciones:" "Yellow"
        Write-ColorOutput "   1. Verifica que el dispositivo esta conectado: adb devices" "Yellow"
        Write-ColorOutput "   2. En emuladores Android, el metodo Telnet deberia funcionar sin problemas" "Yellow"
        Write-ColorOutput "   3. En dispositivos fisicos, puede requerir permisos de root" "Yellow"
        Write-ColorOutput "   4. Asegurate de que ADB tiene permisos de depuracion USB" "Yellow"
        exit 1
    }
}

