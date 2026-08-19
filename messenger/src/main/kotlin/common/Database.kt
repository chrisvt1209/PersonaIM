package common

import org.flywaydb.core.Flyway
import org.ktorm.database.Database

object DatabaseFactory {
    fun create(): Database {
        val jdbcUrl = System.getenv("DATABASE_URL")
            ?: "jdbc:postgresql://localhost:5432/messenger"

        val username = System.getenv("DATABASE_USERNAME")
            ?: "postgres"

        val password = System.getenv("DATABASE_PASSWORD")
            ?: "postgres"

        Flyway.configure()
            .dataSource(jdbcUrl, username, password)
            .load()
            .migrate()

        return Database.connect(
            url = jdbcUrl,
            user = username,
            password = password
        )
    }
}
