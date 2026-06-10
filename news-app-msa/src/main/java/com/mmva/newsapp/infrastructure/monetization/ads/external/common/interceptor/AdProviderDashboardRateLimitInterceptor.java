package com.mmva.newsapp.infrastructure.monetization.ads.external.common.interceptor;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.config.AdProviderDashboardRateLimitConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rate limiting interceptor for ad providers dashboard API
 *
 * Intercepts requests to dashboard endpoints and enforces rate limits
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdProviderDashboardRateLimitInterceptor implements HandlerInterceptor {

    private final AdProviderDashboardRateLimitConfig rateLimitConfig;

    private static final String DASHBOARD_PATH_PREFIX = "/api/v1/admin/ad-providers/dashboard";
    private static final String RATE_LIMIT_EXCEEDED_MESSAGE = "Rate limit exceeded. Please try again later.";
    private static final String X_RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    private static final String X_RATE_LIMIT_RESET = "X-Rate-Limit-Reset";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();

        // Only apply rate limiting to dashboard endpoints
        if (!requestURI.startsWith(DASHBOARD_PATH_PREFIX)) {
            return true;
        }

        // Get client identifier (IP address for now, could be enhanced with user ID)
        String clientId = getClientId(request);

        // Check if request is allowed
        if (!rateLimitConfig.isRequestAllowed(clientId)) {
            log.warn("Rate limit exceeded for client: {} on path: {}", clientId, requestURI);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(createRateLimitExceededResponse());

            // Add rate limit headers
            long remainingRequests = rateLimitConfig.getRemainingRequests(clientId);
            response.setHeader(X_RATE_LIMIT_REMAINING, String.valueOf(remainingRequests));
            response.setHeader(X_RATE_LIMIT_RESET, String.valueOf(System.currentTimeMillis() / 1000 + 60)); // Reset in
                                                                                                            // 60
                                                                                                            // seconds

            return false;
        }

        // Add rate limit headers for successful requests
        long remainingRequests = rateLimitConfig.getRemainingRequests(clientId);
        response.setHeader(X_RATE_LIMIT_REMAINING, String.valueOf(remainingRequests));

        return true;
    }

    /**
     * Get client identifier from request
     *
     * Uses IP address as client identifier. In production, consider:
     * - User ID for authenticated requests
     * - API key for service-to-service calls
     * - Combination of IP and user agent
     *
     * @param request HTTP request
     * @return Client identifier
     */
    private String getClientId(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Handle comma-separated list of IPs (first is original client)
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        // Fallback to remote address
        return request.getRemoteAddr();
    }

    /**
     * Create JSON response for rate limit exceeded
     *
     * @return JSON error response
     */
    private String createRateLimitExceededResponse() {
        return """
                {
                    "success": false,
                    "error": {
                        "code": "RATE_LIMIT_EXCEEDED",
                        "message": "%s",
                        "type": "RATE_LIMIT_ERROR"
                    },
                    "timestamp": "%s"
                }
                """.formatted(RATE_LIMIT_EXCEEDED_MESSAGE, java.time.Instant.now().toString());
    }
}