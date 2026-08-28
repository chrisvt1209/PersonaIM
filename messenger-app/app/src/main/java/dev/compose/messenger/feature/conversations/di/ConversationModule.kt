package dev.compose.messenger.feature.conversations.di

import dev.compose.messenger.feature.conversations.data.ConversationRepository
import dev.compose.messenger.feature.conversations.data.ConversationRepositoryImpl
import dev.compose.messenger.feature.conversations.presentation.ConversationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val conversationModule = module {
    single<ConversationRepository> { ConversationRepositoryImpl(get(), get(), get(), get()) }
    viewModel { ConversationViewModel(get(), get()) }
}
