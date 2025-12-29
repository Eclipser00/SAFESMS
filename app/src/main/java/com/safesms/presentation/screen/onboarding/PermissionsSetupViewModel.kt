package com.safesms.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safesms.domain.usecase.contact.SyncContactsUseCase
import com.safesms.domain.usecase.smsimport.ClassifyImportedMessagesUseCase
import com.safesms.domain.usecase.smsimport.ImportSmsHistoryUseCase
import com.safesms.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de configuración de permisos
 * Gestiona la importación inicial del histórico de SMS
 */
@HiltViewModel
class PermissionsSetupViewModel @Inject constructor(
    private val syncContactsUseCase: SyncContactsUseCase,
    private val importSmsHistoryUseCase: ImportSmsHistoryUseCase,
    private val classifyImportedMessagesUseCase: ClassifyImportedMessagesUseCase
) : ViewModel() {

    private val _importProgress = MutableStateFlow("")
    val importProgress: StateFlow<String> = _importProgress.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    /**
     * Inicia el proceso de importación de histórico de SMS
     * 1. Sincroniza contactos
     * 2. Importa SMS del sistema
     * 3. Clasifica mensajes
     */
    fun startImport() {
        viewModelScope.launch {
            _isImporting.value = true
            _importError.value = null
            
            try {
                // Paso 1: Sincronizar contactos
                _importProgress.value = "Sincronizando contactos..."
                when (val contactsResult = syncContactsUseCase()) {
                    is Result.Success -> {
                        _importProgress.value = "Contactos sincronizados"
                    }
                    is Result.Error -> {
                        _importError.value = "Error al sincronizar contactos: ${contactsResult.exception.message}"
                        _isImporting.value = false
                        return@launch
                    }
                }
                
                // Paso 2: Importar histórico de SMS
                _importProgress.value = "Importando mensajes..."
                when (val importResult = importSmsHistoryUseCase()) {
                    is Result.Success -> {
                        val count = importResult.data
                        _importProgress.value = "Importados $count mensajes"
                    }
                    is Result.Error -> {
                        _importError.value = "Error al importar SMS: ${importResult.exception.message}"
                        _isImporting.value = false
                        return@launch
                    }
                }
                
                // Paso 3: Clasificar mensajes importados
                _importProgress.value = "Clasificando mensajes..."
                when (val classifyResult = classifyImportedMessagesUseCase()) {
                    is Result.Success -> {
                        _importProgress.value = "¡Importación completada!"
                    }
                    is Result.Error -> {
                        _importError.value = "Error al clasificar mensajes: ${classifyResult.exception.message}"
                        _isImporting.value = false
                        return@launch
                    }
                }
                
                _isImporting.value = false
            } catch (e: Exception) {
                _importError.value = "Error inesperado: ${e.message}"
                _isImporting.value = false
            }
        }
    }
}

