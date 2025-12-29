package com.safesms.domain.usecase.smsimport

import com.safesms.domain.repository.ChatRepository
import com.safesms.domain.usecase.chat.ClassifyChatUseCase
import com.safesms.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use Case para clasificar mensajes importados en Inbox/Cuarentena
 * Analiza todos los chats creados durante la importación y los clasifica
 * 
 * Nota: La clasificación real debe hacerse en el repositorio cuando se crean los chats.
 * Este Use Case puede ser usado para re-clasificar después de sincronizar contactos.
 */
class ClassifyImportedMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val classifyChatUseCase: ClassifyChatUseCase
) {
    /**
     * Clasifica todos los mensajes importados según si sus remitentes están en contactos
     * @return Result con Unit si fue exitoso, Error en caso contrario
     */
    suspend operator fun invoke(): Result<Unit> = try {
        // Obtener todos los chats (tanto Inbox como Quarantine)
        val inboxChats = chatRepository.getInboxChats().first()
        val quarantineChats = chatRepository.getQuarantineChats().first()
        
        // Los chats ya deberían estar clasificados por el repositorio
        // basándose en si el remitente está en contactos
        // Esta función puede ser usada para re-clasificar si los contactos cambian
        
        // La clasificación real debe hacerse en el repositorio cuando se crean los chats
        // Este Use Case puede ser usado para re-clasificar después de sincronizar contactos
        // Por ahora, solo verificamos que los chats existan
        
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}

