# SafeSms - Arquitectura Técnica Completa

## Documento de Arquitectura de Software

---

## 1. VISIÓN GENERAL DE LA APLICACIÓN

### 1.1 Objetivo

SafeSms es una aplicación Android que funciona como cliente SMS por defecto y firewall cognitivo contra phishing. Su objetivo no es bloquear mensajes, sino romper el mecanismo psicológico del ataque mediante pausas forzadas, advertencias contextuales y fricción explícita en momentos críticos.

### 1.2 Usuarios objetivo

* Usuarios Android que utilizan SMS como canal de comunicación
* Personas vulnerables a ataques de phishing por SMS
* Usuarios que buscan protección proactiva sin perder acceso a mensajes legítimos

### 1.3 Alcance funcional

* Cliente SMS completo (lectura, envío, gestión de conversaciones)
* Clasificación automática Inbox/Cuarentena basada en contactos
* Sistema de advertencias con cuentas atrás obligatorias
* Protección universal de enlaces
* Sistema de bloqueo reversible y transparente
* Integración con Google AdMob
* Importación completa del histórico de SMS

---

## 2. STACK TECNOLÓGICO

### 2.1 Lenguaje de programación

**Kotlin 100%**

**Justificación:**

* Lenguaje oficial recomendado por Google para Android
* Null-safety nativo reduce errores de runtime críticos en app de seguridad
* Coroutines para operaciones asíncronas (lectura SMS, base de datos)
* Interoperabilidad con Java APIs del sistema Android
* Código más conciso y mantenible que Java
* Soporte completo de Jetpack Compose

### 2.2 Framework UI

**Jetpack Compose**

**Justificación:**

* Toolkit moderno declarativo de Google
* Mejor manejo de estados complejos (Inbox/Cuarentena, bloqueos, advertencias)
* Navegación fluida entre pantallas múltiples
* Recomposición eficiente para listas largas de SMS
* Integración nativa con Material Design 3
* Menor código boilerplate vs XML layouts

### 2.3 Arquitectura base

**Clean Architecture + MVVM**

**Justificación:**

* **Clean Architecture** : Separación clara entre capas (presentación, dominio, datos)
* **MVVM** : Patrón específico para presentación compatible con Compose
* Testabilidad crítica para app de seguridad (tests unitarios, integración)
* Escalabilidad para futuras funcionalidades
* Independencia de frameworks en capa de dominio
* Facilita cambios en UI sin afectar lógica de negocio

### 2.4 Librerías principales

#### Jetpack Components

* **Room** : Persistencia local de SMS, chats, configuración
* **WorkManager** : Sincronización en background, limpieza de datos
* **DataStore** : Configuración de usuario (preferencias, estado onboarding)
* **Lifecycle** : Manejo de ciclos de vida de Activities/Composables
* **Navigation Compose** : Navegación declarativa entre pantallas

#### Inyección de dependencias

* **Hilt** : Inyección de dependencias oficial de Android (sobre Dagger)
* Reduce boilerplate vs Dagger puro
* Integración directa con ViewModels
* Scopes predefinidos para Android

#### Concurrencia

* **Kotlin Coroutines + Flow** : Operaciones asíncronas y streams reactivos
* Lectura de SMS del sistema
* Queries a base de datos
* Observación de cambios en tiempo real

#### Testing

* **JUnit 4/5** : Tests unitarios
* **Mockk** : Mocking para Kotlin
* **Turbine** : Testing de Flows
* **Compose UI Testing** : Tests de interfaz

#### Publicidad

* **Google Mobile Ads SDK (AdMob)** : Monetización mediante banners

#### Análisis y detección

* **Librerías de detección de enlaces** : Patterns.WEB_URL de Android
* **Custom regex patterns** : Detección de remitentes alfanuméricos, short codes

---

## 3. ARQUITECTURA GENERAL

