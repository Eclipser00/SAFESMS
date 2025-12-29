# 📱 Guía de Testing de SMS Reales

Esta guía explica cómo usar los scripts para enviar SMS reales al sistema Android, que aparecerán en cualquier app SMS instalada (incluida SafeSMS si está instalada y es la app por defecto).

## 🎯 Características

- ✅ **No requiere SafeSMS instalada**: Los SMS se insertan directamente en el sistema Android
- ✅ **Funciona en emuladores**: No requiere permisos especiales
- ✅ **SMS reales**: Aparecen en cualquier app SMS del dispositivo
- ✅ **Fácil de usar**: Scripts simples con parámetros claros

## 📋 Requisitos Previos

1. **ADB instalado**: Android SDK Platform Tools
   - Descarga: https://developer.android.com/studio/releases/platform-tools
   - Añade `adb` a tu PATH

2. **Dispositivo Android conectado**:
   - Emulador Android ejecutándose, O
   - Dispositivo físico conectado por USB con depuración USB habilitada

3. **Verificar conexión**:
   ```bash
   adb devices
   ```
   Debe mostrar al menos un dispositivo conectado.

## 🚀 Uso Rápido

### Windows (PowerShell)

```powershell
# Enviar un SMS simple
.\send-real-sms.ps1 -Address "+346001234598" -Body "Hola como los llevas"

# Enviar SMS con remitente alfanumérico
.\send-real-sms.ps1 -Address "BANCO 123" -Body "Tengo un enlace para ti http://listaspam.com"

# Ejecutar todos los escenarios de prueba
.\test-real-sms-scenarios.ps1
```

### Linux/Mac (Bash)

```bash
# Dar permisos de ejecución (solo la primera vez)
chmod +x send-real-sms.sh
chmod +x test-real-sms-scenarios.sh

# Enviar un SMS simple
./send-real-sms.sh "+346001234598" "Hola como los llevas"

# Enviar SMS con remitente alfanumérico
./send-real-sms.sh "BANCO 123" "Tengo un enlace para ti http://listaspam.com"

# Ejecutar todos los escenarios de prueba
./test-real-sms-scenarios.sh
```

## 📝 Ejemplos de Uso

### Ejemplo 1: SMS con número largo
```powershell
.\send-real-sms.ps1 -Address "+346001234598" -Body "Hola como los llevas"
```

### Ejemplo 2: SMS con número corto
```powershell
.\send-real-sms.ps1 -Address "5554" -Body "Tu código de verificación es: 789456"
```

### Ejemplo 3: SMS con remitente alfanumérico
```powershell
.\send-real-sms.ps1 -Address "BANCO 123" -Body "Tengo un enlace para ti http://listaspam.com"
```

### Ejemplo 4: SMS de Telefónica
```powershell
.\send-real-sms.ps1 -Address "telefonica" -Body "Tu factura está lista para descargar"
```

### Ejemplo 5: SMS de Correos
```powershell
.\send-real-sms.ps1 -Address "Correos" -Body "Tienes un paquete pendiente de recoger"
```

## 🔍 Cómo Funciona

Los scripts detectan automáticamente el tipo de dispositivo y usan el método más apropiado:

### Método 1: Telnet (Emuladores) - RECOMENDADO
Cuando se detecta un emulador, los scripts usan `adb emu sms send` que:
- ✅ Dispara los broadcasts `SMS_DELIVER` y `SMS_RECEIVED` correctamente
- ✅ Genera notificaciones automáticamente
- ✅ Activa el `SmsReceiver` de SafeSMS si está instalada
- ✅ Simula un SMS real del sistema

### Método 2: Content Provider (Dispositivos Físicos)
Para dispositivos físicos o como fallback, los scripts usan `adb shell content insert` para insertar SMS directamente en la base de datos del sistema usando el ContentProvider `content://sms/inbox`.

**Campos insertados:**
- `address`: Número de teléfono o nombre del remitente
- `body`: Contenido del mensaje
- `date`: Timestamp en milisegundos
- `read`: 0 = no leído, 1 = leído
- `seen`: 0 = no visto, 1 = visto
- `type`: 1 = recibido, 2 = enviado

**Nota:** El método Content Provider inserta el SMS en la BD pero puede no disparar notificaciones automáticamente. Para notificaciones automáticas, usa un emulador con el método Telnet.

## ⚠️ Limitaciones

1. **Dispositivos físicos**: 
   - El método Content Provider puede requerir permisos de root en algunos dispositivos
   - Puede no disparar notificaciones automáticamente
2. **Emuladores**: 
   - Funciona perfectamente sin permisos especiales usando Telnet
   - Genera notificaciones automáticamente
   - Recomendado para testing
3. **Apps SMS por defecto**: Si SafeSMS está instalada pero NO es la app SMS por defecto, los SMS aparecerán en la app por defecto del sistema

## 🧪 Escenarios de Prueba Incluidos

El script `test-real-sms-scenarios` ejecuta automáticamente estos escenarios:

1. ✅ SMS con número largo (+34)
2. ✅ SMS de código corto (5554)
3. ✅ SMS con remitente alfanumérico - Banco
4. ✅ SMS con enlace sospechoso
5. ✅ SMS de Telefónica
6. ✅ SMS de Correos
7. ✅ SMS con número internacional
8. ✅ SMS con mensaje largo

## 🐛 Solución de Problemas

### Error: "ADB no encontrado"
- Instala Android SDK Platform Tools
- Añade `adb` a tu PATH del sistema

### Error: "No hay dispositivos conectados"
- Ejecuta `adb devices` para verificar
- Asegúrate de que el emulador está ejecutándose
- Si es dispositivo físico, habilita "Depuración USB" en Opciones de Desarrollador

### Error: "Permission denied"
- En emuladores: No debería ocurrir
- En dispositivos físicos: Puede requerir root o permisos especiales

### Los SMS no aparecen en SafeSMS
- Verifica que SafeSMS está instalada
- Verifica que SafeSMS es la app SMS por defecto
- Abre SafeSMS y verifica que tiene permisos de lectura de SMS

## 📚 Archivos Incluidos

- `send-real-sms.ps1`: Script PowerShell para Windows
- `send-real-sms.sh`: Script Bash para Linux/Mac
- `test-real-sms-scenarios.ps1`: Escenarios de prueba para Windows
- `test-real-sms-scenarios.sh`: Escenarios de prueba para Linux/Mac
- `README-SMS-TESTING.md`: Esta guía

## 💡 Tips

1. **Usa emuladores para testing**: Funcionan perfectamente sin permisos especiales
2. **Verifica los logs**: Los scripts muestran información detallada sobre el proceso
3. **Prueba diferentes formatos**: Números largos, cortos, alfanuméricos, etc.
4. **Combina con SafeSMS**: Instala SafeSMS y configúrala como app por defecto para ver cómo procesa los SMS

## 🔗 Referencias

- [Android Content Providers](https://developer.android.com/guide/topics/providers/content-providers)
- [ADB Shell Commands](https://developer.android.com/studio/command-line/adb)
- [Android SMS Provider](https://developer.android.com/reference/android/provider/Telephony.Sms)

