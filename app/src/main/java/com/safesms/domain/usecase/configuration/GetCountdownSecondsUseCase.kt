package com.safesms.domain.usecase.configuration

import com.safesms.domain.repository.ConfigurationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene segundos configurados para cuenta atrás
 */
class GetCountdownSecondsUseCase @Inject constructor(
    private val configurationRepository: ConfigurationRepository
) {
    operator fun invoke(): Flow<Int> {
        return configurationRepository.getCountdownSeconds()
    }
}
