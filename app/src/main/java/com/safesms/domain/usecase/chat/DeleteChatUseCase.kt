package com.safesms.domain.usecase.chat

import com.safesms.domain.repository.ChatRepository
import com.safesms.domain.repository.MessageRepository
import com.safesms.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Elimina un chat completo (mensajes incluidos) usando el threadId.
 */
class DeleteChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(threadId: Long): Result<Unit> = try {
        // Borrar mensajes individuales (permite que el repositorio elimine tambiГ©n del sistema)
        val messages = messageRepository.getMessagesByThreadId(threadId).first()
        messages.forEach { message ->
            messageRepository.deleteMessage(message.id)
        }

        // Borrar el chat (Room elimina en cascada los mensajes)
        chatRepository.deleteChat(threadId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
