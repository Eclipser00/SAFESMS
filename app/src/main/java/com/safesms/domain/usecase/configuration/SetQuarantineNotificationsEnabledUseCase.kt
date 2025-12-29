package com.safesms.domain.usecase.configuration

import com.safesms.domain.repository.ConfigurationRepository
import com.safesms.util.Result
import javax.inject.Inject

/**
 * Habilita/deshabilita notificaciones de Cuarentena
 */
class SetQuarantineNotificationsEnabledUseCase @Inject constructor(
    private val configurationRepository: ConfigurationRepository
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> {
        return configurationRepository.setQuarantineNotificationsEnabled(enabled)
    }
}
