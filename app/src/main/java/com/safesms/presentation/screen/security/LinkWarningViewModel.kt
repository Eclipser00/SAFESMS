package com.safesms.presentation.screen.security

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safesms.domain.repository.ConfigurationRepository
import com.safesms.domain.usecase.security.LinkSafetyInfo
import com.safesms.domain.usecase.security.ValidateLinkSafetyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para lógica de advertencia de enlace
 */
@HiltViewModel
class LinkWarningViewModel @Inject constructor(
    private val validateLinkSafetyUseCase: ValidateLinkSafetyUseCase,
    private val configurationRepository: ConfigurationRepository
) : ViewModel() {
    
    private val _linkUrl = MutableStateFlow<String?>(null)
    val linkUrl: StateFlow<String?> = _linkUrl.asStateFlow()
    
    private val _linkSafety = MutableStateFlow<com.safesms.domain.usecase.security.LinkSafetyInfo?>(null)
    val linkSafety: StateFlow<com.safesms.domain.usecase.security.LinkSafetyInfo?> = _linkSafety.asStateFlow()
    
    private val _countdownSeconds = MutableStateFlow(5)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()
    
    private val _canProceed = MutableStateFlow(false)
    val canProceed: StateFlow<Boolean> = _canProceed.asStateFlow()
    
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
     * Valida el enlace y extrae información de seguridad
     */
    fun validateLink(url: String) {
        _linkUrl.value = url
        
        viewModelScope.launch {
            val safetyInfo = validateLinkSafetyUseCase(url)
            _linkSafety.value = safetyInfo
            startCountdown()
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
    
    /**
     * Crea Intent para abrir el enlace en el navegador
     */
    fun createOpenLinkIntent(): Intent? {
        val url = _linkUrl.value ?: return null
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }
}

