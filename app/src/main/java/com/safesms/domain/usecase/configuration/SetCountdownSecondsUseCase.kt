package com.safesms.domain.usecase.configuration

import com.safesms.domain.repository.ConfigurationRepository
import com.safesms.util.Constants
import com.safesms.util.Result
import javax.inject.Inject

/**
 * Configura segundos de cuenta atrás (3-8)
 */
class SetCountdownSecondsUseCase @Inject constructor(
    private val configurationRepository: ConfigurationRepository
) {
    suspend operator fun invoke(seconds: Int): Result<Unit> {
        val validSeconds = seconds.coerceIn(
            Constants.MIN_COUNTDOWN_SECONDS,
            Constants.MAX_COUNTDOWN_SECONDS
        )
        return configurationRepository.setCountdownSeconds(validSeconds)
    }
}
