# 🚀 Inicio Rápido - Testing de SMS Reales

## ⚡ Uso Inmediato (Windows)

### 1. Verificar que tienes un dispositivo conectado:
```powershell
adb devices
```

### 2. Enviar un SMS de prueba:
```powershell
.\send-real-sms.ps1 -Address "+346001234598" -Body "Hola como los llevas"
```

### 3. Ejecutar todos los escenarios de prueba:
```powershell
.\test-real-sms-scenarios.ps1
```

## 📝 Ejemplos Rápidos

```powershell
# SMS con número largo
.\send-real-sms.ps1 -Address "+346001234598" -Body "Hola como los llevas"

# SMS con número corto
.\send-real-sms.ps1 -Address "5554" -Body "Mensaje de prueba"

# SMS con remitente alfanumérico
.\send-real-sms.ps1 -Address "BANCO 123" -Body "Tengo un enlace para ti http://listaspam.com"

# SMS de Telefónica
.\send-real-sms.ps1 -Address "telefonica" -Body "Tu factura está lista"

# SMS de Correos
.\send-real-sms.ps1 -Address "Correos" -Body "Tienes un paquete pendiente"
```

## ✅ Ventajas

- ✅ **No requiere SafeSMS instalada**: Los SMS se insertan directamente en el sistema
- ✅ **Funciona en emuladores**: Sin permisos especiales, usa Telnet automáticamente
- ✅ **SMS reales**: Aparecen en cualquier app SMS del dispositivo
- ✅ **Notificaciones automáticas**: En emuladores, dispara broadcasts y genera notificaciones
- ✅ **Detección automática**: Detecta emulador vs dispositivo físico y usa el método apropiado
- ✅ **Fácil de usar**: Solo necesitas pasar dirección y mensaje

## 📚 Más Información

Consulta `README-SMS-TESTING.md` para documentación completa.

