package com.safesms.data.mapper

import com.safesms.data.local.database.entities.MessageEntity
import com.safesms.data.local.system.SmsSystemProvider
import com.safesms.domain.model.Message
import com.safesms.domain.model.MessageStatus
import com.safesms.domain.model.MessageType
import com.safesms.util.Constants

/**
 * Conversión entre MessageEntity (Room) y Message (Domain).
 * 
 * ACTUALIZADO: Usa chatThreadId en lugar de chatId y enums para type/status.
 */
object MessageMapper {
    
    /**
     * Convierte Message (Domain) a MessageEntity (Room).
     */
    fun toEntity(message: Message): MessageEntity {
        return MessageEntity(
            id = message.id,
            chatThreadId = message.chatThreadId,
            address = message.address,
            body = message.body,
            timestamp = message.timestamp,
            type = if (message.type == MessageType.RECEIVED) Constants.SMS_TYPE_RECEIVED else Constants.SMS_TYPE_SENT,
            isRead = message.isRead,
            status = message.status.name, // Convertir enum a String
            errorCode = message.errorCode
        )
    }
    
    /**
     * Convierte MessageEntity (Room) a Message (Domain).
     */
    fun toDomain(entity: MessageEntity): Message {
        return Message(
            id = entity.id,
            chatThreadId = entity.chatThreadId,
            address = entity.address,
            body = entity.body,
            timestamp = entity.timestamp,
            type = if (entity.type == Constants.SMS_TYPE_RECEIVED) MessageType.RECEIVED else MessageType.SENT,
            status = try {
                MessageStatus.valueOf(entity.status)
            } catch (e: Exception) {
                MessageStatus.SENT // Default fallback
            },
            errorCode = entity.errorCode,
            isRead = entity.isRead
        )
    }
    
    /**
     * Convierte lista de MessageEntity a lista de Message.
     */
    fun toDomainList(entities: List<MessageEntity>): List<Message> {
        return entities.map { toDomain(it) }
    }
    
    /**
     * Convierte SystemSms a Message (Domain).
     * 
     * @param systemSms SMS del sistema
     * @param threadId thread_id del chat al que pertenece
     */
    fun fromSystemSms(
        systemSms: SmsSystemProvider.SystemSms, 
        threadId: Long
    ): Message {
        return Message(
            id = systemSms.id,
            chatThreadId = threadId,
            address = systemSms.address,
            body = systemSms.body,
            timestamp = systemSms.timestamp,
            type = if (systemSms.type == Constants.SMS_TYPE_RECEIVED) MessageType.RECEIVED else MessageType.SENT,
            status = MessageStatus.RECEIVED, // Los SMS del sistema ya están recibidos
            errorCode = null,
            isRead = systemSms.isRead
        )
    }
}

