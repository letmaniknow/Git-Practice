package com.mmva.newsapp.infrastructure.requestanalytics.filter;

import com.mmva.newsapp.infrastructure.requestanalytics.config.RequestAnalyticsProperties;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestAnalyticsInfoDto;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestAnalyticsService;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Filter for automatically logging HTTP request analytics.
 * 
 * <p>
 * This filter captures comprehensive request information and logs it
 * asynchronously for analytics purposes. It can be configured to exclude
 * certain URL patterns and can be completely disabled via configuration.
 * </p>
 * 
 * <h3>Configuration:</h3>
 * 
 * <pre>
 * app:
 *   analytics:
 *     enabled: true
 *     exclude-patterns:
 *       - /actuator/**
 *       - /swagger-ui/**
 *       - /v3/api-docs/**
 *       - /health
 *     async: true
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestAnalyticsFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestAnalyticsFilter.class);

    private final RequestAnalyticsService analyticsService;
    private final RequestInfoService requestInfoService;
    private final RequestAnalyticsProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RequestAnalyticsFilter(RequestAnalyticsService analyticsService,
            RequestInfoService requestInfoService,
            RequestAnalyticsProperties properties) {
        this.analyticsService = analyticsService;
        this.requestInfoService = requestInfoService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Check if this path should be excluded
        if (shouldExclude(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check sampling (skip logging for sampled-out requests)
        if (!shouldSample(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        RequestAnalyticsInfoDto info = null;
        String errorMessage = null;
        String exceptionClass = null;

        try {
            // Capture request info before processing
            info = requestInfoService.getFullRequestInfo(request);

            // Process the request
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            errorMessage = e.getMessage();
            exceptionClass = e.getClass().getName();
            throw e;
        } finally {
            // Log analytics after response is sent
            if (info != null) {
                long processingTime = System.currentTimeMillis() - startTime;
                logAnalytics(info, response.getStatus(), processingTime, errorMessage, exceptionClass);
            }
        }
    }

    private boolean shouldExclude(String path) {
        return properties.getExcludePatterns().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * Determines if this request should be sampled (logged).
     * Always logs if sampling is disabled or if path matches alwaysLogPatterns.
     */
    private boolean shouldSample(String path) {
        RequestAnalyticsProperties.Sampling sampling = properties.getSampling();

        if (!sampling.isEnabled()) {
            return true; // Log everything when sampling disabled
        }

        // Always log certain patterns (admindashboard, auth)
        boolean alwaysLog = sampling.getAlwaysLogPatterns().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
        if (alwaysLog) {
            return true;
        }

        // Random sampling
        return ThreadLocalRandom.current().nextDouble() < sampling.getRate();
    }

    private void logAnalytics(RequestAnalyticsInfoDto info, int status, long processingTime,
            String errorMessage, String exceptionClass) {
        try {
            // Get user info from security context if available
            UUID userId = null;
            String username = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                username = auth.getName();
            }

            if (properties.isAsync()) {
                if (errorMessage != null) {
                    analyticsService.logRequestWithErrorAsync(info, userId, username, status,
                            processingTime, errorMessage, exceptionClass);
                } else {
                    analyticsService.logRequestAsync(info, userId, username, status, processingTime);
                }
            } else {
                analyticsService.logRequest(info, userId, username, status, processingTime,
                        errorMessage, exceptionClass);
            }
        } catch (Exception e) {
            log.error("Failed to log request analytics: {}", e.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }
}
