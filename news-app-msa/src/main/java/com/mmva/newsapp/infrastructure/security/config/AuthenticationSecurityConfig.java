package com.mmva.newsapp.infrastructure.security.config;

import com.mmva.newsapp.infrastructure.security.handler.JwtAuthenticationEntryPoint;
import com.mmva.newsapp.infrastructure.security.handler.SecurityAccessDeniedHandler;
import com.mmva.newsapp.infrastructure.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration.
 * 
 * <p>
 * Configures JWT-based stateless authentication and authorization.
 * </p>
 * 
 * <h2>Security Rules:</h2>
 * <ul>
 * <li>/api/v1/public/** - No authentication required</li>
 * <li>/api/v1/me/** - Requires USER or ADMIN role</li>
 * <li>/api/v1/admindashboard/** - Requires ADMIN role</li>
 * <li>All other endpoints - Requires authentication</li>
 * </ul>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class AuthenticationSecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
        private final SecurityAccessDeniedHandler securityAccessDeniedHandler;

        @Value("${app.cors.allowed-origins:http://localhost:4200,http://localhost:3000,http://localhost:8080}")
        private String allowedOriginsConfig;

        /**
         * Configures the security filter chain.
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF (stateless JWT doesn't need it)
                                .csrf(AbstractHttpConfigurer::disable)

                                // Configure CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Configure exception handling
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                                .accessDeniedHandler(securityAccessDeniedHandler))

                                // Stateless session (no session cookies)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configure authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // ========================================
                                                // Public endpoints (no auth required)
                                                // ========================================
                                                .requestMatchers("/api/v1/public/**").permitAll()

                                                // RSS Feeds (public access)
                                                .requestMatchers("/newsrss/**").permitAll()

                                                // Auth endpoints (login/refresh must be public!)
                                                .requestMatchers("/api/v1/admin/auth/**").permitAll()

                                                // Swagger/OpenAPI
                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/v3/api-docs.yaml")
                                                .permitAll()

                                                // Actuator health check
                                                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                                                // Static resources and favicon
                                                .requestMatchers(
                                                                "/favicon.ico",
                                                                "/static/**",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/error")
                                                .permitAll()

                                                // ========================================
                                                // App User endpoints (USER or ADMIN)
                                                // ========================================
                                                .requestMatchers("/api/v1/me/**").hasAnyRole("USER", "ADMIN")

                                                // ========================================
                                                // Admin endpoints (ADMIN only)
                                                // ========================================
                                                .requestMatchers(
                                                                "/api/v1/admin/health",
                                                                "/api/v1/admin/engagement/analytics/**",
                                                                "/api/v1/admin/system/analytics/**",
                                                                "/api/v1/admin/subscription-plans/**",
                                                                "/api/v1/admin/user-subscriptions/**")
                                                .hasAnyRole("ADMIN", "SUPER_ADMIN")

                                                // ========================================
                                                // All other endpoints require authentication
                                                // ========================================
                                                .anyRequest().authenticated())

                                // Add JWT filter before UsernamePasswordAuthenticationFilter
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Password encoder bean using BCrypt.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * Authentication manager bean.
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        /**
         * CORS configuration - Production hardened.
         * 
         * <p>
         * Explicitly allows specific domains instead of wildcard "*".
         * Configure via app.cors.allowed-origins in application.yaml
         * </p>
         * 
         * <p>
         * Default (dev): http://localhost:4200, http://localhost:3000,
         * http://localhost:8080
         * Production: Set in application-prod.yaml
         * </p>
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Parse comma-separated allowed origins from environment
                List<String> allowedOrigins = Arrays.asList(
                                allowedOriginsConfig.split(",\\s*"));
                configuration.setAllowedOrigins(allowedOrigins);

                configuration.setAllowedMethods(Arrays.asList(
                                HttpMethod.GET.name(),
                                HttpMethod.POST.name(),
                                HttpMethod.PUT.name(),
                                HttpMethod.PATCH.name(),
                                HttpMethod.DELETE.name(),
                                HttpMethod.OPTIONS.name()));

                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "Origin",
                                "X-Requested-With",
                                "Range"));

                configuration.setExposedHeaders(Arrays.asList(
                                "Content-Length",
                                "Content-Range",
                                "Accept-Ranges"));

                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
