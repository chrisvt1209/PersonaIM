package common

/**
 * Domain exceptions that carry their intended HTTP status. Thrown from services/routes
 * and translated into a uniform JSON error body by [configureStatusPages].
 */
sealed class AppException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : AppException(message)
class UnauthorizedException(message: String = "Unauthorized") : AppException(message)
class ForbiddenException(message: String = "Forbidden") : AppException(message)
class NotFoundException(message: String = "Not found") : AppException(message)
class ConflictException(message: String) : AppException(message)
