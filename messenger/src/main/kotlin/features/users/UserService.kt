package features.users

import common.BadRequestException
import common.ConflictException
import common.NotFoundException
import org.mindrot.jbcrypt.BCrypt

private val ALLOWED_AVATARS = setOf("ann", "ryuji", "yusuke")

class UserService(
    private val userRepository: UserRepository
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

        return userRepository.updateProfile(userId, username, email, avatar)
            ?: throw NotFoundException("User not found")
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
