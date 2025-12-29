#!/bin/bash
# send-real-sms.sh
# Script para enviar SMS REALES al sistema Android (funciona sin SafeSMS instalada)
# Estos SMS aparecerán en cualquier app SMS del dispositivo
# 
# Uso:
#   ./send-real-sms.sh "+34123456789" "Hola como los llevas"
#   ./send-real-sms.sh "BANCO 123" "Tengo un enlace para ti http://listaspam.com"
#   ./send-real-sms.sh "5554" "Mensaje de prueba"
#
# Requisitos:
#   - ADB instalado y en PATH
#   - Dispositivo Android conectado o emulador ejecutándose
#   - Funciona mejor en emuladores (no requiere root)

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

# Verificar argumentos
if [ $# -lt 2 ]; then
    echo -e "${RED}❌ Error: Faltan argumentos${NC}"
    echo -e "${YELLOW}Uso: $0 <address> <body> [timestamp]${NC}"
    echo ""
    echo -e "${GRAY}Ejemplos:${NC}"
    echo -e "  $0 \"+34123456789\" \"Hola como los llevas\""
    echo -e "  $0 \"BANCO 123\" \"Tengo un enlace para ti http://listaspam.com\""
    echo -e "  $0 \"5554\" \"Mensaje de prueba\""
    echo ""
    echo -e "${GRAY}Parámetros:${NC}"
    echo -e "  address   : Número de teléfono o remitente (ej: +34123456789, BANCO 123, 5554)"
    echo -e "  body      : Texto del mensaje SMS"
    echo -e "  timestamp : Opcional, timestamp en milisegundos (por defecto usa tiempo actual)"
    exit 1
fi

ADDRESS="$1"
BODY="$2"
TIMESTAMP="${3:-$(date +%s)000}"  # Si no se proporciona, usar timestamp actual en milisegundos

# Verificar que ADB está disponible
if ! command -v adb &> /dev/null; then
    echo -e "${RED}❌ Error: ADB no encontrado en PATH${NC}"
    echo -e "${YELLOW}   Por favor instala Android SDK Platform Tools${NC}"
    echo -e "${YELLOW}   Descarga: https://developer.android.com/studio/releases/platform-tools${NC}"
    exit 1
fi

# Verificar que hay un dispositivo conectado y obtener información
DEVICES_OUTPUT=$(adb devices 2>&1)
DEVICE_COUNT=$(echo "$DEVICES_OUTPUT" | grep -c "device$")
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}[ERROR] No hay dispositivos Android conectados${NC}"
    echo -e "${YELLOW}   Ejecuta 'adb devices' para verificar la conexion${NC}"
    echo -e "${YELLOW}   Asegurate de que el emulador esta ejecutandose o el dispositivo esta conectado por USB${NC}"
    exit 1
fi

# Detectar si es emulador (por el nombre del dispositivo)
IS_EMULATOR=false
DEVICE_PORT=""
DEVICE_ID=""
while IFS= read -r line; do
    if echo "$line" | grep -q "emulator-"; then
        IS_EMULATOR=true
        DEVICE_PORT=$(echo "$line" | grep -oP "emulator-\K\d+")
        DEVICE_ID="emulator-$DEVICE_PORT"
        break
    elif echo "$line" | grep -q "device$"; then
        DEVICE_ID=$(echo "$line" | awk '{print $1}')
    fi
done <<< "$DEVICES_OUTPUT"

echo -e "${CYAN}[INFO] Enviando SMS REAL al sistema Android...${NC}"
echo -e "${YELLOW}   Remitente: $ADDRESS${NC}"
echo -e "${YELLOW}   Mensaje: $BODY${NC}"
if [ "$IS_EMULATOR" = true ]; then
    echo -e "${GRAY}   Dispositivo: Emulador (puerto $DEVICE_PORT)${NC}"
    echo -e "${GRAY}   Metodo: Telnet (dispara broadcasts correctamente)${NC}"
else
    echo -e "${GRAY}   Dispositivo: Fisico${NC}"
    echo -e "${GRAY}   Metodo: Content Provider${NC}"
fi
echo ""

