package com.safesms.domain.model

/**
 * Tipos de mensaje.
 */
enum class MessageType {
    RECEIVED,
    SENT
}

/**
 * Estados de mensaje.
 */
enum class MessageStatus {
    PENDING,
    SENT,
    RECEIVED,
    FAILED
}

/**
 * Entidad de dominio para mensajes SMS.
 * 
 * ACTUALIZADO: Usa chatThreadId para vincular con Chat.threadId.
 */
data class Message(
    val id: Long = 0,
    val chatThreadId: Long, // CAMBIO: referencia a Chat.threadId
    val address: String, // Address original para display
    val body: String,
    val timestamp: Long,
    val type: MessageType, // CAMBIO: de isReceived a type
    val status: MessageStatus = MessageStatus.SENT, // CAMBIO: de String a enum
    val errorCode: Int? = null,
    val isRead: Boolean = false
) {
    // Helper property para compatibilidad
    val isReceived: Boolean
        get() = type == MessageType.RECEIVED
}
