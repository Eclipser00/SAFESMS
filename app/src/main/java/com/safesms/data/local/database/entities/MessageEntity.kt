package com.safesms.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para mensajes SMS.
 * 
 * CAMBIO IMPORTANTE (Thread-Based System):
 * - Ahora usa chatThreadId (Long) que referencia ChatEntity.threadId
 * - Foreign Key con CASCADE para eliminar mensajes cuando se elimina el chat
 * - address se mantiene para display del remitente/destinatario original
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["threadId"],
            childColumns = ["chatThreadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chatThreadId"]), // Índice para búsquedas por thread_id
        Index(value = ["timestamp"]), // Índice para ordenamiento por timestamp
        Index(value = ["isRead"]) // Índice para filtrado por estado de lectura
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val chatThreadId: Long, // CAMBIO: referencia a ChatEntity.threadId
    
    val address: String, // Número origen/destino original para display
    val body: String,
    val timestamp: Long,
    val type: Int, // TYPE_RECEIVED (1) o TYPE_SENT (2)
    val isRead: Boolean = false,
    val status: String = "SENT", // Status del mensaje: PENDING, SENT, FAILED
    val errorCode: Int? = null // Código de error si falló el envío
)

