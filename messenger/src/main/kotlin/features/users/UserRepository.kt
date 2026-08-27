package features.users

import org.ktorm.database.Database
import org.ktorm.dsl.*

class UserRepository(
    private val database: Database
) {
    fun findById(id: Long): User? {
        return database
            .from(Users)
            .select()
            .where { Users.id eq id }
            .map {
                User(
                    id = it[Users.id]!!,
                    username = it[Users.username]!!,
                    email = it[Users.email]!!,
                    uid = it[Users.uid]!!
                )
            }
            .firstOrNull()
    }

    fun findByEmail(email: String): User? {
        return database
            .from(Users)
            .select()
            .where { Users.email eq email }
            .map {
                User(
                    id = it[Users.id]!!,
                    username = it[Users.username]!!,
                    email = it[Users.email]!!,
                    uid = it[Users.uid]!!
                )
            }
            .firstOrNull()
    }

    fun findByUid(uid: String): User? {
        return database
            .from(Users)
            .select()
            .where { Users.uid eq uid }
            .map {
                User(
                    id = it[Users.id]!!,
                    username = it[Users.username]!!,
                    email = it[Users.email]!!,
                    uid = it[Users.uid]!!
                )
            }
            .firstOrNull()
    }

    fun create(
        username: String,
        email: String,
        passwordHash: String,
        uid: String
    ): Long {
        database.insert(Users) {
            set(it.username, username)
            set(it.email, email)
            set(it.passwordHash, passwordHash)
            set(it.uid, uid)
        }

        return database
            .from(Users)
            .select(Users.id)
            .where { Users.email eq email }
            .map { it[Users.id]!! }
            .first()
    }

    fun getPasswordHash(email: String): String? {
        return database
            .from(Users)
            .select(Users.passwordHash)
            .where { Users.email eq email }
            .map { it[Users.passwordHash]!! }
            .firstOrNull()
    }
}