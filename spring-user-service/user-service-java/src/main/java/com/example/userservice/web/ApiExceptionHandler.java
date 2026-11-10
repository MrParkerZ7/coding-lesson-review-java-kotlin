package com.example.userservice.web;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.userservice.domain.DuplicateUserException;
import com.example.userservice.domain.EmptyUpdateException;
import com.example.userservice.domain.UserNotFoundException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Every error leaves the service as an RFC 9457 problem document (application/problem+json), whoever raised it:
 * the domain, bean validation, JSON parsing, or Spring Security (SecurityConfig sends its failures here too).
 * The base class already maps Spring MVC's own exceptions (405, 415, bad path variable ...) to problem documents.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // ---- domain exceptions ----

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail userNotFound(UserNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "user-not-found", "User not found", ex.getMessage());
    }

    @ExceptionHandler(DuplicateUserException.class)
    ProblemDetail duplicateUser(DuplicateUserException ex) {
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "duplicate-user", "User already exists", ex.getMessage());
        problem.setProperty("field", ex.getField());
        return problem;
    }

    @ExceptionHandler(EmptyUpdateException.class)
    ProblemDetail emptyUpdate(EmptyUpdateException ex) {
        return problem(HttpStatus.BAD_REQUEST, "empty-update", "Nothing to update", ex.getMessage());
    }

    // ---- security: raised by the filter chain or by @PreAuthorize ----

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> unauthenticated(AuthenticationException ex) {
        ProblemDetail problem = problem(HttpStatus.UNAUTHORIZED, "unauthenticated", "Authentication required",
                "Send a valid access token: Authorization: Bearer <token>");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Bearer").body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail forbidden(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "forbidden", "Forbidden", "The token does not allow this operation");
    }

    // ---- the catch-all: a bug. Log everything, tell the client nothing ----

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Unexpected error", "Something went wrong on our side");
    }

    // ---- request body problems: override what ResponseEntityExceptionHandler would have done ----

    /** Bean validation failed (@Valid on the body): list every bad field. */
    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldProblem> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldProblem(error.getField(), String.valueOf(error.getDefaultMessage())))
                .sorted(Comparator.comparing(FieldProblem::field).thenComparing(FieldProblem::message))
                .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed",
                errors.size() + " field(s) are invalid");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    /** The body is not JSON, has an unknown field, or a value of the wrong type (e.g. status "NOPE"). */
    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "malformed-body", "Malformed request body",
                "The request body could not be read");
        if (ex.getCause() instanceof MismatchedInputException mismatch && !mismatch.getPath().isEmpty()) {
            String message = mismatch instanceof UnrecognizedPropertyException ? "unknown field" : "invalid value";
            problem.setProperty("errors", List.of(new FieldProblem(fieldPath(mismatch.getPath()), message)));
        }
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    // ---- helpers ----

    private static ProblemDetail problem(HttpStatus status, String code, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:problem:" + code));
        problem.setTitle(title);
        return problem;
    }

    /** ["address", "city"] -> "address.city", a list index -> "tags[0]". */
    private static String fieldPath(List<JacksonException.Reference> path) {
        StringBuilder field = new StringBuilder();
        for (JacksonException.Reference step : path) {
            if (step.getPropertyName() != null) {
                if (!field.isEmpty()) {
                    field.append('.');
                }
                field.append(step.getPropertyName());
            } else if (step.getIndex() >= 0) {
                field.append('[').append(step.getIndex()).append(']');
            }
        }
        return field.toString();
    }
}
