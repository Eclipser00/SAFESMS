package com.safesms.data.repository

import android.content.Context
import com.safesms.data.local.database.dao.ChatDao
import com.safesms.data.local.database.dao.ContactDao
import com.safesms.data.local.database.entities.ChatEntity
import com.safesms.data.mapper.ChatMapper
import com.safesms.domain.model.Chat
import com.safesms.domain.model.Message
import com.safesms.domain.repository.ChatRepository
import com.safesms.util.Result
import com.safesms.util.ThreadIdHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementa ChatRepository.
 * 
 * ACTUALIZADO: Usa ThreadIdHelper para obtener thread_id del sistema Android.
 * Ya no necesita PhoneNormalizer - delegamos la normalización al sistema.
 */
class ChatRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatDao: ChatDao,
    private val contactDao: ContactDao
) : ChatRepository {
    
    override suspend fun getChatByThreadId(threadId: Long): Chat? {
        return chatDao.getChatByThreadId(threadId)?.let { ChatMapper.toDomain(it) }
    }
    
    override fun getChatByThreadIdFlow(threadId: Long): Flow<Chat?> {
        return chatDao.getChatByThreadIdFlow(threadId)
            .map { entity -> entity?.let { ChatMapper.toDomain(it) } }
    }
    
    override fun getAllChats(): Flow<List<Chat>> {
        return chatDao.getAllChatsFlow()
            .map { entities -> ChatMapper.toDomainList(entities) }
    }
    
    override fun getInboxChats(): Flow<List<Chat>> {
        return chatDao.getInboxChats()
            .map { entities -> ChatMapper.toDomainList(entities) }
    }
    
    override fun getQuarantineChats(): Flow<List<Chat>> {
        return chatDao.getQuarantineChats()
            .map { entities -> ChatMapper.toDomainList(entities) }
    }
    
    override suspend fun createOrUpdateChat(
        threadId: Long,
        address: String,
        lastMessage: Message
    ): Result<Long> {
        return try {
            // 1. Buscar contacto siempre para tener info fresca
            val contact = contactDao.getContactByPhone(address)
            val isInbox = contact != null
            val contactName = contact?.displayName

            // 2. Buscar chat existente
            val existingChat = chatDao.getChatByThreadId(threadId)
            
            if (existingChat != null) {
                // Actualizar chat existente (incluyendo nombre de contacto por si ha cambiado)
                val updatedChat = existingChat.copy(
                    lastMessageBody = lastMessage.body,
                    lastMessageTimestamp = lastMessage.timestamp,
                    contactName = contactName,
                    isInboxChat = isInbox,
                    unreadCount = if (lastMessage.isReceived) existingChat.unreadCount + 1 else existingChat.unreadCount
                )
                chatDao.updateChat(updatedChat)
                Result.Success(threadId)
            } else {
                // Crear nuevo chat
                val chatEntity = ChatEntity(
                    threadId = threadId,
                    address = address,
                    contactName = contactName,
                    lastMessageBody = lastMessage.body,
                    lastMessageTimestamp = lastMessage.timestamp,
                    unreadCount = if (lastMessage.isReceived) 1 else 0,
                    isInboxChat = isInbox,
                    isPinned = false
                )
                chatDao.insertChat(chatEntity)
                Result.Success(threadId)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateChat(chat: Chat) {
        val entity = ChatMapper.toEntity(chat)
        chatDao.updateChat(entity)
    }
    
    override suspend fun deleteChat(threadId: Long) {
        chatDao.deleteChatByThreadId(threadId)
    }
    
    override suspend fun markChatAsRead(threadId: Long) {
        chatDao.markChatAsRead(threadId)
    }
    
    override suspend fun pinChat(threadId: Long, pinned: Boolean) {
        chatDao.updatePinStatus(threadId, pinned)
    }
}
