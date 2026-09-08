package features.users

import common.BadRequestException
import common.ConflictException
import common.NotFoundException
import io.ktor.http.ContentType
import org.mindrot.jbcrypt.BCrypt

private val ALLOWED_AVATARS = setOf("ann", "ryuji", "yusuke")
private val ALLOWED_AVATAR_CONTENT_TYPES = setOf(ContentType.Image.PNG, ContentType.Image.JPEG)
private const val MAX_AVATAR_BYTES = 512 * 1024

class UserService(
    private val userRepository: UserRepository,
    private val avatarStorage: AvatarStorage
) {
    fun updateProfile(
        userId: Long,
        username: String,
        email: String,
        avatar: String
    ): User {
        if (avatar !in ALLOWED_AVATARS) {
            throw BadRequestException("Invalid avatar")
        }

        val existing = userRepository.findByEmail(email)
        if (existing != null && existing.id != userId) {
            throw ConflictException("Email is already registered")
        }

        avatarStorage.delete(userId)
        return userRepository.updateProfile(userId, username, email, avatar)
            ?: throw NotFoundException("User not found")
    }

    fun uploadAvatar(userId: Long, bytes: ByteArray, contentType: ContentType?): User {
        if (contentType == null || ALLOWED_AVATAR_CONTENT_TYPES.none { it.match(contentType) }) {
            throw BadRequestException("Unsupported image type")
        }
        if (bytes.isEmpty()) {
            throw BadRequestException("Avatar image is empty")
        }
        if (bytes.size > MAX_AVATAR_BYTES) {
            throw BadRequestException("Avatar image too large")
        }

        avatarStorage.save(userId, bytes)
        return userRepository.markAvatarCustom(userId)
            ?: throw NotFoundException("User not found")
    }

    fun getAvatarImage(userId: Long): ByteArray {
        return avatarStorage.read(userId)
            ?: throw NotFoundException("No custom avatar")
    }

    fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String
    ) {
        val currentHash = userRepository.getPasswordHashById(userId)
            ?: throw NotFoundException("User not found")

        if (!BCrypt.checkpw(currentPassword, currentHash)) {
            throw BadRequestException("Current password is incorrect")
        }

        val newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        userRepository.updatePasswordHash(userId, newHash)
    }
}
