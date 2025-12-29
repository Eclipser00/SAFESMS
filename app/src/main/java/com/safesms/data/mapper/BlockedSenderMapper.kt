package com.safesms.data.mapper

import com.safesms.data.local.database.entities.BlockedSenderEntity
import com.safesms.domain.model.BlockedSender

/**
 * Conversión entre BlockedSenderEntity y BlockedSender.
 * 
 * ACTUALIZADO: Usa threadId en lugar de address como identificador.
 */
object BlockedSenderMapper {
    
    /**
     * Convierte BlockedSender (Domain) a BlockedSenderEntity (Room).
     */
    fun toEntity(blockedSender: BlockedSender): BlockedSenderEntity {
        return BlockedSenderEntity(
            threadId = blockedSender.threadId, // CAMBIO: usar threadId
            address = blockedSender.address,
            blockedTimestamp = blockedSender.blockedTimestamp,
            reason = blockedSender.reason
        )
    }
    
    /**
     * Convierte BlockedSenderEntity (Room) a BlockedSender (Domain).
     */
    fun toDomain(entity: BlockedSenderEntity): BlockedSender {
        return BlockedSender(
            threadId = entity.threadId, // CAMBIO: usar threadId
            address = entity.address,
            blockedTimestamp = entity.blockedTimestamp,
            reason = entity.reason
        )
    }
    
    /**
     * Convierte lista de BlockedSenderEntity a lista de BlockedSender.
     */
    fun toDomainList(entities: List<BlockedSenderEntity>): List<BlockedSender> {
        return entities.map { toDomain(it) }
    }
}

