package common

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException as KtorBadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String)

/**
 * Single place where any exception thrown from a route, service or repository is turned
 * into a JSON [ErrorResponse] with the right status code. Routes should throw [AppException]
 * subtypes instead of catching errors themselves; anything unexpected falls through to the
 * generic 500 handler so internals (stack traces, SQL, etc.) never reach the client.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message ?: "Not found"))
        }
        exception<UnauthorizedException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(cause.message ?: "Unauthorized"))
        }
        exception<ForbiddenException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, ErrorResponse(cause.message ?: "Forbidden"))
        }
        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, ErrorResponse(cause.message ?: "Conflict"))
        }
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Bad request"))
        }

        // Thrown by kotlin's require()/check() in code not yet migrated to AppException,
        // and a reasonable default for malformed input in general.
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Bad request"))
        }

        // call.receive<T>() failures: malformed/missing JSON body, wrong content type, etc.
        exception<KtorBadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Malformed request body"))
        }
        exception<JsonConvertException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Malformed request body"))
        }
        exception<MissingRequestParameterException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing parameter: ${cause.parameterName}"))
        }

        // Catch-all: log the real cause server-side, never leak it to the client.
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception processing ${call.request.local.uri}", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Something went wrong. Please try again."))
        }

        // Routes that don't exist at all (as opposed to a handled NotFoundException).
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ErrorResponse("Not found"))
        }
    }
}
