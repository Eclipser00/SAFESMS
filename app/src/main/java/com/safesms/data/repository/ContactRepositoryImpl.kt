package com.safesms.data.repository

import com.safesms.data.local.database.dao.ContactDao
import com.safesms.data.local.system.ContactsSystemProvider
import com.safesms.data.mapper.ContactMapper
import com.safesms.domain.model.Contact
import com.safesms.domain.repository.ContactRepository
import com.safesms.util.PhoneNormalizer
import com.safesms.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementa ContactRepository
 * Coordina ContactDao y ContactsSystemProvider
 * 
 * Usa PhoneNormalizer para normalizar números de contactos a formato E.164,
 * permitiendo que se encuentren correctamente cuando se clasifican chats.
 */
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val contactsSystemProvider: ContactsSystemProvider,
    private val phoneNormalizer: PhoneNormalizer
) : ContactRepository {
    
    override suspend fun syncContacts(): Result<Unit> {
        return try {
            // Obtener contactos del sistema
            val systemContacts = contactsSystemProvider.getAllContacts()
            
            // Normalizar números de teléfono a formato canónico E.164 usando PhoneNormalizer
            val normalizedContacts = systemContacts.mapNotNull { systemContact ->
                val normalizedResult = phoneNormalizer.normalizePhone(systemContact.phoneNumber)
                
                // Solo guardar contactos con números E.164 válidos
                // Los short codes, alfanuméricos e inválidos no se guardan como contactos
                if (normalizedResult.type == "E164") {
                    ContactsSystemProvider.SystemContact(
                        id = systemContact.id,
                        displayName = systemContact.displayName,
                        phoneNumber = normalizedResult.key // Usar clave canónica E.164
                    )
                } else {
                    // Ignorar contactos con números no válidos
                    null
                }
            }
            
            // Convertir a entidades
            val contactEntities = normalizedContacts.map { systemContact ->
                ContactMapper.toEntity(ContactMapper.fromSystemContact(systemContact))
            }
            
            // Limpiar y reinsertar
            contactDao.deleteAllContacts()
            contactDao.insertContacts(contactEntities)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun isContactSaved(phoneNumber: String): Boolean {
        // Normalizar número usando PhoneNormalizer para obtener clave canónica E.164
        val normalizedResult = phoneNormalizer.normalizePhone(phoneNumber)
        
        // Solo buscar si es E.164 (números de teléfono válidos)
        // Los short codes, alfanuméricos e inválidos no están en contactos
        if (normalizedResult.type == "E164") {
            val e164Key = normalizedResult.key
            
            // Buscar en la BD con la clave E.164
            val found = contactDao.isContactSaved(e164Key)
            
            // Si no se encuentra, intentar también sin el prefijo + (por si los contactos
            // se guardaron sin prefijo internacional)
            if (!found && e164Key.startsWith("+")) {
                val withoutPlus = e164Key.removePrefix("+")
                return contactDao.isContactSaved(withoutPlus)
            }
            
            return found
        }
        
        return false
    }
    
    override suspend fun getContactByPhone(phoneNumber: String): Contact? {
        // Normalizar número usando PhoneNormalizer para obtener clave canónica E.164
        val normalizedResult = phoneNormalizer.normalizePhone(phoneNumber)
        
        // Solo buscar si es E.164 (números de teléfono válidos)
        if (normalizedResult.type == "E164") {
            val e164Key = normalizedResult.key
            
            // Buscar en la BD con la clave E.164
            var entity = contactDao.getContactByPhone(e164Key)
            
            // Si no se encuentra, intentar también sin el prefijo + (por si los contactos
            // se guardaron sin prefijo internacional)
            if (entity == null && e164Key.startsWith("+")) {
                val withoutPlus = e164Key.removePrefix("+")
                entity = contactDao.getContactByPhone(withoutPlus)
            }
            
            return entity?.let { ContactMapper.toDomain(it) }
        }
        
        return null
    }
    
    override fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContacts()
            .map { entities -> ContactMapper.toDomainList(entities) }
    }
}
