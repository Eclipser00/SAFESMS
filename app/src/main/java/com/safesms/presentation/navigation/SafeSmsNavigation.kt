package com.safesms.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safesms.domain.model.ChatType
import com.safesms.presentation.screen.chatdetail.ChatDetailScreen
import com.safesms.presentation.screen.chatlist.ChatListScreen
import com.safesms.presentation.screen.chatlist.ChatListViewModel
import com.safesms.presentation.screen.onboarding.OnboardingScreen
import com.safesms.presentation.screen.onboarding.PermissionsSetupScreen
import com.safesms.presentation.screen.security.LinkWarningDialog
import com.safesms.presentation.screen.security.QuarantineWarningScreen
import com.safesms.presentation.screen.settings.SettingsScreen

/**
 * NavHost principal de la aplicación
 */
@Composable
fun SafeSmsNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navigationViewModel: NavigationViewModel = hiltViewModel()
    // Verificar si onboarding está completado
    val onboardingCompleted by navigationViewModel.onboardingCompleted.collectAsStateWithLifecycle(initialValue = false)
    
    // Usar remember para mantener startDestination fijo después de la primera composición
    // Siempre empezamos en Onboarding y navegamos si es necesario
    val startDestination = remember { Screen.Onboarding.route }
    
    // Navegar a ChatList si el onboarding ya está completado (solo una vez al inicio)
    var hasNavigated by remember { mutableStateOf(false) }
    LaunchedEffect(onboardingCompleted) {
        if (onboardingCompleted && !hasNavigated) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute == null || currentRoute == Screen.Onboarding.route) {
                hasNavigated = true
                navController.navigate(Screen.ChatList.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
        }
    }
    
    var showLinkWarningDialog by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToPermissions = {
                    navController.navigate(Screen.PermissionsSetup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Permissions Setup Screen
        composable(Screen.PermissionsSetup.route) {
            PermissionsSetupScreen(
                onNavigateToChatList = {
                    navController.navigate(Screen.ChatList.route) {
                        popUpTo(Screen.PermissionsSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Chat List Screen
        composable(Screen.ChatList.route) {
            val viewModel: ChatListViewModel = hiltViewModel()
            ChatListScreen(
                onChatClick = { threadId, chatType ->
                    // Si es Cuarentena, navegar primero a QuarantineWarning
                    if (chatType == ChatType.QUARANTINE) {
                        navController.navigate(Screen.QuarantineWarning.createRoute(threadId))
                    } else {
                        // Si es Inbox, navegar directamente a ChatDetail
                        navController.navigate(Screen.ChatDetail.createRoute(threadId))
                    }
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                viewModel = viewModel
            )
        }

        // Chat Detail Screen
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(navArgument("threadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: return@composable
            
            ChatDetailScreen(
                threadId = threadId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLinkClicked = { url ->
                    showLinkWarningDialog = url
                }
            )
            
            // Mostrar LinkWarningDialog si hay URL
            showLinkWarningDialog?.let { url ->
                LinkWarningDialog(
                    url = url,
                    onDismiss = {
                        showLinkWarningDialog = null
                    }
                )
            }
        }

        // Quarantine Warning Screen
        composable(
            route = Screen.QuarantineWarning.route,
            arguments = listOf(navArgument("threadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: return@composable
            
            QuarantineWarningScreen(
                threadId = threadId,
                onNavigateToChatDetail = {
                    navController.navigate(Screen.ChatDetail.createRoute(threadId)) {
                        popUpTo(Screen.QuarantineWarning.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

