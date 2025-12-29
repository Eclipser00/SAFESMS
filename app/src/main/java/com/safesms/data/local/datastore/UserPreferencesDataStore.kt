package com.safesms.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.safesms.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Persistencia de preferencias de usuario con DataStore
 */
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val dataStore: DataStore<Preferences> = context.dataStore
    
    companion object {
        private val COUNTDOWN_SECONDS_KEY = intPreferencesKey("countdown_seconds")
        private val QUARANTINE_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("quarantine_notifications_enabled")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val SMS_HISTORY_IMPORTED_KEY = booleanPreferencesKey("sms_history_imported")
    }
    
    /**
     * Obtiene los segundos configurados para cuenta atrás (3-8)
     */
    fun getCountdownSeconds(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[COUNTDOWN_SECONDS_KEY] ?: Constants.DEFAULT_COUNTDOWN_SECONDS
    }
    
    /**
     * Guarda los segundos de cuenta atrás
     */
    suspend fun setCountdownSeconds(seconds: Int) {
        val clampedSeconds = seconds.coerceIn(
            Constants.MIN_COUNTDOWN_SECONDS,
            Constants.MAX_COUNTDOWN_SECONDS
        )
        dataStore.edit { preferences ->
            preferences[COUNTDOWN_SECONDS_KEY] = clampedSeconds
        }
    }
    
    /**
     * Obtiene si las notificaciones de Cuarentena están habilitadas
     */
    fun getQuarantineNotificationsEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[QUARANTINE_NOTIFICATIONS_ENABLED_KEY] ?: true
    }
    
    /**
     * Guarda el estado de notificaciones de Cuarentena
     */
    suspend fun setQuarantineNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[QUARANTINE_NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Obtiene si el onboarding está completado
     */
    fun getOnboardingCompleted(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }
    
    /**
     * Guarda el estado de onboarding completado
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }
    
    /**
     * Obtiene si el histórico de SMS ha sido importado
     */
    fun getSmsHistoryImported(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SMS_HISTORY_IMPORTED_KEY] ?: false
    }
    
    /**
     * Guarda el estado de importación de histórico
     */
    suspend fun setSmsHistoryImported(imported: Boolean) {
        dataStore.edit { preferences ->
            preferences[SMS_HISTORY_IMPORTED_KEY] = imported
        }
    }
}
