package dev.compose.messenger.feature.profile.di

import dev.compose.messenger.feature.profile.data.ProfileRepository
import dev.compose.messenger.feature.profile.data.ProfileRepositoryImpl
import dev.compose.messenger.feature.profile.presentation.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
}
