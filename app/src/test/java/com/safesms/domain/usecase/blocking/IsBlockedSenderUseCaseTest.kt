package com.safesms.domain.usecase.blocking

import com.safesms.domain.repository.BlockedSenderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests para IsBlockedSenderUseCase
 */
class IsBlockedSenderUseCaseTest {

    private lateinit var blockedSenderRepository: BlockedSenderRepository
    private lateinit var isBlockedSenderUseCase: IsBlockedSenderUseCase

    @Before
    fun setup() {
        blockedSenderRepository = mockk()
        isBlockedSenderUseCase = IsBlockedSenderUseCase(blockedSenderRepository)
    }

    @Test
    fun `retorna true cuando remitente está bloqueado`() = runTest {
        // Given
        val threadId = 123L
        coEvery { blockedSenderRepository.isThreadBlocked(threadId) } returns true

        // When
        val result = isBlockedSenderUseCase(threadId)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { blockedSenderRepository.isThreadBlocked(threadId) }
    }

    @Test
    fun `retorna false cuando remitente NO está bloqueado`() = runTest {
        // Given
        val threadId = 123L
        coEvery { blockedSenderRepository.isThreadBlocked(threadId) } returns false

        // When
        val result = isBlockedSenderUseCase(threadId)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { blockedSenderRepository.isThreadBlocked(threadId) }
    }

    @Test
    fun `verifica correctamente thread alfanumérico`() = runTest {
        // Given
        val threadId = 456L
        coEvery { blockedSenderRepository.isThreadBlocked(threadId) } returns true

        // When
        val result = isBlockedSenderUseCase(threadId)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { blockedSenderRepository.isThreadBlocked(threadId) }
    }

    @Test
    fun `verifica correctamente thread de short code`() = runTest {
        // Given
        val threadId = 789L
        coEvery { blockedSenderRepository.isThreadBlocked(threadId) } returns false

        // When
        val result = isBlockedSenderUseCase(threadId)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { blockedSenderRepository.isThreadBlocked(threadId) }
    }
}

