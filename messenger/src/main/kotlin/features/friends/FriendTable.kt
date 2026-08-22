package features.friends

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.timestamp

object Friends : Table<Nothing>("friends") {
    val id = long("id").primaryKey()
    val userId = long("user_id")
    val friendId = long("friend_id")
    val createdAt = timestamp("created_at")
}
