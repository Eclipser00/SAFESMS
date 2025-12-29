package com.safesms.util

/**
 * Helper para mostrar direcciones (números de teléfono, remitentes) en la UI.
 * 
 * Elimina los prefijos técnicos (short:, alpha:, raw:) que se usan internamente
 * para agrupar chats pero no deben mostrarse al usuario.
 */
object AddressDisplayHelper {
    
    /**
     * Limpia una dirección para mostrarla en la UI.
     * 
     * Elimina prefijos técnicos:
     * - "short:12345" -> "12345"
     * - "alpha:BBVA" -> "BBVA"
     * - "raw:+34600..." -> "+34600..."
     * - "+34600123456" -> "+34600123456" (sin cambios)
     * 
     * @param address Dirección interna (puede tener prefijos)
     * @return Dirección limpia para mostrar al usuario
     */
    fun cleanAddressForDisplay(address: String): String {
        return when {
            address.startsWith("short:") -> address.removePrefix("short:")
            address.startsWith("alpha:") -> address.removePrefix("alpha:")
            address.startsWith("raw:") -> address.removePrefix("raw:")
            else -> address
        }
    }
}

