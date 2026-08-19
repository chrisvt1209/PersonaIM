package features.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.sirhcvt.features.auth.AuthResponse
import dev.sirhcvt.features.auth.LoginRequest
import dev.sirhcvt.features.auth.RegisterRequest
import features.users.UserRepository
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class AuthService(
    private val userRepository: UserRepository,
    private val jwtSecret: String
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("Email is already registered")
        }

        val passwordHash = BCrypt.hashpw(
            request.password,
            BCrypt.gensalt())

        val userId = userRepository.create(
            username = request.username,
            email = request.email,
            passwordHash = passwordHash
        )

        return AuthResponse(token = generateToken(userId))
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Invalid credentials")

        val passwordHash = userRepository.getPasswordHash(request.email)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!BCrypt.checkpw(request.password, passwordHash)) {
                throw IllegalArgumentException("Invalid credentials")
        }

        return AuthResponse(token = generateToken(user.id))
    }

    private fun generateToken(userId: Long): String {
        val algorithm = Algorithm.HMAC256(jwtSecret)

        return JWT.create()
            .withSubject(userId.toString())
            .withIssuedAt(Date())
            .withExpiresAt(
                Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)
            )
            .sign(algorithm)
    }
}