package com.datn.identityprovip.exception;

import com.datn.identityprovip.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalException {

    private ResponseEntity<ApiResponse<Object>> buildResponse(
            ErrorCode errorCode,
            String message,
            Map<String, Object> metadata,
            Object errors
    ) {
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .status("error")
                .message(message != null ? message : errorCode.getMessage())
                .metadata(metadata)
                .errors(errors)
                .build();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
                .body(apiResponse);
    }

    // 1. Xử lý lỗi logic nghiệp vụ (AppException)
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Object>> handlingAppException(AppException exception) {
        log.error("AppException: {} - Code: {}", exception.getMessage(), exception.getErrorCode().getCode());

        return buildResponse(
                exception.getErrorCode(),
                exception.getMessage(),
                exception.getMetadata(),
                null
        );
    }

    // 2. Xử lý lỗi Validation (@Valid) - NÂNG CẤP: Lấy chi tiết từng field
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handlingValidation(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = new HashMap<>();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        String firstEnumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();
        try {
            errorCode = ErrorCode.valueOf(firstEnumKey);
        } catch (IllegalArgumentException e) {
            log.warn("Validation key not found in Enum: {}", firstEnumKey);
        }
        exception.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        return buildResponse(errorCode, "Dữ liệu không hợp lệ", null, validationErrors);
    }

    // 3. Xử lý lỗi Access Denied
    @ExceptionHandler(value = org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handlingAccessDeniedException(Exception e) {
        return buildResponse(ErrorCode.UNAUTHORIZED, "Bạn không có quyền truy cập", null, null);
    }

    // 4. Xử lý các lỗi hệ thống chưa được định nghĩa
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Object>> handlingRuntimeException(Exception exception) {
        log.error("Unhandled Exception: ", exception);
        return buildResponse(
                ErrorCode.UNCATEGORIZED_EXCEPTION,
                "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.",
                null,
                null
        );
    }
}