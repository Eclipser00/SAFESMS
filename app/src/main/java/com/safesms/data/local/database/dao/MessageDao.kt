package com.safesms.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.safesms.data.local.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceso a datos de mensajes en Room.
 * 
 * ACTUALIZADO: Usa chatThreadId para vincular con ChatEntity.threadId.
 */
@Dao
interface MessageDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long
    
    /**
     * Obtiene todos los mensajes de un thread ordenados por timestamp.
     */
    @Query("SELECT * FROM messages WHERE chatThreadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesByThreadId(threadId: Long): Flow<List<MessageEntity>>
    
    /**
     * Obtiene el último mensaje de un thread.
     */
    @Query("SELECT * FROM messages WHERE chatThreadId = :threadId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageForThread(threadId: Long): MessageEntity?
    
    /**
     * Obtiene todos los mensajes ordenados por timestamp descendente.
     */
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    /**
     * Elimina un mensaje por su ID.
     */
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)
    
    /**
     * Elimina todos los mensajes de un thread.
     */
    @Query("DELETE FROM messages WHERE chatThreadId = :threadId")
    suspend fun deleteMessagesByThreadId(threadId: Long)
    
    /**
     * Marca un mensaje como leído.
     */
    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: Long)
    
    /**
     * Marca todos los mensajes de un thread como leídos.
     */
    @Query("UPDATE messages SET isRead = 1 WHERE chatThreadId = :threadId")
    suspend fun markMessagesAsRead(threadId: Long)
    
    /**
     * Obtiene el número de mensajes no leídos de un thread.
     */
    @Query("SELECT COUNT(*) FROM messages WHERE chatThreadId = :threadId AND isRead = 0")
    suspend fun getUnreadCountForThread(threadId: Long): Int
    
    /**
     * Obtiene un mensaje por su ID.
     */
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): MessageEntity?
    
    /**
     * Actualiza el status de un mensaje (PENDING, SENT, FAILED).
     */
    @Query("UPDATE messages SET status = :status, errorCode = :errorCode WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, status: String, errorCode: Int? = null)
}
