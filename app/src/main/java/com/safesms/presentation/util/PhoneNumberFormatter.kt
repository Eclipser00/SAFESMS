package com.safesms.presentation.util

/**
 * Utilidades para formateo y normalización de números de teléfono
 */
object PhoneNumberFormatter {
    
    /**
     * Formatea número para mostrar en UI
     * Ejemplo: "+34612345678" -> "+34 612 345 678"
     */
    fun formatPhoneNumber(phoneNumber: String): String {
        val normalized = normalizePhoneNumber(phoneNumber)
        
        return when {
            normalized.startsWith("+") && normalized.length > 3 -> {
                val countryCode = normalized.substring(0, 3)
                val number = normalized.substring(3)
                "$countryCode ${formatNumberWithSpaces(number)}"
            }
            normalized.length > 6 -> {
                formatNumberWithSpaces(normalized)
            }
            else -> normalized
        }
    }
    
    /**
     * Normaliza número para comparación
     * Elimina espacios, guiones, paréntesis, etc.
     */
    fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[\\s\\-\\(\\)]"), "")
    }
    
    private fun formatNumberWithSpaces(number: String): String {
        return number.chunked(3).joinToString(" ")
    }
}

