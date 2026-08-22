package common

import common.websockets.WebSocketManager
import features.auth.AuthService
import features.conversations.ConversationRepository
import features.conversations.ConversationService
import features.friends.FriendRepository
import features.friends.FriendService
import features.messages.MessageRepository
import features.messages.MessageService
import features.users.UserRepository
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.ktorm.database.Database

private const val JWT_SECRET_QUALIFIER = "jwtSecret"

private val appModule = module {
    single<Database> { DatabaseFactory.create() }
    single(named(JWT_SECRET_QUALIFIER)) {
        System.getenv("JWT_SECRET")
            ?: "6c80f22b9f52ea31378eeeaf3bd558cd672693adef4dfb37d4eb91660ed3ae46"
    }

    single { UserRepository(get()) }
    single { ConversationRepository(get()) }
    single { ConversationService(get()) }
    single { MessageRepository(get()) }
    single { MessageService(get(), get()) }
    single { FriendRepository(get()) }
    single { FriendService(get(), get()) }
    single { AuthService(get(), get(named(JWT_SECRET_QUALIFIER))) }
    single { WebSocketManager() }
}

fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}
