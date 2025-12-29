package com.safesms.domain.usecase

import com.safesms.domain.model.RiskFactor
import com.safesms.domain.repository.ContactRepository
import com.safesms.domain.usecase.security.DetectLinksInMessageUseCase
import com.safesms.domain.usecase.security.DetectRiskFactorsUseCase
import com.safesms.domain.usecase.security.IsAlphanumericSenderUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests para DetectRiskFactorsUseCase
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DetectRiskFactorsUseCaseTest {

    private lateinit var contactRepository: ContactRepository
    private lateinit var detectLinksUseCase: DetectLinksInMessageUseCase
    private lateinit var isAlphanumericSenderUseCase: IsAlphanumericSenderUseCase
    private lateinit var detectRiskFactorsUseCase: DetectRiskFactorsUseCase

    @Before
    fun setup() {
        contactRepository = mockk()
        detectLinksUseCase = DetectLinksInMessageUseCase()
        isAlphanumericSenderUseCase = IsAlphanumericSenderUseCase()
        detectRiskFactorsUseCase = DetectRiskFactorsUseCase(
            contactRepository,
            detectLinksUseCase,
            isAlphanumericSenderUseCase
        )
    }

    @Test
    fun `detecta enlaces en mensaje`() = runTest {
        // Given
        val address = "34612345678"
        val messageBody = "Visita https://example.com para más información"
        coEvery { contactRepository.isContactSaved(any()) } returns false

        // When
        val result = detectRiskFactorsUseCase(address, messageBody)

        // Then
        assertTrue(result.any { it is RiskFactor.ContainsLinks })
        val containsLinks = result.first { it is RiskFactor.ContainsLinks } as RiskFactor.ContainsLinks
        assertTrue(containsLinks.links.isNotEmpty())
        assertTrue(containsLinks.links.any { it.contains("example.com") })
    }

    @Test
    fun `detecta remitente alfanumérico`() = runTest {
        // Given
        val address = "BANCO123"
        coEvery { contactRepository.isContactSaved(any()) } returns false

        // When
        val result = detectRiskFactorsUseCase(address)

        // Then
        assertTrue(result.contains(RiskFactor.AlphanumericSender))
    }

    @Test
    fun `detecta short code`() = runTest {
        // Given
        val address = "12345" // Menos de 6 dígitos
        coEvery { contactRepository.isContactSaved(any()) } returns false

        // When
        val result = detectRiskFactorsUseCase(address)

        // Then
        assertTrue(result.contains(RiskFactor.ShortCode))
    }

    @Test
    fun `no detecta short code para números largos`() = runTest {
        // Given
        val address = "34612345678" // Más de 6 dígitos
        coEvery { contactRepository.isContactSaved(any()) } returns false

        // When
        val result = detectRiskFactorsUseCase(address)

        // Then
        assertFalse(result.contains(RiskFactor.ShortCode))
    }

    @Test
    fun `detecta remitente desconocido cuando NO está en contactos`() = runTest {
        // Given
        val address = "+34612345678"
        val normalizedAddress = "+34612345678" // normalizePhoneNumber mantiene el +
        coEvery { contactRepository.isContactSaved(normalizedAddress) } returns false

        // When
        val result = detectRiskFactorsUseCase(address)

        // Then
        assertTrue(result.contains(RiskFactor.UnknownSender))
    }

    @Test
    fun `no detecta remitente desconocido cuando está en contactos`() = runTest {
        // Given
        val address = "+34612345678"
        val normalizedAddress = "+34612345678"
        coEvery { contactRepository.isContactSaved(normalizedAddress) } returns true

        // When
        val result = detectRiskFactorsUseCase(address)

        // Then
        assertFalse(result.contains(RiskFactor.UnknownSender))
    }

    @Test
    fun `detecta múltiples factores de riesgo`() = runTest {
        // Given
        val address = "BANCO123" 
        val messageBody = "Visita https://example.com"
        coEvery { contactRepository.isContactSaved(any()) } returns false

        // When
        val result = detectRiskFactorsUseCase(address, messageBody)

        // Then
        // Debe detectar: ContainsLinks, AlphanumericSender, UnknownSender
        assertTrue(result.size >= 2)
        assertTrue(result.any { it is RiskFactor.ContainsLinks })
        assertTrue(result.contains(RiskFactor.AlphanumericSender))
        assertTrue(result.contains(RiskFactor.UnknownSender))
    }
}
