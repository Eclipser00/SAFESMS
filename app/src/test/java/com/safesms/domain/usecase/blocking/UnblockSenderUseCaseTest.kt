package com.safesms.domain.usecase.blocking

import com.safesms.domain.repository.BlockedSenderRepository
import com.safesms.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests para UnblockSenderUseCase
 */
class UnblockSenderUseCaseTest {

    private lateinit var blockedSenderRepository: BlockedSenderRepository
    private lateinit var unblockSenderUseCase: UnblockSenderUseCase

    @Before
    fun setup() {
        blockedSenderRepository = mockk()
        unblockSenderUseCase = UnblockSenderUseCase(blockedSenderRepository)
    }

    @Test
    fun `desbloquea remitente correctamente`() = runTest {
        // Given
        val threadId = 123L
        coEvery { blockedSenderRepository.unblockSender(threadId) } returns Result.Success(Unit)

        // When
        val result = unblockSenderUseCase(threadId)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { blockedSenderRepository.unblockSender(threadId) }
    }

    @Test
    fun `desbloquea remitente alfanumérico correctamente`() = runTest {
        // Given
        val threadId = 456L
        coEvery { blockedSenderRepository.unblockSender(threadId) } returns Result.Success(Unit)

        // When
        val result = unblockSenderUseCase(threadId)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { blockedSenderRepository.unblockSender(threadId) }
    }

    @Test
    fun `retorna error cuando falla el desbloqueo`() = runTest {
        // Given
        val threadId = 123L
        val exception = Exception("Error al desbloquear")
        coEvery { blockedSenderRepository.unblockSender(threadId) } returns Result.Error(exception)

        // When
        val result = unblockSenderUseCase(threadId)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }

    @Test
    fun `desbloquea remitente que no estaba bloqueado`() = runTest {
        // Given
        val threadId = 789L
        // El repositorio podría retornar Success incluso si no estaba bloqueado
        coEvery { blockedSenderRepository.unblockSender(threadId) } returns Result.Success(Unit)

        // When
        val result = unblockSenderUseCase(threadId)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { blockedSenderRepository.unblockSender(threadId) }
    }
}

