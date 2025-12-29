package com.safesms.presentation.screen.onboarding

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Pantalla de onboarding con vídeo explicativo obligatorio
 */
@Composable
fun OnboardingScreen(
    onNavigateToPermissions: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val videoEnded by viewModel.videoEnded.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // TODO: Reemplazar con URI real del vídeo de onboarding
            val videoUri = Uri.parse("android.resource://${context.packageName}/raw/onboarding_video")
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        viewModel.onVideoEnded()
                    }
                }
            })
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    // Box para superponer el botón sobre el video
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Reproductor de vídeo a pantalla completa
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        )
        
        // Botón de continuar superpuesto
        Button(
            onClick = {
                viewModel.markOnboardingCompleted()
                onNavigateToPermissions()
            },
            enabled = videoEnded,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .height(56.dp)
                .zIndex(1f)
        ) {
            Text("Continuar")
        }
    }
}

