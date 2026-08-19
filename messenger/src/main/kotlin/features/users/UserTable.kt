package features.users

import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object Users : Table<Nothing>("users") {
    val id = long("id").primaryKey()
    val username = varchar("username")
    val email = varchar("email")
    val passwordHash = varchar("password_hash")
    val createdAt = datetime("created_at")
}