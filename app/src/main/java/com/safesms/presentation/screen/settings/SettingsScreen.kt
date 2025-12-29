package com.safesms.presentation.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safesms.presentation.ui.theme.Background
import com.safesms.presentation.ui.theme.DangerBackgroundTop
import com.safesms.presentation.ui.theme.HeaderDark
import com.safesms.presentation.ui.theme.HeaderDarkEnd
import com.safesms.presentation.ui.theme.MutedText
import com.safesms.presentation.ui.theme.QuarantineRed
import com.safesms.presentation.ui.theme.Surface
import com.safesms.presentation.ui.theme.SurfaceStroke
import com.safesms.presentation.util.PhoneNumberFormatter

/**
 * Pantalla de ajustes de la app con estetica de banca/seguridad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()
    val quarantineNotificationsEnabled by viewModel.quarantineNotificationsEnabled.collectAsStateWithLifecycle()
    val blockedSenders by viewModel.blockedSenders.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(HeaderDark, HeaderDarkEnd)))
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Ajustes",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, SurfaceStroke)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tiempo de espera de seguridad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${countdownSeconds} s",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when {
                                countdownSeconds <= 3 -> "\uD83D\uDE21"
                                countdownSeconds <= 5 -> "\uD83D\uDE10"
                                else -> "\uD83D\uDE42"
                            },
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(QuarantineRed, DangerBackgroundTop)
                                ),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 8.dp)
                    ) {
                        Slider(
                            value = countdownSeconds.toFloat(),
                            onValueChange = { viewModel.updateCountdownSeconds(it.toInt()) },
                            valueRange = 3f..8f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.Transparent,
                                inactiveTrackColor = Color.Transparent
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("3", style = MaterialTheme.typography.bodySmall, color = MutedText)
                        Text("8", style = MaterialTheme.typography.bodySmall, color = MutedText)
                    }

                    Text(
                        text = "Menos tiempo = Mas riesgo",
                        style = MaterialTheme.typography.titleSmall,
                        color = QuarantineRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, SurfaceStroke)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notificaciones de Cuarentena",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Recibir alertas de mensajes bloqueados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }

                    Switch(
                        checked = quarantineNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleQuarantineNotifications(it) }
                    )
                }
            }

            if (blockedSenders.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, SurfaceStroke)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Remitentes bloqueados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        blockedSenders.forEach { blockedSender ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = PhoneNumberFormatter.formatPhoneNumber(blockedSender.address),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                TextButton(
                                    onClick = { viewModel.unblockSender(blockedSender.threadId) }
                                ) {
                                    Text("Desbloquear")
                                }
                            }

                            androidx.compose.material3.Divider(color = SurfaceStroke)
                        }
                    }
                }
            }
        }
    }
}
