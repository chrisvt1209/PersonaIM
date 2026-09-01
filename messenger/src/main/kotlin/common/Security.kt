package common

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

const val DEFAULT_JWT_SECRET = "6c80f22b9f52ea31378eeeaf3bd558cd672693adef4dfb37d4eb91660ed3ae46"

fun resolveJwtSecret(): String = System.getenv("JWT_SECRET") ?: DEFAULT_JWT_SECRET

fun Application.configureSecurity() {

    val secret = resolveJwtSecret()

    install(Authentication) {

        jwt("auth-jwt") {

            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .build()
            )

            validate { credential ->

                val userId = credential.payload.subject
                    ?.toLongOrNull()

                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}