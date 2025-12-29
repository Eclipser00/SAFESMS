package com.safesms.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safesms.domain.repository.ConfigurationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para lógica de onboarding
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository
) : ViewModel() {
    
    private val _videoEnded = MutableStateFlow(false)
    val videoEnded: StateFlow<Boolean> = _videoEnded.asStateFlow()
    
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()
    
    /**
     * Marca el vídeo como completado
     */
    fun onVideoEnded() {
        _videoEnded.value = true
        _state.value = _state.value.copy(videoCompleted = true)
    }
    
    /**
     * Marca el onboarding como completado y navega al siguiente paso
     */
    fun markOnboardingCompleted() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isNavigatingNext = true)
            configurationRepository.setOnboardingCompleted(true)
        }
    }
}

