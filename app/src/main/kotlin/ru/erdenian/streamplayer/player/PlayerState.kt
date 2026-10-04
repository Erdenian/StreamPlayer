package ru.erdenian.streamplayer.player

/**
 * Состояние воспроизведения потока.
 *
 * Используется UI для отображения статуса и текста ошибки.
 */
internal sealed interface PlayerState {

    /** Плеер создан, но воспроизведение ещё не начиналось. */
    data object Initializing : PlayerState

    /** Идёт подключение к потоку или буферизация. */
    data object Loading : PlayerState

    /** Поток воспроизводится. */
    data object Playing : PlayerState

    /**
     * Произошла ошибка.
     *
     * @property message человекочитаемое описание ошибки.
     */
    data class Error(val message: String) : PlayerState
}
