package dev.compose.messenger.feature.profile.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.core.designsystem.component.BackgroundParticles
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.core.designsystem.component.PersonaButton
import dev.compose.messenger.core.designsystem.component.PersonaTextField
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.common.model.Avatar
import dev.compose.messenger.core.designsystem.util.personaPanelBackground
import dev.compose.messenger.feature.profile.domain.User

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import dev.compose.messenger.core.designsystem.component.SeasonMenu
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileRoute(
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    season: Season,
    onSeasonChange: (Season) -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    ProfileScreen(
        uiState = uiState,
        onLogout = onLogout,
        onBackClick = onBackClick,
        onEditClick = { showEditDialog = true },
        onChangePasswordClick = { showPasswordDialog = true },
        season = season,
        onSeasonChange = onSeasonChange
    )

    val currentUser = uiState.user
    if (showEditDialog && currentUser != null) {
        EditProfileDialog(
            user = currentUser,
            isSaving = uiState.isSavingProfile,
            error = uiState.profileSaveError,
            onSave = { username, email, avatar ->
                viewModel.onEvent(ProfileEvent.UpdateProfile(username, email, avatar))
            },
            onDismiss = { showEditDialog = false },
            didSucceed = !uiState.isSavingProfile && uiState.profileSaveError == null
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            isSaving = uiState.isChangingPassword,
            error = uiState.passwordChangeError,
            success = uiState.passwordChangeSuccess,
            onSave = { currentPassword, newPassword ->
                viewModel.onEvent(ProfileEvent.ChangePassword(currentPassword, newPassword))
            },
            onDismiss = {
                showPasswordDialog = false
                viewModel.onEvent(ProfileEvent.PasswordChangeHandled)
            }
        )
    }
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    season: Season,
    onSeasonChange: (Season) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(
                onBackClick = onBackClick,
                season = season,
                onSeasonChange = onSeasonChange
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else if (uiState.user != null) {
                Image(
                    painter = painterResource(Avatar.fromKey(uiState.user.avatar).drawableRes),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.user.username,
                            color = Color.White,
                            fontFamily = OptimaNova,
                            fontSize = 24.sp
                        )
                        IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit profile",
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = uiState.user.email,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "UID: ${uiState.user.uid}",
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Change Password",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onChangePasswordClick() }
                )
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
    onBackClick: () -> Unit,
    season: Season,
    onSeasonChange: (Season) -> Unit
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
            contentDescription = "Home",
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
        }

        SeasonMenu(
            hostElement = {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Change Season",
                    tint = Color.White,
                    modifier = Modifier.padding(top = 12.dp).size(28.dp)
                )
            },
            onSeasonChange = onSeasonChange
        )
    }
}

@Composable
private fun EditProfileDialog(
    user: User,
    isSaving: Boolean,
    error: String?,
    onSave: (username: String, email: String, avatar: String) -> Unit,
    onDismiss: () -> Unit,
    didSucceed: Boolean
) {
    var username by remember { mutableStateOf(user.username) }
    var email by remember { mutableStateOf(user.email) }
    var avatar by remember { mutableStateOf(user.avatar) }
    var hasSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(hasSubmitted, didSucceed) {
        if (hasSubmitted && didSucceed) {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Avatar.entries.forEach { option ->
                        Image(
                            painter = painterResource(option.drawableRes),
                            contentDescription = option.key,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (avatar == option.key) 3.dp else 0.dp,
                                    color = PersonaRed,
                                    shape = CircleShape
                                )
                                .clickable { avatar = option.key }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PersonaTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "Username",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                PersonaTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email address",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = Color.Red, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving && username.isNotBlank() && email.isNotBlank(),
                onClick = {
                    hasSubmitted = true
                    onSave(username, email, avatar)
                }
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    isSaving: Boolean,
    error: String?,
    success: Boolean,
    onSave: (currentPassword: String, newPassword: String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(success) {
        if (success) {
            onDismiss()
        }
    }

    val mismatch = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                PersonaTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    placeholder = "Current password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                PersonaTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = "New password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                PersonaTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm new password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                if (mismatch) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Passwords do not match", color = Color.Red, fontSize = 13.sp)
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = Color.Red, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving && currentPassword.isNotBlank() &&
                    newPassword.isNotBlank() && newPassword == confirmPassword,
                onClick = { onSave(currentPassword, newPassword) }
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
