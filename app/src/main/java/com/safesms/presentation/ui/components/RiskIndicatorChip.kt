package com.safesms.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.safesms.domain.model.RiskFactor
import com.safesms.presentation.ui.theme.RiskHigh
import com.safesms.presentation.ui.theme.RiskLow
import com.safesms.presentation.ui.theme.RiskMedium

/**
 * Chip de riesgo con estilo serio y jerarquia de color.
 */
@Composable
fun RiskIndicatorChip(
    riskFactor: RiskFactor,
    modifier: Modifier = Modifier
) {
    val (label, color, icon) = when (riskFactor) {
        is RiskFactor.ContainsLinks -> Triple(
            "Contiene enlaces",
            RiskHigh,
            Icons.Default.Warning
        )
        is RiskFactor.AlphanumericSender -> Triple(
            "Remitente alfanumerico",
            RiskMedium,
            Icons.Default.Info
        )
        is RiskFactor.ShortCode -> Triple(
            "Numero corto",
            RiskMedium,
            Icons.Default.Phone
        )
        is RiskFactor.UnknownSender -> Triple(
            "Remitente desconocido",
            RiskLow,
            Icons.Default.Person
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.7f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .padding(end = 2.dp)
                    .align(Alignment.CenterVertically)
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}
