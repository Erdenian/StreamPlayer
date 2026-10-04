package ru.erdenian.streamplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.erdenian.streamplayer.R
import ru.erdenian.streamplayer.player.PlayerState

/**
 * Оверлей с состоянием воспроизведения и кнопками управления.
 *
 * @param state текущее состояние.
 * @param onRestart обработчик кнопки «Перезапустить».
 * @param onOpenSettings обработчик кнопки «Настройки».
 * @param modifier модификатор.
 */
@Composable
internal fun ControlsOverlay(
    state: PlayerState,
    onRestart: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = when (state) {
                PlayerState.Initializing -> stringResource(R.string.controls_status_initializing)
                PlayerState.Loading -> stringResource(R.string.controls_status_loading)
                PlayerState.Playing -> stringResource(R.string.controls_status_playing)
                is PlayerState.Error -> stringResource(R.string.controls_status_error, state.message)
            },
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onRestart) {
                Text(stringResource(R.string.controls_restart))
            }
            OutlinedButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.controls_settings))
            }
        }
    }
}
