package com.safesms.domain.usecase.configuration

import com.safesms.domain.repository.ConfigurationRepository
import com.safesms.util.Result
import javax.inject.Inject

/**
 * Marca el histórico de SMS como importado
 */
class SetSmsHistoryImportedUseCase @Inject constructor(
    private val configurationRepository: ConfigurationRepository
) {
    suspend operator fun invoke(imported: Boolean): Result<Unit> {
        return configurationRepository.setSmsHistoryImported(imported)
    }
}

