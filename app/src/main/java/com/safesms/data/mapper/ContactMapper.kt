package com.safesms.data.mapper

import com.safesms.data.local.database.entities.ContactEntity
import com.safesms.data.local.system.ContactsSystemProvider
import com.safesms.domain.model.Contact

/**
 * Conversión entre ContactEntity y Contact
 */
object ContactMapper {
    
    /**
     * Convierte Contact (Domain) a ContactEntity (Room)
     */
    fun toEntity(contact: Contact): ContactEntity {
        return ContactEntity(
            id = contact.id,
            phoneNumber = contact.phoneNumber,
            displayName = contact.displayName,
            syncTimestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Convierte ContactEntity (Room) a Contact (Domain)
     */
    fun toDomain(entity: ContactEntity): Contact {
        return Contact(
            id = entity.id,
            phoneNumber = entity.phoneNumber,
            displayName = entity.displayName
        )
    }
    
    /**
     * Convierte SystemContact a Contact (Domain)
     */
    fun fromSystemContact(systemContact: ContactsSystemProvider.SystemContact): Contact {
        return Contact(
            id = systemContact.id,
            phoneNumber = systemContact.phoneNumber,
            displayName = systemContact.displayName
        )
    }
    
    /**
     * Convierte lista de ContactEntity a lista de Contact
     */
    fun toDomainList(entities: List<ContactEntity>): List<Contact> {
        return entities.map { toDomain(it) }
    }
}

