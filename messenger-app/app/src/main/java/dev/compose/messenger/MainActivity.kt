package dev.compose.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.compose.messenger.core.designsystem.component.BackgroundParticles
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.designsystem.theme.PersonaTheme
import dev.compose.messenger.core.navigation.MessengerNavHost
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val viewModel: MainViewModel = koinViewModel()
            val season by viewModel.season.collectAsState()

            PersonaTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = PersonaRed)
                ) {
                    BackgroundParticles(season)

                    Image(
                        painter = painterResource(R.drawable.bg_splatter_background),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .statusBarsPadding()
                            .offset(y = (-16).dp)
                    )

                    MessengerNavHost(
                        season = season,
                        onSeasonChange = viewModel::changeSeason,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
