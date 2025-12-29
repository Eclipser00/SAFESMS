package com.safesms.domain.usecase.security

import com.safesms.domain.model.RiskFactor
import com.safesms.domain.repository.ContactRepository
import com.safesms.util.Constants
import com.safesms.util.normalizePhoneNumber
import javax.inject.Inject

/**
 * Analiza mensajes de un chat y detecta factores de riesgo
 */
class DetectRiskFactorsUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val detectLinksUseCase: DetectLinksInMessageUseCase,
    private val isAlphanumericSenderUseCase: IsAlphanumericSenderUseCase
) {
    /**
     * Detecta factores de riesgo para un remitente específico (sin analizar mensajes)
     * Analiza solo el remitente
     * @param address Dirección del remitente
     * @return Lista de factores de riesgo detectados
     */
    suspend operator fun invoke(address: String): List<RiskFactor> {
        val riskFactors = mutableListOf<RiskFactor>()

        // Detectar remitente alfanumérico
        if (isAlphanumericSenderUseCase(address)) {
            riskFactors.add(RiskFactor.AlphanumericSender)
        }

        // Detectar short code
        val digitsOnly = address.filter { it.isDigit() }
        if (digitsOnly.isNotEmpty() && digitsOnly.length < Constants.SHORT_CODE_MAX_DIGITS) {
            riskFactors.add(RiskFactor.ShortCode)
        }

        // Detectar remitente desconocido
        val normalized = address.normalizePhoneNumber()
        val isContact = contactRepository.isContactSaved(normalized)
        if (!isContact) {
            riskFactors.add(RiskFactor.UnknownSender)
        }

        return riskFactors
    }

    /**
     * Detecta factores de riesgo analizando un mensaje específico
     * @param address Dirección del remitente
     * @param messageBody Contenido del mensaje
     * @return Lista de factores de riesgo detectados
     */
    suspend operator fun invoke(address: String, messageBody: String): List<RiskFactor> {
        val riskFactors = mutableListOf<RiskFactor>()

        // Detectar enlaces
        val links = detectLinksUseCase(messageBody)
        if (links.isNotEmpty()) {
            riskFactors.add(RiskFactor.ContainsLinks(links))
        }

        // Detectar remitente alfanumérico
        if (isAlphanumericSenderUseCase(address)) {
            riskFactors.add(RiskFactor.AlphanumericSender)
        }

        // Detectar short code
        val digitsOnly = address.filter { it.isDigit() }
        if (digitsOnly.isNotEmpty() && digitsOnly.length < Constants.SHORT_CODE_MAX_DIGITS) {
            riskFactors.add(RiskFactor.ShortCode)
        }

        // Detectar remitente desconocido
        val normalized = address.normalizePhoneNumber()
        val isContact = contactRepository.isContactSaved(normalized)
        if (!isContact) {
            riskFactors.add(RiskFactor.UnknownSender)
        }

        return riskFactors
    }
}
