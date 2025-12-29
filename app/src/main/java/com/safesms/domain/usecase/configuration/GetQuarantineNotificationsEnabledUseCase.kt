package com.safesms.domain.usecase.configuration

import com.safesms.domain.repository.ConfigurationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene estado de notificaciones de Cuarentena
 */
class GetQuarantineNotificationsEnabledUseCase @Inject constructor(
    private val configurationRepository: ConfigurationRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return configurationRepository.getQuarantineNotificationsEnabled()
    }
}
