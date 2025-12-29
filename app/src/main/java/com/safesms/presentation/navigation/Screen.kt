package com.safesms.presentation.navigation

/**
 * Sealed class con rutas de navegación.
 * 
 * ACTUALIZADO: Usa threadId en lugar de chatId.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object PermissionsSetup : Screen("permissions_setup")
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail/{threadId}") {
        fun createRoute(threadId: Long) = "chat_detail/$threadId"
    }
    object QuarantineWarning : Screen("quarantine_warning/{threadId}") {
        fun createRoute(threadId: Long) = "quarantine_warning/$threadId"
    }
    object Settings : Screen("settings")
}
