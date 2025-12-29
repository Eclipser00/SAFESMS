package com.safesms.presentation.screen.settings

import com.safesms.domain.model.BlockedSender

/**
 * Data class para estado de ajustes
 */
data class SettingsState(
    val countdownSeconds: Int = 5,
    val quarantineNotificationsEnabled: Boolean = true,
    val blockedSenders: List<BlockedSender> = emptyList(),
    val isLoading: Boolean = false
)

