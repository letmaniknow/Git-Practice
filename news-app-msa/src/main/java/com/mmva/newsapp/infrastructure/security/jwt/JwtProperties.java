package com.mmva.newsapp.infrastructure.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties.
 * 
 * <p>
 * Binds to application.yaml properties under 'jwt' prefix.
 * </p>
 * 
 * <pre>
 * jwt:
 *   secret: your-secret-key
 *   accessTokenExpiration: 900000
 *   refreshTokenExpiration: 604800000
 *   issuer: TheNewsApp
 * </pre>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens.
     * Must be at least 256 bits (32 characters) for HS256 algorithm.
     */
    private String secret;

    /**
     * Access token expiration time in milliseconds.
     * Default: 15 minutes (900000ms)
     */
    private long accessTokenExpiration = 900000;

    /**
     * Refresh token expiration time in milliseconds.
     * Default: 7 days (604800000ms)
     */
    private long refreshTokenExpiration = 604800000;

    /**
     * Token issuer identifier.
     */
    private String issuer = "TheNewsApp";
}
