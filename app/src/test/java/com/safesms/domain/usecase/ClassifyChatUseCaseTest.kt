package com.safesms.domain.usecase

import com.safesms.domain.model.ChatType
import com.safesms.domain.repository.ContactRepository
import com.safesms.domain.usecase.chat.ClassifyChatUseCase
import com.safesms.util.NormalizedResult
import com.safesms.util.PhoneNormalizer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests para ClassifyChatUseCase
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ClassifyChatUseCaseTest {

    private lateinit var contactRepository: ContactRepository
    private lateinit var phoneNormalizer: PhoneNormalizer
    private lateinit var classifyChatUseCase: ClassifyChatUseCase

    @Before
    fun setup() {
        contactRepository = mockk()
        phoneNormalizer = mockk()
        
        // Mock PhoneNormalizer
        every { phoneNormalizer.normalizePhone(any()) } answers {
            val phone = firstArg<String>()
            val cleaned = phone.replace(Regex("[\\s\\-\\(\\)]"), "")
            val digitsOnly = cleaned.filter { it.isDigit() }
            
            if (digitsOnly.length > 6) {
                NormalizedResult(
                    type = "E164",
                    key = if (cleaned.startsWith("+")) cleaned else "+34$digitsOnly",
                    original = phone,
                    cleaned = cleaned,
                    regionUsed = "ES"
                )
            } else if (cleaned.any { it.isLetter() }) {
                NormalizedResult(
                    type = "ALPHANUMERIC",
                    key = "alpha:$cleaned",
                    original = phone,
                    cleaned = cleaned,
                    regionUsed = null
                )
            } else {
                NormalizedResult(
                    type = "SHORT_OR_NONPHONE",
                    key = "short:$cleaned",
                    original = phone,
                    cleaned = cleaned,
                    regionUsed = null
                )
            }
        }
        
        classifyChatUseCase = ClassifyChatUseCase(contactRepository, phoneNormalizer)
    }

    @Test
    fun `clasifica como INBOX cuando remitente está en contactos`() = runTest {
        // Given
        val address = "+34612345678"
        val normalizedAddress = "+34612345678" 
        coEvery { contactRepository.isContactSaved(normalizedAddress) } returns true

        // When
        val result = classifyChatUseCase(address)

        // Then
        assertEquals(ChatType.INBOX, result)
    }

    @Test
    fun `clasifica como QUARANTINE cuando remitente NO está en contactos`() = runTest {
        // Given
        val address = "+34612345678"
        val normalizedAddress = "+34612345678"
        coEvery { contactRepository.isContactSaved(normalizedAddress) } returns false

        // When
        val result = classifyChatUseCase(address)

        // Then
        assertEquals(ChatType.QUARANTINE, result)
    }

    @Test
    fun `normaliza número de teléfono antes de verificar contacto`() = runTest {
        // Given
        val address = "+34 612 345 678" // Con espacios
        val normalizedAddress = "+34612345678"
        coEvery { contactRepository.isContactSaved(normalizedAddress) } returns true

        // When
        val result = classifyChatUseCase(address)

        // Then
        assertEquals(ChatType.INBOX, result)
    }

    @Test
    fun `clasifica como QUARANTINE para remitentes alfanuméricos`() = runTest {
        // Given
        val address = "BANCO123"

        // When
        val result = classifyChatUseCase(address)

        // Then
        assertEquals(ChatType.QUARANTINE, result)
    }
}
