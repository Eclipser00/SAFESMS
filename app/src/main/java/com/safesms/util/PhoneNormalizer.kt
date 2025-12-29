package com.safesms.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.safesms.data.local.system.RegionProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resultado de la normalización de un número de teléfono.
 * 
 * @param type Tipo de resultado:
 *   - "E164": Número válido normalizado a formato E.164 (ej: "+34600123456")
 *   - "SHORT_OR_NONPHONE": Número corto (≤6 dígitos) o no es teléfono, no normalizado
 *   - "ALPHANUMERIC": Remitente alfanumérico (contiene letras), no normalizado
 *   - "INVALID_PHONE": Número inválido que no se pudo parsear
 * 
 * @param key Clave canónica para agrupar mensajes en el mismo chat:
 *   - Si type="E164": el número en formato E.164 (ej: "+34600123456")
 *   - Si type="SHORT_OR_NONPHONE": "short:<número_limpio>"
 *   - Si type="ALPHANUMERIC": "alpha:<texto_original>"
 *   - Si type="INVALID_PHONE": "raw:<número_limpio>"
 * 
 * @param original Número original sin modificar
 * @param cleaned Número limpio (sin espacios, guiones, paréntesis)
 * @param regionUsed Región utilizada para el parsing (ej: "ES", "US") o null si no aplica
 */
data class NormalizedResult(
    val type: String,
    val key: String,
    val original: String,
    val cleaned: String,
    val regionUsed: String?
)

/**
 * Normalizador canónico de números de teléfono usando Google libphonenumber.
 * 
 * Convierte números en diferentes formatos a un formato canónico E.164 para agrupar
 * SMS en el mismo chat.
 * 
 * REGLAS:
 * - Solo normaliza números con MÁS de 6 dígitos (después de limpiar)
 * - Números ≤6 dígitos: tratados como short codes, no normalizados
 * - Remitentes alfanuméricos: tratados aparte, no normalizados
 * - Números sin prefijo internacional (+): usan defaultRegion de la SIM
 * 
 * EJEMPLOS (con defaultRegion="ES"):
 * - "600 123 456" -> type="E164", key="+34600123456"
 * - "600-123-456" -> type="E164", key="+34600123456"
 * - "+34 600 123 456" -> type="E164", key="+34600123456"
 * - "34600123456" -> type="E164", key="+34600123456"
 * - "12345" -> type="SHORT_OR_NONPHONE", key="short:12345"
 * - "BBVA" -> type="ALPHANUMERIC", key="alpha:BBVA"
 */
