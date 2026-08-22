package dev.compose.messenger.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.compose.messenger.feature.auth.presentation.AuthRoute
import dev.compose.messenger.feature.chat.presentation.ChatRoute
import dev.compose.messenger.feature.conversations.presentation.ConversationListRoute
import dev.compose.messenger.feature.friends.presentation.FriendListRoute
import dev.compose.messenger.feature.profile.presentation.ProfileRoute

@Composable
fun MessengerNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "auth"
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
                }
            )
        }
        composable("friends") {
            FriendListRoute(
                onFriendClick = { friendId ->
                    navController.navigate("conversations") {
                        popUpTo("conversations") { inclusive = true }
                    }
                    // We could also immediately trigger a conversation creation here
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("chat/{conversationId}") { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatRoute(
                conversationId = conversationId,
                onBackClick = {
                    navController.popBackStack()
                }
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
                }
            )
        }
    }
}
