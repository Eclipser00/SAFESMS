package com.safesms.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.safesms.presentation.navigation.SafeSmsNavigation
import com.safesms.presentation.ui.theme.SafeSmsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principal de la aplicación
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Manejar resultado de permisos
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SafeSmsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SafeSmsNavigation()
                }
            }
        }
    }

    /**
     * Solicita permisos SMS y contactos
     */
    fun requestSmsPermissions() {
        val permissions = mutableListOf<String>().apply {
            add(Manifest.permission.READ_SMS)
            add(Manifest.permission.SEND_SMS)
            add(Manifest.permission.RECEIVE_SMS)
            add(Manifest.permission.READ_CONTACTS)
        }
        
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    /**
     * Verifica si la app es la app SMS por defecto
     */
    fun checkDefaultSmsApp(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            packageName == Telephony.Sms.getDefaultSmsPackage(this)
        } else {
            true
        }
    }

    /**
     * Solicita ser app SMS por defecto
     */
    fun requestDefaultSmsApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            }
            startActivity(intent)
        }
    }

    /**
     * Verifica si los permisos están concedidos
     */
    fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

