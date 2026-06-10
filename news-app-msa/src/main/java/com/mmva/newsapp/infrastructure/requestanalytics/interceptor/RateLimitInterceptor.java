package com.mmva.newsapp.infrastructure.requestanalytics.interceptor;

import com.mmva.newsapp.infrastructure.requestanalytics.config.RateLimitConfig;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP interceptor that applies rate limiting to incoming requests.
 * Returns 429 Too Many Requests when rate limit is exceeded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;
    private final RequestInfoService requestInfoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientIP = requestInfoService.getClientIpAddress(request);

        if (!rateLimitConfig.tryConsume(clientIP)) {
            log.warn("Rate limit exceeded for IP: {}", clientIP);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Rate limit exceeded. Please try again later.\",\"data\":null}");
            return false;
        }

        // Add rate limit headers for client awareness
        long remainingTokens = rateLimitConfig.getAvailableTokens(clientIP);
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));

        return true;
    }
}
