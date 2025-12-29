package com.safesms.di

import android.content.Context
import androidx.room.Room
import com.safesms.data.local.database.SafeSmsDatabase
import com.safesms.data.local.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para Room Database.
 * 
 * ACTUALIZADO: Incluye migración de versión 1 a 2 (thread-based system)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSafeSmsDatabase(@ApplicationContext context: Context): SafeSmsDatabase {
        return Room.databaseBuilder(
            context,
            SafeSmsDatabase::class.java,
            "safesms_database"
        )
        .addMigrations(SafeSmsDatabase.getMigration1To2(context)) // Migración 1->2
        .build()
    }

    @Provides
    fun provideMessageDao(database: SafeSmsDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideChatDao(database: SafeSmsDatabase): ChatDao {
        return database.chatDao()
    }

    @Provides
    fun provideContactDao(database: SafeSmsDatabase): ContactDao {
        return database.contactDao()
    }

    @Provides
    fun provideBlockedSenderDao(database: SafeSmsDatabase): BlockedSenderDao {
        return database.blockedSenderDao()
    }
}
