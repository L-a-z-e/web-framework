package com.laze.backend.common.exception;

import com.laze.backend.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException occurred: Code={}, Message={}", errorCode.getCode(), e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
            .body(ApiResponse.fail(errorCode.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("AccessDeniedException occurred: {}", e.getMessage());
        ErrorCode accessDenied = ErrorCode.ACCESS_DENIED;
        return ResponseEntity.status(accessDenied.getStatus())
            .body(ApiResponse.fail(accessDenied.getCode(), accessDenied.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException occurred: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorCode invalidInput = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(invalidInput.getStatus())
            .body(ApiResponse.fail(invalidInput.getCode(), errorMessage));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        log.warn("BindException occurred: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorCode invalidInput = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(invalidInput.getStatus())
            .body(ApiResponse.fail(invalidInput.getCode(), errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception occurred", e);
        ErrorCode internalError = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(internalError.getStatus())
            .body(ApiResponse.fail(internalError.getCode(), internalError.getMessage()));
    }
}
