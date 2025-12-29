package com.safesms.domain.usecase.security

import com.safesms.util.isAlphanumeric
import javax.inject.Inject

/**
 * Verifica si remitente es alfanumérico
 */
class IsAlphanumericSenderUseCase @Inject constructor() {
    operator fun invoke(address: String): Boolean {
        return address.isAlphanumeric()
    }
}
