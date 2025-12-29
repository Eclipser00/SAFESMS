package com.safesms.data.repository

import com.safesms.data.local.database.dao.MessageDao
import com.safesms.data.local.system.SmsSystemProvider
import com.safesms.data.mapper.MessageMapper
import com.safesms.domain.model.Message
import com.safesms.domain.repository.MessageRepository
import com.safesms.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementa MessageRepository.
 * 
 * ACTUALIZADO: Usa threadId en lugar de chatId.
 */
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val smsSystemProvider: SmsSystemProvider
) : MessageRepository {
    
    override fun getMessagesByThreadId(threadId: Long): Flow<List<Message>> {
        return messageDao.getMessagesByThreadId(threadId)
            .map { entities -> MessageMapper.toDomainList(entities) }
    }
    
    override fun getAllMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages()
            .map { entities -> MessageMapper.toDomainList(entities) }
    }
    
    override suspend fun sendMessage(address: String, body: String, threadId: Long): Result<Unit> {
        return try {
            // Enviar SMS a través del sistema (sin prefijos)
            val cleanAddress = address.removePrefix("short:").removePrefix("alpha:").removePrefix("raw:")
            smsSystemProvider.sendSms(cleanAddress, body, threadId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun markAsRead(messageId: Long): Result<Unit> {
        return try {
            messageDao.markAsRead(messageId)
            smsSystemProvider.markSmsAsRead(messageId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deleteMessage(messageId: Long): Result<Unit> {
        return try {
            messageDao.deleteMessage(messageId)
            smsSystemProvider.deleteSms(messageId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun insertMessage(message: Message): Result<Long> {
        return try {
            val entity = MessageMapper.toEntity(message)
            val messageId = messageDao.insertMessage(entity)
            Result.Success(messageId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
