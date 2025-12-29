package com.safesms.domain.repository

import com.safesms.domain.model.BlockedSender
import com.safesms.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para gestión de bloqueados.
 * 
 * ACTUALIZADO: Usa threadId en lugar de address.
 */
interface BlockedSenderRepository {
    /**
     * Bloquea un remitente por su thread_id.
     */
    suspend fun blockSender(threadId: Long, address: String, reason: String? = null): Result<Unit>
    
    /**
     * Desbloquea un remitente por su thread_id.
     */
    suspend fun unblockSender(threadId: Long): Result<Unit>
    
    /**
     * Verifica si un thread está bloqueado.
     */
    suspend fun isThreadBlocked(threadId: Long): Boolean
    
    /**
     * Obtiene todos los remitentes bloqueados.
     */
    fun getAllBlockedSenders(): Flow<List<BlockedSender>>
}
