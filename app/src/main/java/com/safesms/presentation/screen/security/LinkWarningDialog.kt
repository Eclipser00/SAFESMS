package com.safesms.presentation.screen.security

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safesms.presentation.ui.components.CountdownTimer
import com.safesms.presentation.ui.theme.SurfaceStroke

/**
 * Dialogo de advertencia al pulsar enlace.
 */
@Composable
fun LinkWarningDialog(
    url: String,
    onDismiss: () -> Unit,
    viewModel: LinkWarningViewModel = hiltViewModel()
) {
    val linkSafety by viewModel.linkSafety.collectAsStateWithLifecycle()
    val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()
    val canProceed by viewModel.canProceed.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(url) {
        viewModel.validateLink(url)
    }

    Dialog(onDismissRequest = onDismiss) {
        val shape = RoundedCornerShape(18.dp)
        Surface(
            shape = shape,
            color = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .clip(shape)
            ) {
                Image(
                    painter = painterResource(id = com.safesms.R.drawable.alerta),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0x884A0A0D))
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Abrir enlace externo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }

                    linkSafety?.let { safety ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Dominio",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = safety.domain ?: safety.url,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )

                            if (safety.isSuspicious && safety.suspiciousReasons.isNotEmpty()) {
                                safety.suspiciousReasons.forEach { reason ->
                                    Text(
                                        text = "\u2022 $reason",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Riesgo de phishing",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    CountdownTimer(
                        seconds = countdownSeconds,
                        onFinish = { },
                        textColor = Color.White,
                        textSize = 72.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                viewModel.createOpenLinkIntent()?.let { intent: Intent ->
                                    context.startActivity(intent)
                                }
                                onDismiss()
                            },
                            enabled = canProceed,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFB01217),
                                disabledContainerColor = SurfaceStroke,
                                disabledContentColor = Color.White
                            )
                        ) {
                            Text("Abrir enlace", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
