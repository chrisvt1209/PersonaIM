package dev.compose.messenger.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.feature.auth.presentation.AuthRoute
import dev.compose.messenger.feature.chat.presentation.ChatRoute
import dev.compose.messenger.feature.conversations.presentation.ConversationListRoute
import dev.compose.messenger.feature.friends.presentation.FriendListRoute
import dev.compose.messenger.feature.profile.presentation.ProfileRoute

@Composable
fun MessengerNavHost(
    season: Season,
    onSeasonChange: (Season) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "auth",
        modifier = modifier
    ) {
        composable("auth") {
            AuthRoute(
                onAuthSuccess = {
                    navController.navigate("conversations") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("conversations") {
            ConversationListRoute(
                onConversationClick = { conversationId ->
                    navController.navigate("chat/$conversationId")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onAddClick = {
                    navController.navigate("friends")
                },
                season = season
            )
        }
        composable("friends") {
            FriendListRoute(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("conversations")
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                },
                season = season
            )
        }
        composable("chat/{conversationId}") { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatRoute(
                conversationId = conversationId,
                onBackClick = {
                    navController.popBackStack()
                },
                season = season
            )
        }
        composable("profile") {
            ProfileRoute(
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                },
                season = season,
                onSeasonChange = onSeasonChange
            )
        }
    }
}
