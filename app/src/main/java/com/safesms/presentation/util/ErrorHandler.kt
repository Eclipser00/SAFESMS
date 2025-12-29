package com.safesms.presentation.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.safesms.R
import java.sql.SQLException

/**
 * Maneja errores globalmente y muestra mensajes apropiados
 */
object ErrorHandler {
    
    @Composable
    fun HandleError(
        error: Throwable?,
        snackbarHostState: SnackbarHostState,
        onPermissionError: (() -> Unit)? = null
    ) {
        val context = LocalContext.current
        
        LaunchedEffect(error) {
            error?.let {
                val message = when (it) {
                    is SecurityException -> {
                        onPermissionError?.invoke()
                        context.getString(R.string.error_permissions)
                    }
                    is SQLException -> context.getString(R.string.error_database)
                    else -> context.getString(R.string.error_generic)
                }
                
                snackbarHostState.showSnackbar(message)
            }
        }
    }
}

