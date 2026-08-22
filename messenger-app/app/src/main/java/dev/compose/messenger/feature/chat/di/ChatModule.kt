package dev.compose.messenger.feature.chat.di

import dev.compose.messenger.feature.chat.data.ChatRepository
import dev.compose.messenger.feature.chat.data.ChatRepositoryImpl
import dev.compose.messenger.feature.chat.presentation.ChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    single<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }
    viewModel { (conversationId: String) -> ChatViewModel(get(), conversationId) }
}
