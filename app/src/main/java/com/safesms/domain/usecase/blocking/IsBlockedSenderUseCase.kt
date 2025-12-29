package com.safesms.domain.usecase.blocking

import com.safesms.domain.repository.BlockedSenderRepository
import javax.inject.Inject

/**
 * Verifica si un thread está bloqueado.
 * 
 * ACTUALIZADO: Usa threadId en lugar de address.
 */
class IsBlockedSenderUseCase @Inject constructor(
    private val blockedSenderRepository: BlockedSenderRepository
) {
    /**
     * Verifica si un thread está bloqueado.
     * 
     * @param threadId thread_id del chat a verificar
     * @return true si está bloqueado, false si no
     */
    suspend operator fun invoke(threadId: Long): Boolean {
        return blockedSenderRepository.isThreadBlocked(threadId)
    }
}
