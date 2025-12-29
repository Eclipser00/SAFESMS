package com.safesms.domain.usecase.contact

import com.safesms.domain.repository.ContactRepository
import com.safesms.util.normalizePhoneNumber
import javax.inject.Inject

/**
 * Verifica si un número está en contactos
 */
class IsContactSavedUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(phoneNumber: String): Boolean {
        val normalized = phoneNumber.normalizePhoneNumber()
        return contactRepository.isContactSaved(normalized)
    }
}
