package com.safesms.domain.repository

import com.safesms.domain.model.Chat
import com.safesms.domain.model.Message
import com.safesms.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para operaciones de chats.
 * 
 * ACTUALIZADO: Usa threadId en lugar de id autoincremental y address.
 */
interface ChatRepository {
    /**
     * Obtiene un chat por su thread_id.
     */
    suspend fun getChatByThreadId(threadId: Long): Chat?
    
    /**
     * Obtiene un chat por su thread_id como Flow (reactivo).
     */
    fun getChatByThreadIdFlow(threadId: Long): Flow<Chat?>
    
    /**
     * Obtiene todos los chats.
     */
    fun getAllChats(): Flow<List<Chat>>
    
    /**
     * Obtiene chats de Inbox (remitentes en contactos).
     */
    fun getInboxChats(): Flow<List<Chat>>
    
    /**
     * Obtiene chats de Cuarentena (remitentes NO en contactos).
     */
    fun getQuarantineChats(): Flow<List<Chat>>
    
    /**
     * Crea o actualiza un chat a partir de un mensaje.
     * 
     * @param threadId thread_id del chat
     * @param address Address original del mensaje
     * @param lastMessage Último mensaje recibido/enviado
     * @return Result con el thread_id
     */
    suspend fun createOrUpdateChat(
        threadId: Long,
        address: String,
        lastMessage: Message
    ): Result<Long>
    
    /**
     * Actualiza un chat existente.
     */
    suspend fun updateChat(chat: Chat)
    
    /**
     * Elimina un chat por su thread_id.
     */
    suspend fun deleteChat(threadId: Long)
    
    /**
     * Marca un chat como leído.
     */
    suspend fun markChatAsRead(threadId: Long)
    
    /**
     * Fija o desfija un chat.
     */
    suspend fun pinChat(threadId: Long, pinned: Boolean)
}
