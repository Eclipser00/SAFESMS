package com.safesms.presentation.screen.chatdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safesms.domain.model.Chat
import com.safesms.domain.model.Message
import com.safesms.domain.repository.BlockedSenderRepository
import com.safesms.domain.repository.ChatRepository
import com.safesms.domain.usecase.blocking.BlockSenderUseCase
import com.safesms.domain.usecase.blocking.UnblockSenderUseCase
import com.safesms.domain.usecase.chat.GetChatMessagesUseCase
import com.safesms.domain.usecase.message.MarkMessageAsReadUseCase
import com.safesms.domain.usecase.message.SendMessageUseCase
import com.safesms.domain.usecase.security.DetectLinksInMessageUseCase
import com.safesms.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para lógica de detalle de chat.
 * 
 * ACTUALIZADO: Usa threadId en lugar de chatId.
 */
@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase,
    private val detectLinksInMessageUseCase: DetectLinksInMessageUseCase,
    private val chatRepository: ChatRepository,
    private val blockSenderUseCase: BlockSenderUseCase,
    private val unblockSenderUseCase: UnblockSenderUseCase
) : ViewModel() {
    
    // Obtener threadId de los argumentos de navegación
    private val threadId: Long = savedStateHandle.get<Long>("threadId")
        ?: throw IllegalArgumentException("threadId is required")
    
    private val _chat = MutableStateFlow<Chat?>(null)
    val chat: StateFlow<Chat?> = _chat.asStateFlow()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadMessages()
    }
    
    /**
     * Carga mensajes del chat usando threadId.
     */
    private fun loadMessages() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            // Cargar chat y mensajes en paralelo
            launch {
                chatRepository.getChatByThreadIdFlow(threadId)
                    .catch { exception ->
                        _error.value = exception.message
                    }
                    .collect { chat ->
                        _chat.value = chat
                    }
            }
            
            launch {
                getChatMessagesUseCase(threadId)
                    .catch { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                    .collect { messagesList ->
                        _messages.value = messagesList
                        _isLoading.value = false
                        
                        // Marcar chat como leído
                        if (messagesList.any { !it.isRead && it.isReceived }) {
                            chatRepository.markChatAsRead(threadId)
                            
                            // Marcar mensajes individuales como leídos
                            messagesList.filter { !it.isRead && it.isReceived }
                                .forEach { message ->
                                    markMessageAsReadUseCase(message.id)
                                }
                        }
                    }
            }
        }
    }
    
    /**
     * Actualiza el texto del input.
     */
    fun updateMessageInput(text: String) {
        _messageInput.value = text
    }
    
    /**
     * Envía un mensaje.
     */
    fun sendMessage() {
        val text = _messageInput.value.trim()
        val currentChat = _chat.value
        
        if (text.isEmpty() || currentChat == null) return
        
        viewModelScope.launch {
            _isSending.value = true
            _error.value = null
            
            when (val result = sendMessageUseCase(threadId, currentChat.address, text)) {
                is Result.Success -> {
                    _messageInput.value = ""
                    _isSending.value = false
                }
                is Result.Error -> {
                    _error.value = result.exception.message
                    _isSending.value = false
                }
            }
        }
    }
    
    /**
     * Maneja click en enlace.
     */
    fun onLinkClicked(url: String) {
        // La detección de enlaces se hace en el componente MessageBubble
        // Este método puede ser usado para logging o analytics
    }
    
    /**
     * Bloquea el remitente del chat.
     */
    fun blockSender() {
        val currentChat = _chat.value ?: return
        
        viewModelScope.launch {
            when (blockSenderUseCase(threadId, currentChat.address)) {
                is Result.Success -> {
                    // El chat se recargará automáticamente por el Flow
                }
                is Result.Error -> {
                    _error.value = "Error al bloquear remitente"
                }
            }
        }
    }
    
    /**
     * Desbloquea el remitente del chat.
     */
    fun unblockSender() {
        viewModelScope.launch {
            when (unblockSenderUseCase(threadId)) {
                is Result.Success -> {
                    // El chat se recargará automáticamente por el Flow
                }
                is Result.Error -> {
                    _error.value = "Error al desbloquear remitente"
                }
            }
        }
    }
}

