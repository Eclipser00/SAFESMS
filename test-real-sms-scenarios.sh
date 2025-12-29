#!/bin/bash
# test-real-sms-scenarios.sh
# Ejecuta varios escenarios de prueba comunes enviando SMS reales al sistema
# 
# Uso: ./test-real-sms-scenarios.sh

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SEND_SCRIPT="$SCRIPT_DIR/send-real-sms.sh"

if [ ! -f "$SEND_SCRIPT" ]; then
    echo -e "${RED}❌ Error: No se encontró send-real-sms.sh en el mismo directorio${NC}"
    exit 1
fi

# Dar permisos de ejecución si no los tiene
chmod +x "$SEND_SCRIPT" 2>/dev/null

echo -e "${CYAN}🧪 Ejecutando escenarios de prueba de SMS reales...${NC}"
echo ""

# Escenario 1: SMS con número largo (típico de España)
echo -e "${YELLOW}📥 Escenario 1: SMS con número largo (+34)${NC}"
"$SEND_SCRIPT" "+346001234598" "Hola como los llevas"
sleep 2

# Escenario 2: SMS con número corto (código corto)
echo ""
echo -e "${YELLOW}📥 Escenario 2: SMS de código corto${NC}"
"$SEND_SCRIPT" "5554" "Tu código de verificación es: 789456"
sleep 2

# Escenario 3: SMS con remitente alfanumérico - Banco (phishing típico)
echo ""
echo -e "${YELLOW}📥 Escenario 3: SMS con remitente alfanumérico - Banco${NC}"
"$SEND_SCRIPT" "BANCO 123" "Tengo un enlace para ti http://listaspam.com"
sleep 2

# Escenario 4: SMS con enlace sospechoso
echo ""
echo -e "${YELLOW}📥 Escenario 4: SMS con enlace sospechoso${NC}"
"$SEND_SCRIPT" "InfoBANCO" "Urgente: Tu cuenta ha sido bloqueada. Verifica aquí: https://banco-falso.com/verificar"
sleep 2

# Escenario 5: SMS de Telefónica
echo ""
echo -e "${YELLOW}📥 Escenario 5: SMS de Telefónica${NC}"
"$SEND_SCRIPT" "telefonica" "Tu factura está lista para descargar en tu área de cliente"
sleep 2

# Escenario 6: SMS de Correos
echo ""
echo -e "${YELLOW}📥 Escenario 6: SMS de Correos${NC}"
"$SEND_SCRIPT" "Correos" "Tienes un paquete pendiente de recoger en tu oficina más cercana"
sleep 2

# Escenario 7: SMS con número internacional
echo ""
echo -e "${YELLOW}📥 Escenario 7: SMS con número internacional${NC}"
"$SEND_SCRIPT" "+1234567890" "Mensaje de prueba desde número internacional"
sleep 2

# Escenario 8: SMS con mensaje largo
echo ""
echo -e "${YELLOW}📥 Escenario 8: SMS con mensaje largo${NC}"
"$SEND_SCRIPT" "+34123456789" "Este es un mensaje de prueba más largo que contiene múltiples palabras y frases para probar cómo se maneja el contenido extenso en el sistema de SMS del dispositivo Android."
sleep 2

echo ""
echo -e "${GREEN}✅ Todos los escenarios completados${NC}"
echo ""
echo -e "${CYAN}💡 Revisa tu dispositivo/emulador para ver los SMS recibidos${NC}"
echo -e "${CYAN}   Si SafeSMS está instalada, abre la app para ver cómo los procesa${NC}"

