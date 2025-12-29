package com.safesms.data.local.system

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.safesms.util.normalizePhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lectura de contactos del sistema Android
 */
@Singleton
class ContactsSystemProvider @Inject constructor(
    private val context: Context
) {
    
    private val contentResolver: ContentResolver = context.contentResolver
    
    /**
     * Lista todos los contactos con teléfonos
     */
    suspend fun getAllContacts(): List<SystemContact> = withContext(Dispatchers.IO) {
        val contactsList = mutableListOf<SystemContact>()
        
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )
        
        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex) ?: ""
                val number = it.getString(numberIndex) ?: ""
                
                if (number.isNotEmpty()) {
                    // Usar la función centralizada de normalización que mantiene el +
                    contactsList.add(
                        SystemContact(
                            id = id,
                            displayName = name,
                            phoneNumber = number.normalizePhoneNumber()
                        )
                    )
                }
            }
        }
        
        contactsList
    }
    
    /**
     * Busca contacto por teléfono
     */
    suspend fun getContactByPhone(phoneNumber: String): SystemContact? = withContext(Dispatchers.IO) {
        // Usar la función centralizada de normalización que mantiene el +
        val normalizedPhone = phoneNumber.normalizePhoneNumber()
        
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        
        // Buscar con el número normalizado y también sin el + para mayor flexibilidad
        val phoneWithoutPlus = normalizedPhone.removePrefix("+")
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?",
            arrayOf(normalizedPhone, "%$normalizedPhone%", "%$phoneWithoutPlus%"),
            null
        )
        
        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            if (it.moveToFirst()) {
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex) ?: ""
                // Normalizar el número encontrado con la función centralizada
                val number = (it.getString(numberIndex) ?: "").normalizePhoneNumber()
                
                return@withContext SystemContact(
                    id = id,
                    displayName = name,
                    phoneNumber = number
                )
            }
        }
        
        null
    }
    
    /**
     * Data class para contacto del sistema
     */
    data class SystemContact(
        val id: Long,
        val displayName: String,
        val phoneNumber: String
    )
}
