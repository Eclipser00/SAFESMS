package com.safesms.presentation.screen.onboarding

/**
 * Data class para estado de onboarding
 */
data class OnboardingState(
    val videoCompleted: Boolean = false,
    val isNavigatingNext: Boolean = false
)

