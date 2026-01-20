package com.github.dimitryivaniuta.dealflow.api.auth;

import com.github.dimitryivaniuta.dealflow.service.auth.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central REST error handling for auth endpoints.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> onAuth(AuthException e, HttpServletRequest req) {
        return ResponseEntity.status(e.getStatus()).body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", e.getStatus().value(),
            "error", e.getStatus().getReasonPhrase(),
            "message", e.getMessage(),
            "path", req.getRequestURI()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> onValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = e.getBindingResult().getAllErrors().isEmpty()
            ? "Validation error"
            : e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(status).body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", msg,
            "path", req.getRequestURI()
        ));
    }
}
