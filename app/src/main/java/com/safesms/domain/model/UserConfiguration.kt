package com.safesms.domain.model

/**
 * Configuración de usuario
 */
data class UserConfiguration(
    val countdownSeconds: Int = 5,
    val quarantineNotificationsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val smsHistoryImported: Boolean = false
)
