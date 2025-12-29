package com.safesms.data.repository

import com.safesms.data.local.database.dao.BlockedSenderDao
import com.safesms.data.local.database.entities.BlockedSenderEntity
import com.safesms.data.mapper.BlockedSenderMapper
import com.safesms.domain.model.BlockedSender
import com.safesms.domain.repository.BlockedSenderRepository
import com.safesms.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementa BlockedSenderRepository.
 * 
 * ACTUALIZADO: Usa threadId en lugar de address normalizado.
 * Un thread_id bloqueado bloquea TODAS las variantes del número.
 */
class BlockedSenderRepositoryImpl @Inject constructor(
    private val blockedSenderDao: BlockedSenderDao
) : BlockedSenderRepository {
    
    override suspend fun blockSender(threadId: Long, address: String, reason: String?): Result<Unit> {
        return try {
            val entity = BlockedSenderEntity(
                threadId = threadId,
                address = address, // Address original para display
                blockedTimestamp = System.currentTimeMillis(),
                reason = reason
            )
            blockedSenderDao.insertBlockedSender(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun unblockSender(threadId: Long): Result<Unit> {
        return try {
            blockedSenderDao.unblockByThreadId(threadId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun isThreadBlocked(threadId: Long): Boolean {
        return blockedSenderDao.isThreadBlocked(threadId)
    }
    
    override fun getAllBlockedSenders(): Flow<List<BlockedSender>> {
        return blockedSenderDao.getAllBlockedSendersFlow()
            .map { entities -> BlockedSenderMapper.toDomainList(entities) }
    }
}
