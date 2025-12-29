package com.safesms.presentation.navigation

import androidx.lifecycle.ViewModel
import com.safesms.domain.repository.ConfigurationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ViewModel helper para obtener el estado de onboarding en la navegación
 */
@HiltViewModel
class NavigationViewModel @Inject constructor(
    configurationRepository: ConfigurationRepository
) : ViewModel() {
    val onboardingCompleted: Flow<Boolean> = configurationRepository.getOnboardingCompleted()
}

