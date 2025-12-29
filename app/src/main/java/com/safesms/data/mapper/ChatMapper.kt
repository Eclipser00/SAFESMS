package com.safesms.data.mapper

import com.safesms.data.local.database.entities.ChatEntity
import com.safesms.domain.model.Chat
import com.safesms.domain.model.ChatType

/**
 * Conversión entre ChatEntity y Chat.
 * 
 * ACTUALIZADO: Usa threadId en lugar de id autoincremental.
 */
object ChatMapper {
    
    /**
     * Convierte Chat (Domain) a ChatEntity (Room).
     */
    fun toEntity(chat: Chat): ChatEntity {
        return ChatEntity(
            threadId = chat.threadId, // CAMBIO: usar threadId
            address = chat.address,
            contactName = chat.contactName,
            lastMessageBody = chat.lastMessageBody,
            lastMessageTimestamp = chat.lastMessageTimestamp,
            unreadCount = chat.unreadCount,
            isInboxChat = chat.chatType == ChatType.INBOX,
            isPinned = chat.isPinned
        )
    }
    
    /**
     * Convierte ChatEntity (Room) a Chat (Domain).
     */
    fun toDomain(
        entity: ChatEntity,
        riskFactors: List<com.safesms.domain.model.RiskFactor> = emptyList(),
        isBlocked: Boolean = false
    ): Chat {
        return Chat(
            threadId = entity.threadId, // CAMBIO: usar threadId
            address = entity.address,
            contactName = entity.contactName,
            lastMessageBody = entity.lastMessageBody,
            lastMessageTimestamp = entity.lastMessageTimestamp,
            unreadCount = entity.unreadCount,
            chatType = if (entity.isInboxChat) ChatType.INBOX else ChatType.QUARANTINE,
            riskFactors = riskFactors,
            isBlocked = isBlocked,
            isPinned = entity.isPinned
        )
    }
    
    /**
     * Convierte lista de ChatEntity a lista de Chat.
     */
    fun toDomainList(
        entities: List<ChatEntity>,
        riskFactorsMap: Map<Long, List<com.safesms.domain.model.RiskFactor>> = emptyMap(),
        blockedThreadIds: Set<Long> = emptySet() // CAMBIO: usar threadIds en lugar de addresses
    ): List<Chat> {
        return entities.map { entity ->
            toDomain(
                entity,
                riskFactorsMap[entity.threadId] ?: emptyList(),
                blockedThreadIds.contains(entity.threadId) // CAMBIO: verificar por threadId
            )
        }
    }
}

