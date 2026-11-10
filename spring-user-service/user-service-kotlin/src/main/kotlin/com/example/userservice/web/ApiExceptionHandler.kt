package com.example.userservice.web

import com.example.userservice.domain.DuplicateUserException
import com.example.userservice.domain.EmptyUpdateException
import com.example.userservice.domain.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import tools.jackson.core.JacksonException
import tools.jackson.databind.exc.MismatchedInputException
import tools.jackson.databind.exc.UnrecognizedPropertyException
import tools.jackson.module.kotlin.KotlinInvalidNullException
import java.net.URI

/** One entry of the "errors" list in a 400 response: which JSON field, and what is wrong with it. */
internal data class FieldProblem(val field: String, val message: String)

/**
 * Every error leaves the service as an RFC 9457 problem document (application/problem+json), whoever raised it:
 * the domain, bean validation, JSON parsing, or Spring Security (SecurityConfig sends its failures here too).
 * The base class already maps Spring MVC's own exceptions (405, 415, bad path variable ...) to problem documents.
 */
@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    // ---- domain exceptions ----

    @ExceptionHandler(UserNotFoundException::class)
    fun userNotFound(ex: UserNotFoundException): ProblemDetail =
        problem(HttpStatus.NOT_FOUND, "user-not-found", "User not found", ex.message)

    @ExceptionHandler(DuplicateUserException::class)
    fun duplicateUser(ex: DuplicateUserException): ProblemDetail =
        problem(HttpStatus.CONFLICT, "duplicate-user", "User already exists", ex.message)
            .also { it.setProperty("field", ex.field) }

    @ExceptionHandler(EmptyUpdateException::class)
    fun emptyUpdate(ex: EmptyUpdateException): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, "empty-update", "Nothing to update", ex.message)

    // ---- security: raised by the filter chain or by @PreAuthorize ----

    @ExceptionHandler(AuthenticationException::class)
    fun unauthenticated(ex: AuthenticationException): ResponseEntity<ProblemDetail> {
        val problem = problem(
            HttpStatus.UNAUTHORIZED, "unauthenticated", "Authentication required",
            "Send a valid access token: Authorization: Bearer <token>",
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Bearer").body(problem)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun forbidden(ex: AccessDeniedException): ProblemDetail =
        problem(HttpStatus.FORBIDDEN, "forbidden", "Forbidden", "The token does not allow this operation")

    // ---- the catch-all: a bug. Log everything, tell the client nothing ----

    @ExceptionHandler(Exception::class)
    fun unexpected(ex: Exception): ProblemDetail {
        log.error("Unhandled exception", ex)
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Unexpected error", "Something went wrong on our side")
    }

    // ---- request body problems: override what ResponseEntityExceptionHandler would have done ----

    /** Bean validation failed (@Valid on the body): list every bad field. */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val errors = ex.bindingResult.fieldErrors
            .map { FieldProblem(it.field, it.defaultMessage.orEmpty()) }
            .sortedWith(compareBy({ it.field }, { it.message }))
        val problem = problem(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed", "${errors.size} field(s) are invalid")
        problem.setProperty("errors", errors)
        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    /** The body is not JSON, has an unknown field, a value of the wrong type, or lacks a field that cannot be null. */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val cause = ex.cause
        // Kotlin only: a field typed `String` (not `String?`) was missing or null, so Jackson's Kotlin module refuses the
        // body before bean validation can run. Answer the way bean validation would, so clients see one kind of error.
        // Jackson stops at the first such field: a body missing three fields reports one here and all three in the Java twin.
        if (cause is KotlinInvalidNullException && cause.path.isNotEmpty()) {
            val problem = problem(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed", "1 field(s) are invalid")
            problem.setProperty("errors", listOf(FieldProblem(fieldPath(cause.path), "must not be null")))
            return handleExceptionInternal(ex, problem, headers, status, request)
        }
        val problem = problem(HttpStatus.BAD_REQUEST, "malformed-body", "Malformed request body", "The request body could not be read")
        if (cause is MismatchedInputException && cause.path.isNotEmpty()) {
            val message = if (cause is UnrecognizedPropertyException) "unknown field" else "invalid value"
            problem.setProperty("errors", listOf(FieldProblem(fieldPath(cause.path), message)))
        }
        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    // ---- helpers ----

    private fun problem(status: HttpStatus, code: String, title: String, detail: String?): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            type = URI.create("urn:problem:$code")
            this.title = title
        }

    /** ["address", "city"] -> "address.city", a list index -> "tags[0]". */
    private fun fieldPath(path: List<JacksonException.Reference>): String = buildString {
        for (step in path) {
            val name = step.propertyName
            if (name != null) {
                if (isNotEmpty()) append('.')
                append(name)
            } else if (step.index >= 0) {
                append('[').append(step.index).append(']')
            }
        }
    }
}
