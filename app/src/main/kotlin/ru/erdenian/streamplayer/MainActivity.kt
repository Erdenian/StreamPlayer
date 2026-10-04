package ru.erdenian.streamplayer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import ru.erdenian.streamplayer.theme.StreamPlayerTheme
import ru.erdenian.streamplayer.ui.PlayerScreen

/**
 * Единственная Activity приложения.
 *
 * Настраивает полноэкранный режим (immersive), запрещает отключение экрана
 * и передаёт управление в [PlayerScreen].
 */
internal class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()
        setContent {
            StreamPlayerTheme {
                PlayerScreen()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // После системных диалогов/шторки скрываем панели повторно.
        if (hasFocus) hideSystemBars()
    }

    /**
     * Скрывает системные панели.
     *
     * Панели показываются временно по свайпу и автоматически прячутся обратно.
     */
    private fun hideSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
