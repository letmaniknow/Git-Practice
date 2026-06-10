package com.mmva.newsapp.infrastructure.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.common.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Security Access Denied Handler.
 * 
 * <p>
 * Handles authorization errors (403 Forbidden) by returning proper JSON error
 * responses.
 * Invoked when an authenticated user lacks required permissions.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityAccessDeniedHandler implements AccessDeniedHandler {

        private final ObjectMapper objectMapper;

        @Override
        public void handle(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException, ServletException {

                log.warn("Access denied to: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                "You don't have permission to access this resource.",
                                request.getRequestURI());

                ApiResponseDto<ErrorResponse> apiResponse = ApiResponseDto.<ErrorResponse>builder()
                                .status("error")
                                .message("Access denied")
                                .timestamp(java.time.Instant.now().toString())
                                .data(errorResponse)
                                .build();

                objectMapper.writeValue(response.getOutputStream(), apiResponse);
        }
}