### 3.1 Capas de Clean Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                     │
│  (UI - Jetpack Compose, ViewModels, Navigation)         │
│                                                          │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐       │
│  │  Screens   │  │ ViewModels │  │ UI States  │       │
│  └────────────┘  └────────────┘  └────────────┘       │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                          │
│         (Business Logic - Pure Kotlin)                   │
│                                                          │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐       │
│  │ Use Cases  │  │  Entities  │  │Repositories│       │
│  │            │  │            │  │ Interfaces │       │
│  └────────────┘  └────────────┘  └────────────┘       │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     DATA LAYER                           │
│  (Data Sources, Repository Implementations)              │
│                                                          │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐       │
│  │ Repository │  │  Room DB   │  │ SMS System │       │
│  │    Impl    │  │    DAO     │  │  Provider  │       │
│  └────────────┘  └────────────┘  └────────────┘       │
└─────────────────────────────────────────────────────────┘
```

### 3.2 Responsabilidades por capa

#### Presentation Layer

* Renderizado de UI con Compose
* Captura de eventos de usuario
* Navegación entre pantallas
* Observación de estados desde ViewModels
* Integración de AdMob banners

#### Domain Layer

* Casos de uso (clasificación SMS, detección de riesgos, bloqueo)
* Entidades de negocio (Message, Chat, Contact)
* Reglas de negocio puras (sin dependencias de Android)
* Interfaces de repositorios

#### Data Layer

* Implementación de repositorios
* Acceso a Room Database
* Lectura/escritura de SMS del sistema Android
* Lectura de contactos
* Persistencia de configuración (DataStore)

### 3.3 Flujo de dependencias

```
Presentation → Domain ← Data
(depende)      (núcleo)  (implementa)
```

* Domain NO conoce Presentation ni Data
* Data implementa interfaces definidas en Domain
* Presentation invoca Use Cases de Domain

---

## 4. ESTRUCTURA DE CARPETAS Y ARCHIVOS

### 4.1 Árbol de proyecto compatible con Android Studio

```
SafeSms/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/safesms/
│   │   │   │   │
│   │   │   │   ├── SafeSmsApplication.kt
│   │   │   │   │
│   │   │   │   ├── di/                          # Dependency Injection
│   │   │   │   │   ├── AppModule.kt
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   ├── RepositoryModule.kt
│   │   │   │   │   └── UseCaseModule.kt
│   │   │   │   │
│   │   │   │   ├── data/                        # DATA LAYER
│   │   │   │   │   │
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── database/
│   │   │   │   │   │   │   ├── SafeSmsDatabase.kt
│   │   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   │   ├── MessageDao.kt
│   │   │   │   │   │   │   │   ├── ChatDao.kt
│   │   │   │   │   │   │   │   ├── ContactDao.kt
│   │   │   │   │   │   │   │   └── BlockedSenderDao.kt
│   │   │   │   │   │   │   └── entities/
│   │   │   │   │   │   │       ├── MessageEntity.kt
│   │   │   │   │   │   │       ├── ChatEntity.kt
│   │   │   │   │   │   │       ├── ContactEntity.kt
│   │   │   │   │   │   │       └── BlockedSenderEntity.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── datastore/
│   │   │   │   │   │   │   └── UserPreferencesDataStore.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── system/
│   │   │   │   │   │       ├── SmsSystemProvider.kt
│   │   │   │   │   │       ├── ContactsSystemProvider.kt
│   │   │   │   │   │       └── SmsReceiver.kt
│   │   │   │   │   │
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── MessageRepositoryImpl.kt
│   │   │   │   │   │   ├── ChatRepositoryImpl.kt
│   │   │   │   │   │   ├── ContactRepositoryImpl.kt
│   │   │   │   │   │   ├── BlockedSenderRepositoryImpl.kt
│   │   │   │   │   │   └── ConfigurationRepositoryImpl.kt
│   │   │   │   │   │
│   │   │   │   │   └── mapper/
│   │   │   │   │       ├── MessageMapper.kt
│   │   │   │   │       ├── ChatMapper.kt
│   │   │   │   │       └── ContactMapper.kt
│   │   │   │   │
│   │   │   │   ├── domain/                      # DOMAIN LAYER
│   │   │   │   │   │
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Message.kt
│   │   │   │   │   │   ├── Chat.kt
│   │   │   │   │   │   ├── Contact.kt
│   │   │   │   │   │   ├── ChatType.kt
│   │   │   │   │   │   ├── RiskFactor.kt
│   │   │   │   │   │   ├── BlockedSender.kt
│   │   │   │   │   │   └── UserConfiguration.kt
│   │   │   │   │   │
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── MessageRepository.kt
│   │   │   │   │   │   ├── ChatRepository.kt
│   │   │   │   │   │   ├── ContactRepository.kt
│   │   │   │   │   │   ├── BlockedSenderRepository.kt
│   │   │   │   │   │   └── ConfigurationRepository.kt
│   │   │   │   │   │
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── chat/
│   │   │   │   │       │   ├── GetInboxChatsUseCase.kt
│   │   │   │   │       │   ├── GetQuarantineChatsUseCase.kt
│   │   │   │   │       │   ├── GetChatMessagesUseCase.kt
│   │   │   │   │       │   └── ClassifyChatUseCase.kt
│   │   │   │   │       │
│   │   │   │   │       ├── message/
│   │   │   │   │       │   ├── SendMessageUseCase.kt
│   │   │   │   │       │   ├── ReceiveMessageUseCase.kt
│   │   │   │   │       │   ├── MarkMessageAsReadUseCase.kt
│   │   │   │   │       │   └── DeleteMessageUseCase.kt
│   │   │   │   │       │
│   │   │   │   │       ├── security/
│   │   │   │   │       │   ├── DetectRiskFactorsUseCase.kt
│   │   │   │   │       │   ├── DetectLinksInMessageUseCase.kt
│   │   │   │   │       │   ├── ValidateLinkSafetyUseCase.kt
│   │   │   │   │       │   └── IsAlphanumericSenderUseCase.kt
│   │   │   │   │       │
│   │   │   │   │       ├── blocking/
│   │   │   │   │       │   ├── BlockSenderUseCase.kt
│   │   │   │   │       │   ├── UnblockSenderUseCase.kt
│   │   │   │   │       │   ├── IsBlockedSenderUseCase.kt
│   │   │   │   │       │   └── GetBlockedSendersUseCase.kt
│   │   │   │   │       │
│   │   │   │   │       ├── contact/
│   │   │   │   │       │   ├── IsContactSavedUseCase.kt
│   │   │   │   │       │   ├── SyncContactsUseCase.kt
│   │   │   │   │       │   └── GetContactByPhoneUseCase.kt
│   │   │   │   │       │
│   │   │   │   │       ├── import/
│   │   │   │   │       │   ├── ImportSmsHistoryUseCase.kt
│   │   │   │   │       │   └── ClassifyImportedMessagesUseCase.kt
│   │   │   │   │       │
│   │   │   │   │       └── configuration/
│   │   │   │   │           ├── GetCountdownSecondsUseCase.kt
│   │   │   │   │           ├── SetCountdownSecondsUseCase.kt
│   │   │   │   │           ├── GetQuarantineNotificationsEnabledUseCase.kt
│   │   │   │   │           └── SetQuarantineNotificationsEnabledUseCase.kt
│   │   │   │   │
│   │   │   │   ├── presentation/                # PRESENTATION LAYER
│   │   │   │   │   │
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   │
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   ├── SafeSmsNavigation.kt
│   │   │   │   │   │   └── Screen.kt
│   │   │   │   │   │
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── theme/
│   │   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   │   └── Type.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── AdBanner.kt
│   │   │   │   │   │       ├── CountdownTimer.kt
│   │   │   │   │   │       ├── SecurityWarningDialog.kt
│   │   │   │   │   │       ├── ChatListItem.kt
│   │   │   │   │   │       ├── MessageBubble.kt
│   │   │   │   │   │       ├── RiskIndicatorChip.kt
│   │   │   │   │   │       └── TabSelector.kt
│   │   │   │   │   │
│   │   │   │   │   ├── screen/
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── onboarding/
│   │   │   │   │   │   │   ├── OnboardingScreen.kt
│   │   │   │   │   │   │   ├── OnboardingViewModel.kt
│   │   │   │   │   │   │   ├── OnboardingState.kt
│   │   │   │   │   │   │   └── PermissionsSetupScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── chatlist/
│   │   │   │   │   │   │   ├── ChatListScreen.kt
│   │   │   │   │   │   │   ├── ChatListViewModel.kt
│   │   │   │   │   │   │   └── ChatListState.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── chatdetail/
│   │   │   │   │   │   │   ├── ChatDetailScreen.kt
│   │   │   │   │   │   │   ├── ChatDetailViewModel.kt
│   │   │   │   │   │   │   └── ChatDetailState.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── security/
│   │   │   │   │   │   │   ├── QuarantineWarningScreen.kt
│   │   │   │   │   │   │   ├── QuarantineWarningViewModel.kt
│   │   │   │   │   │   │   ├── LinkWarningDialog.kt
│   │   │   │   │   │   │   └── LinkWarningViewModel.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── settings/
│   │   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │   │       ├── SettingsViewModel.kt
│   │   │   │   │   │       └── SettingsState.kt
│   │   │   │   │   │
│   │   │   │   │   └── util/
│   │   │   │   │       ├── DateFormatter.kt
│   │   │   │   │       ├── PhoneNumberFormatter.kt
│   │   │   │   │       └── ResourceProvider.kt
│   │   │   │   │
│   │   │   │   └── util/                        # UTILITIES
│   │   │   │       ├── Constants.kt
│   │   │   │       ├── Result.kt
│   │   │   │       └── Extensions.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── drawable/
│   │   │   │   └── raw/
│   │   │   │       └── onboarding_video.mp4
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── test/                                # Unit Tests
│   │   │   └── java/com/safesms/
│   │   │       ├── domain/usecase/
│   │   │       │   ├── ClassifyChatUseCaseTest.kt
│   │   │       │   ├── DetectRiskFactorsUseCaseTest.kt
│   │   │       │   └── BlockSenderUseCaseTest.kt
│   │   │       └── data/repository/
│   │   │           └── MessageRepositoryImplTest.kt
│   │   │
│   │   └── androidTest/                         # Instrumented Tests
│   │       └── java/com/safesms/
│   │           ├── ui/
│   │           │   └── ChatListScreenTest.kt
│   │           └── integration/
│   │               └── SmsFlowIntegrationTest.kt
│   │
│   ├── build.gradle.kts                         # App-level Gradle
│   └── proguard-rules.pro
│
├── build.gradle.kts                             # Project-level Gradle
├── settings.gradle.kts
├── gradle.properties
└── local.properties
```

---

## 5. DETALLE POR MÓDULO Y ARCHIVO

### 5.1 SafeSmsApplication.kt

**Responsabilidad:**

* Punto de entrada de la aplicación
* Inicialización de Hilt
* Configuración global de AdMob

**Dependencias:**

* Hilt (Android)
* Google Mobile Ads SDK

**Funciones clave:**

* `onCreate()`: Inicializa Hilt, configura AdMob, registra handlers globales

---

### 5.2 CAPA DI (Dependency Injection)

#### AppModule.kt

**Responsabilidad:**

* Provee dependencias de aplicación general
* Context, recursos del sistema

**Funciones:**

* `provideApplicationContext()`: Provee contexto de aplicación
* `provideResources()`: Provee acceso a recursos Android

#### DatabaseModule.kt

**Responsabilidad:**

* Provee instancia de Room Database
* Configura DAOs

**Funciones:**

* `provideSafeSmsDatabase()`: Crea/recupera instancia de Room
* `provideMessageDao()`: Provee DAO de mensajes
* `provideChatDao()`: Provee DAO de chats
* `provideContactDao()`: Provee DAO de contactos
* `provideBlockedSenderDao()`: Provee DAO de remitentes bloqueados

#### RepositoryModule.kt

**Responsabilidad:**

* Vincula interfaces de repositorios con implementaciones

**Funciones:**

* `bindMessageRepository()`: Vincula MessageRepository
* `bindChatRepository()`: Vincula ChatRepository
* `bindContactRepository()`: Vincula ContactRepository
* `bindBlockedSenderRepository()`: Vincula BlockedSenderRepository
* `bindConfigurationRepository()`: Vincula ConfigurationRepository

#### UseCaseModule.kt

**Responsabilidad:**

* Provee instancias de casos de uso

**Funciones:**

* `provideGetInboxChatsUseCase()`
* `provideGetQuarantineChatsUseCase()`
* `provideDetectRiskFactorsUseCase()`
* `provideBlockSenderUseCase()`
* (Resto de casos de uso...)

---

### 5.3 CAPA DATA - Local - Database

#### SafeSmsDatabase.kt

**Responsabilidad:**

* Clase abstracta de Room Database
* Define versión y entidades
* Provee DAOs

**Dependencias:**

* Room

**Funciones:**

* `messageDao()`: Retorna DAO de mensajes
* `chatDao()`: Retorna DAO de chats
* `contactDao()`: Retorna DAO de contactos
* `blockedSenderDao()`: Retorna DAO de bloqueados

#### MessageDao.kt

**Responsabilidad:**

* Acceso a datos de mensajes en Room

**Funciones:**

* `insertMessage()`: Inserta nuevo mensaje
* `getMessagesByChatId()`: Flow de mensajes de un chat
* `getAllMessages()`: Flow de todos los mensajes
* `updateMessage()`: Actualiza mensaje existente
* `deleteMessage()`: Elimina mensaje
* `markAsRead()`: Marca mensaje como leído
* `getUnreadCount()`: Cuenta mensajes no leídos

#### ChatDao.kt

**Responsabilidad:**

* Acceso a datos de chats en Room

**Funciones:**

* `insertChat()`: Inserta nuevo chat
* `getChatById()`: Obtiene chat por ID
* `getInboxChats()`: Flow de chats de Inbox (remitente en contactos)
* `getQuarantineChats()`: Flow de chats de Cuarentena (remitente NO en contactos)
* `updateChat()`: Actualiza chat
* `deleteChat()`: Elimina chat
* `updateLastMessage()`: Actualiza último mensaje del chat

#### ContactDao.kt

**Responsabilidad:**

* Acceso a datos de contactos sincronizados

**Funciones:**

* `insertContacts()`: Inserta lista de contactos
* `getAllContacts()`: Flow de todos los contactos
* `getContactByPhone()`: Busca contacto por teléfono
* `isContactSaved()`: Verifica si un teléfono está en contactos
* `deleteAllContacts()`: Limpia tabla de contactos (para re-sync)

#### BlockedSenderDao.kt

**Responsabilidad:**

* Gestión de remitentes bloqueados

**Funciones:**

* `insertBlockedSender()`: Bloquea un remitente
* `deleteBlockedSender()`: Desbloquea un remitente
* `getBlockedSenders()`: Flow de todos los bloqueados
* `isBlocked()`: Verifica si un remitente está bloqueado

---

### 5.4 CAPA DATA - Local - Entities

#### MessageEntity.kt

**Responsabilidad:**

* Entidad Room para mensajes SMS

**Campos:**

* `id`: Long (Primary Key)
* `chatId`: Long (Foreign Key)
* `address`: String (teléfono remitente/destinatario)
* `body`: String (contenido del mensaje)
* `timestamp`: Long (milisegundos)
* `type`: Int (TYPE_RECEIVED o TYPE_SENT)
* `isRead`: Boolean
* `threadId`: Long (ID del thread del sistema)

#### ChatEntity.kt

**Responsabilidad:**

* Entidad Room para agrupación de chats

**Campos:**

* `id`: Long (Primary Key)
* `address`: String (teléfono)
* `contactName`: String? (nombre si está en contactos)
* `lastMessageBody`: String
* `lastMessageTimestamp`: Long
* `unreadCount`: Int
* `isInboxChat`: Boolean (true = Inbox, false = Cuarentena)

#### ContactEntity.kt

**Responsabilidad:**

* Entidad Room para contactos sincronizados

**Campos:**

* `id`: Long (Primary Key)
* `phoneNumber`: String (normalizado)
* `displayName`: String
* `syncTimestamp`: Long

#### BlockedSenderEntity.kt

**Responsabilidad:**

* Entidad Room para remitentes bloqueados

**Campos:**

* `address`: String (Primary Key - teléfono)
* `blockedTimestamp`: Long
* `reason`: String? (opcional, para tracking)

---

### 5.5 CAPA DATA - Local - DataStore

#### UserPreferencesDataStore.kt

**Responsabilidad:**

* Persistencia de preferencias de usuario con DataStore

**Dependencias:**

* Jetpack DataStore (Proto o Preferences)

**Funciones:**

* `getCountdownSeconds()`: Flow`<Int>` (3-8)
* `setCountdownSeconds()`: suspend
* `getQuarantineNotificationsEnabled()`: Flow`<Boolean>`
* `setQuarantineNotificationsEnabled()`: suspend
* `getOnboardingCompleted()`: Flow`<Boolean>`
* `setOnboardingCompleted()`: suspend
* `getSmsHistoryImported()`: Flow`<Boolean>`
* `setSmsHistoryImported()`: suspend

---

### 5.6 CAPA DATA - Local - System

#### SmsSystemProvider.kt

**Responsabilidad:**

* Interfaz con ContentProvider de SMS del sistema Android
* Lectura y escritura de SMS

**Dependencias:**

* ContentResolver
* Telephony API

**Funciones:**

* `getAllSms()`: suspend - Lista de SMS del sistema
* `getSmsFromThreadId()`: suspend - SMS de un thread específico
* `sendSms()`: suspend - Envía SMS usando SmsManager
* `markSmsAsRead()`: suspend - Marca SMS como leído en sistema
* `deleteSms()`: suspend - Elimina SMS del sistema

#### ContactsSystemProvider.kt

**Responsabilidad:**

* Lectura de contactos del sistema Android

**Dependencias:**

* ContentResolver
* ContactsContract API

**Funciones:**

* `getAllContacts()`: suspend - Lista de contactos con teléfonos
* `getContactByPhone()`: suspend - Busca contacto por teléfono
* `normalizePhoneNumber()`: Normaliza formato de teléfono para comparación

#### SmsReceiver.kt

**Responsabilidad:**

* BroadcastReceiver para recepción de SMS en tiempo real

**Dependencias:**

* BroadcastReceiver
* Telephony.Sms.Intents

**Funciones:**

* `onReceive()`: Captura SMS entrante, dispara procesamiento
* `extractSmsFromIntent()`: Extrae datos del SMS del Intent

---

### 5.7 CAPA DATA - Repository Implementations

#### MessageRepositoryImpl.kt

**Responsabilidad:**

* Implementa MessageRepository
* Coordina MessageDao y SmsSystemProvider

**Dependencias:**

* MessageDao
* SmsSystemProvider
* MessageMapper

**Funciones:**

* `getMessagesByChatId()`: Flow de mensajes de un chat
* `sendMessage()`: Envía SMS y persiste localmente
* `markAsRead()`: Marca mensaje como leído
* `deleteMessage()`: Elimina mensaje
* `insertMessage()`: Guarda mensaje en BD local

#### ChatRepositoryImpl.kt

**Responsabilidad:**

* Implementa ChatRepository
* Coordina ChatDao y ContactDao

**Dependencias:**

* ChatDao
* ContactDao
* ChatMapper

**Funciones:**

* `getInboxChats()`: Flow de chats de Inbox
* `getQuarantineChats()`: Flow de chats de Cuarentena
* `getChatById()`: Obtiene chat específico
* `updateChat()`: Actualiza datos del chat
* `createOrUpdateChatFromMessage()`: Crea/actualiza chat al recibir mensaje

#### ContactRepositoryImpl.kt

**Responsabilidad:**

* Implementa ContactRepository
* Coordina ContactDao y ContactsSystemProvider

**Dependencias:**

* ContactDao
* ContactsSystemProvider

**Funciones:**

* `syncContacts()`: Sincroniza contactos del sistema con BD local
* `isContactSaved()`: Verifica si teléfono está en contactos
* `getContactByPhone()`: Busca contacto por teléfono

#### BlockedSenderRepositoryImpl.kt

**Responsabilidad:**

* Implementa BlockedSenderRepository

**Dependencias:**

* BlockedSenderDao

**Funciones:**

* `blockSender()`: Bloquea remitente
* `unblockSender()`: Desbloquea remitente
* `isBlocked()`: Verifica si remitente está bloqueado
* `getBlockedSenders()`: Flow de remitentes bloqueados

#### ConfigurationRepositoryImpl.kt

**Responsabilidad:**

* Implementa ConfigurationRepository

**Dependencias:**

* UserPreferencesDataStore

**Funciones:**

* `getCountdownSeconds()`: Flow de segundos configurados
* `setCountdownSeconds()`: Guarda preferencia
* `getQuarantineNotificationsEnabled()`: Flow de estado de notificaciones
* `setQuarantineNotificationsEnabled()`: Guarda preferencia

---

### 5.8 CAPA DATA - Mappers

#### MessageMapper.kt

**Responsabilidad:**

* Conversión entre MessageEntity (Room) y Message (Domain)

**Funciones:**

* `toEntity()`: Message → MessageEntity
* `toDomain()`: MessageEntity → Message
* `toDomainList()`: List`<MessageEntity>` → List`<Message>`

#### ChatMapper.kt

**Responsabilidad:**

* Conversión entre ChatEntity y Chat

**Funciones:**

* `toEntity()`: Chat → ChatEntity
* `toDomain()`: ChatEntity → Chat
* `toDomainList()`: List`<ChatEntity>` → List`<Chat>`

#### ContactMapper.kt

**Responsabilidad:**

* Conversión entre ContactEntity y Contact

**Funciones:**

* `toEntity()`: Contact → ContactEntity
* `toDomain()`: ContactEntity → Contact

---

### 5.9 CAPA DOMAIN - Models

#### Message.kt

**Responsabilidad:**

* Entidad de dominio para mensajes SMS

**Campos:**

* `id`: Long
* `chatId`: Long
* `address`: String
* `body`: String
* `timestamp`: Long
* `isReceived`: Boolean
* `isRead`: Boolean

#### Chat.kt

**Responsabilidad:**

* Entidad de dominio para chats agrupados

**Campos:**

* `id`: Long
* `address`: String
* `contactName`: String?
* `lastMessageBody`: String
* `lastMessageTimestamp`: Long
* `unreadCount`: Int
* `chatType`: ChatType
* `riskFactors`: List`<RiskFactor>`
* `isBlocked`: Boolean

#### ChatType.kt

**Responsabilidad:**

* Enum para clasificación de chat

**Valores:**

* `INBOX`
* `QUARANTINE`

#### RiskFactor.kt

**Responsabilidad:**

* Sealed class para factores de riesgo detectados

**Subclases:**

* `ContainsLinks`: Mensaje contiene URLs
* `AlphanumericSender`: Remitente es alfanumérico
* `ShortCode`: Número corto (típicamente < 6 dígitos)
* `UnknownSender`: Remitente no está en contactos

#### Contact.kt

**Responsabilidad:**

* Entidad de dominio para contactos

**Campos:**

* `id`: Long
* `phoneNumber`: String
* `displayName`: String

#### BlockedSender.kt

**Responsabilidad:**

* Entidad de dominio para remitentes bloqueados

**Campos:**

* `address`: String
* `blockedTimestamp`: Long

#### UserConfiguration.kt

**Responsabilidad:**

* Configuración de usuario

**Campos:**

* `countdownSeconds`: Int (3-8)
* `quarantineNotificationsEnabled`: Boolean
* `onboardingCompleted`: Boolean

---

### 5.10 CAPA DOMAIN - Repository Interfaces

#### MessageRepository.kt

**Responsabilidad:**

* Interfaz para operaciones de mensajes

**Funciones:**

* `getMessagesByChatId()`: Flow<List`<Message>`>
* `sendMessage()`: suspend Result`<Unit>`
* `markAsRead()`: suspend Result`<Unit>`
* `deleteMessage()`: suspend Result`<Unit>`

#### ChatRepository.kt

**Responsabilidad:**

* Interfaz para operaciones de chats

**Funciones:**

* `getInboxChats()`: Flow<List`<Chat>`>
* `getQuarantineChats()`: Flow<List`<Chat>`>
* `getChatById()`: Flow<Chat?>

#### ContactRepository.kt

**Responsabilidad:**

* Interfaz para operaciones de contactos

**Funciones:**

* `syncContacts()`: suspend Result`<Unit>`
* `isContactSaved()`: suspend Boolean
* `getContactByPhone()`: suspend Contact?

#### BlockedSenderRepository.kt

**Responsabilidad:**

* Interfaz para gestión de bloqueados

**Funciones:**

* `blockSender()`: suspend Result`<Unit>`
* `unblockSender()`: suspend Result`<Unit>`
* `isBlocked()`: suspend Boolean
* `getBlockedSenders()`: Flow<List`<BlockedSender>`>

#### ConfigurationRepository.kt

**Responsabilidad:**

* Interfaz para configuración de usuario

**Funciones:**

* `getCountdownSeconds()`: Flow`<Int>`
* `setCountdownSeconds()`: suspend Result`<Unit>`
* `getQuarantineNotificationsEnabled()`: Flow`<Boolean>`
* `setQuarantineNotificationsEnabled()`: suspend Result`<Unit>`

---

### 5.11 CAPA DOMAIN - Use Cases

#### GetInboxChatsUseCase.kt

**Responsabilidad:**

* Obtiene chats de Inbox con sus factores de riesgo

**Dependencias:**

* ChatRepository
* DetectRiskFactorsUseCase
* BlockedSenderRepository

**Funciones:**

* `invoke()`: Flow<List`<Chat>`> - Combina chats con riesgos y estado de bloqueo

#### GetQuarantineChatsUseCase.kt

**Responsabilidad:**

* Obtiene chats de Cuarentena con sus factores de riesgo

**Dependencias:**

* ChatRepository
* DetectRiskFactorsUseCase
* BlockedSenderRepository

**Funciones:**

* `invoke()`: Flow<List`<Chat>`>

#### GetChatMessagesUseCase.kt

**Responsabilidad:**

* Obtiene mensajes de un chat específico

**Dependencias:**

* MessageRepository

**Funciones:**

* `invoke()`: Flow<List`<Message>`>

#### ClassifyChatUseCase.kt

**Responsabilidad:**

* Clasifica un chat como Inbox o Cuarentena

**Dependencias:**

* ContactRepository

**Funciones:**

* `invoke()`: suspend ChatType - Determina si remitente está en contactos

#### SendMessageUseCase.kt

**Responsabilidad:**

* Envía un SMS

**Dependencias:**

* MessageRepository
* ChatRepository

**Funciones:**

* `invoke()`: suspend Result`<Unit>` - Envía SMS y actualiza chat

#### ReceiveMessageUseCase.kt

**Responsabilidad:**

* Procesa SMS recibido

**Dependencias:**

* MessageRepository
* ChatRepository
* ClassifyChatUseCase
* BlockedSenderRepository

**Funciones:**

* `invoke()`: suspend Result`<Unit>` - Guarda mensaje, actualiza/crea chat, verifica bloqueo

#### MarkMessageAsReadUseCase.kt

**Responsabilidad:**

* Marca mensaje como leído

**Dependencias:**

* MessageRepository

**Funciones:**

* `invoke()`: suspend Result`<Unit>`

#### DeleteMessageUseCase.kt

**Responsabilidad:**

* Elimina mensaje

**Dependencias:**

* MessageRepository

**Funciones:**

* `invoke()`: suspend Result`<Unit>`

#### DetectRiskFactorsUseCase.kt

**Responsabilidad:**

* Analiza mensajes de un chat y detecta factores de riesgo

**Dependencias:**

* MessageRepository
* ContactRepository
* DetectLinksInMessageUseCase
* IsAlphanumericSenderUseCase

**Funciones:**

* `invoke()`: suspend List`<RiskFactor>` - Analiza remitente y contenido

#### DetectLinksInMessageUseCase.kt

**Responsabilidad:**

* Detecta presencia de URLs en texto

**Funciones:**

* `invoke()`: List`<String>` - Retorna lista de URLs encontradas

#### ValidateLinkSafetyUseCase.kt

**Responsabilidad:**

* Valida seguridad básica de un link (análisis local)

**Funciones:**

* `invoke()`: LinkSafetyInfo - Extrae dominio, verifica patrones sospechosos

#### IsAlphanumericSenderUseCase.kt

**Responsabilidad:**

* Verifica si remitente es alfanumérico

**Funciones:**

* `invoke()`: Boolean - Analiza si contiene letras

#### BlockSenderUseCase.kt

**Responsabilidad:**

* Bloquea un remitente

**Dependencias:**

* BlockedSenderRepository

**Funciones:**

* `invoke()`: suspend Result`<Unit>`

#### UnblockSenderUseCase.kt

**Responsabilidad:**

* Desbloquea un remitente

**Dependencias:**

* BlockedSenderRepository

**Funciones:**

* `invoke()`: suspend Result`<Unit>`

#### IsBlockedSenderUseCase.kt

**Responsabilidad:**

* Verifica si remitente está bloqueado

**Dependencias:**

* BlockedSenderRepository

**Funciones:**

* `invoke()`: suspend Boolean

#### GetBlockedSendersUseCase.kt

**Responsabilidad:**

* Obtiene lista de remitentes bloqueados

**Dependencias:**

* BlockedSenderRepository

**Funciones:**

* `invoke()`: Flow<List`<BlockedSender>`>

#### IsContactSavedUseCase.kt

**Responsabilidad:**

* Verifica si un número está en contactos

**Dependencias:**

* ContactRepository

**Funciones:**

* `invoke()`: suspend Boolean

#### SyncContactsUseCase.kt

**Responsabilidad:**

* Sincroniza contactos del sistema con BD local

**Dependencias:**

* ContactRepository

**Funciones:**

* `invoke()`: suspend Result`<Unit>`

#### GetContactByPhoneUseCase.kt

**Responsabilidad:**

* Obtiene contacto por teléfono

**Dependencias:**

* ContactRepository

**Funciones:**

* `invoke()`: suspend Contact?

#### ImportSmsHistoryUseCase.kt

**Responsabilidad:**

* Importa histórico completo de SMS del sistema

**Dependencias:**

* SmsSystemProvider (vía Repository)
* MessageRepository
* ChatRepository
* ClassifyImportedMessagesUseCase

**Funciones:**

* `invoke()`: suspend Result`<ImportProgress>` - Importa y clasifica SMS

#### ClassifyImportedMessagesUseCase.kt

**Responsabilidad:**

* Clasifica mensajes importados en Inbox/Cuarentena

**Dependencias:**

* ContactRepository
* ClassifyChatUseCase

**Funciones:**

* `invoke()`: suspend Result`<Unit>` - Analiza y clasifica chats creados

#### GetCountdownSecondsUseCase.kt

**Responsabilidad:**

* Obtiene segundos configurados para cuenta atrás

**Dependencias:**

* ConfigurationRepository

**Funciones:**

* `invoke()`: Flow`<Int>`

#### SetCountdownSecondsUseCase.kt

**Responsabilidad:**

* Configura segundos de cuenta atrás (3-8)

**Dependencias:**

* ConfigurationRepository

**Funciones:**

* `invoke()`: suspend Result`<Unit>` - Valida rango antes de guardar

#### GetQuarantineNotificationsEnabledUseCase.kt

**Responsabilidad:**

* Obtiene estado de notificaciones de Cuarentena

**Dependencias:**

* ConfigurationRepository

**Funciones:**

* `invoke()`: Flow`<Boolean>`

#### SetQuarantineNotificationsEnabledUseCase.kt

**Responsabilidad:**

* Habilita/deshabilita notificaciones de Cuarentena

**Dependencias:**

* ConfigurationRepository

**Funciones:**

* `invoke()`: suspend Result`<Unit>`

---

### 5.12 CAPA PRESENTATION - MainActivity

#### MainActivity.kt

**Responsabilidad:**

* Activity principal de la aplicación
* Host de navegación Compose
* Manejo de permisos

**Dependencias:**

* Jetpack Compose
* Navigation Compose
* Hilt

**Funciones:**

* `onCreate()`: Configura UI Compose, verifica permisos
* `requestSmsPermissions()`: Solicita permisos SMS y contactos
* `checkDefaultSmsApp()`: Verifica y solicita ser app SMS por defecto

---

### 5.13 CAPA PRESENTATION - Navigation

#### SafeSmsNavigation.kt

**Responsabilidad:**

* Define grafo de navegación de la app

**Funciones:**

* `SafeSmsNavHost()`: Composable - NavHost principal
* Define rutas y transiciones entre pantallas

#### Screen.kt

**Responsabilidad:**

* Sealed class con rutas de navegación

**Objetos:**

* `Onboarding`: Pantalla de onboarding
* `PermissionsSetup`: Configuración de permisos
* `ChatList`: Lista de chats (Inbox/Cuarentena)
* `ChatDetail`: Detalle de conversación
* `QuarantineWarning`: Advertencia de Cuarentena
* `Settings`: Ajustes

---

### 5.14 CAPA PRESENTATION - UI Components

#### AdBanner.kt

**Responsabilidad:**

* Composable para banner de AdMob

**Funciones:**

* `AdBanner()`: Composable - Muestra banner AdMob

#### CountdownTimer.kt

**Responsabilidad:**

* Composable para cuenta atrás visual

**Funciones:**

* `CountdownTimer()`: Composable - Muestra countdown animado
* Parámetros: seconds, onFinish, modifier

#### SecurityWarningDialog.kt

**Responsabilidad:**

* Composable para diálogos de advertencia genéricos

**Funciones:**

* `SecurityWarningDialog()`: Composable
* Parámetros: title, message, riskFactors, countdownSeconds, onConfirm, onDismiss

#### ChatListItem.kt

**Responsabilidad:**

* Composable para item de chat en lista

**Funciones:**

* `ChatListItem()`: Composable
* Muestra: nombre/número, último mensaje, hora, badge no leídos, indicadores de riesgo

#### MessageBubble.kt

**Responsabilidad:**

* Composable para burbuja de mensaje

**Funciones:**

* `MessageBubble()`: Composable
* Diferencia mensajes enviados/recibidos
* Detecta y marca enlaces como clickeables

#### RiskIndicatorChip.kt

**Responsabilidad:**

* Composable para chips de factores de riesgo

**Funciones:**

* `RiskIndicatorChip()`: Composable
* Parámetros: riskFactor, color

#### TabSelector.kt

**Responsabilidad:**

* Composable para selector Inbox/Cuarentena

**Funciones:**

* `TabSelector()`: Composable
* Parámetros: selectedTab, onTabSelected
* Aplica código de color verde/rojo

---

### 5.15 CAPA PRESENTATION - Screens

#### OnboardingScreen.kt

**Responsabilidad:**

* Pantalla de onboarding con vídeo explicativo

**Dependencias:**

* OnboardingViewModel

**Funciones:**

* `OnboardingScreen()`: Composable
* Reproduce vídeo obligatorio
* Botón "Continuar" deshabilitado hasta fin de vídeo

#### OnboardingViewModel.kt

**Responsabilidad:**

* Lógica de onboarding

**Dependencias:**

* ConfigurationRepository

**Funciones:**

* `markOnboardingCompleted()`: Guarda estado de onboarding completado
* `videoEnded`: StateFlow`<Boolean>` - Estado de reproducción

#### OnboardingState.kt

**Responsabilidad:**

* Data class para estado de onboarding

**Campos:**

* `videoCompleted`: Boolean
* `isNavigatingNext`: Boolean

#### PermissionsSetupScreen.kt

**Responsabilidad:**

* Pantalla de solicitud de permisos y configuración inicial

**Dependencias:**

* Activity para solicitar permisos

**Funciones:**

* `PermissionsSetupScreen()`: Composable
* Explica cada permiso requerido
* Inicia importación de histórico tras concesión

#### ChatListScreen.kt

**Responsabilidad:**

* Pantalla principal con lista de chats (Inbox/Cuarentena)

**Dependencias:**

* ChatListViewModel

**Funciones:**

* `ChatListScreen()`: Composable
* TabSelector en header
* LazyColumn de chats
* Banner AdMob en footer
* Maneja navegación a ChatDetail

#### ChatListViewModel.kt

**Responsabilidad:**

* Lógica de lista de chats

**Dependencias:**

* GetInboxChatsUseCase
* GetQuarantineChatsUseCase

**Funciones:**

* `loadInboxChats()`: Carga chats de Inbox
* `loadQuarantineChats()`: Carga chats de Cuarentena
* `switchTab()`: Cambia entre Inbox/Cuarantena
* `inboxChats`: StateFlow<List`<Chat>`>
* `quarantineChats`: StateFlow<List`<Chat>`>
* `selectedTab`: StateFlow`<ChatType>`

#### ChatListState.kt

**Responsabilidad:**

* Data class para estado de lista de chats

**Campos:**

* `inboxChats`: List`<Chat>`
* `quarantineChats`: List`<Chat>`
* `selectedTab`: ChatType
* `isLoading`: Boolean
* `error`: String?

#### ChatDetailScreen.kt

**Responsabilidad:**

* Pantalla de conversación individual

**Dependencias:**

* ChatDetailViewModel

**Funciones:**

* `ChatDetailScreen()`: Composable
* LazyColumn de mensajes (MessageBubbles)
* Barra inferior para escribir
* Maneja clicks en enlaces (dispara LinkWarningDialog)

#### ChatDetailViewModel.kt

**Responsabilidad:**

* Lógica de detalle de chat

**Dependencias:**

Continuar

**19:57**

**Orchestrated comprehensive architecture continuation strategy.**

El usuario está pidiendo que continúe con el detalle exhaustivo de la arquitectura. Debo seguir con el ChatDetailViewModel y completar todos los componentes de presentación, luego los flujos, permisos, roadmap, etc.

* GetChatMessagesUseCase
* SendMessageUseCase
* MarkMessageAsReadUseCase
* DetectLinksInMessageUseCase
* BlockSenderUseCase
* UnblockSenderUseCase

**Funciones:**

* `loadMessages()`: Carga mensajes del chat
* `sendMessage()`: Envía nuevo mensaje
* `onLinkClicked()`: Detecta click en link, dispara advertencia
* `blockSender()`: Bloquea remitente del chat
* `unblockSender()`: Desbloquea remitente
* `messages`: StateFlow<List`<Message>`>
* `chat`: StateFlow<Chat?>
* `messageInput`: StateFlow`<String>`
* `updateMessageInput()`: Actualiza texto del input

#### ChatDetailState.kt

**Responsabilidad:**

* Data class para estado de detalle de chat

**Campos:**

* `chat`: Chat?
* `messages`: List`<Message>`
* `messageInput`: String
* `isLoading`: Boolean
* `isSending`: Boolean
* `error`: String?

#### QuarantineWarningScreen.kt

**Responsabilidad:**

* Pantalla de advertencia al entrar a chat de Cuarentena

**Dependencias:**

* QuarantineWarningViewModel

**Funciones:**

* `QuarantineWarningScreen()`: Composable
* Muestra advertencia + factores de riesgo
* CountdownTimer
* Botón "Entrar igualmente" (bloqueado hasta countdown)
* Opción "Volver"

#### QuarantineWarningViewModel.kt

**Responsabilidad:**

* Lógica de advertencia de Cuarentena

**Dependencias:**

* GetCountdownSecondsUseCase
* GetChatMessagesUseCase (para detectar riesgos)
* DetectRiskFactorsUseCase

**Funciones:**

* `loadRiskFactors()`: Carga factores de riesgo del chat
* `startCountdown()`: Inicia countdown
* `canProceed`: StateFlow`<Boolean>`
* `countdownSeconds`: StateFlow`<Int>`
* `riskFactors`: StateFlow<List`<RiskFactor>`>

#### LinkWarningDialog.kt

**Responsabilidad:**

* Dialog de advertencia al pulsar link

**Dependencias:**

* LinkWarningViewModel

**Funciones:**

* `LinkWarningDialog()`: Composable
* Muestra URL, dominio, advertencia
* CountdownTimer
* Botón "Abrir enlace" (bloqueado hasta countdown)
* Botón "Cancelar"

#### LinkWarningViewModel.kt

**Responsabilidad:**

* Lógica de advertencia de link

**Dependencias:**

* ValidateLinkSafetyUseCase
* GetCountdownSecondsUseCase

**Funciones:**

* `validateLink()`: Analiza seguridad del link
* `startCountdown()`: Inicia countdown
* `openLink()`: Abre link en navegador tras countdown
* `canProceed`: StateFlow`<Boolean>`
* `linkSafety`: StateFlow<LinkSafetyInfo?>

#### SettingsScreen.kt

**Responsabilidad:**

* Pantalla de ajustes de la app

**Dependencias:**

* SettingsViewModel

**Funciones:**

* `SettingsScreen()`: Composable
* Slider de segundos de countdown (3-8)
* Switch de notificaciones de Cuarentena
* Emoticono disuasorio asociado al slider
* Lista de remitentes bloqueados (con opción de desbloquear)

#### SettingsViewModel.kt

**Responsabilidad:**

* Lógica de ajustes

**Dependencias:**

* GetCountdownSecondsUseCase
* SetCountdownSecondsUseCase
* GetQuarantineNotificationsEnabledUseCase
* SetQuarantineNotificationsEnabledUseCase
* GetBlockedSendersUseCase
* UnblockSenderUseCase

**Funciones:**

* `loadSettings()`: Carga configuración actual
* `updateCountdownSeconds()`: Actualiza segundos
* `toggleQuarantineNotifications()`: Cambia estado de notificaciones
* `unblockSender()`: Desbloquea remitente desde ajustes
* `countdownSeconds`: StateFlow`<Int>`
* `notificationsEnabled`: StateFlow`<Boolean>`
* `blockedSenders`: StateFlow<List`<BlockedSender>`>

#### SettingsState.kt

**Responsabilidad:**

* Data class para estado de ajustes

**Campos:**

* `countdownSeconds`: Int
* `quarantineNotificationsEnabled`: Boolean
* `blockedSenders`: List`<BlockedSender>`
* `isLoading`: Boolean

---

### 5.16 CAPA PRESENTATION - Utils

#### DateFormatter.kt

**Responsabilidad:**

* Formateo de fechas para UI

**Funciones:**

* `formatChatTimestamp()`: Formatea timestamp para lista de chats (Hoy, Ayer, dd/MM)
* `formatMessageTimestamp()`: Formatea timestamp para mensaje (HH:mm)

#### PhoneNumberFormatter.kt

**Responsabilidad:**

* Formateo y normalización de números de teléfono

**Funciones:**

* `formatPhoneNumber()`: Formatea número para mostrar en UI
* `normalizePhoneNumber()`: Normaliza para comparación (elimina espacios, guiones, etc.)

#### ResourceProvider.kt

**Responsabilidad:**

* Acceso a recursos Android desde ViewModels

**Dependencias:**

* Context

**Funciones:**

* `getString()`: Obtiene string resource
* `getStringArray()`: Obtiene array resource
* `getColor()`: Obtiene color resource

---

### 5.17 CAPA UTIL

#### Constants.kt

**Responsabilidad:**

* Constantes globales de la aplicación

**Constantes:**

* `MIN_COUNTDOWN_SECONDS = 3`
* `MAX_COUNTDOWN_SECONDS = 8`
* `DEFAULT_COUNTDOWN_SECONDS = 5`
* `SMS_TYPE_RECEIVED = 1`
* `SMS_TYPE_SENT = 2`
* `NOTIFICATION_CHANNEL_INBOX = "inbox_channel"`
* `NOTIFICATION_CHANNEL_QUARANTINE = "quarantine_channel"`
* `ADMOB_BANNER_ID = "ca-app-pub-xxxxx"`

#### Result.kt

**Responsabilidad:**

* Sealed class para manejo de resultados de operaciones

**Subclases:**

* `Success<T>`: data class con valor T
* `Error`: data class con exception

**Funciones:**

* Extension functions para mapeo, fold, etc.

#### Extensions.kt

**Responsabilidad:**

* Extension functions útiles

**Funciones:**

* `String.containsUrl()`: Detecta si string contiene URL
* `String.isAlphanumeric()`: Verifica si contiene letras y números
* `String.isShortCode()`: Verifica si es número corto (< 6 dígitos)
* `Flow<T>.asState()`: Convierte Flow a State para Compose

---

## 6. DESCRIPCIÓN DE PRESENTACIÓN

### 6.1 Pantallas principales

1. **OnboardingScreen**
   * Vídeo explicativo obligatorio
   * Explicación del concepto SafeSms
   * Transición automática a PermissionsSetup al finalizar
2. **PermissionsSetupScreen**
   * Solicitud de permisos SMS
   * Solicitud de permisos Contactos
   * Solicitud para ser app SMS por defecto
   * Importación automática de histórico
   * Progress indicator durante importación
3. **ChatListScreen** (Principal)
   * Header con TabSelector (Inbox/Cuarentena)
   * Lista de chats con ChatListItem
   * Código de color verde (Inbox) / rojo (Cuarentena)
   * Banner AdMob en footer
   * FAB para nuevo mensaje (opcional)
4. **QuarantineWarningScreen**
   * Aparece SIEMPRE antes de entrar a chat de Cuarentena
   * Advertencia clara
   * Lista de factores de riesgo
   * CountdownTimer
   * Botón "Entrar igualmente" (bloqueado)
   * Botón "Volver"
5. **ChatDetailScreen**
   * Header con nombre/número de contacto
   * LazyColumn de MessageBubbles
   * Barra inferior con TextField y botón enviar
   * Botón de opciones (bloquear/desbloquear)
   * Links clickeables que disparan LinkWarningDialog
6. **SettingsScreen**
   * Slider de countdown con emoticono
   * Switch de notificaciones de Cuarentena
   * Lista de remitentes bloqueados
   * Botón de desbloquear por remitente

### 6.2 Navegación

```
┌──────────────────┐
│  Onboarding      │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ PermissionsSetup │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐      ┌────────────────────┐
│   ChatList       │◄─────┤  QuarantineWarning │
│ (Inbox/Cuaranten)│      └────────┬───────────┘
└────────┬─────────┘               │
         │                         │
         ▼                         ▼
