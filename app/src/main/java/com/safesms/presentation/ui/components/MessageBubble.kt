package com.safesms.presentation.ui.components

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.safesms.domain.model.Message
import com.safesms.presentation.ui.theme.ReceivedMessageBackground
import com.safesms.presentation.ui.theme.ReceivedMessageText
import com.safesms.presentation.ui.theme.SentMessageBackground
import com.safesms.presentation.ui.theme.SentMessageText
import com.safesms.presentation.util.DateFormatter
import java.util.regex.Pattern

/**
 * Burbuja de mensaje con diferenciacion segura/riesgo y enlaces destacados.
 */
@Composable
fun MessageBubble(
    message: Message,
    onLinkClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isReceived = message.isReceived
    val annotatedText = detectAndStyleLinks(message.body)
    val bubbleShape = if (isReceived) {
        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 6.dp)
    } else {
        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 6.dp)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isReceived) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            shape = bubbleShape,
            color = if (isReceived) ReceivedMessageBackground else SentMessageBackground,
            tonalElevation = if (isReceived) 0.dp else 2.dp,
            shadowElevation = if (isReceived) 1.dp else 4.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                ClickableText(
                    text = annotatedText,
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                            .firstOrNull()
                            ?.let { annotation -> onLinkClicked(annotation.item) }
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isReceived) ReceivedMessageText else SentMessageText
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateFormatter.formatMessageTimestamp(message.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isReceived) {
                                ReceivedMessageText.copy(alpha = 0.65f)
                            } else {
                                SentMessageText.copy(alpha = 0.85f)
                            }
                        )
                    )

                    if (!isReceived) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = if (message.isRead) "Leido" else "Enviado",
                            tint = if (message.isRead) SentMessageText else SentMessageText.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}

private fun detectAndStyleLinks(text: String, linkColor: Color = Color(0xFF2E6AC7)): AnnotatedString {
    val urlPattern = Pattern.compile(
        Patterns.WEB_URL.pattern(),
        Pattern.CASE_INSENSITIVE
    )

    val matcher = urlPattern.matcher(text)
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val url = matcher.group()

            append(text.substring(lastIndex, start))

            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(url)
            }
            pop()

            lastIndex = end
        }

        append(text.substring(lastIndex))
    }

    return annotatedString
}
