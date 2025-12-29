package com.safesms.data.local.database.migration

import android.content.Context
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.safesms.util.ThreadIdHelper

/**
 * Helper para migrar datos del sistema antiguo (address-based) al nuevo (thread_id-based).
 * 
 * Esta clase maneja la migración completa de:
 * - Chats: de address como identificador a thread_id
 * - Messages: de chatId a chatThreadId
 * - BlockedSenders: de address a threadId
 * 
 * PROCESO:
 * 1. Para cada chat antiguo, obtener thread_id del sistema Android
 * 2. Migrar datos a nuevas tablas con thread_id
 * 3. Consolidar chats duplicados (mismo thread_id)
 * 4. Eliminar tablas antiguas
 */
class MigrationHelper(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "MigrationHelper"
    }
    
    /**
     * Migra chats antiguos obteniendo thread_id del sistema.
     * 
     * Para cada chat antiguo:
     * 1. Limpiar el address (eliminar prefijos short:, alpha:, raw:)
     * 2. Obtener thread_id del sistema Android
     * 3. Insertar en nueva tabla (o actualizar si ya existe el thread_id)
     * 
     * Los chats duplicados (con diferentes formatos del mismo número)
     * se consolidarán automáticamente porque tendrán el mismo thread_id.
     */
    fun migrateChats(database: SupportSQLiteDatabase) {
        Log.i(TAG, "Starting chats migration...")
        
        try {
            // Consultar todos los chats antiguos
            val cursor = database.query("SELECT id, address, contactName, lastMessageBody, lastMessageTimestamp, unreadCount, isInboxChat FROM chats_old ORDER BY lastMessageTimestamp DESC")
            
            var migratedCount = 0
            var skippedCount = 0
            
            while (cursor.moveToNext()) {
                val oldId = cursor.getLong(0)
                val address = cursor.getString(1)
                val contactName = cursor.getString(2)
                val lastMessageBody = cursor.getString(3)
                val lastMessageTimestamp = cursor.getLong(4)
                val unreadCount = cursor.getInt(5)
                val isInboxChat = cursor.getInt(6) == 1
                
                try {
                    // Limpiar address y obtener thread_id
                    val cleanAddress = ThreadIdHelper.cleanAddress(address)
                    val threadId = ThreadIdHelper.getOrCreateThreadId(context, cleanAddress)
                    
                    // Insertar o actualizar chat con thread_id
                    // Si ya existe (por duplicado), mantener el más reciente
                    database.execSQL("""
                        INSERT INTO chats_new (threadId, address, contactName, lastMessageBody, lastMessageTimestamp, unreadCount, isInboxChat, isPinned)
                        VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                        ON CONFLICT(threadId) DO UPDATE SET
                            lastMessageTimestamp = CASE 
                                WHEN excluded.lastMessageTimestamp > lastMessageTimestamp 
                                THEN excluded.lastMessageTimestamp 
                                ELSE lastMessageTimestamp 
                            END,
                            lastMessageBody = CASE 
                                WHEN excluded.lastMessageTimestamp > lastMessageTimestamp 
                                THEN excluded.lastMessageBody 
                                ELSE lastMessageBody 
                            END,
                            unreadCount = unreadCount + excluded.unreadCount,
                            contactName = COALESCE(excluded.contactName, contactName)
                    """.trimIndent(), arrayOf(threadId, cleanAddress, contactName, lastMessageBody, lastMessageTimestamp, unreadCount, if (isInboxChat) 1 else 0))
                    
                    migratedCount++
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error migrating chat $oldId with address $address", e)
                    skippedCount++
                }
            }
            
            cursor.close()
            Log.i(TAG, "Chats migration completed: $migratedCount migrated, $skippedCount skipped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error during chats migration", e)
            throw e
        }
    }
    
    /**
     * Migra mensajes vinculándolos al thread_id correspondiente.
     * 
     * Para cada mensaje:
     * 1. Buscar el thread_id del chat al que pertenecía (usando chatId antiguo)
     * 2. Si no existe el chat, obtener thread_id del address del mensaje
     * 3. Insertar mensaje con chatThreadId
     */
    fun migrateMessages(database: SupportSQLiteDatabase) {
        Log.i(TAG, "Starting messages migration...")
        
        try {
            // Crear mapa de oldChatId -> threadId para migración rápida
            val chatIdMap = mutableMapOf<Long, Long>()
            val chatMapCursor = database.query("SELECT id, address FROM chats_old")
            while (chatMapCursor.moveToNext()) {
                val oldChatId = chatMapCursor.getLong(0)
                val address = chatMapCursor.getString(1)
                try {
                    val cleanAddress = ThreadIdHelper.cleanAddress(address)
                    val threadId = ThreadIdHelper.getOrCreateThreadId(context, cleanAddress)
                    chatIdMap[oldChatId] = threadId
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping chat $oldChatId", e)
                }
            }
            chatMapCursor.close()
            
            // Migrar mensajes
            val cursor = database.query("SELECT id, chatId, address, body, timestamp, type, isRead FROM messages_old ORDER BY timestamp ASC")
            
            var migratedCount = 0
            var skippedCount = 0
            
            while (cursor.moveToNext()) {
                val oldId = cursor.getLong(0)
                val oldChatId = cursor.getLong(1)
                val address = cursor.getString(2)
                val body = cursor.getString(3)
                val timestamp = cursor.getLong(4)
                val type = cursor.getInt(5)
                val isRead = cursor.getInt(6) == 1
                
                try {
                    // Obtener thread_id del mapa o del address
                    val threadId = chatIdMap[oldChatId] ?: run {
                        val cleanAddress = ThreadIdHelper.cleanAddress(address)
                        ThreadIdHelper.getOrCreateThreadId(context, cleanAddress)
                    }
                    
                    // Insertar mensaje
                    database.execSQL("""
                        INSERT INTO messages_new (chatThreadId, address, body, timestamp, type, isRead, status)
                        VALUES (?, ?, ?, ?, ?, ?, 'SENT')
                    """.trimIndent(), arrayOf(threadId, address, body, timestamp, type, if (isRead) 1 else 0))
                    
                    migratedCount++
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error migrating message $oldId", e)
                    skippedCount++
                }
            }
            
            cursor.close()
            Log.i(TAG, "Messages migration completed: $migratedCount migrated, $skippedCount skipped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error during messages migration", e)
            throw e
        }
    }
    
    /**
     * Migra remitentes bloqueados de address a threadId.
     * 
     * Para cada bloqueado:
     * 1. Limpiar address
     * 2. Obtener thread_id
     * 3. Insertar en nueva tabla con threadId
     */
    fun migrateBlockedSenders(database: SupportSQLiteDatabase) {
        Log.i(TAG, "Starting blocked senders migration...")
        
        try {
            val cursor = database.query("SELECT address, blockedTimestamp, reason FROM blocked_senders_old")
            
            var migratedCount = 0
            var skippedCount = 0
            
            while (cursor.moveToNext()) {
                val address = cursor.getString(0)
                val blockedTimestamp = cursor.getLong(1)
                val reason = cursor.getString(2)
                
                try {
                    val cleanAddress = ThreadIdHelper.cleanAddress(address)
                    val threadId = ThreadIdHelper.getOrCreateThreadId(context, cleanAddress)
                    
                    // Insertar bloqueado
                    database.execSQL("""
                        INSERT OR REPLACE INTO blocked_senders_new (threadId, address, blockedTimestamp, reason)
                        VALUES (?, ?, ?, ?)
                    """.trimIndent(), arrayOf(threadId, cleanAddress, blockedTimestamp, reason))
                    
                    migratedCount++
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error migrating blocked sender $address", e)
                    skippedCount++
                }
            }
            
            cursor.close()
            Log.i(TAG, "Blocked senders migration completed: $migratedCount migrated, $skippedCount skipped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error during blocked senders migration", e)
            throw e
        }
    }
}

