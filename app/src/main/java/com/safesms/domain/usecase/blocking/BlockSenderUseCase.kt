package com.safesms.domain.usecase.blocking

import com.safesms.domain.repository.BlockedSenderRepository
import com.safesms.domain.repository.ChatRepository
import com.safesms.util.Result
import javax.inject.Inject

/**
 * Bloquea un remitente por su thread_id.
 * 
 * ACTUALIZADO: Usa threadId en lugar de address.
 */
class BlockSenderUseCase @Inject constructor(
    private val blockedSenderRepository: BlockedSenderRepository,
    private val chatRepository: ChatRepository
) {
    /**
     * Bloquea un thread completo.
     * 
     * @param threadId thread_id del chat a bloquear
     * @param address Address original para display
     * @return Result con Unit si fue exitoso
     */
    suspend operator fun invoke(threadId: Long, address: String): Result<Unit> {
        val result = blockedSenderRepository.blockSender(
            threadId = threadId,
            address = address,
            reason = "Blocked by user"
        )
        
        // Opcional: marcar chat como leído al bloquear
        if (result is Result.Success) {
            chatRepository.markChatAsRead(threadId)
        }
        
        return result
    }
}