┌──────────────────┐      ┌────────────────────┐
│   ChatDetail     │      │    ChatDetail      │
│  (desde Inbox)   │      │ (desde Cuarentena) │
└────────┬─────────┘      └────────┬───────────┘
         │                         │
         └─────────┬───────────────┘
                   │
                   ▼
           ┌────────────────┐
           │    Settings    │
           └────────────────┘
```

**Flujo de navegación:**

* Onboarding → PermissionsSetup → ChatList (primera vez)
* ChatList es pantalla principal (siempre arranca aquí tras primer uso)
* Click en chat de Inbox → ChatDetail directo
* Click en chat de Cuarentena → QuarantineWarning → ChatDetail (si confirma)
* Menú/botón settings → SettingsScreen
* Click en link (cualquier pantalla) → LinkWarningDialog (modal)

### 6.3 Estados de UI

#### ChatListState

* **Loading** : Mostrando skeleton/shimmer de chats
* **Loaded** : Lista de chats visible
* **Empty** : Sin chats en categoría seleccionada
* **Error** : Error al cargar chats

#### ChatDetailState

* **Loading** : Cargando mensajes
* **Loaded** : Conversación visible
* **Sending** : Enviando mensaje (spinner en botón)
* **Error** : Error al cargar/enviar

#### QuarantineWarningState

* **CountdownActive** : Countdown en progreso, botón bloqueado
* **CountdownFinished** : Botón "Entrar" habilitado
* **Proceeding** : Navegando a ChatDetail

#### LinkWarningState

* **CountdownActive** : Countdown en progreso, botón bloqueado
* **CountdownFinished** : Botón "Abrir" habilitado
* **Opening** : Abriendo navegador

---

## 7. FLUJOS PRINCIPALES DE USUARIO

### 7.1 Primer arranque

```
Usuario instala SafeSms
    ↓
