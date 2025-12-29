package com.safesms.data.local.system

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import com.safesms.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interfaz con ContentProvider de SMS del sistema Android
 */
@Singleton
class SmsSystemProvider @Inject constructor(
    private val context: Context
) {
    
    private val contentResolver: ContentResolver = context.contentResolver
    
    /**
     * Obtiene la instancia de SmsManager usando la API moderna
     */
    private fun getSmsManager(): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            context.getSystemService(SmsManager::class.java)
        } else {
            // Android 6.0 - 11 (API 23-30)
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }
    
    /**
     * Lista todos los SMS del sistema
     */
    suspend fun getAllSms(): List<SystemSms> = withContext(Dispatchers.IO) {
        val smsList = mutableListOf<SystemSms>()
        val uri = Uri.parse("content://sms/")
        val cursor: Cursor? = contentResolver.query(
            uri,
            null,
            null,
            null,
            "date DESC"
        )
        
        cursor?.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)
            val readIndex = it.getColumnIndex(Telephony.Sms.READ)
            val threadIdIndex = it.getColumnIndex(Telephony.Sms.THREAD_ID)
            
            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val address = it.getString(addressIndex) ?: ""
                val body = it.getString(bodyIndex) ?: ""
                val date = it.getLong(dateIndex)
                val type = it.getInt(typeIndex)
                val read = it.getInt(readIndex) == 1
                val threadId = it.getLong(threadIdIndex)
                
                smsList.add(
                    SystemSms(
                        id = id,
                        address = address,
                        body = body,
                        timestamp = date,
                        type = type,
                        isRead = read,
                        threadId = threadId
                    )
                )
            }
        }
        
        smsList
    }
    
    /**
     * Obtiene SMS de un thread específico
     */
    suspend fun getSmsFromThreadId(threadId: Long): List<SystemSms> = withContext(Dispatchers.IO) {
        val smsList = mutableListOf<SystemSms>()
        val uri = Uri.parse("content://sms/")
        val cursor: Cursor? = contentResolver.query(
            uri,
            null,
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "date ASC"
        )
        
        cursor?.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)
            val readIndex = it.getColumnIndex(Telephony.Sms.READ)
            val threadIdIndex = it.getColumnIndex(Telephony.Sms.THREAD_ID)
            
            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val address = it.getString(addressIndex) ?: ""
                val body = it.getString(bodyIndex) ?: ""
                val date = it.getLong(dateIndex)
                val type = it.getInt(typeIndex)
                val read = it.getInt(readIndex) == 1
                val threadIdValue = it.getLong(threadIdIndex)
                
                smsList.add(
                    SystemSms(
                        id = id,
                        address = address,
                        body = body,
                        timestamp = date,
                        type = type,
                        isRead = read,
                        threadId = threadIdValue
                    )
                )
            }
        }
        
        smsList
    }
    
    /**
     * Envía SMS usando SmsManager
     */
    suspend fun sendSms(address: String, body: String, threadId: Long? = null): Long = withContext(Dispatchers.IO) {
        try {
            val smsManager = getSmsManager()
            val parts = smsManager.divideMessage(body)
            val sentIntents = ArrayList<PendingIntent?>(parts.size).apply {
                repeat(parts.size) { add(null) }
            }
            val deliveryIntents = ArrayList<PendingIntent?>(parts.size).apply {
                repeat(parts.size) { add(null) }
            }
            
            smsManager.sendMultipartTextMessage(
                address,
                null,
                parts,
                sentIntents,
                deliveryIntents
            )
            
            // Insertar SMS enviado en el sistema
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, address)
                put(Telephony.Sms.BODY, body)
                put(Telephony.Sms.DATE, System.currentTimeMillis())
                put(Telephony.Sms.TYPE, Constants.SMS_TYPE_SENT)
                put(Telephony.Sms.READ, 1)
                threadId?.let { put(Telephony.Sms.THREAD_ID, it) }
            }
            
            val uri = contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
            uri?.lastPathSegment?.toLongOrNull() ?: -1L
        } catch (e: Exception) {
            throw Exception("Error al enviar SMS: ${e.message}", e)
        }
    }
    
    /**
     * Marca SMS como leído en el sistema
     */
    suspend fun markSmsAsRead(messageId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 1)
            }
            val uri = Uri.parse("content://sms/$messageId")
            val rowsUpdated = contentResolver.update(uri, values, null, null)
            rowsUpdated > 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Elimina SMS del sistema
     */
    suspend fun deleteSms(messageId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse("content://sms/$messageId")
            val rowsDeleted = contentResolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Data class para SMS del sistema
     */
    data class SystemSms(
        val id: Long,
        val address: String,
        val body: String,
        val timestamp: Long,
        val type: Int,
        val isRead: Boolean,
        val threadId: Long
    )
}
