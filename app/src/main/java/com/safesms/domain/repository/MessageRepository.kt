package com.safesms.domain.repository

import com.safesms.domain.model.Message
import com.safesms.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para operaciones de mensajes.
 * 
 * ACTUALIZADO: Usa threadId en lugar de chatId.
 */
interface MessageRepository {
    /**
     * Obtiene mensajes por thread_id.
     */
    fun getMessagesByThreadId(threadId: Long): Flow<List<Message>>
    
    /**
     * Obtiene todos los mensajes.
     */
    fun getAllMessages(): Flow<List<Message>>
    
    /**
     * Envía un SMS.
     * 
     * @param address Dirección del destinatario
     * @param body Contenido del mensaje
     * @param threadId thread_id del chat
     */
    suspend fun sendMessage(address: String, body: String, threadId: Long): Result<Unit>
    
    /**
     * Marca un mensaje como leído.
     */
    suspend fun markAsRead(messageId: Long): Result<Unit>
    
    /**
     * Elimina un mensaje.
     */
    suspend fun deleteMessage(messageId: Long): Result<Unit>
    
    /**
     * Inserta un mensaje en la BD local.
     */
    suspend fun insertMessage(message: Message): Result<Long>
}
