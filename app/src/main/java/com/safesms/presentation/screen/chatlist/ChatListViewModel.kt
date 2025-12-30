package com.safesms.presentation.screen.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safesms.domain.model.Chat
import com.safesms.domain.model.ChatType
import com.safesms.domain.usecase.chat.DeleteChatUseCase
import com.safesms.domain.usecase.chat.GetInboxChatsUseCase
import com.safesms.domain.usecase.chat.GetQuarantineChatsUseCase
import com.safesms.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para lista de chats
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getInboxChatsUseCase: GetInboxChatsUseCase,
    private val getQuarantineChatsUseCase: GetQuarantineChatsUseCase,
    private val deleteChatUseCase: DeleteChatUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(ChatType.INBOX)
    val selectedTab: StateFlow<ChatType> = _selectedTab.asStateFlow()

    private val _inboxChats = MutableStateFlow<List<Chat>>(emptyList())
    private val _quarantineChats = MutableStateFlow<List<Chat>>(emptyList())
    private val _selectedChats = MutableStateFlow<Set<Long>>(emptySet())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val combinedState: StateFlow<ChatListState> = combine(
        _selectedTab,
        _inboxChats,
        _quarantineChats,
        _selectedChats,
        _isLoading
    ) { tab, inbox, quarantine, selected, loading ->
        ChatListState(
            inboxChats = inbox,
            quarantineChats = quarantine,
            selectedTab = tab,
            selectedChatIds = selected,
            isLoading = loading,
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatListState()
    )

    val uiState: StateFlow<ChatListState> = combine(
        combinedState,
        _error
    ) { state, error ->
        state.copy(error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatListState()
    )

    init {
        loadChats()
    }

    fun switchTab(tab: ChatType) {
        _selectedTab.value = tab
        clearSelection()
        _error.value = null
    }

    fun toggleChatSelection(threadId: Long) {
        _selectedChats.update { current ->
            if (current.contains(threadId)) current - threadId else current + threadId
        }
    }

    fun clearSelection() {
        _selectedChats.value = emptySet()
    }

    fun deleteSelectedChats() {
        val chatsToDelete = _selectedChats.value
        if (chatsToDelete.isEmpty()) return

        viewModelScope.launch {
            _error.value = null

            chatsToDelete.forEach { threadId ->
                when (val result = deleteChatUseCase(threadId)) {
                    is Result.Error -> {
                        _error.value = result.exception.message ?: "Error al eliminar chat"
                        return@launch
                    }
                    is Result.Success -> Unit
                }
            }

            clearSelection()
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Cargar chats de Inbox
            launch {
                getInboxChatsUseCase()
                    .catch { exception ->
                        _error.value = "Error al cargar bandeja: ${exception.message}"
                        if (_selectedTab.value == ChatType.INBOX) {
                            _isLoading.value = false
                        }
                    }
                    .collect { chats ->
                        _inboxChats.value = chats
                        if (_selectedTab.value == ChatType.INBOX) {
                            _isLoading.value = false
                        }
                    }
            }

            // Cargar chats de Cuarentena
            launch {
                getQuarantineChatsUseCase()
                    .catch { exception ->
                        _error.value = "Error al cargar cuarentena: ${exception.message}"
                        if (_selectedTab.value == ChatType.QUARANTINE) {
                            _isLoading.value = false
                        }
                    }
                    .collect { chats ->
                        _quarantineChats.value = chats
                        if (_selectedTab.value == ChatType.QUARANTINE) {
                            _isLoading.value = false
                        }
                    }
            }
        }
    }
}
