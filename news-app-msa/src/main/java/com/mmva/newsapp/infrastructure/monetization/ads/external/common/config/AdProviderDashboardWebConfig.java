package com.mmva.newsapp.infrastructure.monetization.ads.external.common.config;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.interceptor.AdProviderDashboardRateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for ad providers dashboard
 *
 * Registers interceptors and configures web-related settings
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class AdProviderDashboardWebConfig implements WebMvcConfigurer {

        private final AdProviderDashboardRateLimitInterceptor rateLimitInterceptor;

        private static final String[] DASHBOARD_ENDPOINTS = {
                        "/api/v1/admin/ad-providers/dashboard/**"
        };

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(rateLimitInterceptor)
                                .addPathPatterns(DASHBOARD_ENDPOINTS)
                                .excludePathPatterns(
                                                "/api/v1/admin/ad-providers/dashboard/health" // Exclude health endpoint
                                                                                              // from rate limiting
                                );
        }
}