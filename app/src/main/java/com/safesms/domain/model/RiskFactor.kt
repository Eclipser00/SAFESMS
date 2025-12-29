package com.safesms.domain.model

/**
 * Sealed class para factores de riesgo detectados
 */
sealed class RiskFactor {
    data class ContainsLinks(val links: List<String>) : RiskFactor()
    data object AlphanumericSender : RiskFactor()
    data object ShortCode : RiskFactor()
    data object UnknownSender : RiskFactor()
}
