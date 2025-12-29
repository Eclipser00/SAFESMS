package com.safesms.util

import java.util.regex.Pattern

/**
 * Extension functions útiles para el dominio
 */

/**
 * Detecta si el string contiene una URL usando Patterns.WEB_URL
 */
fun String.containsUrl(): Boolean {
    val urlPattern = Pattern.compile(
        "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?]))",
        Pattern.CASE_INSENSITIVE
    )
    return urlPattern.matcher(this).find()
}

/**
 * Extrae todas las URLs encontradas en el string
 */
fun String.extractUrls(): List<String> {
    val urlPattern = Pattern.compile(
        "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?]))",
        Pattern.CASE_INSENSITIVE
    )
    val matcher = urlPattern.matcher(this)
    val urls = mutableListOf<String>()
    while (matcher.find()) {
        urls.add(matcher.group())
    }
    return urls
}

/**
 * Verifica si el string contiene letras (alfanumérico)
 */
fun String.isAlphanumeric(): Boolean {
    return this.any { it.isLetter() }
}

/**
 * Verifica si el string es un número corto (menos de SHORT_CODE_MAX_DIGITS dígitos)
 * Solo cuenta dígitos, ignora espacios, guiones, etc.
 */
fun String.isShortCode(): Boolean {
    val digitsOnly = this.filter { it.isDigit() }
    return digitsOnly.length > 0 && digitsOnly.length < Constants.SHORT_CODE_MAX_DIGITS
}

/**
 * Normaliza un número de teléfono eliminando espacios, guiones, paréntesis, etc.
 * Solo deja dígitos y el símbolo + al inicio
 * 
 * IMPORTANTE: Solo normaliza números de teléfono válidos.
 * - Remitentes alfanuméricos (contienen letras): NO se modifican, se devuelven tal cual
 * - Números cortos (< 6 dígitos): NO se modifican, se devuelven tal cual
 * - Números de teléfono (≥ 6 dígitos, solo números): SÍ se normalizan completamente
 */
fun String.normalizePhoneNumber(): String {
    val cleaned = this.trim()
    
    // Si contiene letras, es un remitente alfanumérico (ej: "BANCO123")
    // NO modificar nada, devolver tal cual
    if (cleaned.any { it.isLetter() }) {
        return cleaned
    }
    
    // Contar solo dígitos (sin contar el +)
    val digitsOnly = cleaned.filter { it.isDigit() }
    
    // Si es un número corto (< 6 dígitos), NO modificar nada, devolver tal cual
    if (digitsOnly.length > 0 && digitsOnly.length < Constants.SHORT_CODE_MAX_DIGITS) {
        return cleaned
    }
    
    // Es un número de teléfono válido (≥ 6 dígitos, solo números), normalizar completamente
    val normalized = StringBuilder()
    
    // Mantener el + al inicio si existe
    if (cleaned.startsWith("+")) {
        normalized.append("+")
    }
    
    // Agregar solo dígitos
    cleaned.forEach { char ->
        if (char.isDigit()) {
            normalized.append(char)
        }
    }
    
    return normalized.toString()
}

/**
 * Normaliza un número de teléfono a un formato canónico para comparación.
 * Extrae los últimos 9 dígitos si el número tiene código de país español (+34 o 34).
 * Esto permite agrupar números que solo difieren en el código de país o formato.
 * 
 * Ejemplos:
 * - "+34600123456" → "600123456"
 * - "34600123456" → "600123456"
 * - "600123456" → "600123456"
 * - "600 123456" → "600123456"
 * 
 * Para números que no son españoles o tienen menos de 9 dígitos, devuelve el número normalizado.
 */
fun String.normalizeToCanonicalFormat(): String {
    val normalized = this.normalizePhoneNumber()
    
    // Si contiene letras o es un número corto, devolver tal cual
    if (normalized.any { it.isLetter() }) {
        return normalized
    }
    
    val digitsOnly = normalized.filter { it.isDigit() }
    
    // Si es un número corto, devolver tal cual
    if (digitsOnly.length < Constants.SHORT_CODE_MAX_DIGITS) {
        return normalized
    }
    
    // Código de país español: +34 o 34
    val spanishCountryCode = "34"
    
    // Si el número empieza con +34 o 34 y tiene más de 9 dígitos, extraer los últimos 9
    if (digitsOnly.length > 9) {
        // Verificar si empieza con código de país español
        if (normalized.startsWith("+$spanishCountryCode") || 
            (normalized.startsWith("+") && digitsOnly.startsWith(spanishCountryCode)) ||
            digitsOnly.startsWith(spanishCountryCode)) {
            // Extraer los últimos 9 dígitos (número móvil español)
            return digitsOnly.takeLast(9)
        }
    }
    
    // Si tiene exactamente 9 dígitos o menos, devolver solo los dígitos (sin +)
    if (digitsOnly.length <= 9) {
        return digitsOnly
    }
    
    // Para otros casos, devolver el número normalizado completo
    return normalized
}

/**
 * Extrae el dominio de una URL
 */
fun String.extractDomain(): String? {
    return try {
        val urlPattern = Pattern.compile(
            "(?i)(?:https?://)?(?:www\\.)?([a-z0-9.\\-]+\\.[a-z]{2,})",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = urlPattern.matcher(this)
        if (matcher.find()) {
            matcher.group(1)?.lowercase()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

