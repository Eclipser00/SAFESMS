package com.safesms.domain.usecase.message

import com.safesms.domain.repository.MessageRepository
import com.safesms.util.Result
import javax.inject.Inject

/**
 * Marca mensaje como leído
 */
class MarkMessageAsReadUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(messageId: Long): Result<Unit> {
        return messageRepository.markAsRead(messageId)
    }
}
