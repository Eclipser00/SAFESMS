package com.safesms.di

import com.safesms.data.mapper.ChatMapper
import com.safesms.data.mapper.ContactMapper
import com.safesms.data.mapper.MessageMapper
import com.safesms.data.repository.*
import com.safesms.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo Hilt para Repositories
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    abstract fun bindBlockedSenderRepository(impl: BlockedSenderRepositoryImpl): BlockedSenderRepository

    @Binds
    abstract fun bindConfigurationRepository(impl: ConfigurationRepositoryImpl): ConfigurationRepository

    companion object {
        // SmsSystemProvider, ContactsSystemProvider y UserPreferencesDataStore
        // se inyectan automáticamente con @Inject constructor
        // No necesitamos providers explícitos para ellos

        @Provides
        fun provideMessageMapper(): MessageMapper = MessageMapper

        @Provides
        fun provideChatMapper(): ChatMapper = ChatMapper

        @Provides
        fun provideContactMapper(): ContactMapper = ContactMapper
    }
}
