package com.safesms.presentation.screen.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safesms.domain.model.RiskFactor
import com.safesms.domain.repository.ChatRepository
import com.safesms.domain.repository.ConfigurationRepository
import com.safesms.domain.usecase.security.DetectRiskFactorsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para lógica de advertencia de Cuarentena.
 * 
 * ACTUALIZADO: Usa threadId en lugar de chatId.
 */
@HiltViewModel
class QuarantineWarningViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val detectRiskFactorsUseCase: DetectRiskFactorsUseCase,
    private val configurationRepository: ConfigurationRepository
) : ViewModel() {
    
    private val _riskFactors = MutableStateFlow<List<RiskFactor>>(emptyList())
    val riskFactors: StateFlow<List<RiskFactor>> = _riskFactors.asStateFlow()
    
    private val _countdownSeconds = MutableStateFlow(5)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()
    
    private val _canProceed = MutableStateFlow(false)
    val canProceed: StateFlow<Boolean> = _canProceed.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Cargar configuración de countdown
        viewModelScope.launch {
            configurationRepository.getCountdownSeconds()
                .collect { seconds ->
                    _countdownSeconds.value = seconds
                }
        }
    }
    
    /**
     * Carga factores de riesgo del chat usando threadId.
     */
    fun loadRiskFactors(threadId: Long) {
        _isLoading.value = true
        
        viewModelScope.launch {
            chatRepository.getChatByThreadIdFlow(threadId)
                .catch { exception ->
                    _isLoading.value = false
                }
                .collect { chat ->
                    chat?.let {
                        launch {
                            val factors = detectRiskFactorsUseCase(it.address)
                            _riskFactors.value = factors
                            _isLoading.value = false
                            startCountdown()
                        }
                    } ?: run {
                        _isLoading.value = false
                    }
                }
        }
    }
    
    /**
     * Inicia el countdown
     */
    fun startCountdown() {
        _canProceed.value = false
        
        viewModelScope.launch {
            delay(_countdownSeconds.value * 1000L)
            _canProceed.value = true
        }
    }
}

