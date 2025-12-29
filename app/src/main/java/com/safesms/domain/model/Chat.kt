package com.safesms.domain.model

/**
 * Entidad de dominio para chats agrupados.
 * 
 * ACTUALIZADO: Usa threadId como identificador único.
 */
data class Chat(
    val threadId: Long, // CAMBIO: thread_id del sistema Android
    val address: String, // Address para display (puede variar)
    val contactName: String?,
    val lastMessageBody: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int,
    val chatType: ChatType,
    val riskFactors: List<RiskFactor> = emptyList(),
    val isBlocked: Boolean = false,
    val isPinned: Boolean = false // Nuevo: soporte para chats fijados
)
