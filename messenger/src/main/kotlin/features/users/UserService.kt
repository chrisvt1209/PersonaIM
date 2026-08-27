package features.users

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
            throw IllegalArgumentException("Invalid avatar")
        }

        val existing = userRepository.findByEmail(email)
        if (existing != null && existing.id != userId) {
            throw IllegalArgumentException("Email is already registered")
        }

        return userRepository.updateProfile(userId, username, email, avatar)
            ?: throw IllegalArgumentException("User not found")
    }

    fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String
    ) {
        val currentHash = userRepository.getPasswordHashById(userId)
            ?: throw IllegalArgumentException("User not found")

        if (!BCrypt.checkpw(currentPassword, currentHash)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        val newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        userRepository.updatePasswordHash(userId, newHash)
    }
}
