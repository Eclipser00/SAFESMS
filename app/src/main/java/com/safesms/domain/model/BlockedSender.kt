package com.safesms.domain.model

/**
 * Entidad de dominio para remitentes bloqueados.
 * 
 * ACTUALIZADO: Usa threadId como identificador único.
 */
data class BlockedSender(
    val threadId: Long, // CAMBIO: thread_id del sistema Android
    val address: String, // Address original para display
    val blockedTimestamp: Long,
    val reason: String? = null
)
