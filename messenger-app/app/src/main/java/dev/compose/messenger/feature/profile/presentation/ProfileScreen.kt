package dev.compose.messenger.feature.profile.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.core.designsystem.component.BackgroundParticles
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.core.designsystem.component.PersonaButton
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.designsystem.util.personaPanelBackground
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileRoute(
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreen(
        uiState = uiState,
        onLogout = onLogout,
        onBackClick = onBackClick
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onLogout: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else if (uiState.user != null) {
                Image(
                    painter = painterResource(R.drawable.ann),
                    contentDescription = null,
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .personaPanelBackground(
                            accentColor = Color.White,
                            fillColor = Color.Black.copy(alpha = 0.76f)
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = uiState.user.username,
                        color = Color.White,
                        fontFamily = OptimaNova,
                        fontSize = 24.sp
                    )
                    Text(
                        text = uiState.user.email,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "ID: ${uiState.user.id}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    
                    uiState.user.bio?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    text = uiState.error ?: "User not found",
                    color = Color.White,
                    fontFamily = OptimaNova,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PersonaButton(
                text = "Logout",
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileHeader(
    onBackClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 8.dp, top = 2.dp, end = 12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_im),
            contentDescription = "Back",
            modifier = Modifier
                .height(100.dp)
                .offset(x = 4.dp, y = (-4).dp)
                .clickable { onBackClick() }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp, start = 4.dp),
        ) {
            Text(
                text = "PROFILE",
                color = Color.White,
                fontFamily = OptimaNova,
                fontSize = 26.sp,
            )
            Text(
                text = "user settings | clean feed",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
            )
        }
    }
}
