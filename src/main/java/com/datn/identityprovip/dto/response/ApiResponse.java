package com.datn.identityprovip.dto.response;

import com.datn.identityprovip.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    int code = 1000;
    @Builder.Default
    String status = "success";
    String message;
    T result;

    @Builder.Default
    Instant timestamp = Instant.now();

    Object errors;
    Map<String, Object> metadata;

    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
                .code(200)
                .status("success")
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> success(T result, String message) {
        ApiResponse<T> response = success(result);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> created(T result, String message) {
        return ApiResponse.<T>builder()
                .code(201)
                .status("success")
                .message(message)
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .status("error")
                .message(errorCode.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, Map<String, Object> metadata) {
        ApiResponse<T> response = error(errorCode);
        response.setMetadata(metadata);
        return response;
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, Object errors) {
        ApiResponse<T> response = error(errorCode);
        response.setErrors(errors);
        return response;
    }
}