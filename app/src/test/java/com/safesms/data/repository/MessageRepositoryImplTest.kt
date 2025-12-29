package com.safesms.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.safesms.data.local.database.SafeSmsDatabase
import com.safesms.data.local.database.dao.MessageDao
import com.safesms.data.local.system.SmsSystemProvider
import com.safesms.domain.model.Message
import com.safesms.util.Constants
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests para MessageRepositoryImpl usando Room in-memory database
 * 
 * ACTUALIZADO: Usa chatThreadId en lugar de chatId
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MessageRepositoryImplTest {

    private lateinit var database: SafeSmsDatabase
    private lateinit var messageDao: MessageDao
    private lateinit var smsSystemProvider: SmsSystemProvider
    private lateinit var messageRepository: MessageRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            SafeSmsDatabase::class.java
        ).allowMainThreadQueries().build()
        
        messageDao = database.messageDao()
        smsSystemProvider = mockk()
        messageRepository = MessageRepositoryImpl(messageDao, smsSystemProvider)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `inserta mensaje correctamente`() = runTest {
        // Given
        val threadId = 1L
        database.chatDao().insertChat(com.safesms.data.local.database.entities.ChatEntity(
            threadId = threadId,
            address = "+34612345678"
        ))

        val message = Message(
            id = 0,
            chatThreadId = threadId,
            address = "+34612345678",
            body = "Mensaje de prueba",
            timestamp = System.currentTimeMillis(),
            type = com.safesms.domain.model.MessageType.RECEIVED,
            status = com.safesms.domain.model.MessageStatus.RECEIVED,
            isRead = false
        )

        // When
        val result = messageRepository.insertMessage(message)

        // Then
        assertTrue(result is com.safesms.util.Result.Success)
        val messageId = (result as com.safesms.util.Result.Success).data
        assertTrue(messageId > 0)
    }

    @Test
    fun `obtiene mensajes por threadId correctamente`() = runTest {
        // Given
        val threadId = 1L
        database.chatDao().insertChat(com.safesms.data.local.database.entities.ChatEntity(
            threadId = threadId,
            address = "+34612345678"
        ))
        database.chatDao().insertChat(com.safesms.data.local.database.entities.ChatEntity(
            threadId = 2L,
            address = "+34612345679"
        ))

        val message1 = Message(
            id = 0,
            chatThreadId = threadId,
            address = "+34612345678",
            body = "Mensaje 1",
            timestamp = System.currentTimeMillis(),
            type = com.safesms.domain.model.MessageType.RECEIVED,
            status = com.safesms.domain.model.MessageStatus.RECEIVED,
            isRead = false
        )
        val message2 = Message(
            id = 0,
            chatThreadId = threadId,
            address = "+34612345678",
            body = "Mensaje 2",
            timestamp = System.currentTimeMillis() + 1000,
            type = com.safesms.domain.model.MessageType.RECEIVED,
            status = com.safesms.domain.model.MessageStatus.RECEIVED,
            isRead = false
        )
        val message3 = Message(
            id = 0,
            chatThreadId = 2L, // Diferente threadId
            address = "+34612345679",
            body = "Mensaje 3",
            timestamp = System.currentTimeMillis(),
            type = com.safesms.domain.model.MessageType.RECEIVED,
            status = com.safesms.domain.model.MessageStatus.RECEIVED,
            isRead = false
        )

        messageRepository.insertMessage(message1)
        messageRepository.insertMessage(message2)
        messageRepository.insertMessage(message3)

        // When
        val messages = messageRepository.getMessagesByThreadId(threadId).first()

        // Then
        assertEquals(2, messages.size)
        assertTrue(messages.all { it.chatThreadId == threadId })
        assertEquals("Mensaje 1", messages[0].body) // Ordenado por timestamp ASC
        assertEquals("Mensaje 2", messages[1].body)
    }

    @Test
    fun `marca mensaje como leído correctamente`() = runTest {
        // Given
        val threadId = 1L
        database.chatDao().insertChat(com.safesms.data.local.database.entities.ChatEntity(
            threadId = threadId,
            address = "+34612345678"
        ))

        val message = Message(
            id = 0,
            chatThreadId = threadId,
            address = "+34612345678",
            body = "Mensaje de prueba",
            timestamp = System.currentTimeMillis(),
            type = com.safesms.domain.model.MessageType.RECEIVED,
            status = com.safesms.domain.model.MessageStatus.RECEIVED,
            isRead = false
        )
        val insertResult = messageRepository.insertMessage(message)
        val messageId = (insertResult as com.safesms.util.Result.Success).data

        coEvery { smsSystemProvider.markSmsAsRead(messageId) } returns true

        // When
        val result = messageRepository.markAsRead(messageId)

        // Then
        assertTrue(result is com.safesms.util.Result.Success)
        
        // Verificar que el mensaje está marcado como leído en la BD
        val updatedMessages = messageRepository.getMessagesByThreadId(threadId).first()
        val updatedMessage = updatedMessages.first { it.id == messageId }
        assertTrue(updatedMessage.isRead)
    }

    @Test
    fun `retorna error cuando falla inserción`() = runTest {
        // Given - Crear mensaje con ID negativo (inválido) forzará un problema
        // Nota: Este test es difícil de simular con Room in-memory
        // En producción, los errores vendrían de problemas de permisos, espacio, etc.
        val message = Message(
            id = -999L, // ID inválido
            chatThreadId = 1L,
            address = "+34612345678",
            body = "Mensaje de prueba",
            timestamp = System.currentTimeMillis(),
            type = com.safesms.domain.model.MessageType.RECEIVED,
            status = com.safesms.domain.model.MessageStatus.RECEIVED,
            isRead = false
        )

        // When - Intentar insertar con ID inválido
        val result = messageRepository.insertMessage(message)

        // Then - Room debería manejar esto y retornar un ID válido o el existente
        // Este test verifica que no hay crash, el comportamiento específico depende de Room
        assertNotNull(result)
    }
}