@Singleton
class PhoneNormalizer @Inject constructor(
    private val regionProvider: RegionProvider
) {
    
    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    
    /**
     * Normaliza un número de teléfono a formato canónico.
     * 
     * @param phoneNumber Número original (puede tener espacios, guiones, paréntesis, etc.)
     * @param defaultRegion Región por defecto si el número no tiene prefijo internacional.
     *                      Si es null, se obtiene desde RegionProvider.
     * @return NormalizedResult con el tipo, clave canónica y metadatos
     */
    fun normalizePhone(
        phoneNumber: String,
        defaultRegion: String? = null
    ): NormalizedResult {
        val original = phoneNumber.trim()
        
        // Paso 1: Limpiar el número PRIMERO (eliminar TODOS los espacios, guiones, paréntesis)
        // IMPORTANTE: La limpieza debe hacerse ANTES de cualquier otra operación
        val cleaned = cleanPhoneNumber(original)
        
        // Paso 2: Verificar si es alfanumérico (después de limpiar)
        if (containsLetters(cleaned)) {
            return NormalizedResult(
                type = "ALPHANUMERIC",
                key = "alpha:$cleaned",
                original = original,
                cleaned = cleaned,
                regionUsed = null
            )
        }
        
        // Paso 3: Contar dígitos DESPUÉS de limpiar completamente (sin contar el +)
        // Los números cortos son los que DESPUÉS de normalizados (quitados signos y espacios)
        // tienen menos de 6 cifras
        val digitsOnly = cleaned.filter { it.isDigit() }
        
        // Paso 4: Si tiene 6 dígitos o menos DESPUÉS de limpiar, tratar como short code
        if (digitsOnly.length <= Constants.SHORT_CODE_MAX_DIGITS) {
            return NormalizedResult(
                type = "SHORT_OR_NONPHONE",
                key = "short:$cleaned",
                original = original,
                cleaned = cleaned,
                regionUsed = null
            )
        }
        
        // Paso 5: Intentar parsear como número de teléfono válido
        val region = defaultRegion ?: regionProvider.getDefaultRegion()
        
        return try {
            parseAndNormalize(cleaned, region, original)
        } catch (e: Exception) {
            // Si falla el parsing, devolver como inválido
            NormalizedResult(
                type = "INVALID_PHONE",
                key = "raw:$cleaned",
                original = original,
                cleaned = cleaned,
                regionUsed = region
            )
        }
    }
    
    /**
     * Limpia el número eliminando TODOS los espacios, guiones, paréntesis, etc.
     * Mantiene solo dígitos y el símbolo + al inicio.
     * IMPORTANTE: Debe eliminar TODOS los espacios antes de contar dígitos.
     */
    private fun cleanPhoneNumber(phoneNumber: String): String {
        // Eliminar TODOS los espacios, guiones, paréntesis y otros caracteres no numéricos
        // excepto el + al inicio
        val cleaned = phoneNumber
            .replace(Regex("[\\s\\-\\(\\)\\.]"), "") // Eliminar espacios, guiones, paréntesis, puntos
            .trim()
        
        return cleaned
    }
    
    /**
     * Verifica si el string contiene letras
     */
    private fun containsLetters(text: String): Boolean {
        return text.any { it.isLetter() }
    }
    
    /**
     * Parsea y normaliza el número usando libphonenumber.
     * 
     * Maneja casos especiales:
     * - Números con prefijo internacional (+): parsear con null (E.164 completo)
     * - Números sin prefijo internacional: usa defaultRegion
     * - Números que parecen internacionales pero sin '+': intenta anteponer '+'
     */
    private fun parseAndNormalize(
        cleaned: String,
        defaultRegion: String,
        original: String
    ): NormalizedResult {
        var parsedNumber: Phonenumber.PhoneNumber? = null
        
        // Intento 1: Si empieza con '+', parsear como E.164 completo (null = sin región)
        if (cleaned.startsWith("+")) {
            try {
                parsedNumber = phoneNumberUtil.parse(cleaned, null) // null = tratar como E.164 completo
            } catch (e: NumberParseException) {
                // Si falla, intentar con defaultRegion como fallback
                try {
                    parsedNumber = phoneNumberUtil.parse(cleaned, defaultRegion)
                } catch (e2: NumberParseException) {
                    // Continuar con otros intentos
                }
            }
        } else {
            // Intento 2: Parsear con defaultRegion (número nacional)
            try {
                parsedNumber = phoneNumberUtil.parse(cleaned, defaultRegion)
            } catch (e: NumberParseException) {
                // Si falla, intentar anteponer '+' si parece internacional
                val digitsOnly = cleaned.filter { it.isDigit() }
                
                // Verificar si parece un número internacional (empieza por código de país conocido)
                if (digitsOnly.length > 7) { // Números internacionales suelen tener más de 7 dígitos
                    val withPlus = "+$digitsOnly"
                    try {
                        parsedNumber = phoneNumberUtil.parse(withPlus, null) // null = tratar como E.164 completo
                    } catch (e2: NumberParseException) {
                        // Fallar silenciosamente, continuar con el intento original
                    }
                }
            }
        }
        
        // Si aún no se pudo parsear, devolver como inválido
        if (parsedNumber == null) {
            return NormalizedResult(
                type = "INVALID_PHONE",
                key = "raw:$cleaned",
                original = original,
                cleaned = cleaned,
                regionUsed = defaultRegion
            )
        }
        
        // Intentar formatear a E.164 (formato canónico internacional)
        // Si libphonenumber puede parsear y formatear, confiamos en él (como hace Android)
        // No validamos isPossibleNumber porque es demasiado estricto y rechaza números válidos
        val e164 = try {
            phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: Exception) {
            null
        }
        
        // Si el formato E.164 está vacío o no empieza con '+', significa que algo falló
        // E.164 SIEMPRE debe empezar con '+' (incluso si la entrada no tenía '+')
        if (e164.isNullOrBlank() || !e164.startsWith("+")) {
            return NormalizedResult(
                type = "INVALID_PHONE",
                key = "raw:$cleaned",
                original = original,
                cleaned = cleaned,
                regionUsed = defaultRegion
            )
        }
        
        // Si tenemos un E.164 válido, usarlo sin validar isPossibleNumber
        // Esto funciona internacionalmente como Android
        return NormalizedResult(
            type = "E164",
            key = e164,
            original = original,
            cleaned = cleaned,
            regionUsed = defaultRegion
        )
    }
}

