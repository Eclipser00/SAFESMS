package com.safesms.presentation.screen.security

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safesms.presentation.ui.components.CountdownTimer
import com.safesms.presentation.ui.components.RiskIndicatorChip
import com.safesms.presentation.ui.theme.QuarantineRed
import com.safesms.presentation.ui.theme.SurfaceStroke

/**
 * Pantalla de advertencia al entrar a chat de Cuarentena.
 */
@Composable
fun QuarantineWarningScreen(
    threadId: Long,
    onNavigateToChatDetail: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: QuarantineWarningViewModel = hiltViewModel()
) {
    val riskFactors by viewModel.riskFactors.collectAsStateWithLifecycle()
    val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()
    val canProceed by viewModel.canProceed.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(threadId) {
        viewModel.loadRiskFactors(threadId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(top = 350.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Remite sospechoso",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                if (riskFactors.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        riskFactors.take(3).forEach { riskFactor ->
                            RiskIndicatorChip(
                                riskFactor = riskFactor,
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(44.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Acceso disponible en",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                CountdownTimer(
                    seconds = countdownSeconds,
                    onFinish = { },
                    textColor = Color.White,
                    textSize = 78.sp
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToChatDetail,
                    enabled = canProceed,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = QuarantineRed,
                        disabledContainerColor = SurfaceStroke.copy(alpha = 0.6f),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Entrar igualmente",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
