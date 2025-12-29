package com.safesms.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.safesms.domain.model.RiskFactor

/**
 * Composable para diálogos de advertencia genéricos reutilizables
 * Integra CountdownTimer y deshabilita botón Confirmar hasta que termine el countdown
 * @param title Título del diálogo
 * @param message Mensaje de advertencia
 * @param riskFactors Lista de factores de riesgo detectados
 * @param countdownSeconds Segundos del countdown (3-8)
 * @param onConfirm Callback cuando se confirma la acción
 * @param onDismiss Callback cuando se cancela
 * @param modifier Modifier para el componente
 */
@Composable
fun SecurityWarningDialog(
    title: String,
    message: String,
    riskFactors: List<RiskFactor>,
    countdownSeconds: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var canProceed by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = message)
                
                if (riskFactors.isNotEmpty()) {
                    Text(
                        text = "Factores de riesgo detectados:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        riskFactors.forEach { riskFactor ->
                            RiskIndicatorChip(
                                riskFactor = riskFactor,
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // CountdownTimer integrado
                CountdownTimer(
                    seconds = countdownSeconds,
                    onFinish = { canProceed = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = canProceed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