Abre app por primera vez
    ↓
OnboardingScreen
    ├─ Reproduce vídeo obligatorio
    └─ Botón "Continuar" bloqueado hasta fin
    ↓
PermissionsSetupScreen
    ├─ Solicita READ_SMS
    ├─ Solicita SEND_SMS
    ├─ Solicita READ_CONTACTS
    ├─ Solicita ser app SMS por defecto
    └─ Inicia importación de histórico
    ↓
Importación completa
    ├─ SyncContactsUseCase
    ├─ ImportSmsHistoryUseCase
    └─ ClassifyImportedMessagesUseCase
    ↓
Navegación a ChatListScreen (Inbox activo)
```

### 7.2 Recepción de SMS (runtime)

```
SMS llega al dispositivo
    ↓
SmsReceiver.onReceive()
    ↓
ReceiveMessageUseCase
    ├─ Extrae datos del SMS
    ├─ ClassifyChatUseCase (verifica si remitente en contactos)
    ├─ Guarda en MessageRepository
    └─ Crea/actualiza Chat en ChatRepository
    ↓
¿Remitente bloqueado?
    ├─ Sí → No muestra notificación, no actualiza UI de chat
    └─ No → Continuar
    ↓
¿Chat es Inbox?
    ├─ Sí → Notificación normal con texto
    └─ No (Cuarentena)
        ├─ ¿Notificaciones de Cuarentena habilitadas?
        │   ├─ Sí → Notificación genérica "Nuevo mensaje en Cuarentena"
        │   └─ No → Sin notificación
        └─ Actualiza lista de chats de Cuarentena
