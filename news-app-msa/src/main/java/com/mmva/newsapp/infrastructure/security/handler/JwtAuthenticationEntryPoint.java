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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point.
 * 
 * <p>
 * Handles authentication errors by returning proper JSON error responses.
 * Invoked when an unauthenticated user tries to access a protected resource.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

        private final ObjectMapper objectMapper;

        @Override
        public void commence(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

                log.warn("Unauthorized request to: {} - {}", request.getRequestURI(), authException.getMessage());

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Authentication required. Please provide a valid JWT token.",
                                request.getRequestURI());

                ApiResponseDto<ErrorResponse> apiResponse = ApiResponseDto.<ErrorResponse>builder()
                                .status("error")
                                .message("Authentication required")
                                .timestamp(java.time.Instant.now().toString())
                                .data(errorResponse)
                                .build();

                objectMapper.writeValue(response.getOutputStream(), apiResponse);
        }
}
