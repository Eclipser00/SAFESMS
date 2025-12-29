package com.safesms.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safesms.data.local.system.SmsSystemProvider
import com.safesms.presentation.ui.theme.SafeSmsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity para enviar SMS desde otras aplicaciones
 * Componente requerido para ser app SMS por defecto en Android
 * 
 * Se invoca cuando otra app usa Intent.ACTION_SENDTO con esquema "sms:" o "smsto:"
 */
@AndroidEntryPoint
class ComposeSmsActivity : ComponentActivity() {
    
    @Inject
    lateinit var smsSystemProvider: SmsSystemProvider
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val phoneNumber = extractPhoneNumber(intent)
        val initialMessage = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        
        if (phoneNumber.isNullOrEmpty()) {
            // Si no hay número, redirigir a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        setContent {
            SafeSmsTheme {
                ComposeSmsScreen(
                    phoneNumber = phoneNumber,
                    initialMessage = initialMessage,
                    onSend = { message ->
                        sendSmsAndFinish(phoneNumber, message)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
    
    private fun extractPhoneNumber(intent: Intent): String? {
        return when {
            intent.data != null -> {
                val uri = intent.data
                uri?.schemeSpecificPart?.replace("-", "")
            }
            intent.hasExtra("address") -> {
                intent.getStringExtra("address")
            }
            else -> null
        }
    }
    
    private fun sendSmsAndFinish(phoneNumber: String, message: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            try {
                smsSystemProvider.sendSms(phoneNumber, message)
                setResult(RESULT_OK)
            } catch (e: Exception) {
                setResult(RESULT_CANCELED)
            } finally {
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeSmsScreen(
    phoneNumber: String,
    initialMessage: String,
    onSend: (String) -> Unit,
    onCancel: () -> Unit
) {
    var messageText by remember { mutableStateOf(initialMessage) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enviar SMS") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Text("✕")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Para: $phoneNumber",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Mensaje") },
                placeholder = { Text("Escribe tu mensaje...") }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = { onSend(messageText) },
                    modifier = Modifier.weight(1f),
                    enabled = messageText.isNotBlank()
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}

