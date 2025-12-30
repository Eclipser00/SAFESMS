package com.safesms.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta principal inspirada en los mockups (seguridad bancaria, tonos verdes y rojos)
val HeaderDark = Color(0xFF242B35)
val HeaderDarkEnd = Color(0xFF303947)

val Primary = Color(0xFF2E7D63) // Verde institucional para Inbox/acciones seguras
val Secondary = Color(0xFFB01217) // Rojo intenso para Cuarentena/alertas
val WarningOrange = Color(0xFFF5A623)

// Estados
val InboxGreen = Primary
val InboxGreenDark = Color(0xFF1F5C47)
val QuarantineRed = Secondary
val QuarantineRedDark = Color(0xFF7A0C0F)
val DangerGlow = Color(0xFFE84B3C)

// Superficies
val Background = Color(0xFFF4F5F8)
val Surface = Color(0xFFFFFFFF)
val SurfaceMuted = Color(0xFFEAECEF)
val SurfaceStroke = Color(0xFFE1E4EA)
val BlockedBackground = Color(0xFFCDCFD3)
val BlockedSurface = Color(0xFFBFC2C6)
val BlockedStroke = Color(0xFF9FA3A8)
val QuarantineSurface = Color(0xFF3A0609)
val QuarantineSurfaceAlt = Color(0xFF50090D)
val QuarantineBorder = Color(0xFF8E1013)

// Texto
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1B1F24)
val OnBackground = Color(0xFF1B1F24)
val MutedText = Color(0xFF6A7079)
val OnError = Color(0xFFFFFFFF)

// Mensajes
val SentMessageBackground = InboxGreen
val ReceivedMessageBackground = Color(0xFFE2E5EA)
val SentMessageText = Color(0xFFFFFFFF)
val ReceivedMessageText = Color(0xFF11151A)

// Chips de riesgo
val RiskHigh = DangerGlow
val RiskMedium = WarningOrange
val RiskLow = Color(0xFFF4C542)

// Auxiliares
val DisabledControl = Color(0xFFB6BCC7)
val DividerSoft = Color(0xFFCDD1D9)
val ModalBackdrop = Color(0xCC1B1F24)
val DangerBackgroundTop = QuarantineRed
val DangerBackgroundBottom = Color(0xFF52060A)
val Error = QuarantineRed

