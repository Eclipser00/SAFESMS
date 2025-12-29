package com.safesms.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.safesms.domain.model.ChatType
import com.safesms.presentation.ui.theme.InboxGreen
import com.safesms.presentation.ui.theme.InboxGreenDark
import com.safesms.presentation.ui.theme.MutedText
import com.safesms.presentation.ui.theme.QuarantineRed
import com.safesms.presentation.ui.theme.QuarantineRedDark
import com.safesms.presentation.ui.theme.Surface as SurfaceColor
import com.safesms.presentation.ui.theme.SurfaceStroke

/**
 * Composable para selector Inbox/Cuarentena con código de color
 */
@Composable
fun TabSelector(
    selectedTab: ChatType,
    onTabSelected: (ChatType) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(50.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            color = SurfaceColor,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabButton(
                    text = "INBOX",
                    isSelected = selectedTab == ChatType.INBOX,
                    activeBrush = Brush.horizontalGradient(
                        listOf(InboxGreen, InboxGreenDark)
                    ),
                    inactiveTextColor = MutedText,
                    onClick = { onTabSelected(ChatType.INBOX) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                )

                TabButton(
                    text = "CUARENTENA",
                    isSelected = selectedTab == ChatType.QUARANTINE,
                    activeBrush = Brush.horizontalGradient(
                        listOf(QuarantineRed, QuarantineRedDark)
                    ),
                    inactiveTextColor = MutedText,
                    onClick = { onTabSelected(ChatType.QUARANTINE) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    activeBrush: Brush,
    inactiveTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                brush = if (isSelected) activeBrush else Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.Transparent)
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else SurfaceStroke,
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else inactiveTextColor
        )
    }
}

