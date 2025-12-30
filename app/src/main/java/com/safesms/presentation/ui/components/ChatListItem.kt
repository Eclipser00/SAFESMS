package com.safesms.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.safesms.domain.model.Chat
import com.safesms.domain.model.ChatType
import com.safesms.presentation.ui.theme.InboxGreen
import com.safesms.presentation.ui.theme.InboxGreenDark
import com.safesms.presentation.ui.theme.MutedText
import com.safesms.presentation.ui.theme.QuarantineBorder
import com.safesms.presentation.ui.theme.QuarantineRed
import com.safesms.presentation.ui.theme.QuarantineRedDark
import com.safesms.presentation.ui.theme.Surface
import com.safesms.presentation.ui.theme.SurfaceMuted
import com.safesms.presentation.ui.theme.SurfaceStroke
import com.safesms.presentation.ui.theme.BlockedBackground
import com.safesms.presentation.ui.theme.BlockedSurface
import com.safesms.presentation.util.DateFormatter
import com.safesms.presentation.util.PhoneNumberFormatter
import com.safesms.util.AddressDisplayHelper

/**
 * Item de chat con diferenciacion visual entre Inbox (seguro) y Cuarentena (riesgo).
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onLongPress: () -> Unit = {}
) {
    val isQuarantine = chat.chatType == ChatType.QUARANTINE
    val shape = RoundedCornerShape(14.dp)
    val containerBrush = when {
        chat.isBlocked -> Brush.verticalGradient(listOf(BlockedSurface, BlockedBackground))
        isQuarantine -> Brush.verticalGradient(listOf(Surface, SurfaceMuted))
        else -> Brush.verticalGradient(listOf(Surface, SurfaceMuted))
    }
    val contentColor: Color = when {
        chat.isBlocked -> MutedText
        isQuarantine -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    val accentColor = if (isQuarantine) QuarantineRed else InboxGreen
    val selectionOutline = if (isSelected) MaterialTheme.colorScheme.primary else QuarantineRed.copy(alpha = 0.35f)
    val itemAlpha = if (selectionMode && !isSelected) 0.8f else 1f

    val cleanAddress = AddressDisplayHelper.cleanAddressForDisplay(chat.address)
    val displayName = chat.contactName ?: PhoneNumberFormatter.formatPhoneNumber(cleanAddress)
    val contentDescription = buildString {
        append(displayName)
        if (chat.unreadCount > 0) {
            append(", ${chat.unreadCount} mensajes no leidos")
        }
        if (chat.riskFactors.isNotEmpty()) {
            append(", ${chat.riskFactors.size} factores de riesgo")
        }
        if (chat.isBlocked) {
            append(", bloqueado")
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(elevation = if (isQuarantine) 10.dp else 4.dp, shape = shape, clip = false)
            .clip(shape)
            .alpha(itemAlpha)
            .background(containerBrush)
            .border(
                width = when {
                    isSelected -> 2.dp
                    isQuarantine && !chat.isBlocked -> 1.dp
                    else -> 0.dp
                },
                color = selectionOutline,
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .semantics {
                this.contentDescription = contentDescription
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = if (isQuarantine) 0.25f else 0.18f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Seleccionado",
                            tint = accentColor
                        )
                    } else {
                        Text(
                            text = displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (chat.isBlocked) MutedText else accentColor
                        )
                    }

                    if (!isSelected && (isQuarantine || chat.isBlocked)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (chat.isBlocked) SurfaceStroke else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (chat.isBlocked) MutedText else QuarantineRed,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = DateFormatter.formatChatTimestamp(chat.lastMessageTimestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = chat.lastMessageBody,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor.copy(alpha = if (chat.isBlocked) 0.5f else 0.85f)
                )

                if (chat.riskFactors.isNotEmpty() && isQuarantine) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        chat.riskFactors.take(2).forEach { riskFactor ->
                            RiskIndicatorChip(
                                riskFactor = riskFactor,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }

            if (chat.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isQuarantine) QuarantineRed else InboxGreenDark,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
