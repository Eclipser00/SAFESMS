package com.safesms.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.safesms.data.local.database.dao.BlockedSenderDao
import com.safesms.data.local.database.dao.ChatDao
import com.safesms.data.local.database.dao.ContactDao
import com.safesms.data.local.database.dao.MessageDao
import com.safesms.data.local.database.entities.BlockedSenderEntity
import com.safesms.data.local.database.entities.ChatEntity
import com.safesms.data.local.database.entities.ContactEntity
import com.safesms.data.local.database.entities.MessageEntity
import com.safesms.data.local.database.migration.MigrationHelper

/**
 * Clase abstracta de Room Database.
 * 
 * VERSIÓN 2 - MIGRACIÓN A THREAD-BASED SYSTEM:
 * - Chats usan thread_id como PK (en lugar de id autoincremental)
 * - Messages usan chatThreadId (en lugar de chatId)
 * - BlockedSenders usan threadId (en lugar de address)
 * 
 * Esta migración elimina la necesidad de normalización manual de números
 * y delega al sistema Android la agrupación de conversaciones.
 */
@Database(
    entities = [
        MessageEntity::class,
        ChatEntity::class,
        ContactEntity::class,
        BlockedSenderEntity::class
    ],
    version = 2, // INCREMENTADO: de 1 a 2
    exportSchema = false
)
abstract class SafeSmsDatabase : RoomDatabase() {
    
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun contactDao(): ContactDao
    abstract fun blockedSenderDao(): BlockedSenderDao
    
    companion object {
        
        /**
         * Migración de versión 1 a 2: Cambio de address-based a thread_id-based.
         * 
         * PROCESO:
         * 1. Renombrar tablas antiguas a *_old
         * 2. Crear nuevas tablas con thread_id
         * 3. Migrar datos obteniendo thread_id para cada address
         * 4. Eliminar tablas antiguas
         * 
         * NOTA: Requiere Context para usar ThreadIdHelper
         */
        fun getMigration1To2(context: Context): Migration {
            return object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // PASO 1: Renombrar tablas antiguas
                    database.execSQL("ALTER TABLE chats RENAME TO chats_old")
                    database.execSQL("ALTER TABLE messages RENAME TO messages_old")
                    database.execSQL("ALTER TABLE blocked_senders RENAME TO blocked_senders_old")
                    
                    // PASO 2: Crear nuevas tablas con thread_id
                    
                    // Nueva tabla chats
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS chats (
                            threadId INTEGER PRIMARY KEY NOT NULL,
                            address TEXT NOT NULL,
                            contactName TEXT,
                            lastMessageBody TEXT NOT NULL,
                            lastMessageTimestamp INTEGER NOT NULL,
                            unreadCount INTEGER NOT NULL DEFAULT 0,
                            isInboxChat INTEGER NOT NULL DEFAULT 1,
                            isPinned INTEGER NOT NULL DEFAULT 0
                        )
                    """.trimIndent())
                    
                    // Índices para chats
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_chats_address ON chats(address)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_chats_isInboxChat ON chats(isInboxChat)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_chats_lastMessageTimestamp ON chats(lastMessageTimestamp)")
                    
                    // Nueva tabla messages
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS messages (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            chatThreadId INTEGER NOT NULL,
                            address TEXT NOT NULL,
                            body TEXT NOT NULL,
                            timestamp INTEGER NOT NULL,
                            type INTEGER NOT NULL,
                            isRead INTEGER NOT NULL DEFAULT 0,
                            status TEXT NOT NULL DEFAULT 'SENT',
                            errorCode INTEGER,
                            FOREIGN KEY(chatThreadId) REFERENCES chats(threadId) ON DELETE CASCADE
                        )
                    """.trimIndent())
                    
                    // Índices para messages
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_chatThreadId ON messages(chatThreadId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_timestamp ON messages(timestamp)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_isRead ON messages(isRead)")
                    
                    // Nueva tabla blocked_senders
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS blocked_senders (
                            threadId INTEGER PRIMARY KEY NOT NULL,
                            address TEXT NOT NULL,
                            blockedTimestamp INTEGER NOT NULL,
                            reason TEXT
                        )
                    """.trimIndent())
                    
                    // Renombrar nuevas tablas temporalmente para migración
                    database.execSQL("ALTER TABLE chats RENAME TO chats_new")
                    database.execSQL("ALTER TABLE messages RENAME TO messages_new")
                    database.execSQL("ALTER TABLE blocked_senders RENAME TO blocked_senders_new")
                    
                    // PASO 3: Migrar datos usando MigrationHelper
                    val migrationHelper = MigrationHelper(context)
                    
                    // Migrar chats primero (necesario para foreign keys)
                    migrationHelper.migrateChats(database)
                    
                    // Migrar mensajes
                    migrationHelper.migrateMessages(database)
                    
                    // Migrar bloqueados
                    migrationHelper.migrateBlockedSenders(database)
                    
                    // PASO 4: Eliminar tablas antiguas
                    database.execSQL("DROP TABLE IF EXISTS chats_old")
                    database.execSQL("DROP TABLE IF EXISTS messages_old")
                    database.execSQL("DROP TABLE IF EXISTS blocked_senders_old")
                    
                    // PASO 5: Renombrar tablas nuevas a nombres finales
                    database.execSQL("ALTER TABLE chats_new RENAME TO chats")
                    database.execSQL("ALTER TABLE messages_new RENAME TO messages")
                    database.execSQL("ALTER TABLE blocked_senders_new RENAME TO blocked_senders")
                }
            }
        }
    }
}
