# SafeSMS - Aplicación Android Cliente SMS con Firewall Cognitivo

## Descripción

SafeSMS es una aplicación Android que funciona como cliente SMS por defecto y firewall cognitivo contra phishing. Su objetivo no es bloquear mensajes, sino romper el mecanismo psicológico del ataque mediante pausas forzadas, advertencias contextuales y fricción explícita en momentos críticos.

## Stack Tecnológico

- **Kotlin 100%**
- **Jetpack Compose** - Framework UI declarativo
- **Clean Architecture + MVVM** - Arquitectura limpia y escalable
- **Room** - Persistencia local de datos
- **Hilt** - Inyección de dependencias
- **Coroutines/Flow** - Programación asíncrona y reactiva
- **AdMob** - Monetización mediante banners

## Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/safesms/
│   │   │   ├── data/              # Capa de datos
│   │   │   │   ├── local/         # Room, DataStore, System Providers
│   │   │   │   └── repository/   # Implementaciones de repositorios
│   │   │   ├── domain/            # Capa de dominio
│   │   │   │   ├── model/         # Entidades de dominio
│   │   │   │   ├── repository/    # Interfaces de repositorios
│   │   │   │   └── usecase/       # Casos de uso
│   │   │   ├── presentation/     # Capa de presentación
│   │   │   │   ├── screen/       # Pantallas
│   │   │   │   ├── navigation/    # Navegación
│   │   │   │   └── ui/           # Componentes UI
│   │   │   ├── di/               # Módulos Hilt
│   │   │   └── util/            # Utilidades
│   │   └── res/                  # Recursos Android
│   └── test/                     # Tests unitarios
```

## Requisitos

- Android Studio Hedgehog o superior
- JDK 17
- Android SDK mínimo: API 23 (Android 6.0)
- Android SDK objetivo: API 34

## Instrucciones de Compilación

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd SafeSMSapp-v2.0
```

### 2. Configurar AdMob

1. Abre `app/src/main/java/com/safesms/util/Constants.kt`
2. Reemplaza `ADMOB_BANNER_ID` con tu ID de banner de AdMob
3. Para desarrollo, puedes usar el ID de prueba: `ca-app-pub-3940256099942544/6300978111`

**Nota**: En producción, asegúrate de:
- Configurar tu ID real de AdMob
- Implementar el consent form de GDPR/CCPA si es necesario
- Configurar políticas de privacidad según las políticas de Google Play

### 3. Sincronizar dependencias

Abre el proyecto en Android Studio y espera a que Gradle sincronice todas las dependencias.

### 4. Compilar y ejecutar

1. Conecta un dispositivo Android o inicia un emulador
2. Haz clic en "Run" (▶️) o presiona `Shift + F10`
3. La aplicación se instalará y ejecutará automáticamente

### 5. Configurar como app SMS por defecto

Al ejecutar la aplicación por primera vez:
1. Concede los permisos solicitados (SMS, Contactos)
2. Cuando se solicite, configura SafeSMS como tu aplicación de SMS por defecto
3. La aplicación importará automáticamente tu histórico de SMS

## Funcionalidades Principales

### Clasificación Automática
- **Inbox**: Mensajes de contactos guardados
- **Cuarentena**: Mensajes de remitentes desconocidos

### Sistema de Advertencias
- Advertencia obligatoria antes de acceder a chats de Cuarentena
- Advertencia obligatoria antes de abrir enlaces
- Cuenta atrás configurable (3-8 segundos)

### Bloqueo de Remitentes
- Bloquea remitentes sospechosos
- Desbloqueo reversible desde ajustes
- Mensajes bloqueados no generan notificaciones

### Notificaciones Diferenciadas
- Notificaciones normales para Inbox
- Notificaciones genéricas para Cuarentena (configurable)

## Testing

### Tests Implementados

La aplicación incluye tests básicos para los componentes críticos:

#### Tests Unitarios

- **ClassifyChatUseCaseTest**: Verifica la clasificación correcta de chats como INBOX o QUARANTINE
- **DetectRiskFactorsUseCaseTest**: Verifica la detección de factores de riesgo (enlaces, remitentes alfanuméricos, short codes, remitentes desconocidos)
- **MessageRepositoryImplTest**: Verifica operaciones CRUD en el repositorio de mensajes usando Room in-memory database
- **SmsFlowIntegrationTest**: Tests de integración end-to-end del flujo completo de SMS

### Ejecutar tests

#### Tests unitarios (JVM)

```bash
./gradlew test
```

#### Tests instrumentados (Android)

```bash
./gradlew connectedAndroidTest
```

