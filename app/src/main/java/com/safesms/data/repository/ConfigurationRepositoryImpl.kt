package com.safesms.data.repository

import com.safesms.data.local.datastore.UserPreferencesDataStore
import com.safesms.domain.repository.ConfigurationRepository
import com.safesms.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementa ConfigurationRepository
 */
class ConfigurationRepositoryImpl @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ConfigurationRepository {
    
    override fun getCountdownSeconds(): Flow<Int> {
        return userPreferencesDataStore.getCountdownSeconds()
    }
    
    override suspend fun setCountdownSeconds(seconds: Int): Result<Unit> {
        return try {
            userPreferencesDataStore.setCountdownSeconds(seconds)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun getQuarantineNotificationsEnabled(): Flow<Boolean> {
        return userPreferencesDataStore.getQuarantineNotificationsEnabled()
    }
    
    override suspend fun setQuarantineNotificationsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            userPreferencesDataStore.setQuarantineNotificationsEnabled(enabled)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun getOnboardingCompleted(): Flow<Boolean> {
        return userPreferencesDataStore.getOnboardingCompleted()
    }
    
    override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> {
        return try {
            userPreferencesDataStore.setOnboardingCompleted(completed)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun getSmsHistoryImported(): Flow<Boolean> {
        return userPreferencesDataStore.getSmsHistoryImported()
    }
    
    override suspend fun setSmsHistoryImported(imported: Boolean): Result<Unit> {
        return try {
            userPreferencesDataStore.setSmsHistoryImported(imported)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
