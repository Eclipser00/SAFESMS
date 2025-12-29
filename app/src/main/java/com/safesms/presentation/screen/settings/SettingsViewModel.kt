package com.safesms.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safesms.domain.model.BlockedSender
import com.safesms.domain.repository.BlockedSenderRepository
import com.safesms.domain.repository.ConfigurationRepository
import com.safesms.domain.usecase.blocking.GetBlockedSendersUseCase
import com.safesms.domain.usecase.blocking.UnblockSenderUseCase
import com.safesms.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para lógica de ajustes
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository,
    private val getBlockedSendersUseCase: GetBlockedSendersUseCase,
    private val unblockSenderUseCase: UnblockSenderUseCase
) : ViewModel() {
    
    private val _countdownSeconds = MutableStateFlow(5)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()
    
    private val _quarantineNotificationsEnabled = MutableStateFlow(true)
    val quarantineNotificationsEnabled: StateFlow<Boolean> = _quarantineNotificationsEnabled.asStateFlow()
    
    private val _blockedSenders = MutableStateFlow<List<BlockedSender>>(emptyList())
    val blockedSenders: StateFlow<List<BlockedSender>> = _blockedSenders.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * Carga configuración actual
     */
    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Cargar countdown seconds
            configurationRepository.getCountdownSeconds()
                .collect { seconds ->
                    _countdownSeconds.value = seconds
                }
            
            // Cargar notificaciones de cuarentena
            configurationRepository.getQuarantineNotificationsEnabled()
                .collect { enabled ->
                    _quarantineNotificationsEnabled.value = enabled
                }
            
            // Cargar remitentes bloqueados
            getBlockedSendersUseCase()
                .collect { senders ->
                    _blockedSenders.value = senders
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Actualiza segundos de countdown
     */
    fun updateCountdownSeconds(seconds: Int) {
        viewModelScope.launch {
            configurationRepository.setCountdownSeconds(seconds)
                .fold(
                    onSuccess = {
                        _countdownSeconds.value = seconds
                    },
                    onError = { }
                )
        }
    }
    
    /**
     * Cambia estado de notificaciones de Cuarentena
     */
    fun toggleQuarantineNotifications(enabled: Boolean) {
        viewModelScope.launch {
            configurationRepository.setQuarantineNotificationsEnabled(enabled)
                .fold(
                    onSuccess = {
                        _quarantineNotificationsEnabled.value = enabled
                    },
                    onError = { }
                )
        }
    }
    
    /**
     * Desbloquea un remitente desde ajustes.
     * 
     * ACTUALIZADO: Usa threadId en lugar de address.
     */
    fun unblockSender(threadId: Long) {
        viewModelScope.launch {
            unblockSenderUseCase(threadId)
                .fold(
                    onSuccess = {
                        // La lista se actualizará automáticamente por el Flow
                    },
                    onError = { }
                )
        }
    }
}

