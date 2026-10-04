package ru.erdenian.streamplayer.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Тема приложения.
 *
 * Всегда использует тёмную палитру: фон под плеером чёрный, оверлей
 * тоже рассчитан на тёмную схему.
 */
@Composable
internal fun StreamPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content,
    )
}
