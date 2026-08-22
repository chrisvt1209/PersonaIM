package dev.compose.messenger.feature.friends.di

import dev.compose.messenger.feature.friends.data.FriendRepository
import dev.compose.messenger.feature.friends.data.FriendRepositoryImpl
import dev.compose.messenger.feature.friends.presentation.FriendViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val friendModule = module {
    single<FriendRepository> { FriendRepositoryImpl(get(), get()) }
    viewModel { FriendViewModel(get(), get()) }
}