```

### 7.3 Usuario accede a chat de Inbox

```
Usuario en ChatListScreen (Inbox activo)
    ↓
Pulsa un chat
    ↓
Navegación directa a ChatDetailScreen
    ├─ Carga mensajes del chat
    ├─ Marca mensajes no leídos como leídos
    └─ Muestra conversación
    ↓
Usuario escribe mensaje y pulsa enviar
    ↓
SendMessageUseCase
    ├─ Envía SMS vía SmsManager
    ├─ Guarda en MessageRepository
    └─ Actualiza ChatRepository
    ↓
UI actualizada con mensaje enviado
```

### 7.4 Usuario accede a chat de Cuarentena

```
Usuario en ChatListScreen (Cuarentena activa)
    ↓
Pulsa un chat
    ↓
Navegación a QuarantineWarningScreen
    ├─ DetectRiskFactorsUseCase ejecutado
    ├─ Muestra advertencia
    ├─ Lista factores de riesgo
    └─ Inicia CountdownTimer (3-8 seg según config)
    ↓
¿Usuario espera countdown?
    ├─ Sí → Botón "Entrar igualmente" se habilita
    │       └─ Usuario pulsa → Navega a ChatDetailScreen
    └─ No → Pulsa "Volver" → Regresa a ChatListScreen
