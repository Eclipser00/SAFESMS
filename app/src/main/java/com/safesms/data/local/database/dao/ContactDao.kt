package com.safesms.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safesms.data.local.database.entities.ContactEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceso a datos de contactos sincronizados
 */
@Dao
interface ContactDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)
    
    @Query("SELECT * FROM contacts ORDER BY displayName ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>
    
    /**
     * Busca un contacto por teléfono, comparando también variaciones del número
     * (con/sin código de país, con/sin +, etc.)
     */
    @Query("""
        SELECT * FROM contacts 
        WHERE phoneNumber = :phoneNumber 
           OR phoneNumber = REPLACE(:phoneNumber, '+', '')
           OR phoneNumber = '+' || REPLACE(:phoneNumber, '+', '')
           OR (LENGTH(REPLACE(phoneNumber, '+', '')) >= 9 
               AND LENGTH(REPLACE(:phoneNumber, '+', '')) >= 9
               AND SUBSTR(REPLACE(phoneNumber, '+', ''), LENGTH(REPLACE(phoneNumber, '+', '')) - 8) = SUBSTR(REPLACE(:phoneNumber, '+', ''), LENGTH(REPLACE(:phoneNumber, '+', '')) - 8)
           )
        LIMIT 1
    """)
    suspend fun getContactByPhone(phoneNumber: String): ContactEntity?
    
    /**
     * Verifica si un contacto existe, comparando también variaciones del número
     * (con/sin código de país, con/sin +, etc.)
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM contacts 
        WHERE phoneNumber = :phoneNumber 
           OR phoneNumber = REPLACE(:phoneNumber, '+', '')
           OR phoneNumber = '+' || REPLACE(:phoneNumber, '+', '')
           OR (LENGTH(REPLACE(phoneNumber, '+', '')) >= 9 
               AND LENGTH(REPLACE(:phoneNumber, '+', '')) >= 9
               AND SUBSTR(REPLACE(phoneNumber, '+', ''), LENGTH(REPLACE(phoneNumber, '+', '')) - 8) = SUBSTR(REPLACE(:phoneNumber, '+', ''), LENGTH(REPLACE(:phoneNumber, '+', '')) - 8)
           )
    """)
    suspend fun isContactSaved(phoneNumber: String): Boolean
    
    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()
}
