package com.safesms.domain.repository

import com.safesms.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para configuración de usuario
 */
interface ConfigurationRepository {
    fun getCountdownSeconds(): Flow<Int>
    suspend fun setCountdownSeconds(seconds: Int): Result<Unit>
    fun getQuarantineNotificationsEnabled(): Flow<Boolean>
    suspend fun setQuarantineNotificationsEnabled(enabled: Boolean): Result<Unit>
    fun getOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit>
    fun getSmsHistoryImported(): Flow<Boolean>
    suspend fun setSmsHistoryImported(imported: Boolean): Result<Unit>
}