```

### 7.5 Usuario pulsa enlace (Inbox o Cuarentena)

```
Usuario en ChatDetailScreen (cualquier tipo)
    ↓
Pulsa enlace en mensaje
    ↓
LinkWarningDialog aparece
    ├─ ValidateLinkSafetyUseCase ejecutado
    ├─ Muestra URL completa
    ├─ Extrae dominio
    ├─ Muestra advertencia
    └─ Inicia CountdownTimer
    ↓
¿Usuario espera countdown?
    ├─ Sí → Botón "Abrir enlace" se habilita
    │       └─ Usuario pulsa → Abre navegador con URL
    └─ No → Pulsa "Cancelar" → Dialog se cierra
```

### 7.6 Usuario bloquea remitente

```
Usuario en ChatDetailScreen (Cuarentena)
    ↓
Pulsa menú → "Bloquear contacto"
    ↓
BlockSenderUseCase
    ├─ Guarda en BlockedSenderRepository
    └─ Actualiza estado del chat
    ↓
Chat aparece atenuado en lista de Cuarentena
Nuevos mensajes de ese remitente:
    ├─ No generan notificaciones
    └─ No se muestran en UI
```

### 7.7 Usuario desbloquea remitente

```
Usuario en SettingsScreen o ChatDetailScreen
    ↓
Pulsa "Desbloquear contacto"
    ↓
UnblockSenderUseCase
    ├─ Elimina de BlockedSenderRepository
    └─ Actualiza estado del chat
    ↓
Chat vuelve a estado normal en Cuarentena
Nuevos mensajes vuelven a procesarse normalmente
```

---

## 8. FLUJOS DE DATOS

### 8.1 Lectura de chats (UI → Domain → Data)

```
ChatListScreen (UI)
    ↓
ChatListViewModel observa
    ↓
GetInboxChatsUseCase / GetQuarantineChatsUseCase (Domain)
    ↓
ChatRepository (Domain interface)
    ↓
ChatRepositoryImpl (Data)
    ├─ ChatDao.getInboxChats() / getQuarantineChats()
    │   └─ Room Database (local)
    └─ ContactDao.isContactSaved()
    ↓
Flow<List<ChatEntity>> → Mapper → Flow<List<Chat>>
    ↓
DetectRiskFactorsUseCase (enriquece con riesgos)
    ↓
BlockedSenderRepository (verifica bloqueos)
    ↓
Flow<List<Chat>> con riesgos y estado de bloqueo
    ↓
ChatListViewModel.inboxChats / quarantineChats (StateFlow)
    ↓
ChatListScreen recompone con datos
```

### 8.2 Envío de mensaje (UI → Domain → Data → Sistema)

```
ChatDetailScreen (UI)
    ↓
Usuario escribe y pulsa enviar
    ↓
ChatDetailViewModel.sendMessage()
    ↓
SendMessageUseCase (Domain)
    ↓
MessageRepository.sendMessage() (Domain interface)
    ↓
MessageRepositoryImpl (Data)
    ├─ SmsSystemProvider.sendSms()
    │   └─ SmsManager.sendTextMessage() (Android System)
    └─ MessageDao.insertMessage()
        └─ Room Database (local)
    ↓
ChatRepository.updateChat()
    └─ Actualiza último mensaje del chat
    ↓
Result<Unit> retornado a ViewModel
    ↓
ChatDetailViewModel actualiza UI
```

### 8.3 Recepción de SMS (Sistema → Data → Domain → UI)

```
SMS llega al dispositivo (Android System)
    ↓
SmsReceiver.onReceive() (Data Layer)
    ├─ Extrae datos del Intent
    └─ Dispara procesamiento asíncrono
    ↓
ReceiveMessageUseCase (Domain)
    ├─ ClassifyChatUseCase
    │   └─ ContactRepository.isContactSaved()
    │       └─ ContactDao.isContactSaved() (Room)
    └─ BlockedSenderRepository.isBlocked()
    ↓
¿Bloqueado? → Sí → Finaliza
    ↓
No → MessageRepository.insertMessage()
    └─ MessageDao.insertMessage() (Room)
    ↓
ChatRepository.createOrUpdateChatFromMessage()
    └─ ChatDao.insertChat() / updateChat() (Room)
    ↓
Flow de Room emite cambio
    ↓
ViewModels con Flow activo se actualizan
    ↓
UI recompone automáticamente
```

### 8.4 Sincronización de contactos (Data → Sistema → Data)

```
SyncContactsUseCase ejecutado
    ↓
ContactRepository.syncContacts() (Domain interface)
    ↓
ContactRepositoryImpl (Data)
    ├─ ContactsSystemProvider.getAllContacts()
    │   └─ ContentResolver.query(ContactsContract) (Android System)
    ├─ Normaliza números de teléfono
    └─ ContactDao.deleteAllContacts() + insertContacts()
        └─ Room Database (local)
    ↓
Flow de ContactDao emite cambio
    ↓
Chats se reclasifican si es necesario
```

---

## 9. CAPACIDADES DEL SISTEMA ANDROID

### 9.1 Permisos requeridos (AndroidManifest.xml)

**Runtime Permissions (API 23+):**

* `READ_SMS`: Lectura de mensajes SMS del sistema
* `SEND_SMS`: Envío de mensajes SMS
* `RECEIVE_SMS`: Recepción de SMS en tiempo real
* `READ_CONTACTS`: Lectura de contactos para clasificación

**Special Permissions:**

* `ACTION_DEFAULT_SMS_APP`: Ser app SMS por defecto (obligatorio para leer/escribir SMS)

**Otros:**

* `INTERNET`: Para AdMob
* `ACCESS_NETWORK_STATE`: Para AdMob

### 9.2 ContentProviders

**SMS Provider:**

* Uri: `content://sms/`
* Uso: Lectura de histórico de SMS, escritura de SMS enviados
* Operaciones: query, insert, update, delete

