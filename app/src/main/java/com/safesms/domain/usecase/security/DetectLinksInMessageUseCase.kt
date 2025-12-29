package com.safesms.domain.usecase.security

import com.safesms.util.extractUrls
import javax.inject.Inject

/**
 * Detecta presencia de URLs en texto
 */
class DetectLinksInMessageUseCase @Inject constructor() {
    operator fun invoke(messageBody: String): List<String> {
        return messageBody.extractUrls()
    }
}
