package dev.compose.messenger.core.di

import androidx.room.Room
import dev.compose.messenger.MainViewModel
import dev.compose.messenger.core.database.MessengerDatabase
import dev.compose.messenger.core.datastore.PreferencesManager
import dev.compose.messenger.core.network.WebSocketService
import dev.compose.messenger.core.network.api.AuthApi
import dev.compose.messenger.core.network.api.ConversationApi
import dev.compose.messenger.core.network.api.FriendApi
import dev.compose.messenger.core.network.api.MessageApi
import dev.compose.messenger.core.network.api.UserApi
import dev.compose.messenger.core.network.createHttpClient
import dev.compose.messenger.feature.auth.di.authModule
import dev.compose.messenger.feature.chat.di.chatModule
import dev.compose.messenger.feature.conversations.di.conversationModule
import dev.compose.messenger.feature.friends.di.friendModule
import dev.compose.messenger.feature.profile.di.profileModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val coreModule = module {
    single { PreferencesManager(androidContext()) }
    viewModel { MainViewModel(get()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            MessengerDatabase::class.java,
            "messenger_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<MessengerDatabase>().userDao() }
    single { get<MessengerDatabase>().conversationDao() }
    single { get<MessengerDatabase>().messageDao() }
    single { get<MessengerDatabase>().friendDao() }

    single { createHttpClient(get()) }
    single { WebSocketService(get(), get()) }
    single { AuthApi(get()) }
    single { ConversationApi(get()) }
    single { FriendApi(get()) }
    single { MessageApi(get()) }
    single { UserApi(get()) }
}

val appModule = module {
    includes(coreModule, authModule, conversationModule, chatModule, profileModule, friendModule)
}
