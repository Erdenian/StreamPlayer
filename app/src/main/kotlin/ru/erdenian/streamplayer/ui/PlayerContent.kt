package ru.erdenian.streamplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import ru.erdenian.streamplayer.player.PlayerState
import android.graphics.Color as AndroidColor

/**
 * UI-контент экрана с плеером.
 *
 * @param player экземпляр [Player] для привязки к [PlayerView].
 * @param state текущее состояние воспроизведения.
 * @param showControls показывать ли оверлей управления.
 * @param onScreenClick обработчик нажатия по экрану (кроме элементов оверлея).
 * @param onRestartClick обработчик кнопки «Перезапустить».
 * @param onSettingsClick обработчик кнопки «Настройки».
 */
@Composable
internal fun PlayerContent(
    player: Player,
    state: PlayerState,
    showControls: Boolean,
    onScreenClick: () -> Unit,
    onRestartClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onScreenClick,
            ),
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    setShutterBackgroundColor(AndroidColor.BLACK)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            update = { view -> view.player = player },
            modifier = Modifier.fillMaxSize(),
        )

        // Небольшая плашка об ошибке — видна всегда, не только в оверлее.
        if (state is PlayerState.Error) {
            ErrorIndicator(
                message = state.message,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            )
        }

        if (showControls) {
            ControlsOverlay(
                state = state,
                onRestart = onRestartClick,
                onOpenSettings = onSettingsClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Компактный индикатор ошибки в углу экрана.
 *
 * @param message текст ошибки.
 * @param modifier модификатор.
 */
@Composable
private fun ErrorIndicator(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
        contentColor = MaterialTheme.colorScheme.onError,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
