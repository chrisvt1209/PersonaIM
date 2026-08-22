package dev.compose.messenger.core.designsystem.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PersonaRed,
    secondary = PersonaBlack,
    tertiary = AnnColor,
    background = PersonaWhite,
    surface = PersonaWhite,
    onPrimary = PersonaWhite,
    onSecondary = PersonaWhite,
    onTertiary = PersonaWhite,
    onBackground = PersonaBlack,
    onSurface = PersonaBlack,
)

@Composable
fun PersonaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
