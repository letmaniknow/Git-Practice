package com.mmva.newsapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mmva.newsapp.infrastructure.requestanalytics.interceptor.RateLimitInterceptor;

/**
 * Web MVC configuration to register interceptors.
 * 
 * <p>
 * Registers interceptors for cross-cutting concerns:
 * </p>
 * <ul>
 * <li>{@link RateLimitInterceptor} - Applies rate limiting to API
 * endpoints</li>
 * </ul>
 * 
 * <p>
 * <b>Note:</b> Soft-delete filtering is handled by
 * {@link SoftDeleteFilterAspect}
 * which runs at the service layer AFTER transactions start, ensuring proper
 * Hibernate Session access.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply rate limiting to all API endpoints
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                // Exclude health check and swagger endpoints
                .excludePathPatterns(
                        "/api/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**");
    }
}