**Contacts Provider:**

* Uri: `content://com.android.contacts/`
* Uso: Lectura de contactos para clasificación
* Operaciones: query

### 9.3 BroadcastReceivers

**SmsReceiver:**

* Intent Filter: `android.provider.Telephony.SMS_RECEIVED`
* Priority: Alta (para interceptar SMS antes que otras apps)
* Uso: Recepción de SMS en tiempo real

### 9.4 Notificaciones

**Canales de notificación (API 26+):**

* `inbox_channel`: Notificaciones de mensajes de Inbox
  * Importancia: Alta
  * Sonido: Habilitado
  * Vibración: Habilitada
* `quarantine_channel`: Notificaciones de mensajes de Cuarentena
  * Importancia: Baja
  * Sonido: Deshabilitado por defecto
  * Contenido: Genérico ("Nuevo mensaje en Cuarentena")

### 9.5 Almacenamiento local

**Room Database:**

* Nombre: `safesms_database`
* Versión: 1 (incrementar con migraciones)
* Ubicación: Almacenamiento interno de la app
* Exportación: Deshabilitada para seguridad

**DataStore:**

* Tipo: Preferences DataStore
* Nombre: `user_preferences`
* Ubicación: Almacenamiento interno de la app

### 9.6 Background tasks

**WorkManager (opcional para futuras versiones):**

* `SyncContactsWorker`: Sincronización periódica de contactos
* `CleanupOldMessagesWorker`: Limpieza de mensajes antiguos (opcional)
* Constraints: Requiere batería y red Wi-Fi

### 9.7 Intents externos

**ACTION_VIEW (para abrir enlaces):**

* Uso: Abrir URLs en navegador tras advertencia
* Intent: `Intent(Intent.ACTION_VIEW, Uri.parse(url))`

---

## 10. CONSIDERACIONES TÉCNICAS

### 10.1 Ciclo de vida Android

**Activity:**

* `onCreate()`: Inicialización de Compose, permisos
* `onResume()`: Verificación de permisos, actualización de UI
* `onPause()`: Cancelación de timers activos
* Configuration changes: Manejados por Compose (sin recreación)

**ViewModels:**

* Scope: ViewModelScope (sobrevive a configuration changes)
* Limpieza: `onCleared()` cancela coroutines activas

**Flows:**

* Recolección: `collectAsStateWithLifecycle()` en Composables
* Lifecycle-aware: Se pausan en onStop, se reanudan en onStart

### 10.2 Persistencia y estado

**Room Database:**

* Migrations: Definir estrategia para cambios de esquema
* Transacciones: Operaciones múltiples (inserción de chat + mensajes) en transacción
* Índices: Añadir en campos de búsqueda frecuente (address, timestamp)

**DataStore:**

* Thread-safe: Operaciones atómicas
* Observación reactiva: Flow`<T>` para cambios en tiempo real

**Estado en ViewModels:**

* StateFlow para estado observable
* Immutable data classes para estados de UI
* Transformaciones con `map`, `combine` para flows complejos

### 10.3 Seguridad y privacidad

**Datos sensibles:**

* SMS almacenados solo localmente (Room)
* No se envían datos a servidores externos
* Backup de Android deshabilitado para Room

**Permisos:**

* Solicitud gradual (solo cuando se necesitan)
* Explicación clara antes de solicitar
* Manejo de denegación (degradación elegante)

**Enlaces:**

* Validación local (no se envían URLs a servidores)
* Parseo de dominio visible al usuario
* Advertencia obligatoria sin excepciones

**AdMob:**

* Cumplimiento de GDPR/CCPA (consent form obligatorio en producción)
* Personalización de anuncios según preferencias de usuario

### 10.4 Escalabilidad y mantenibilidad

**Modularización futura:**

* Posibilidad de extraer capas en módulos Gradle separados:
  * `:core:domain`
  * `:core:data`
  * `:feature:chatlist`
  * `:feature:chatdetail`

**Testing:**

* Unit tests para Use Cases (lógica pura)
* Integration tests para Repositories
* UI tests para flujos críticos (advertencias, bloqueos)

**Inyección de dependencias:**

* Hilt facilita cambio de implementaciones
* Interfaces en domain permiten mocking en tests

**Compose:**

* Composables pequeños y reutilizables
* Preview functions para desarrollo rápido
* Themed components para consistencia visual

### 10.5 Manejo de errores

**Result wrapper:**

* Todas las operaciones suspend retornan `Result<T>`
* `Result.Success<T>` o `Result.Error(exception)`
* ViewModels manejan errores y actualizan UI state

**Excepciones comunes:**

* `SecurityException`: Permisos no concedidos
* `SQLException`: Errores de base de datos
* `NetworkException`: Fallos en AdMob (no crítico)

**Logs:**

* Timber o similar para logging estructurado
* Niveles: ERROR para fallos críticos, INFO para eventos, DEBUG para desarrollo

**UI de error:**

* Snackbars para errores transitorios
* Dialogs para errores que requieren acción
* Empty states para listas vacías por error

### 10.6 Funcionamiento offline

**Completamente offline:**

* SafeSms no requiere conexión a internet para funcionalidad core
* SMS y contactos son datos locales del sistema
* AdMob: Maneja fallos de red gracefully (no muestra banner si falla)

**Sincronización:**

* Contactos: Se sincronizan al inicio y opcionalmente en background
* SMS: Siempre disponibles localmente

---

## 11. DECISIONES ABIERTAS, RIESGOS Y SUPUESTOS

### 11.1 Decisiones abiertas

1. **Estrategia de backup/restore:**
   * ¿Permitir backup manual de chats?
   * ¿Exportar a archivo encriptado?
   * **Decisión pendiente** : Definir en Fase 2
2. **Análisis avanzado de phishing:**
   * ¿Integrar API de análisis de URLs (VirusTotal, Google Safe Browsing)?
   * **Supuesto actual** : Solo análisis local (patrones sospechosos)
   * **Decisión pendiente** : Evaluar impacto en privacidad y costo
3. **Idiomas:**
   * ¿Soportar múltiples idiomas desde MVP?
   * **Decisión pendiente** : MVP solo en español, internacionalización en Fase 3
4. **Límite de mensajes históricos:**
   * ¿Importar TODO el histórico sin límite?
   * ¿Establecer límite (ej. últimos 6 meses)?
   * **Supuesto actual** : Importar todos (puede afectar performance)
   * **Decisión pendiente** : Validar con testing de performance
5. **Modo oscuro:**
   * ¿Implementar desde MVP?
   * **Decisión pendiente** : Fase 2, seguir tema del sistema

### 11.2 Riesgos técnicos

1. **Fragmentación de Android:**
   * **Riesgo** : Comportamiento inconsistente de SMS API entre fabricantes (Samsung, Xiaomi, etc.)
   * **Mitigación** : Testing exhaustivo en dispositivos reales de múltiples marcas
   * **Impacto** : Alto
2. **Permisos SMS en Android 10+:**
   * **Riesgo** : Restricciones más estrictas para apps SMS por defecto
   * **Mitigación** : Cumplir estrictamente requisitos de Google Play
   * **Impacto** : Crítico para publicación
3. **Performance con históricos grandes:**
   * **Riesgo** : Importación de 10,000+ SMS puede bloquear UI
   * **Mitigación** : Procesamiento por lotes con suspend functions, progress indicator
   * **Impacto** : Medio
4. **Detección de enlaces:**
   * **Riesgo** : Links ofuscados (bit.ly, acortadores) no son detectables como maliciosos
   * **Mitigación** : Advertir en TODOS los enlaces, sin distinción
   * **Impacto** : Bajo (diseño ya contempla esto)
5. **AdMob revenue:**
   * **Riesgo** : Revenue insuficiente para sostener desarrollo
   * **Mitigación** : Monitorear métricas, considerar modelo freemium en futuro
   * **Impacto** : Bajo a medio (no afecta funcionalidad)

### 11.3 Supuestos

1. **Usuarios objetivo:**
   * Usuarios Android con conocimiento básico de SMS
   * Vulnerables a phishing pero dispuestos a esperar 3-8 segundos
   * No necesitan cliente SMS con funciones avanzadas (MMS, grupos, etc.)
2. **Comportamiento del sistema:**
   * ContentProvider de SMS es consistente entre versiones de Android
   * SmsManager funciona correctamente en todos los dispositivos
   * Contactos sincronizados del sistema están actualizados
3. **Recursos del dispositivo:**
   * Dispositivos con mínimo 2GB RAM
   * Android 6.0 (API 23) o superior
   * Espacio de almacenamiento suficiente para histórico de SMS
4. **Cumplimiento legal:**
   * App cumple con políticas de Google Play para apps SMS
   * No se requiere consentimiento especial para almacenar SMS localmente
   * AdMob cumple con regulaciones de publicidad
5. **UX de countdown:**
   * Usuarios aceptarán esperar 3-8 segundos antes de acciones críticas
   * La fricción mejora la seguridad sin frustrar excesivamente

---

## 12. ROADMAP DE IMPLEMENTACIÓN POR FASES

### FASE 1: NÚCLEO FUNCIONAL / MVP (4-6 semanas)

**Objetivo:** App funcional con flujos críticos de seguridad

#### Módulos a implementar:

**Data Layer:**

* ✅ Room Database (SafeSmsDatabase, todos los DAOs y Entities)
* ✅ UserPreferencesDataStore
* ✅ SmsSystemProvider (lectura/escritura SMS)
* ✅ ContactsSystemProvider (lectura contactos)
* ✅ Todos los Repositories (implementaciones)
* ✅ Mappers básicos

**Domain Layer:**