if [ "$IS_EMULATOR" = true ]; then
    # Método 1: Usar Telnet para emuladores (dispara broadcasts SMS_DELIVER/SMS_RECEIVED correctamente)
    echo -e "${GRAY}[INFO] Usando metodo Telnet para emulador...${NC}"
    
    # Escapar comillas y caracteres especiales para el comando
    ESCAPED_BODY=$(echo "$BODY" | sed 's/"/\\"/g' | sed 's/\$/\\$/g')
    
    # Usar adb emu sms send que es más confiable y dispara los broadcasts correctamente
    TELNET_CMD="adb -s $DEVICE_ID emu sms send $ADDRESS \"$ESCAPED_BODY\""
    
    echo -e "${GRAY}Ejecutando comando Telnet...${NC}"
    echo ""
    
    eval $TELNET_CMD 2>&1
    EXIT_CODE=$?
    
    if [ $EXIT_CODE -eq 0 ]; then
        # Verificar que no haya errores en la salida
        OUTPUT=$(eval $TELNET_CMD 2>&1)
        if echo "$OUTPUT" | grep -qiE "error|Error|ERROR|KO|Unknown command"; then
            echo -e "${YELLOW}[WARN] Error al enviar SMS via Telnet${NC}"
            echo -e "${GRAY}   Detalles: $OUTPUT${NC}"
            echo ""
            echo -e "${YELLOW}   Intentando metodo alternativo (Content Provider)...${NC}"
            IS_EMULATOR=false
        else
            echo -e "${GREEN}[OK] SMS enviado correctamente via Telnet${NC}"
            echo ""
            echo -e "${GRAY}   El SMS deberia aparecer y generar notificaciones automaticamente${NC}"
            echo -e "${GRAY}   (incluida SafeSMS si esta instalada y es la app por defecto)${NC}"
            echo ""
            echo -e "${CYAN}[TIP] Si SafeSMS esta instalada, deberias ver la notificacion${NC}"
            exit 0
        fi
    else
        echo -e "${YELLOW}[WARN] Error al enviar SMS via Telnet${NC}"
        echo ""
        echo -e "${YELLOW}   Intentando metodo alternativo (Content Provider)...${NC}"
        IS_EMULATOR=false
    fi
fi

if [ "$IS_EMULATOR" = false ]; then
    # Método 2: Content Provider (para dispositivos físicos o fallback)
    echo -e "${GRAY}[INFO] Usando metodo Content Provider...${NC}"
    
    # Si timestamp no se proporcionó, usar tiempo actual
    if [ -z "$3" ]; then
        TIMESTAMP=$(date +%s)000
    fi
    
    echo -e "${GRAY}   Timestamp: $TIMESTAMP${NC}"
    echo ""
    
    # Escapar comillas en el body para evitar problemas con comandos ADB
    ESCAPED_BODY=$(echo "$BODY" | sed 's/"/\\"/g')
    ESCAPED_ADDRESS=$(echo "$ADDRESS" | sed 's/"/\\"/g')
    
    # Insertar SMS directamente en la base de datos del sistema usando ContentProvider
    # Esto simula un SMS real que llegó al dispositivo
    # Campos importantes:
    #   - address: número o nombre del remitente
    #   - body: contenido del mensaje
    #   - date: timestamp en milisegundos
    #   - read: 0 = no leído, 1 = leído
    #   - seen: 0 = no visto, 1 = visto
    #   - type: 1 = recibido, 2 = enviado
    
    if [ -n "$DEVICE_ID" ]; then
        ADB_CMD="adb -s $DEVICE_ID shell content insert --uri content://sms/inbox --bind address:s:\"$ESCAPED_ADDRESS\" --bind body:s:\"$ESCAPED_BODY\" --bind date:l:$TIMESTAMP --bind read:i:0 --bind seen:i:0 --bind type:i:1"
    else
        ADB_CMD="adb shell content insert --uri content://sms/inbox --bind address:s:\"$ESCAPED_ADDRESS\" --bind body:s:\"$ESCAPED_BODY\" --bind date:l:$TIMESTAMP --bind read:i:0 --bind seen:i:0 --bind type:i:1"
    fi
    
    echo -e "${GRAY}Ejecutando comando ADB...${NC}"
    echo ""
    
    # Ejecutar comando
    eval $ADB_CMD 2>&1
    EXIT_CODE=$?
    
    if [ $EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}[OK] SMS insertado en el sistema correctamente${NC}"
        echo ""
        echo -e "${YELLOW}   NOTA: Este metodo inserta el SMS en la BD pero puede no disparar notificaciones automaticamente${NC}"
        echo -e "${GRAY}   El SMS aparecera en cualquier app SMS instalada${NC}"
        echo -e "${GRAY}   (incluida SafeSMS si esta instalada y es la app por defecto)${NC}"
        echo ""
        echo -e "${CYAN}[TIP] Si SafeSMS esta instalada, abre la app para ver el mensaje${NC}"
        echo -e "${CYAN}[TIP] Para notificaciones automaticas, usa un emulador con el metodo Telnet${NC}"
    else
        echo -e "${RED}[ERROR] Error al insertar SMS${NC}"
        echo ""
        echo -e "${YELLOW}   Posibles soluciones:${NC}"
        echo -e "${YELLOW}   1. Verifica que el dispositivo esta conectado: adb devices${NC}"
        echo -e "${YELLOW}   2. En emuladores Android, el metodo Telnet deberia funcionar sin problemas${NC}"
        echo -e "${YELLOW}   3. En dispositivos fisicos, puede requerir permisos de root${NC}"
        echo -e "${YELLOW}   4. Asegurate de que ADB tiene permisos de depuracion USB${NC}"
        exit 1
    fi
fi

