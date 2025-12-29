package com.safesms.domain.usecase.contact

import com.safesms.domain.model.Contact
import com.safesms.domain.repository.ContactRepository
import javax.inject.Inject

/**
 * Obtiene contacto por teléfono
 */
class GetContactByPhoneUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(phoneNumber: String): Contact? {
        return contactRepository.getContactByPhone(phoneNumber)
    }
}
