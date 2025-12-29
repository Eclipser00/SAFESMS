package com.safesms.domain.usecase.contact

import com.safesms.domain.repository.ContactRepository
import com.safesms.util.Result
import javax.inject.Inject

/**
 * Sincroniza contactos del sistema con BD local
 */
class SyncContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return contactRepository.syncContacts()
    }
}
