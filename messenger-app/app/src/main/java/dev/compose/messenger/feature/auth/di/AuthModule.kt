package dev.compose.messenger.feature.auth.di

import dev.compose.messenger.feature.auth.data.AuthRepository
import dev.compose.messenger.feature.auth.data.AuthRepositoryImpl
import dev.compose.messenger.feature.auth.presentation.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    viewModel { AuthViewModel(get()) }
}
