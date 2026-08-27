package features.friends

import features.users.User
import features.users.Users
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.Instant

class FriendRepository(private val database: Database) {

    fun addFriend(userId: Long, friendId: Long) {
        database.insert(Friends) {
            set(it.userId, userId)
            set(it.friendId, friendId)
            set(it.createdAt, Instant.now())
        }
    }

    fun getFriends(userId: Long): List<User> {
        return database
            .from(Friends)
            .innerJoin(Users, on = Friends.friendId eq Users.id)
            .select(Users.id, Users.username, Users.email, Users.uid, Users.avatar)
            .where { Friends.userId eq userId }
            .map {
                User(
                    id = it[Users.id]!!,
                    username = it[Users.username]!!,
                    email = it[Users.email]!!,
                    uid = it[Users.uid]!!,
                    avatar = it[Users.avatar]!!
                )
            }
    }

    fun areFriends(userId: Long, friendId: Long): Boolean {
        return database
            .from(Friends)
            .select()
            .where { (Friends.userId eq userId) and (Friends.friendId eq friendId) }
            .map { 1 }
            .isNotEmpty()
    }
}
