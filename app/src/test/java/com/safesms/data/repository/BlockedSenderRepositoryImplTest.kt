package com.safesms.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.safesms.data.local.database.SafeSmsDatabase
import com.safesms.data.local.database.dao.BlockedSenderDao
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
 * Tests para BlockedSenderRepositoryImpl
 * 
 * ACTUALIZADO: Usa threadId en lugar de address
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BlockedSenderRepositoryImplTest {

    private lateinit var database: SafeSmsDatabase
    private lateinit var blockedSenderDao: BlockedSenderDao
    private lateinit var blockedSenderRepository: BlockedSenderRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            SafeSmsDatabase::class.java
        ).allowMainThreadQueries().build()
        
        blockedSenderDao = database.blockedSenderDao()
        blockedSenderRepository = BlockedSenderRepositoryImpl(blockedSenderDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `bloquea thread correctamente`() = runTest {
        // Given
        val threadId = 123L
        val address = "+34612345678"

        // When
        val result = blockedSenderRepository.blockSender(threadId, address)

        // Then
        assertTrue(result is com.safesms.util.Result.Success)
        assertTrue(blockedSenderRepository.isThreadBlocked(threadId))
    }

    @Test
    fun `desbloquea thread correctamente`() = runTest {
        // Given
        val threadId = 123L
        val address = "+34612345678"
        blockedSenderRepository.blockSender(threadId, address)
        assertTrue(blockedSenderRepository.isThreadBlocked(threadId))

        // When
        val result = blockedSenderRepository.unblockSender(threadId)

        // Then
        assertTrue(result is com.safesms.util.Result.Success)
        assertFalse(blockedSenderRepository.isThreadBlocked(threadId))
    }

    @Test
    fun `retorna false cuando thread no está bloqueado`() = runTest {
        // Given
        val threadId = 456L

        // When
        val isBlocked = blockedSenderRepository.isThreadBlocked(threadId)

        // Then
        assertFalse(isBlocked)
    }

    @Test
    fun `retorna true cuando thread está bloqueado`() = runTest {
        // Given
        val threadId = 123L
        val address = "+34612345678"
        blockedSenderRepository.blockSender(threadId, address)

        // When
        val isBlocked = blockedSenderRepository.isThreadBlocked(threadId)

        // Then
        assertTrue(isBlocked)
    }

    @Test
    fun `getAllBlockedSenders retorna lista vacía cuando no hay threads bloqueados`() = runTest {
        // When
        val result = blockedSenderRepository.getAllBlockedSenders().first()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllBlockedSenders retorna todos los threads bloqueados`() = runTest {
        // Given
        val threadId1 = 123L
        val address1 = "+34612345678"
        val threadId2 = 456L
        val address2 = "+34987654321"
        val threadId3 = 789L
        val address3 = "BANCO123"
        
        blockedSenderRepository.blockSender(threadId1, address1)
        blockedSenderRepository.blockSender(threadId2, address2)
        blockedSenderRepository.blockSender(threadId3, address3)

        // When
        val result = blockedSenderRepository.getAllBlockedSenders().first()

        // Then
        assertEquals(3, result.size)
        assertTrue(result.any { it.threadId == threadId1 && it.address == address1 })
        assertTrue(result.any { it.threadId == threadId2 && it.address == address2 })
        assertTrue(result.any { it.threadId == threadId3 && it.address == address3 })
    }

    @Test
    fun `bloquear el mismo thread dos veces no duplica`() = runTest {
        // Given
        val threadId = 123L
        val address = "+34612345678"

        // When
        blockedSenderRepository.blockSender(threadId, address)
        blockedSenderRepository.blockSender(threadId, address)

        // Then
        val result = blockedSenderRepository.getAllBlockedSenders().first()
        assertEquals(1, result.size)
    }

    @Test
    fun `desbloquear thread que no está bloqueado no produce error`() = runTest {
        // Given
        val threadId = 999L

        // When
        val result = blockedSenderRepository.unblockSender(threadId)

        // Then
        assertTrue(result is com.safesms.util.Result.Success)
    }

    @Test
    fun `bloquear con razón guarda la razón correctamente`() = runTest {
        // Given
        val threadId = 123L
        val address = "+34612345678"
        val reason = "Spam"

        // When
        blockedSenderRepository.blockSender(threadId, address, reason)

        // Then
        val result = blockedSenderRepository.getAllBlockedSenders().first()
        assertEquals(1, result.size)
        assertEquals(reason, result[0].reason)
    }
}
