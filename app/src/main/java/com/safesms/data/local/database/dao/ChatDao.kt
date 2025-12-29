package com.safesms.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.safesms.data.local.database.entities.ChatEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceso a datos de chats en Room.
 * 
 * ACTUALIZADO: Usa thread_id como clave primaria.
 * Ya no necesitamos queries complejas con fallbacks de normalización.
 */
@Dao
interface ChatDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long
    
    /**
     * Obtiene un chat por su thread_id.
     */
    @Query("SELECT * FROM chats WHERE threadId = :threadId")
    suspend fun getChatByThreadId(threadId: Long): ChatEntity?
    
    /**
     * Obtiene un chat por su thread_id como Flow (reactivo).
     */
    @Query("SELECT * FROM chats WHERE threadId = :threadId")
    fun getChatByThreadIdFlow(threadId: Long): Flow<ChatEntity?>
    
    /**
     * Obtiene todos los chats ordenados por timestamp y pin.
     * Los chats fijados aparecen primero.
     */
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllChatsFlow(): Flow<List<ChatEntity>>
    
    /**
     * Obtiene chats de Inbox (remitentes en contactos).
     */
    @Query("SELECT * FROM chats WHERE isInboxChat = 1 ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getInboxChats(): Flow<List<ChatEntity>>
    
    /**
     * Obtiene chats de Cuarentena (remitentes NO en contactos).
     */
    @Query("SELECT * FROM chats WHERE isInboxChat = 0 ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getQuarantineChats(): Flow<List<ChatEntity>>
    
    @Update
    suspend fun updateChat(chat: ChatEntity)
    
    /**
     * Elimina un chat por thread_id.
     * Los mensajes se eliminan en cascada (ForeignKey).
     */
    @Query("DELETE FROM chats WHERE threadId = :threadId")
    suspend fun deleteChatByThreadId(threadId: Long)
    
    /**
     * Actualiza el último mensaje de un chat.
     */
    @Query("""
        UPDATE chats 
        SET lastMessageBody = :body, 
            lastMessageTimestamp = :timestamp,
            unreadCount = unreadCount + 1
        WHERE threadId = :threadId
    """)
    suspend fun updateLastMessage(threadId: Long, body: String, timestamp: Long)
    
    /**
     * Marca un chat como leído (unreadCount = 0).
     */
    @Query("UPDATE chats SET unreadCount = 0 WHERE threadId = :threadId")
    suspend fun markChatAsRead(threadId: Long)
    
    /**
     * Actualiza el estado de fijado de un chat.
     */
    @Query("UPDATE chats SET isPinned = :pinned WHERE threadId = :threadId")
    suspend fun updatePinStatus(threadId: Long, pinned: Boolean)
    
    /**
     * Obtiene el número total de mensajes no leídos en todos los chats.
     */
    @Query("SELECT SUM(unreadCount) FROM chats")
    suspend fun getTotalUnreadCount(): Int?
}
