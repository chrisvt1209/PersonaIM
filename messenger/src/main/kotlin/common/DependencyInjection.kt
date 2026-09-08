package common

import common.websockets.WebSocketManager
import features.auth.AuthService
import features.conversations.ConversationRepository
import features.conversations.ConversationService
import features.friends.FriendRepository
import features.friends.FriendService
import features.messages.MessageRepository
import features.messages.MessageService
import features.users.AvatarStorage
import features.users.UserRepository
import features.users.UserService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.ktorm.database.Database
import java.nio.file.Path

const val JWT_SECRET_QUALIFIER = "jwtSecret"

private fun appModule(database: Database, avatarStorageDir: Path) = module {
    single<Database> { database }
    single(named(JWT_SECRET_QUALIFIER)) { resolveJwtSecret() }

    single { UserRepository(get()) }
    single { AvatarStorage(avatarStorageDir) }
    single { UserService(get(), get()) }
    single { ConversationRepository(get()) }
    single { ConversationService(get()) }
    single { MessageRepository(get()) }
    single { MessageService(get(), get()) }
    single { FriendRepository(get()) }
    single { FriendService(get(), get()) }
    single { AuthService(get(), get(named(JWT_SECRET_QUALIFIER))) }
    single { WebSocketManager() }
}

fun Application.configureDependencyInjection(
    database: Database = DatabaseFactory.create(),
    avatarStorageDir: Path = Path.of("storage", "avatars")
) {
    install(Koin) {
        slf4jLogger()
        modules(appModule(database, avatarStorageDir))
    }
}