* ✅ Todas las entidades de dominio (Message, Chat, Contact, etc.)
* ✅ Interfaces de repositorios
* ✅ Use Cases críticos:
  * ClassifyChatUseCase
  * GetInboxChatsUseCase
  * GetQuarantineChatsUseCase
  * GetChatMessagesUseCase
  * SendMessageUseCase
  * ReceiveMessageUseCase
  * DetectRiskFactorsUseCase
  * DetectLinksInMessageUseCase
  * IsContactSavedUseCase
  * ImportSmsHistoryUseCase
  * ClassifyImportedMessagesUseCase

**Presentation Layer:**

* ✅ MainActivity
* ✅ Navigation (SafeSmsNavigation, Screen)
* ✅ Theme básico (Color, Theme, Type)
* ✅ Componentes esenciales:
  * CountdownTimer
  * SecurityWarningDialog
  * ChatListItem
  * MessageBubble
  * TabSelector
* ✅ Pantallas MVP:
  * OnboardingScreen + ViewModel + State
  * PermissionsSetupScreen
  * ChatListScreen + ViewModel + State
  * ChatDetailScreen + ViewModel + State
  * QuarantineWarningScreen + ViewModel + State
  * LinkWarningDialog + ViewModel

**DI:**

* ✅ Todos los módulos Hilt

**System:**

* ✅ SmsReceiver (recepción de SMS en tiempo real)
* ✅ Configuración de permisos en Manifest

**Testing básico:**

* ✅ ClassifyChatUseCaseTest
* ✅ DetectRiskFactorsUseCaseTest
* ✅ MessageRepositoryImplTest (simulado con mocks)

#### Funcionalidades completas:

* ✅ Onboarding con vídeo obligatorio
* ✅ Solicitud de permisos y configuración inicial
* ✅ Importación de histórico completo de SMS
* ✅ Clasificación Inbox/Cuarentena
* ✅ Lista de chats con Tab Selector
* ✅ Acceso directo a chats de Inbox
* ✅ Advertencia obligatoria para chats de Cuarentena
* ✅ Advertencia obligatoria para enlaces
* ✅ Envío y recepción de SMS
* ✅ Countdown timer funcionando (3-8 segundos)

#### NO incluido en Fase 1:

* ❌ AdMob (integración en Fase 2)
* ❌ Bloqueo de remitentes (Fase 2)
* ❌ SettingsScreen (Fase 2)
* ❌ Notificaciones avanzadas (Fase 2)
* ❌ Tests UI automatizados (Fase 2)

---

### FASE 2: MEJORAS FUNCIONALES Y UX (3-4 semanas)

**Objetivo:** Completar funcionalidades y pulir experiencia de usuario

#### Módulos a implementar:

**Domain Layer:**

* ✅ Use Cases de bloqueo:
  * BlockSenderUseCase
  * UnblockSenderUseCase
  * IsBlockedSenderUseCase
  * GetBlockedSendersUseCase
* ✅ Use Cases de configuración:
  * GetCountdownSecondsUseCase
  * SetCountdownSecondsUseCase
  * GetQuarantineNotificationsEnabledUseCase
  * SetQuarantineNotificationsEnabledUseCase

**Data Layer:**

* ✅ Lógica de bloqueo en ReceiveMessageUseCase
* ✅ NotificationManager wrapper

**Presentation Layer:**

* ✅ SettingsScreen + ViewModel + State
* ✅ AdBanner component
* ✅ RiskIndicatorChip component
* ✅ Mejoras en ChatListItem (indicadores de bloqueo)
* ✅ Opciones de bloqueo en ChatDetailScreen

**Integraciones:**

* ✅ Google AdMob SDK
  * Configuración de banner IDs
  * Integración en ChatListScreen
  * Manejo de fallos de red
* ✅ Sistema de notificaciones
  * Canales de notificación
  * Notificaciones diferenciadas Inbox/Cuarentena
  * Toggle para desactivar notificaciones de Cuarentena

**Testing:**

* ✅ BlockSenderUseCaseTest
* ✅ ConfigurationUseCasesTests
* ✅ Tests de integración para flujo de bloqueo

#### Funcionalidades completas:

* ✅ Bloqueo y desbloqueo de remitentes
* ✅ Chat bloqueado aparece atenuado en lista
* ✅ Pantalla de ajustes funcional
* ✅ Slider de countdown con emoticono
* ✅ Monetización con AdMob
* ✅ Notificaciones diferenciadas
* ✅ Toggle de notificaciones de Cuarentena

---

### FASE 3: OPTIMIZACIÓN, ESCALADO Y HARDENING (2-3 semanas)

**Objetivo:** Preparar para producción y escala

#### Testing exhaustivo:

* ✅ Tests UI con Compose Testing
  * ChatListScreenTest
  * QuarantineWarningScreenTest
  * LinkWarningDialogTest
* ✅ Tests de integración end-to-end
  * SmsFlowIntegrationTest (envío y recepción completa)
* ✅ Tests de performance
  * Importación de 10,000+ SMS
  * Scroll de listas largas

#### Optimizaciones:

* ✅ Paginación en Room para listas grandes
* ✅ Lazy loading de mensajes en ChatDetail
* ✅ Índices en base de datos
* ✅ Proguard/R8 optimization
* ✅ Reducción de tamaño de APK

#### Hardening:

* ✅ Manejo exhaustivo de errores
* ✅ Logging estructurado (Timber)
* ✅ Crash reporting (Firebase Crashlytics opcional)
* ✅ Validación de inputs
* ✅ Sanitización de datos de SMS

#### Cumplimiento:

* ✅ Revisión de políticas de Google Play para apps SMS
* ✅ Implementación de consent form de AdMob (GDPR/CCPA)
* ✅ Política de privacidad
* ✅ Términos de servicio

#### Mejoras opcionales:

* ⚪ Modo oscuro (seguir tema del sistema)
* ⚪ Búsqueda de mensajes
* ⚪ Backup/restore manual de chats
* ⚪ Internacionalización (i18n)
* ⚪ WorkManager para sync en background

---

### FASE 4: LANZAMIENTO Y MONITOREO (1-2 semanas)

**Objetivo:** Publicar en Google Play y monitorear

#### Pre-lanzamiento:

* ✅ Testing en dispositivos físicos de múltiples marcas
  * Samsung, Xiaomi, OnePlus, Google Pixel, etc.
* ✅ Beta testing con usuarios reales (Google Play Console)
* ✅ Preparación de assets de Play Store
  * Screenshots
  * Íconos
  * Descripción optimizada para SEO

#### Lanzamiento:

* ✅ Publicación en Google Play
* ✅ Release notes
* ✅ Estrategia de marketing básica

#### Post-lanzamiento:

* ✅ Monitoreo de crashes (Crashlytics)
* ✅ Análisis de métricas AdMob
* ✅ Feedback de usuarios (reviews)
* ✅ Hotfixes si es necesario
* ✅ Plan de actualizaciones futuras

---

## 13. PREPARACIÓN PARA GENERACIÓN DE CÓDIGO

### 13.1 Garantías de diseño

Este diseño arquitectónico garantiza:

1. **Responsabilidades claras:**
   * Cada archivo tiene una única responsabilidad bien definida
   * No hay ambigüedad sobre dónde colocar nueva funcionalidad
2. **Nombres definitivos:**
   * Todos los nombres de clases, funciones y archivos están finalizados
   * Convención de nombres consistente (UseCase, Repository, ViewModel, etc.)
3. **Interfaces explícitas:**
   * Todas las dependencias están declaradas
   * Contratos entre capas están definidos (interfaces de repositorios)
4. **Flujos completos:**
   * Cada flujo de usuario está mapeado a componentes específicos
   * No hay "puntos ciegos" funcionales
5. **Estructura compatible:**
   * Árbol de carpetas 100% compatible con Android Studio
   * Organización estándar de proyectos Gradle con Kotlin

### 13.2 Próximos pasos para implementación

**Al generar código, seguir este orden:**

1. **Setup inicial:**
   * build.gradle.kts (dependencias)
   * AndroidManifest.xml (permisos, receiver)
   * SafeSmsApplication.kt
2. **Data Layer (bottom-up):**
   * Entities → DAOs → Database
   * System providers (SMS, Contacts)
   * Mappers
   * Repository implementations
3. **Domain Layer:**
   * Models
   * Repository interfaces
   * Use Cases (empezar por los más simples)
4. **DI:**
   * Módulos Hilt en orden: Database → Repository → UseCase → App
5. **Presentation Layer:**
   * Theme y componentes reutilizables
   * Navigation
   * Screens y ViewModels (empezar por Onboarding)
6. **Testing:**
   * Unit tests para Use Cases
   * Integration tests para Repositories
   * UI tests para pantallas críticas

### 13.3 Sin ambigüedades

* **No hay decisiones pendientes críticas** para MVP (Fase 1)
* **Todas las funciones tienen propósito claro** y parámetros definidos
* **Dependencias entre módulos son explícitas**
* **Flujos de datos están completamente trazados**
* **Manejo de errores tiene estrategia definida**

---

## RESUMEN EJECUTIVO

**SafeSms** es una aplicación Android de cliente SMS con firewall cognitivo contra phishing, construida con:

* **Kotlin 100%** + **Jetpack Compose**
* **Clean Architecture + MVVM**
* **Room** (persistencia) + **Hilt** (DI) + **Coroutines/Flow** (async)
* **AdMob** (monetización)

**Arquitectura en 3 capas:**

1. **Presentation** : Compose UI + ViewModels
2. **Domain** : Use Cases + Entities + Repository Interfaces
3. **Data** : Repository Implementations + Room + System Providers

**Funcionalidades core:**

* Clasificación automática Inbox/Cuarentena (basada en contactos)
* Advertencias obligatorias con countdown para chats y enlaces peligrosos
* Bloqueo reversible de remitentes
* Sistema de notificaciones diferenciado
* Monetización no intrusiva con AdMob

**Roadmap:**

* **Fase 1 (MVP)** : Núcleo funcional completo
* **Fase 2** : Bloqueo, ajustes, AdMob, notificaciones
* **Fase 3** : Testing, optimización, hardening
* **Fase 4** : Lanzamiento y monitoreo

**El diseño está listo para implementación directa sin ambigüedades.**

---

**FIN DEL DOCUMENTO DE ARQUITECTURA**
