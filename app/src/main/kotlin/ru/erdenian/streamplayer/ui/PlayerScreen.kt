package ru.erdenian.streamplayer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import ru.erdenian.streamplayer.player.PlayerViewModel

/**
 * Корневой экран приложения.
 *
 * Отображает полноэкранный плеер, обрабатывает нажатие по экрану для
 * показа оверлея управления и открывает диалог настроек.
 *
 * @param viewModel ViewModel плеера.
 */
@Composable
internal fun PlayerScreen(
    viewModel: PlayerViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val url by viewModel.streamUrl.collectAsState()

    var showControls by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // Автоскрытие оверлея через 5 секунд. Пока открыт диалог настроек — не скрываем.
    LaunchedEffect(showControls, showSettings) {
        if (showControls && !showSettings) {
            delay(5_000L)
            showControls = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PlayerContent(
            player = viewModel.player,
            state = state,
            showControls = showControls,
            onScreenClick = { showControls = !showControls },
            onRestartClick = viewModel::restartPlayback,
            onSettingsClick = { showSettings = true },
        )
    }

    if (showSettings) {
        SettingsDialog(
            initialUrl = url,
            onDismiss = { showSettings = false },
            onSave = { newUrl ->
                viewModel.setStreamUrl(newUrl)
                showSettings = false
            },
        )
    }
}
