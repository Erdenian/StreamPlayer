package ru.erdenian.streamplayer.settings

import android.content.Context
import androidx.core.content.edit
import ru.erdenian.streamplayer.BuildConfig

/**
 * Простое хранилище настроек на базе [android.content.SharedPreferences].
 *
 * Хранит URL HLS-потока. Значение по умолчанию берётся из
 * [BuildConfig.DEFAULT_STREAM_URL], которое может быть задано через
 * `stream.url` в `local.properties` при сборке.
 */
internal class SettingsRepository(context: Context) {

    private companion object {
        const val PREFS_NAME = "stream_player_settings"
        const val KEY_URL = "stream_url"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * URL потока. Если пользователь ничего не сохранял — возвращается
     * значение из `local.properties`.
     */
    var streamUrl: String
        get() = prefs.getString(KEY_URL, null) ?: BuildConfig.DEFAULT_STREAM_URL
        set(value) = prefs.edit { putString(KEY_URL, value) }
}
