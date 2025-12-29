package com.safesms.presentation.screen.chatdetail

import com.safesms.domain.model.Chat
import com.safesms.domain.model.Message

/**
 * Data class para estado de detalle de chat
 */
data class ChatDetailState(
    val chat: Chat? = null,
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

