package common

import org.ktorm.database.Database

object DatabaseFactory {
    lateinit var database: Database private set

    fun initialize() {
        val jdbcUrl = System.getenv("DATABASE_URL")
            ?: "jdbc:postgresql://localhost:5432/messenger"

        val username = System.getenv("DATABASE_USERNAME")
            ?: "postgres"

        val password = System.getenv("DATABASE_PASSWORD")
            ?: "postgres"

        database = Database.connect(
            url = jdbcUrl,
            user = username,
            password = password
        )
    }
}