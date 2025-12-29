package com.safesms.domain.usecase.chat

import com.safesms.domain.model.ChatType
import com.safesms.domain.repository.ContactRepository
import com.safesms.util.PhoneNormalizer
import javax.inject.Inject

/**
 * Clasifica un chat como Inbox o Cuarentena.
 * 
 * Usa PhoneNormalizer para obtener la clave canónica E.164 y buscar contactos.
 * Solo busca contactos si el número se normaliza a E.164 (no para short codes, alfanuméricos, etc.)
 */
class ClassifyChatUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val phoneNormalizer: PhoneNormalizer
) {
    suspend operator fun invoke(address: String): ChatType {
        // Normalizar usando PhoneNormalizer para obtener la clave canónica
        val normalizedResult = phoneNormalizer.normalizePhone(address)
        
        // Solo buscar en contactos si el número es E.164 (número de teléfono válido)
        // Los short codes, alfanuméricos e inválidos van directamente a Cuarentena
        if (normalizedResult.type == "E164") {
            // Usar la clave canónica E.164 para buscar el contacto
            val isContact = contactRepository.isContactSaved(normalizedResult.key)
            return if (isContact) ChatType.INBOX else ChatType.QUARANTINE
        }
        
        // Si no es E.164, va a Cuarentena
        return ChatType.QUARANTINE
    }
}
