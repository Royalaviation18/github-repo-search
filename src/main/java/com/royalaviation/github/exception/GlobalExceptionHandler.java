package com.royalaviation.github.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindErrors(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(Map.of("message", "Database integrity error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<?> handleGitHubResponse(WebClientResponseException ex) {
        boolean rateLimited = ex.getStatusCode().value() == 429
                || (ex.getStatusCode().value() == 403
                && ("0".equals(ex.getHeaders().getFirst("X-RateLimit-Remaining"))
                || ex.getHeaders().containsKey(HttpHeaders.RETRY_AFTER)));
        if (rateLimited) {
            HttpHeaders headers = new HttpHeaders();
            String retryAfter = ex.getHeaders().getFirst("Retry-After");
            if (retryAfter != null) {
                headers.set(HttpHeaders.RETRY_AFTER, retryAfter);
            }
            return new ResponseEntity<>(Map.of("message", "GitHub API rate limit exceeded"),
                    headers, HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<>(Map.of("message", "GitHub API request failed"), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<?> handleGitHubConnection(WebClientRequestException ex) {
        return new ResponseEntity<>(Map.of("message", "Unable to reach GitHub API"), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        return new ResponseEntity<>(Map.of("message", "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return new ResponseEntity<>(Map.of("message", "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}