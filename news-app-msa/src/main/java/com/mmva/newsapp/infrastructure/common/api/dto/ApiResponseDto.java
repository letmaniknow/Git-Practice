package com.mmva.newsapp.infrastructure.common.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> implements Serializable {
    @Schema(description = "Status of the response", example = "success")
    private String status;

    @Schema(description = "Message describing the response", example = "Operation completed successfully.")
    private String message;

    @Schema(description = "Timestamp of the response (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String timestamp;

    @Schema(description = "Response data payload")
    private T data;

    /**
     * Creates a success response with the given message and data.
     *
     * @param message the success message
     * @param data    the response data
     * @param <T>     the type of data
     * @return ApiResponseDto with status "success"
     */
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return ApiResponseDto.<T>builder()
                .status("success")
                .message(message)
                .timestamp(Instant.now().toString())
                .data(data)
                .build();
    }

    /**
     * Creates an error response with the given message.
     *
     * @param message the error message
     * @param <T>     the type of data
     * @return ApiResponseDto with status "error"
     */
    public static <T> ApiResponseDto<T> error(String message) {
        return ApiResponseDto.<T>builder()
                .status("error")
                .message(message)
                .timestamp(Instant.now().toString())
                .data(null)
                .build();
    }
}
