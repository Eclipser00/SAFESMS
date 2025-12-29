package com.safesms.domain.repository

import com.safesms.domain.model.Contact
import com.safesms.util.Result

/**
 * Interfaz para operaciones de contactos
 */
interface ContactRepository {
    suspend fun syncContacts(): Result<Unit>
    suspend fun isContactSaved(phoneNumber: String): Boolean
    suspend fun getContactByPhone(phoneNumber: String): Contact?
    fun getAllContacts(): kotlinx.coroutines.flow.Flow<List<Contact>>
}
