package com.safesms.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para agrupación de chats.
 * 
 * CAMBIO IMPORTANTE (Thread-Based System):
 * - Ahora usa thread_id como clave primaria (en lugar de id autoincremental)
 * - thread_id viene del sistema Android (Telephony.Threads.getOrCreateThreadId)
 * - Android automáticamente agrupa variaciones del mismo número en el mismo thread
 * - address se mantiene para display, pero puede variar ("+34600...", "600...", etc.)
 * 
 * BENEFICIOS:
 * - Consistencia con sistema nativo de Android
 * - Sin duplicados de chats para el mismo número en diferentes formatos
 * - Bloqueos funcionan correctamente con todas las variantes del número
 */
@Entity(
    tableName = "chats",
    indices = [
        Index(value = ["address"]), // Índice para búsquedas rápidas
        Index(value = ["isInboxChat"]), // Índice para filtrado Inbox/Cuarentena
        Index(value = ["lastMessageTimestamp"]) // Índice para ordenamiento
    ]
)
data class ChatEntity(
    @PrimaryKey
    val threadId: Long, // CAMBIO: thread_id del sistema Android como PK
    
    val address: String, // Address original para display (puede variar)
    val contactName: String? = null,
    val lastMessageBody: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0,
    val isInboxChat: Boolean = true, // true = Inbox, false = Cuarentena
    val isPinned: Boolean = false // Nuevo: soporte para chats fijados
)
