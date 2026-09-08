package features.users

import java.nio.file.Files
import java.nio.file.Path

/** Reads/writes uploaded avatar images as plain files under [baseDir], one PNG per user id. */
class AvatarStorage(baseDir: Path) {
    private val directory = baseDir.also { Files.createDirectories(it) }

    fun save(userId: Long, bytes: ByteArray) {
        Files.write(pathFor(userId), bytes)
    }

    fun read(userId: Long): ByteArray? {
        val path = pathFor(userId)
        return if (Files.exists(path)) Files.readAllBytes(path) else null
    }

    fun delete(userId: Long) {
        Files.deleteIfExists(pathFor(userId))
    }

    private fun pathFor(userId: Long): Path = directory.resolve("$userId.png")
}
