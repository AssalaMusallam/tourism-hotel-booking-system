package com.swer313.projectstep1.errors;
import com.swer313.projectstep1.availabilitypricing.pricing.OverlappingPricingRuleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.dao.DataIntegrityViolationException;
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 1. كل exceptions اللي عندها status محدد ────────────────────────────
    // BaseApiException تشمل الآن:
    // BadRequestException(400), NotFoundException(404),
    // ConflictException(409), BusinessValidationException(422),
    // DuplicateResourceException(409), ResourceNotFoundException(404)
    // وكل ما يرث منهم
    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<ApiError> handleBaseApiException(
            BaseApiException ex, HttpServletRequest request) {

        ApiError body = new ApiError(
                Instant.now().toString(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // ── 2. OverlappingPricingRuleException ──────────────────────────────────
    // تبقى منفصلة لأنها ترث من ConflictException اللي كانت خارج الـ hierarchy
    // بعد التعديل هي تنمسك تلقائياً من handler رقم 1 — بس نبقيها للوضوح
    @ExceptionHandler(OverlappingPricingRuleException.class)
    public ResponseEntity<ApiError> handleOverlappingPricingRule(
            OverlappingPricingRuleException ex, HttpServletRequest request) {

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ── 3. Bean Validation — @Valid على الـ RequestBody ────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<Map<String, String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "field",   err.getField(),
                        "message", err.getDefaultMessage() != null
                                ? err.getDefaultMessage() : "Invalid value"
                ))
                .toList();

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed — check 'errors' for details",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    // ── 4. @Validated على الـ @RequestParam / @PathVariable ────────────────
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<Map<String, String>> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> {
                    // نزيل اسم الكلاس من الـ path — نرجع فقط اسم الـ field
                    String path = v.getPropertyPath().toString();
                    String field = path.contains(".")
                            ? path.substring(path.lastIndexOf('.') + 1)
                            : path;
                    return Map.of("field", field, "message", v.getMessage());
                })
                .toList();

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Parameter validation failed",
                request.getRequestURI(),
                violations
        );
        return ResponseEntity.badRequest().body(body);
    }

    // ── 5. Bad Request متنوعة ───────────────────────────────────────────────
    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(
            Exception ex, HttpServletRequest request) {

        // HttpMessageNotReadableException رسالتها قد تكون طويلة — نبسّطها
        String message = ex instanceof HttpMessageNotReadableException
                ? "Malformed or unreadable request body"
                : ex.getMessage();

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // ── 6. Type mismatch — مثل UUID مكان Long في الـ PathVariable ──────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format(
                "Parameter '%s' should be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "unknown"
        );

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // ── 7. Method Not Allowed — POST على endpoint يقبل GET فقط ────────────
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You don't have permission for this action",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Unsupported Media Type",
                "Content-Type is not supported",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String message = "Cannot delete this resource because it is still referenced by other records";

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    // ── 8. Fallback — أي exception غير متوقعة ──────────────────────────────
    // مهم: لا ترجع ex.getMessage() — قد يحتوي على تفاصيل حساسة
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
    // ══════════════════════════════════════════════════════════════════
// أضف هذا الـ handler داخل GlobalExceptionHandler.java الموجود عندك
// بعد آخر @ExceptionHandler method
// ══════════════════════════════════════════════════════════════════

    // ── Auth Exceptions ───────────────────────────────────────────────────────

    @ExceptionHandler(com.swer313.projectstep1.user.DuplicateUserEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(
            com.swer313.projectstep1.user.DuplicateUserEmailException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            org.springframework.security.core.userdetails.UsernameNotFoundException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid email or password",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(org.springframework.security.authentication.LockedException.class)
    public ResponseEntity<ApiError> handleLocked(
            org.springframework.security.authentication.LockedException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Account is disabled",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }






}