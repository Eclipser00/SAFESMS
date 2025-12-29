package com.safesms.domain.usecase.message

import android.content.Context
import com.safesms.domain.repository.ChatRepository
import com.safesms.domain.repository.MessageRepository
import com.safesms.util.Result
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests para SendMessageUseCase
 * 
 * ACTUALIZADO: Usa threadId
 */
class SendMessageUseCaseTest {

    private lateinit var context: Context
    private lateinit var messageRepository: MessageRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var sendMessageUseCase: SendMessageUseCase

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        messageRepository = mockk()
        chatRepository = mockk()
        
        sendMessageUseCase = SendMessageUseCase(
            context,
            messageRepository,
            chatRepository
        )
    }

    @Test
    fun `envía mensaje correctamente`() = runTest {
        // Given
        val threadId = 123L
        val address = "+34612345678"
        val body = "Mensaje de prueba"
        val messageId = 1L

        coEvery { messageRepository.insertMessage(any()) } returns Result.Success(messageId)
        coEvery { chatRepository.createOrUpdateChat(threadId, address, any()) } returns Result.Success(threadId)
        coEvery { messageRepository.sendMessage(address, body, threadId) } returns Result.Success(Unit)

        // When
        val result = sendMessageUseCase(threadId, address, body)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { messageRepository.insertMessage(any()) }
        coVerify(exactly = 1) { chatRepository.createOrUpdateChat(threadId, address, any()) }
        coVerify(exactly = 1) { messageRepository.sendMessage(address, body, threadId) }
    }

    @Test
    fun `maneja error al insertar mensaje`() = runTest {
        // Given
        val threadId = 123L
        val address = "+34612345678"
        val body = "Mensaje de prueba"

        coEvery { chatRepository.createOrUpdateChat(any(), any(), any()) } returns Result.Success(threadId)
        coEvery { messageRepository.insertMessage(any()) } returns Result.Error(Exception("Error DB"))

        // When
        val result = sendMessageUseCase(threadId, address, body)

        // Then
        assertTrue(result is Result.Error)

        // El chat se actualiza PRIMERO para asegurar la FK, luego falla el mensaje
        coVerify(exactly = 1) { chatRepository.createOrUpdateChat(any(), any(), any()) }
        coVerify(exactly = 0) { messageRepository.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `envía mensaje con address corto`() = runTest {
        // Given
        val threadId = 789L
        val address = "12345"
        val body = "Mensaje de prueba"
        val messageId = 2L

        coEvery { messageRepository.insertMessage(any()) } returns Result.Success(messageId)
        coEvery { chatRepository.createOrUpdateChat(threadId, address, any()) } returns Result.Success(threadId)
        coEvery { messageRepository.sendMessage(address, body, threadId) } returns Result.Success(Unit)

        // When
        val result = sendMessageUseCase(threadId, address, body)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { messageRepository.insertMessage(any()) }
        coVerify(exactly = 1) { chatRepository.createOrUpdateChat(threadId, address, any()) }
        coVerify(exactly = 1) { messageRepository.sendMessage(address, body, threadId) }
    }
}
