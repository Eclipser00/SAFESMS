package com.safesms.domain.usecase.security

import com.safesms.util.extractDomain
import javax.inject.Inject

/**
 * Valida seguridad básica de un link (análisis local)
 */
class ValidateLinkSafetyUseCase @Inject constructor() {
    operator fun invoke(url: String): LinkSafetyInfo {
        val domain = url.extractDomain()
        val suspiciousReasons = mutableListOf<String>()
        
        domain?.let { d ->
            if (d.contains("bit.ly")) {
                suspiciousReasons.add("Acortador de URL conocido")
            }
            if (d.contains("tinyurl")) {
                suspiciousReasons.add("Acortador de URL conocido")
            }
            if (d.contains("t.co")) {
                suspiciousReasons.add("Acortador de URL de Twitter")
            }
            if (d.length < 5) {
                suspiciousReasons.add("Dominio muy corto")
            }
        } ?: run {
            suspiciousReasons.add("No se pudo extraer el dominio")
        }
        
        val isSuspicious = suspiciousReasons.isNotEmpty()

        return LinkSafetyInfo(
            url = url,
            domain = domain,
            isSuspicious = isSuspicious,
            suspiciousReasons = suspiciousReasons
        )
    }
}

/**
 * Información de seguridad de un enlace
 */
data class LinkSafetyInfo(
    val url: String,
    val domain: String?,
    val isSuspicious: Boolean,
    val suspiciousReasons: List<String> = emptyList()
)
