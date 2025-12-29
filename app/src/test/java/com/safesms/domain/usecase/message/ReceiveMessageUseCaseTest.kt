package com.safesms.domain.usecase.message

import android.content.Context
import com.safesms.domain.repository.BlockedSenderRepository
import com.safesms.domain.repository.ChatRepository
import com.safesms.domain.repository.ConfigurationRepository
import com.safesms.domain.repository.MessageRepository
import com.safesms.domain.usecase.chat.ClassifyChatUseCase
import com.safesms.presentation.util.NotificationManager
import com.safesms.domain.model.ChatType
import com.safesms.util.Result
import com.safesms.util.ThreadIdHelper
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests para ReceiveMessageUseCase
 * 
 * ACTUALIZADO: Usa threadId y ThreadIdHelper
 */
class ReceiveMessageUseCaseTest {

    private lateinit var context: Context
    private lateinit var messageRepository: MessageRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var classifyChatUseCase: ClassifyChatUseCase
    private lateinit var blockedSenderRepository: BlockedSenderRepository
    private lateinit var configurationRepository: ConfigurationRepository
    private lateinit var notificationManager: NotificationManager
    private lateinit var receiveMessageUseCase: ReceiveMessageUseCase

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        messageRepository = mockk()
        chatRepository = mockk()
        classifyChatUseCase = mockk()
        blockedSenderRepository = mockk()
        configurationRepository = mockk()
        notificationManager = mockk(relaxed = true)
        
        receiveMessageUseCase = ReceiveMessageUseCase(
            context,
            messageRepository,
            chatRepository,
            classifyChatUseCase,
            blockedSenderRepository,
            configurationRepository,
            notificationManager
        )
        
        // Mock ThreadIdHelper
        mockkObject(ThreadIdHelper)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `recibe mensaje y lo procesa correctamente`() = runTest {
        // Given
        val address = "+34612345678"
        val body = "Mensaje de prueba"
        val timestamp = System.currentTimeMillis()
        val threadId = 123L
        val messageIdLong = 1L
        val messageIdInt = 1 // El código convierte Long a Int

        every { ThreadIdHelper.getOrCreateThreadId(context, address) } returns threadId
        coEvery { blockedSenderRepository.isThreadBlocked(threadId) } returns false
        coEvery { classifyChatUseCase(address) } returns ChatType.INBOX
        coEvery { messageRepository.insertMessage(any()) } returns Result.Success(messageIdLong)
        coEvery { chatRepository.createOrUpdateChat(threadId, address, any()) } returns Result.Success(threadId)
        coEvery { chatRepository.getChatByThreadId(threadId) } returns null
        coEvery { configurationRepository.getQuarantineNotificationsEnabled() } returns flowOf(false)

        // When
        val result = receiveMessageUseCase(address, body, timestamp)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { messageRepository.insertMessage(any()) }
        coVerify(exactly = 1) { chatRepository.createOrUpdateChat(threadId, address, any()) }
        verify(exactly = 1) { notificationManager.showInboxNotification(messageIdInt, address, body, threadId) }
    }

    @Test
    fun `ignora mensaje de thread bloqueado`() = runTest {
        // Given
        val address = "+34666666666"
        val body = "Mensaje bloqueado"
        val timestamp = System.currentTimeMillis()
        val threadId = 456L

        every { ThreadIdHelper.getOrCreateThreadId(context, address) } returns threadId
        coEvery { blockedSenderRepository.isThreadBlocked(threadId) } returns true

        // When
        val result = receiveMessageUseCase(address, body, timestamp)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 0) { messageRepository.insertMessage(any()) }
        coVerify(exactly = 0) { chatRepository.createOrUpdateChat(any(), any(), any()) }
        verify(exactly = 0) { notificationManager.showInboxNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { notificationManager.showQuarantineNotification(any(), any()) }
    }

    @Test
    fun `maneja error al crear threadId`() = runTest {
        // Given
        val address = "+34612345678"
        val body = "Mensaje de prueba"
        val timestamp = System.currentTimeMillis()

        every { ThreadIdHelper.getOrCreateThreadId(context, address) } throws IllegalStateException("Error")

        // When/Then - Should catch and return Error Result
        val result = receiveMessageUseCase(address, body, timestamp)
        assertTrue(result is Result.Error)
    }
}
