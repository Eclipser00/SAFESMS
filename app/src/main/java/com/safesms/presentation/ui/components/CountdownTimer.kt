package com.safesms.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Composable para cuenta atrás visual animado
 * Muestra número grande centrado que cuenta hacia atrás con animaciones suaves
 * @param seconds Segundos totales del countdown
 * @param onFinish Callback cuando el countdown termina
 * @param modifier Modifier para el componente
 */
@Composable
fun CountdownTimer(
    seconds: Int,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.error,
    textSize: TextUnit = 68.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold
) {
    var remainingSeconds by remember { mutableStateOf(seconds) }
    var isFinished by remember { mutableStateOf(false) }
    
    // Animación de escala para el número (efecto bounce)
    val scale by animateFloatAsState(
        targetValue = if (isFinished) 1f else 1.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // Animación de opacidad para transiciones suaves
    val alpha by animateFloatAsState(
        targetValue = if (remainingSeconds == 0) 0.5f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    LaunchedEffect(seconds) {
        remainingSeconds = seconds
        isFinished = false
        
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        
        isFinished = true
        onFinish()
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = remainingSeconds.toString(),
            fontSize = textSize,
            fontWeight = fontWeight,
            color = textColor,
            style = MaterialTheme.typography.displayMedium.copy(
                shadow = Shadow(
                    color = textColor.copy(alpha = 0.45f),
                    blurRadius = 18f,
                    offset = Offset(0f, 3f)
                )
            ),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        )
    }
}

