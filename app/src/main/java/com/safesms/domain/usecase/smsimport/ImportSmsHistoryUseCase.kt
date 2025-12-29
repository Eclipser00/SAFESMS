package com.safesms.domain.usecase.smsimport

import android.content.Context
import com.safesms.data.local.system.SmsSystemProvider
import com.safesms.data.mapper.MessageMapper
import com.safesms.domain.model.Message
import com.safesms.domain.repository.ChatRepository
import com.safesms.domain.repository.MessageRepository
import com.safesms.domain.usecase.chat.ClassifyChatUseCase
import com.safesms.util.Result
import com.safesms.util.ThreadIdHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Importa histórico completo de SMS del sistema.
 * 
 * ACTUALIZADO: Usa ThreadIdHelper para obtener thread_id del sistema.
 */
class ImportSmsHistoryUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsSystemProvider: SmsSystemProvider,
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val classifyChatUseCase: ClassifyChatUseCase
) {
    suspend operator fun invoke(): Result<Int> = try {
        val systemMessages = smsSystemProvider.getAllSms()
        var importedCount = 0

        systemMessages.forEach { systemSms ->
            // Obtener thread_id del sistema Android
            val threadId = ThreadIdHelper.getOrCreateThreadId(context, systemSms.address)
            
            // Clasificar chat (Inbox o Cuarentena)
            val chatType = classifyChatUseCase(systemSms.address)

            // Convertir SystemSms a Message
            val message = MessageMapper.fromSystemSms(systemSms, threadId)
            
            // Crear o actualizar chat
            val chatResult = chatRepository.createOrUpdateChat(
                threadId = threadId,
                address = systemSms.address,
                lastMessage = message
            )
            
            if (chatResult is Result.Error) {
                return@forEach
            }

            // Insertar mensaje
            val insertResult = messageRepository.insertMessage(message)
            if (insertResult is Result.Success) {
                importedCount++
            }
        }

        Result.Success(importedCount)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