#### Ejecutar un test específico

```bash
./gradlew test --tests "com.safesms.domain.usecase.ClassifyChatUseCaseTest"
```

### Testing de SMS Reales

Para probar la recepción de SMS en dispositivos/emuladores, usa los scripts incluidos que insertan SMS reales en el sistema Android:

#### Windows (PowerShell)

```powershell
# Enviar un SMS de prueba
.\send-real-sms.ps1 -Address "+346001234598" -Body "Hola como los llevas"

# Ejecutar todos los escenarios de prueba
.\test-real-sms-scenarios.ps1
```

#### Linux/Mac (Bash)

```bash
# Enviar un SMS de prueba
./send-real-sms.sh "+346001234598" "Hola como los llevas"

# Ejecutar todos los escenarios de prueba
./test-real-sms-scenarios.sh
```

**Ventajas:**
- ✅ No requiere SafeSMS instalada (los SMS se insertan directamente en el sistema)
- ✅ Funciona perfectamente en emuladores sin permisos especiales
- ✅ Los SMS aparecen en cualquier app SMS del dispositivo
- ✅ Útil para testing end-to-end realista

Para más información, consulta `README-SMS-TESTING.md`.

### Estructura de Tests

```
app/src/test/java/com/safesms/
├── domain/usecase/
│   ├── ClassifyChatUseCaseTest.kt
│   └── DetectRiskFactorsUseCaseTest.kt
├── data/repository/
│   └── MessageRepositoryImplTest.kt
└── integration/
    └── SmsFlowIntegrationTest.kt
```

### Dependencias de Testing

- **JUnit 4**: Framework de testing
- **Mockk**: Mocking para Kotlin
- **Turbine**: Testing de Flows
- **Room Testing**: Room in-memory database para tests
- **Coroutines Test**: Testing de código asíncrono
- **Robolectric**: Testing de componentes Android sin necesidad de dispositivo

## Permisos Requeridos

- `READ_SMS` - Lectura de mensajes SMS
- `SEND_SMS` - Envío de mensajes SMS
- `RECEIVE_SMS` - Recepción de SMS en tiempo real
- `READ_CONTACTS` - Lectura de contactos para clasificación
- `INTERNET` - Para AdMob
- `ACCESS_NETWORK_STATE` - Para AdMob

## Arquitectura

La aplicación sigue Clean Architecture con tres capas principales:

1. **Presentation Layer**: UI con Jetpack Compose, ViewModels, Navigation
2. **Domain Layer**: Lógica de negocio pura, Use Cases, Entidades
3. **Data Layer**: Implementaciones de repositorios, Room, System Providers

### Flujo de Datos

```
UI → ViewModel → Use Case → Repository → Data Source
```

## Optimizaciones Implementadas

### Base de Datos (Room)
- **Índices agregados** para optimizar queries frecuentes:
  - `address` en tabla `chats` (búsquedas por dirección)
  - `chatId` en tabla `messages` (búsquedas por chat)
  - `phoneNumber` en tabla `contacts` (búsquedas por teléfono)
  - `isInboxChat` en tabla `chats` (filtrado Inbox/Cuarentena)
  - `timestamp` en tabla `messages` (ordenamiento)
  - `isRead` en tabla `messages` (filtrado por estado)

### ViewModels
- Todas las coroutines se ejecutan en `viewModelScope` que se cancela automáticamente cuando el ViewModel se destruye
- Los ViewModels usan `StateFlow` para manejo eficiente del estado

### UI (Compose)
- Todas las pantallas usan `collectAsStateWithLifecycle()` en lugar de `collectAsState()` para respetar el ciclo de vida y mejorar el rendimiento
- Los Flows se pausan automáticamente cuando la pantalla está en background

### Operaciones Pesadas
- La importación de SMS se ejecuta en background usando coroutines
- Las operaciones de base de datos se ejecutan en dispatchers apropiados

## Estado del Proyecto

### ✅ Completado (Fase 1 - MVP)
- Estructura completa de Clean Architecture
- Room Database con todas las entidades, DAOs e índices optimizados
- System Providers (SMS, Contactos)
- Repositorios implementados
- Use Cases críticos
- Módulos Hilt configurados
- Sistema de notificaciones básico
- MainActivity con manejo de permisos
- Pantallas completas (Onboarding, ChatList, ChatDetail, Settings, QuarantineWarning, LinkWarning)
- Componentes UI reutilizables
- Tests unitarios básicos implementados
- Optimizaciones de performance aplicadas

## Contribuir

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo LICENSE para más detalles.

## Contacto

Para preguntas o soporte, abre un issue en el repositorio.

