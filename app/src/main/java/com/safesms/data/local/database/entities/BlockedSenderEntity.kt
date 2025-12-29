package com.safesms.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para remitentes bloqueados.
 * 
 * CAMBIO IMPORTANTE (Thread-Based System):
 * - Ahora usa threadId como clave primaria
 * - Un thread_id bloqueado bloquea TODAS las variantes del número
 * - address se mantiene como referencia para display en UI
 * 
 * BENEFICIOS:
 * - Bloqueo consistente: bloquear "+34600..." también bloquea "600..."
 * - No hay escapatoria cambiando formato del número
 */
@Entity(tableName = "blocked_senders")
data class BlockedSenderEntity(
    @PrimaryKey
    val threadId: Long, // CAMBIO: usar thread_id como PK
    
    val address: String, // Address original que se bloqueó (para display)
    val blockedTimestamp: Long = System.currentTimeMillis(),
    val reason: String? = null
)

