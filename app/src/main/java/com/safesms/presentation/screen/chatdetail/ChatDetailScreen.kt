package com.safesms.presentation.screen.chatdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.BackHandler
import com.safesms.presentation.ui.components.MessageBubble
import com.safesms.presentation.ui.theme.Background
import com.safesms.presentation.ui.theme.HeaderDark
import com.safesms.presentation.ui.theme.HeaderDarkEnd
import com.safesms.presentation.ui.theme.InboxGreen
import com.safesms.presentation.ui.theme.Surface as SurfaceColor
import com.safesms.presentation.ui.theme.SurfaceStroke
import com.safesms.presentation.ui.theme.BlockedBackground
import com.safesms.presentation.ui.theme.BlockedSurface
import com.safesms.presentation.util.PhoneNumberFormatter
import com.safesms.util.AddressDisplayHelper
import androidx.compose.ui.res.stringResource
import com.safesms.R

/**
 * Pantalla de conversacion individual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    threadId: Long,
    onNavigateBack: () -> Unit,
    onLinkClicked: (String) -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val chat by viewModel.chat.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val messageInput by viewModel.messageInput.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val selectedMessageIds by viewModel.selectedMessages.collectAsStateWithLifecycle()
    val isSelectionMode = selectedMessageIds.isNotEmpty()

    if (isSelectionMode) {
        BackHandler { viewModel.clearSelection() }
    }

    val cleanAddress = chat?.address?.let { AddressDisplayHelper.cleanAddressForDisplay(it) } ?: ""
    val displayName = chat?.contactName ?: PhoneNumberFormatter.formatPhoneNumber(cleanAddress)
    val isBlockedChat = chat?.isBlocked == true

    Scaffold(
        containerColor = Background,
        topBar = {
            if (isSelectionMode) {
                MessageSelectionTopBar(
                    count = selectedMessageIds.size,
                    onClose = { viewModel.clearSelection() },
                    onDelete = { viewModel.deleteSelectedMessages() }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(HeaderDark, HeaderDarkEnd)))
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = displayName,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        var showMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                offset = DpOffset(x = 12.dp, y = 0.dp)
                            ) {
                                chat?.let { currentChat ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (currentChat.isBlocked) {
                                                    stringResource(R.string.chat_detail_unblock)
                                                } else {
                                                    stringResource(R.string.chat_detail_block)
                                                }
                                            )
                                        },
                                        onClick = {
                                            if (currentChat.isBlocked) {
                                                viewModel.unblockSender()
                                            } else {
                                                viewModel.blockSender()
                                            }
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isBlockedChat) BlockedBackground else Color(0xFFF6F7FA))
                .padding(paddingValues)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                color = if (isBlockedChat) BlockedSurface else SurfaceColor,
                shadowElevation = 6.dp,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, SurfaceStroke)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = InboxGreen)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true,
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(
                            items = messages.reversed(),
                            key = { it.id }
                        ) { message ->
                            MessageBubble(
                                message = message,
                                onLinkClicked = onLinkClicked,
                                isSelected = selectedMessageIds.contains(message.id),
                                selectionMode = isSelectionMode,
                                onClick = { viewModel.toggleMessageSelection(message.id) },
                                onLongPress = { viewModel.toggleMessageSelection(message.id) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 10.dp,
                tonalElevation = 2.dp,
                color = if (isBlockedChat) BlockedSurface else SurfaceColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { viewModel.updateMessageInput(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribir mensaje...") },
                        enabled = !isSending,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            unfocusedBorderColor = SurfaceStroke,
                            focusedBorderColor = InboxGreen,
                            cursorColor = InboxGreen
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = InboxGreen,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(width = 56.dp, height = 48.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.sendMessage() },
                            enabled = messageInput.isNotBlank() && !isSending,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Enviar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            error?.let {
                Snackbar(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(it)
                }
            }
        }
    }
}

@Composable
private fun MessageSelectionTopBar(
    count: Int,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(HeaderDark, HeaderDarkEnd)))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Salir de selección",
                    tint = Color.White
                )
            }

            Text(
                text = count.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar mensajes seleccionados",
                    tint = Color.White
                )
            }
        }
    }
}
