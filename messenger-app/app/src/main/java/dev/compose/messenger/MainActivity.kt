package dev.compose.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import dev.compose.messenger.core.designsystem.theme.PersonaTheme
import dev.compose.messenger.core.navigation.MessengerNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PersonaTheme {
                MessengerNavHost()
            }
        }
    }
}
