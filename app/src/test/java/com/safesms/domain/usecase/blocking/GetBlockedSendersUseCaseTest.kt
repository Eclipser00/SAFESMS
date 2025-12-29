package com.safesms.domain.usecase.blocking

import com.safesms.domain.model.BlockedSender
import com.safesms.domain.repository.BlockedSenderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests para GetBlockedSendersUseCase
 */
class GetBlockedSendersUseCaseTest {

    private lateinit var blockedSenderRepository: BlockedSenderRepository
    private lateinit var getBlockedSendersUseCase: GetBlockedSendersUseCase

    @Before
    fun setup() {
        blockedSenderRepository = mockk()
        getBlockedSendersUseCase = GetBlockedSendersUseCase(blockedSenderRepository)
    }

    @Test
    fun `obtiene lista vacía cuando no hay remitentes bloqueados`() = runTest {
        // Given
        every { blockedSenderRepository.getAllBlockedSenders() } returns flowOf(emptyList())

        // When
        val result = getBlockedSendersUseCase().first()

        // Then
        assertTrue(result.isEmpty())
        verify(exactly = 1) { blockedSenderRepository.getAllBlockedSenders() }
    }

    @Test
    fun `obtiene lista de remitentes bloqueados correctamente`() = runTest {
        // Given
        val blockedSenders = listOf(
            BlockedSender(
                threadId = 123L,
                address = "+34612345678",
                blockedTimestamp = System.currentTimeMillis()
            ),
            BlockedSender(
                threadId = 456L,
                address = "BANCO123",
                blockedTimestamp = System.currentTimeMillis()
            ),
            BlockedSender(
                threadId = 789L,
                address = "12345",
                blockedTimestamp = System.currentTimeMillis()
            )
        )
        every { blockedSenderRepository.getAllBlockedSenders() } returns flowOf(blockedSenders)

        // When
        val result = getBlockedSendersUseCase().first()

        // Then
        assertEquals(3, result.size)
        assertEquals(123L, result[0].threadId)
        assertEquals("+34612345678", result[0].address)
        assertEquals(456L, result[1].threadId)
        assertEquals("BANCO123", result[1].address)
        assertEquals(789L, result[2].threadId)
        assertEquals("12345", result[2].address)
        verify(exactly = 1) { blockedSenderRepository.getAllBlockedSenders() }
    }

    @Test
    fun `obtiene un solo remitente bloqueado`() = runTest {
        // Given
        val blockedSender = BlockedSender(
            threadId = 123L,
            address = "+34612345678",
            blockedTimestamp = System.currentTimeMillis()
        )
        every { blockedSenderRepository.getAllBlockedSenders() } returns flowOf(listOf(blockedSender))

        // When
        val result = getBlockedSendersUseCase().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(123L, result[0].threadId)
        assertEquals("+34612345678", result[0].address)
    }

    @Test
    fun `maneja actualizaciones del flow correctamente`() = runTest {
        // Given
        val blockedSenders1 = listOf(
            BlockedSender(
                threadId = 123L,
                address = "+34612345678",
                blockedTimestamp = System.currentTimeMillis()
            )
        )
        val blockedSenders2 = listOf(
            BlockedSender(
                threadId = 123L,
                address = "+34612345678",
                blockedTimestamp = System.currentTimeMillis()
            ),
            BlockedSender(
                threadId = 456L,
                address = "BANCO123",
                blockedTimestamp = System.currentTimeMillis()
            )
        )
        every { blockedSenderRepository.getAllBlockedSenders() } returns flowOf(blockedSenders1, blockedSenders2)

        // When
        val result1 = getBlockedSendersUseCase().first()

        // Then
        assertEquals(1, result1.size)
        verify(exactly = 1) { blockedSenderRepository.getAllBlockedSenders() }
    }
}

