package com.safesms.presentation.screen.chatlist

import com.safesms.domain.model.Chat
import com.safesms.domain.model.ChatType

/**
 * Data class para estado de lista de chats
 */
data class ChatListState(
    val inboxChats: List<Chat> = emptyList(),
    val quarantineChats: List<Chat> = emptyList(),
    val selectedTab: ChatType = ChatType.INBOX,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val chats: List<Chat>
        get() = when (selectedTab) {
            ChatType.INBOX -> inboxChats
            ChatType.QUARANTINE -> quarantineChats
        }
}

