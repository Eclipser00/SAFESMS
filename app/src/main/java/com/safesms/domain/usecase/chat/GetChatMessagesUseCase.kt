package com.safesms.domain.usecase.chat

import com.safesms.domain.model.Message
import com.safesms.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene mensajes de un chat específico.
 * 
 * ACTUALIZADO: Usa threadId en lugar de chatId.
 */
class GetChatMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    /**
     * Obtiene todos los mensajes de un thread.
     * 
     * @param threadId thread_id del chat
     * @return Flow con lista de mensajes ordenados por timestamp
     */
    operator fun invoke(threadId: Long): Flow<List<Message>> {
        return messageRepository.getMessagesByThreadId(threadId)
    }
}
