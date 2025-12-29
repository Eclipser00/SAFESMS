package com.safesms.domain.usecase.message

import android.content.Context
import com.safesms.domain.model.Message
import com.safesms.domain.repository.ChatRepository
import com.safesms.domain.repository.MessageRepository
import com.safesms.util.Result
import com.safesms.util.ThreadIdHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Envía un SMS.
 * 
 * ACTUALIZADO: Usa ThreadIdHelper para obtener thread_id.
 */
class SendMessageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository
) {
    /**
     * Envía un SMS.
     * 
     * @param threadId thread_id del chat
     * @param address Address del destinatario
     * @param body Contenido del mensaje
     * @return Result con Unit si fue exitoso
     */
    suspend operator fun invoke(
        threadId: Long,
        address: String,
        body: String
    ): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            
            // 1. Crear mensaje pendiente
            val message = Message(
                id = 0, // Room asignará el ID
                chatThreadId = threadId,
                address = address,
                body = body,
                timestamp = timestamp,
                type = com.safesms.domain.model.MessageType.SENT,
                status = com.safesms.domain.model.MessageStatus.PENDING,
                errorCode = null,
                isRead = true // Los mensajes enviados ya están "leídos"
            )
            
            // 2. Actualizar o crear el chat PRIMERO
            chatRepository.createOrUpdateChat(
                threadId = threadId,
                address = address,
                lastMessage = message
            )
            
            // 3. Guardar mensaje en BD local DESPUÉS
            val messageId = when (val insertResult = messageRepository.insertMessage(message)) {
                is Result.Success -> insertResult.data
                is Result.Error -> return Result.Error(insertResult.exception)
            }
            
            // 4. Enviar SMS (el repository manejará el tracking de estado)
            val sendResult = messageRepository.sendMessage(address, body, threadId)
            
            sendResult
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
