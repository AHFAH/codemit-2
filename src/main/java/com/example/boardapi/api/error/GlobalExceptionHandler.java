package com.example.boardapi.api.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApi(ApiException e, HttpServletRequest request) {
        return response(e.getStatus(), e.getCode(), e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage)
                .distinct().collect(Collectors.joining("; "));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> handleBadRequest(Exception e, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "요청 형식이 올바르지 않습니다.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> handleNotFound(NoResourceFoundException e, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 경로를 찾을 수 없습니다.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다.", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "이메일 또는 비밀번호가 올바르지 않습니다.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleDenied(AccessDeniedException e, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "FORBIDDEN", "요청을 수행할 권한이 없습니다.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException e, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "CONFLICT", "중복된 값이 있거나 데이터 제약 조건을 위반했습니다.", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception e, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "요청을 처리하는 중 오류가 발생했습니다.", request);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), code, message, request.getRequestURI()));
    }
}
