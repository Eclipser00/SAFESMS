package com.safesms.domain.usecase.message

import android.content.Context
import com.safesms.domain.model.ChatType
import com.safesms.domain.model.Message
import com.safesms.domain.repository.BlockedSenderRepository
import com.safesms.domain.repository.ChatRepository
import com.safesms.domain.repository.ConfigurationRepository
import com.safesms.domain.repository.MessageRepository
import com.safesms.domain.usecase.chat.ClassifyChatUseCase
import com.safesms.presentation.util.NotificationManager
import com.safesms.util.Result
import com.safesms.util.ThreadIdHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use Case para procesar un SMS recibido.
 * 
 * ACTUALIZADO: Usa ThreadIdHelper para obtener thread_id del sistema Android.
 * Ya no necesita PhoneNormalizer - delegamos al sistema la agrupación.
 */
class ReceiveMessageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val classifyChatUseCase: ClassifyChatUseCase,
    private val blockedSenderRepository: BlockedSenderRepository,
    private val configurationRepository: ConfigurationRepository,
    private val notificationManager: NotificationManager
) {
    /**
     * Procesa un SMS recibido.
     * 
     * @param address Dirección del remitente
     * @param body Contenido del mensaje
     * @param timestamp Timestamp del mensaje en milisegundos
     * @return Result con Unit si fue exitoso, Error en caso contrario
     */
    suspend operator fun invoke(
        address: String,
        body: String,
        timestamp: Long
    ): Result<Unit> {
        return try {
            // 1. Obtener thread_id del sistema Android (esto reemplaza TODA normalización)
            val threadId = ThreadIdHelper.getOrCreateThreadId(context, address)
            
            // 2. Verificar si el thread está bloqueado
            val isBlocked = blockedSenderRepository.isThreadBlocked(threadId)
            if (isBlocked) {
                // Si está bloqueado, no procesar el mensaje ni generar notificación
                return Result.Success(Unit)
            }

            // 3. Clasificar el chat (INBOX o QUARANTINE)
            val chatType = classifyChatUseCase(address)

            // 4. Crear el mensaje (objeto dominio)
            val message = Message(
                id = 0, // Se asignará al insertar
                chatThreadId = threadId,
                address = address,
                body = body,
                timestamp = timestamp,
                type = com.safesms.domain.model.MessageType.RECEIVED,
                status = com.safesms.domain.model.MessageStatus.RECEIVED,
                errorCode = null,
                isRead = false
            )

            // 5. Crear o actualizar el chat PRIMERO (para que el mensaje encuentre a su "padre" en BD)
            val chatResult = chatRepository.createOrUpdateChat(
                threadId = threadId,
                address = address,
                lastMessage = message
            )
            
            if (chatResult is Result.Error) {
                return Result.Success(Unit)
            }

            // 6. Guardar el mensaje DESPUÉS
            val messageId = when (val insertResult = messageRepository.insertMessage(message)) {
                is Result.Success -> insertResult.data.toInt()
                is Result.Error -> return Result.Success(Unit)
            }

            // 7. Generar notificación según tipo de chat y configuración
            val isInbox = chatType == ChatType.INBOX
            
            if (isInbox) {
                // Notificación normal para Inbox con texto del mensaje
                val chat = chatRepository.getChatByThreadId(threadId)
                val displayName = chat?.contactName ?: address
                
                notificationManager.showInboxNotification(
                    messageId = messageId,
                    address = displayName,
                    body = body,
                    chatId = threadId
                )
            } else {
                // Notificación para Cuarentena (solo si está habilitada)
                val notificationsEnabled = configurationRepository
                    .getQuarantineNotificationsEnabled()
                    .first()
                
                if (notificationsEnabled) {
                    notificationManager.showQuarantineNotification(
                        messageId = messageId,
                        chatId = threadId
                    )
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
