package com.safesms.presentation.screen.chatlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.BackHandler
import com.safesms.domain.model.ChatType
import com.safesms.presentation.ui.components.AdBanner
import com.safesms.presentation.ui.components.ChatListItem
import com.safesms.presentation.ui.components.TabSelector
import com.safesms.presentation.ui.theme.Background
import com.safesms.presentation.ui.theme.DangerBackgroundBottom
import com.safesms.presentation.ui.theme.DangerBackgroundTop
import com.safesms.presentation.ui.theme.HeaderDark
import com.safesms.presentation.ui.theme.HeaderDarkEnd
import com.safesms.presentation.ui.theme.MutedText
import com.safesms.presentation.ui.theme.Surface as SurfaceColor
import com.safesms.presentation.ui.theme.SurfaceStroke

/**
 * Pantalla principal con lista de chats (Inbox/Cuarentena)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (Long, ChatType) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isQuarantine = uiState.selectedTab == ChatType.QUARANTINE
    val isSelectionMode = uiState.isSelectionMode
    val backgroundBrush = if (isQuarantine) {
        Brush.verticalGradient(listOf(DangerBackgroundTop, DangerBackgroundBottom))
    } else {
        Brush.verticalGradient(listOf(Background, Background.copy(alpha = 0.85f)))
    }

    if (isSelectionMode) {
        BackHandler { viewModel.clearSelection() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            if (isSelectionMode) {
                SelectionTopBar(
                    count = uiState.selectionCount,
                    onClose = { viewModel.clearSelection() },
                    onDelete = { viewModel.deleteSelectedChats() }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(HeaderDark, HeaderDarkEnd)))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Text(
                            text = "SAFE SMS",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth())
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isQuarantine) {
                Image(
                    painter = painterResource(id = com.safesms.R.drawable.fondo),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xAA52060A))
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(backgroundBrush)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TabSelector(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.switchTab(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (isQuarantine) Color.Transparent else SurfaceColor,
                    shadowElevation = if (isQuarantine) 0.dp else 10.dp,
                    tonalElevation = if (isQuarantine) 0.dp else 2.dp,
                    shape = RoundedCornerShape(18.dp),
                    border = if (isQuarantine) null else BorderStroke(1.dp, SurfaceStroke)
                ) {
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = if (isQuarantine) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        uiState.chats.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (uiState.selectedTab == ChatType.INBOX) {
                                        "No hay mensajes en la bandeja"
                                    } else {
                                        "No hay mensajes en cuarentena"
                                    },
                                    color = if (isQuarantine) Color.White else MutedText
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = uiState.chats,
                                    key = { it.threadId }
                                ) { chat ->
                                    ChatListItem(
                                        chat = chat,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleChatSelection(chat.threadId)
                                            } else {
                                                onChatClick(chat.threadId, chat.chatType)
                                            }
                                        },
                                        onLongPress = { viewModel.toggleChatSelection(chat.threadId) },
                                        isSelected = uiState.selectedChatIds.contains(chat.threadId),
                                        selectionMode = isSelectionMode
                                    )
                                }
                            }
                        }
                    }
                }

                if (isQuarantine) {
                    Text(
                        text = "PELIGRO",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(DangerBackgroundTop, DangerBackgroundBottom)))
                            .padding(vertical = 10.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun SelectionTopBar(
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
                    contentDescription = "Eliminar chats seleccionados",
                    tint = Color.White
                )
            }
        }
    }
}
