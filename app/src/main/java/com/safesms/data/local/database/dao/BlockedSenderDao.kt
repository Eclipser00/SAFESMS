package com.safesms.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safesms.data.local.database.entities.BlockedSenderEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para gestión de remitentes bloqueados.
 * 
 * ACTUALIZADO: Usa threadId como clave primaria.
 * Un thread_id bloqueado bloquea TODAS las variantes del número.
 */
@Dao
interface BlockedSenderDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedSender(blockedSender: BlockedSenderEntity)
    
    /**
     * Obtiene un remitente bloqueado por su thread_id.
     */
    @Query("SELECT * FROM blocked_senders WHERE threadId = :threadId")
    suspend fun getBlockedSenderByThreadId(threadId: Long): BlockedSenderEntity?
    
    /**
     * Obtiene todos los remitentes bloqueados.
     */
    @Query("SELECT * FROM blocked_senders ORDER BY blockedTimestamp DESC")
    fun getAllBlockedSendersFlow(): Flow<List<BlockedSenderEntity>>
    
    /**
     * Desbloquea un remitente por su thread_id.
     */
    @Query("DELETE FROM blocked_senders WHERE threadId = :threadId")
    suspend fun unblockByThreadId(threadId: Long)
    
    /**
     * Verifica si un thread está bloqueado.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM blocked_senders WHERE threadId = :threadId)")
    suspend fun isThreadBlocked(threadId: Long): Boolean
}
