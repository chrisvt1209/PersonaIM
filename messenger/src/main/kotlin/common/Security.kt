package common

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {

    val secret = System.getenv("JWT_SECRET")
        ?: "development-secret-change-me"

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