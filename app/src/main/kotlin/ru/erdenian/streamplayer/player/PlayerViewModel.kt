package ru.erdenian.streamplayer.player

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.erdenian.streamplayer.settings.SettingsRepository

/**
 * ViewModel, управляющая воспроизведением HLS-потока.
 *
 * Отвечает за:
 * - создание и освобождение [ExoPlayer];
 * - автоматический перезапуск воспроизведения при ошибках, зависаниях
 *   и возврате сети;
 * - хранение текущего состояния для UI;
 * - сохранение пользовательского URL потока.
 */
internal class PlayerViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private companion object {
        /** Задержка перед автоматическим перезапуском после ошибки (мс). */
        const val RESTART_DELAY_MS = 5_000L

        /** Интервал проверки «живости» воспроизведения (мс). */
        const val WATCHDOG_INTERVAL_MS = 5_000L

        /** Сколько подряд проверок позиция не должна меняться, чтобы считать поток зависшим. */
        const val STUCK_THRESHOLD = 3
    }

    private val settings = SettingsRepository(application)

    /** Плеер ExoPlayer. Отдаётся в UI для привязки к [androidx.media3.ui.PlayerView]. */
    val player: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _state = MutableStateFlow<PlayerState>(PlayerState.Initializing)

    /** Текущее состояние воспроизведения. */
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _streamUrl = MutableStateFlow(settings.streamUrl)

    /** Текущий URL потока (для отображения в настройках). */
    val streamUrl: StateFlow<String> = _streamUrl.asStateFlow()

    private var restartJob: Job? = null
    private val connectivityManager: ConnectivityManager? =
        application.getSystemService(ConnectivityManager::class.java)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Сеть вернулась. Если плеер не играет — перезапускаем с задержкой,
            // чтобы соединение успело «прогреться».
            if (_state.value !is PlayerState.Playing) {
                viewModelScope.launch {
                    delay(2_000L)
                    startPlayback()
                }
            }
        }
    }

    init {
        player.addListener(PlayerEventsListener())
        registerNetworkCallback()
        startPlayback()
        viewModelScope.launch { watchdog() }
    }

    /**
     * Меняет URL потока и сразу перезапускает воспроизведение.
     *
     * @param url новый URL HLS-потока.
     */
    fun setStreamUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed == _streamUrl.value) return
        settings.streamUrl = trimmed
        _streamUrl.value = trimmed
        restartPlayback()
    }

    /** Немедленно перезапускает воспроизведение (кнопка «Перезапустить»). */
    fun restartPlayback() {
        restartJob?.cancel()
        startPlayback()
    }

    override fun onCleared() {
        super.onCleared()
        restartJob?.cancel()
        runCatching { connectivityManager?.unregisterNetworkCallback(networkCallback) }
        player.release()
    }

    /**
     * Запускает воспроизведение текущего URL.
     *
     * Вызывается при инициализации, после ошибки, зависания и вручную.
     */
    private fun startPlayback() {
        val url = _streamUrl.value
        if (url.isBlank()) {
            _state.value = PlayerState.Error("URL потока не задан")
            return
        }
        _state.value = PlayerState.Loading
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }

    /**
     * Планирует перезапуск через [RESTART_DELAY_MS] миллисекунд.
     *
     * Используется при ошибке: даём CDN/сети немного времени, затем пробуем снова.
     */
    private fun scheduleRestart() {
        restartJob?.cancel()
        restartJob = viewModelScope.launch {
            delay(RESTART_DELAY_MS)
            startPlayback()
        }
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching { connectivityManager?.registerNetworkCallback(request, networkCallback) }
    }

    /**
     * Периодически проверяет, продвигается ли воспроизведение.
     *
     * Если позиция не меняется несколько проверок подряд (или плеер долго
     * буферизует при непустой позиции) — считаем поток зависшим и
     * перезапускаем.
     */
    private suspend fun watchdog() {
        var lastPosition = -1L
        var stuckCounter = 0

        while (true) {
            delay(WATCHDOG_INTERVAL_MS)

            val playbackState = player.playbackState
            val currentPosition = player.currentPosition

            if (playbackState == Player.STATE_IDLE) {
                // В IDLE ничего не воспроизводится: перезапуск обрабатывается
                // обработчиком ошибок, watchdog молчит.
                lastPosition = -1L
                stuckCounter = 0
                continue
            }

            if (playbackState == Player.STATE_BUFFERING || currentPosition == lastPosition) {
                stuckCounter++
            } else {
                stuckCounter = 0
            }

            if (stuckCounter >= STUCK_THRESHOLD) {
                stuckCounter = 0
                restartJob?.cancel()
                startPlayback()
            }

            lastPosition = currentPosition
        }
    }

    /** Слушатель событий ExoPlayer. */
    private inner class PlayerEventsListener : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> _state.value = PlayerState.Loading
                Player.STATE_READY -> if (player.isPlaying) _state.value = PlayerState.Playing
                Player.STATE_ENDED -> {
                    // Живой поток не должен заканчиваться, но если это случилось — перезапускаем.
                    _state.value = PlayerState.Error("Поток завершён")
                    scheduleRestart()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) _state.value = PlayerState.Playing
        }

        override fun onPlayerError(error: PlaybackException) {
            val message = error.message ?: error.errorCodeName
            _state.value = PlayerState.Error(message)
            scheduleRestart()
        }
    }
}
