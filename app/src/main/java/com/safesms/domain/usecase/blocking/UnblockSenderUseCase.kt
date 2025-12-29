package com.safesms.domain.usecase.blocking

import com.safesms.domain.repository.BlockedSenderRepository
import com.safesms.util.Result
import javax.inject.Inject

/**
 * Desbloquea un remitente por su thread_id.
 * 
 * ACTUALIZADO: Usa threadId en lugar de address.
 */
class UnblockSenderUseCase @Inject constructor(
    private val blockedSenderRepository: BlockedSenderRepository
) {
    /**
     * Desbloquea un thread.
     * 
     * @param threadId thread_id del chat a desbloquear
     * @return Result con Unit si fue exitoso
     */
    suspend operator fun invoke(threadId: Long): Result<Unit> {
        return blockedSenderRepository.unblockSender(threadId)
    }
}
