package com.safesms.domain.usecase.blocking

import com.safesms.domain.model.BlockedSender
import com.safesms.domain.repository.BlockedSenderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene lista de remitentes bloqueados.
 */
class GetBlockedSendersUseCase @Inject constructor(
    private val blockedSenderRepository: BlockedSenderRepository
) {
    operator fun invoke(): Flow<List<BlockedSender>> {
        return blockedSenderRepository.getAllBlockedSenders()
    }
}
