package com.safesms.presentation.screen.onboarding

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import android.util.Log

/**
 * Pantalla de solicitud de permisos y configuración inicial
 */
@Composable
fun PermissionsSetupScreen(
    onNavigateToChatList: () -> Unit,
    viewModel: PermissionsSetupViewModel = hiltViewModel<PermissionsSetupViewModel>()
) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }
    var isDefaultSmsApp by remember { mutableStateOf(false) }
    
    val importProgress by viewModel.importProgress.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val importError by viewModel.importError.collectAsStateWithLifecycle()
    
    // Función para verificar si es app SMS por defecto
    fun checkIfDefaultSmsApp(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10+: Usar RoleManager
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                roleManager?.isRoleHeld(RoleManager.ROLE_SMS) ?: false
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                // Android 4.4-9: Usar Telephony
                Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
            }
            else -> {
                // Android < 4.4: No existe concepto de app SMS por defecto
                true
            }
        }
    }
    
    // Verificar estado inicial
    LaunchedEffect(Unit) {
        isDefaultSmsApp = checkIfDefaultSmsApp()
    }
    
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionsGranted = allGranted
        
        if (allGranted) {
            // Verificar si es app SMS por defecto antes de importar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                isDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
            } else {
                isDefaultSmsApp = true
            }
            
            if (isDefaultSmsApp) {
                // Iniciar importación de histórico
                viewModel.startImport()
            }
        }
    }
    
    val requestDefaultSmsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("PermissionsSetup", "Resultado del launcher recibido: ${result.resultCode}")
        // Verificar nuevamente el estado
        isDefaultSmsApp = checkIfDefaultSmsApp()
        Log.d("PermissionsSetup", "Estado después del launcher - isDefaultSmsApp: $isDefaultSmsApp")
        
        if (isDefaultSmsApp) {
            Log.d("PermissionsSetup", "App configurada como SMS por defecto, iniciando importación")
            viewModel.startImport()
        } else {
            Log.d("PermissionsSetup", "App NO configurada como SMS por defecto")
        }
    }
    
    // Re-verificar cuando la pantalla vuelve a estar activa
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val listener = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultSmsApp = checkIfDefaultSmsApp()
                // Si ya es app por defecto y permisos concedidos, iniciar importación
                if (isDefaultSmsApp && permissionsGranted && !isImporting && importProgress.isEmpty()) {
                    viewModel.startImport()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(listener)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(listener)
        }
    }
    
    LaunchedEffect(Unit) {
        val permissionsToRequest = listOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.READ_CONTACTS
        )
        
        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            permissionsGranted = true
            if (isDefaultSmsApp) {
                viewModel.startImport()
            }
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    
    // Navegar cuando la importación se complete exitosamente
    LaunchedEffect(isImporting, importProgress) {
        if (!isImporting && importProgress == "¡Importación completada!") {
            delay(1000) // Esperar un segundo para mostrar el mensaje
            onNavigateToChatList()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Configuración de Permisos",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isImporting || importProgress.isNotEmpty()) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = importProgress.ifEmpty { "Importando histórico de SMS..." },
                style = MaterialTheme.typography.bodyMedium
            )
            
            importError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (!isDefaultSmsApp && permissionsGranted) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "SafeSMS necesita ser tu aplicación SMS por defecto para funcionar correctamente.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = {
                        Log.d("PermissionsSetup", "Botón pulsado - Intentando configurar app SMS por defecto")
                        Log.d("PermissionsSetup", "Package name: ${context.packageName}")
                        Log.d("PermissionsSetup", "SDK Version: ${Build.VERSION.SDK_INT}")
                        Log.d("PermissionsSetup", "isDefaultSmsApp actual: $isDefaultSmsApp")
                        Log.d("PermissionsSetup", "permissionsGranted: $permissionsGranted")
                        
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                // Android 10+ (API 29+): Usar RoleManager
                                Log.d("PermissionsSetup", "Usando RoleManager (Android 10+)")
                                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                                if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                                    if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                                        Log.d("PermissionsSetup", "Intent RoleManager creado")
                                        requestDefaultSmsLauncher.launch(intent)
                                        Log.d("PermissionsSetup", "Launcher invocado con RoleManager")
                                    } else {
                                        Log.d("PermissionsSetup", "Ya es SMS por defecto según RoleManager")
                                    }
                                } else {
                                    Log.e("PermissionsSetup", "ROLE_SMS no disponible en este dispositivo")
                                }
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                // Android 4.4-9 (API 19-28): Usar Intent tradicional
                                Log.d("PermissionsSetup", "Usando Intent tradicional (Android 4.4-9)")
                                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                                    putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                                }
                                Log.d("PermissionsSetup", "Intent creado: ${intent.action}")
                                requestDefaultSmsLauncher.launch(intent)
                                Log.d("PermissionsSetup", "Launcher invocado con Intent tradicional")
                            } else {
                                Log.d("PermissionsSetup", "SDK < KITKAT, no se requiere configuración")
                            }
                        } catch (e: Exception) {
                            Log.e("PermissionsSetup", "Error al lanzar intent", e)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configurar como app SMS por defecto")
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionItem(
                    title = "Lectura de SMS",
                    description = "Necesario para leer tus mensajes SMS"
                )
                
                PermissionItem(
                    title = "Envío de SMS",
                    description = "Necesario para enviar mensajes SMS"
                )
                
                PermissionItem(
                    title = "Recepción de SMS",
                    description = "Necesario para recibir mensajes en tiempo real"
                )
                
                PermissionItem(
                    title = "Recepción de MMS",
                    description = "Requerido por Android para ser app SMS por defecto"
                )
                
                PermissionItem(
                    title = "Contactos",
                    description = "Necesario para clasificar mensajes seguros"
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val permissionsToRequest = listOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.RECEIVE_MMS,
                        Manifest.permission.READ_CONTACTS
                    )
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Conceder Permisos")
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


