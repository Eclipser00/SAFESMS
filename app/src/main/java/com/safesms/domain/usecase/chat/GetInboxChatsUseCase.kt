package com.safesms.domain.usecase.chat

import com.safesms.domain.model.Chat
import com.safesms.domain.repository.BlockedSenderRepository
import com.safesms.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use Case para obtener chats de Inbox con factores de riesgo y estado de bloqueo.
 * 
 * ACTUALIZADO: Usa threadId para verificar bloqueos.
 */
class GetInboxChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val blockedSenderRepository: BlockedSenderRepository
) {
    /**
     * Obtiene todos los chats de Inbox enriquecidos con estado de bloqueo.
     */
    operator fun invoke(): Flow<List<Chat>> {
        return combine(
            chatRepository.getInboxChats(),
            blockedSenderRepository.getAllBlockedSenders()
        ) { chats, blockedSenders ->
            val blockedThreadIds = blockedSenders.map { it.threadId }.toSet()
            chats.map { chat ->
                // Verificar si el thread está bloqueado
                val isBlocked = blockedThreadIds.contains(chat.threadId)
                
                // Actualizar chat con estado de bloqueo
                chat.copy(
                    isBlocked = isBlocked
                )
            }
        }
    }
}
