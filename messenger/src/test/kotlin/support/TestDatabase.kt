package support

import org.flywaydb.core.Flyway
import org.ktorm.database.Database
import java.sql.DriverManager

/**
 * A dedicated local Postgres database ("messenger_test", separate from the dev "messenger" DB)
 * used for backend integration tests. Override via TEST_DATABASE_URL/_USERNAME/_PASSWORD if
 * pointing at a different instance (e.g. CI).
 */
object TestDatabase {
    private val jdbcUrl = System.getenv("TEST_DATABASE_URL")
        ?: "jdbc:postgresql://localhost:5432/messenger_test"

    private val username = System.getenv("TEST_DATABASE_USERNAME")
        ?: "postgres"

    private val password = System.getenv("TEST_DATABASE_PASSWORD")
        ?: "postgres"

    val database: Database by lazy {
        Flyway.configure()
            .dataSource(jdbcUrl, username, password)
            .load()
            .migrate()

        Database.connect(
            url = jdbcUrl,
            user = username,
            password = password
        )
    }

    /** Call between tests to wipe all data while keeping the migrated schema. */
    fun reset() {
        database // ensure migration has run
        DriverManager.getConnection(jdbcUrl, username, password).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    "TRUNCATE TABLE messages, conversation_participants, conversations, friends, users RESTART IDENTITY CASCADE"
                )
            }
        }
    }
}
