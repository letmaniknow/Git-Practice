package com.mmva.newsapp.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT Token Provider.
 * 
 * <p>
 * Handles JWT token generation, validation, and parsing.
 * Supports both access tokens and refresh tokens.
 * </p>
 * 
 * <h2>Token Types:</h2>
 * <ul>
 * <li>ACCESS - Short-lived token for API access (15 min default)</li>
 * <li>REFRESH - Long-lived token for obtaining new access tokens (7 days
 * default)</li>
 * </ul>
 * 
 * <h2>Token Claims:</h2>
 * <ul>
 * <li>sub - User/Admin UUID</li>
 * <li>type - USER or ADMIN</li>
 * <li>tokenType - ACCESS or REFRESH</li>
 * <li>roles - List of role names</li>
 * <li>permissions - List of permission names (for ADMIN)</li>
 * </ul>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // ========================================
    // Token Type Constants
    // ========================================

    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";
    public static final String USER_TYPE_ADMIN = "ADMIN";
    public static final String USER_TYPE_USER = "USER";

    // ========================================
    // Token Generation
    // ========================================

    /**
     * Generates an access token for an admindashboard user.
     *
     * @param adminId     Admin UUID
     * @param username    Admin username
     * @param roles       List of role names
     * @param permissions List of permission names
     * @return JWT access token
     */
    public String generateAdminAccessToken(UUID adminId, String username, List<String> roles,
            List<String> permissions) {
        return generateToken(
                adminId.toString(),
                username,
                USER_TYPE_ADMIN,
                TOKEN_TYPE_ACCESS,
                roles,
                permissions,
                jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Generates a refresh token for an admindashboard user.
     *
     * @param adminId  Admin UUID
     * @param username Admin username
     * @return JWT refresh token
     */
    public String generateAdminRefreshToken(UUID adminId, String username) {
        return generateToken(
                adminId.toString(),
                username,
                USER_TYPE_ADMIN,
                TOKEN_TYPE_REFRESH,
                List.of(),
                List.of(),
                jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * Generates an access token for an app user.
     *
     * @param userId   User UUID
     * @param username User email or username
     * @return JWT access token
     */
    public String generateUserAccessToken(UUID userId, String username) {
        return generateToken(
                userId.toString(),
                username,
                USER_TYPE_USER,
                TOKEN_TYPE_ACCESS,
                List.of("USER"),
                List.of(),
                jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Generates a refresh token for an app user.
     *
     * @param userId   User UUID
     * @param username User email or username
     * @return JWT refresh token
     */
    public String generateUserRefreshToken(UUID userId, String username) {
        return generateToken(
                userId.toString(),
                username,
                USER_TYPE_USER,
                TOKEN_TYPE_REFRESH,
                List.of(),
                List.of(),
                jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * Core token generation method.
     */
    private String generateToken(
            String subject,
            String username,
            String userType,
            String tokenType,
            List<String> roles,
            List<String> permissions,
            long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(subject)
                .claim("username", username)
                .claim("userType", userType)
                .claim("tokenType", tokenType)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // ========================================
    // Token Validation
    // ========================================

    /**
     * Validates the JWT token.
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Validates that the token is an access token.
     *
     * @param token JWT token
     * @return true if valid access token
     */
    public boolean isAccessToken(String token) {
        try {
            String tokenType = getClaimFromToken(token, claims -> claims.get("tokenType", String.class));
            return TOKEN_TYPE_ACCESS.equals(tokenType);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Validates that the token is a refresh token.
     *
     * @param token JWT token
     * @return true if valid refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = getClaimFromToken(token, claims -> claims.get("tokenType", String.class));
            return TOKEN_TYPE_REFRESH.equals(tokenType);
        } catch (JwtException e) {
            return false;
        }
    }

    // ========================================
    // Token Parsing
    // ========================================

    /**
     * Extracts subject (user/admindashboard ID) from token.
     *
     * @param token JWT token
     * @return UUID as string
     */
    public String getSubjectFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extracts user ID as UUID from token.
     *
     * @param token JWT token
     * @return User/Admin UUID
     */
    public UUID getIdFromToken(String token) {
        return UUID.fromString(getSubjectFromToken(token));
    }

    /**
     * Extracts username from token.
     *
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("username", String.class));
    }

    /**
     * Extracts user type (ADMIN or USER) from token.
     *
     * @param token JWT token
     * @return User type
     */
    public String getUserTypeFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userType", String.class));
    }

    /**
     * Extracts roles from token.
     *
     * @param token JWT token
     * @return List of role names
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("roles", List.class));
    }

    /**
     * Extracts permissions from token.
     *
     * @param token JWT token
     * @return List of permission names
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("permissions", List.class));
    }

    /**
     * Extracts expiration date from token.
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Checks if token is expired.
     *
     * @param token JWT token
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Generic method to extract any claim from token.
     *
     * @param token          JWT token
     * @param claimsResolver Function to extract specific claim
     * @param <T>            Claim type
     * @return Extracted claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from token.
     *
     * @param token JWT token
     * @return All claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Gets the signing key from the secret.
     *
     * @return SecretKey for signing/verifying
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gets access token expiration in seconds (for response).
     *
     * @return Expiration in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration() / 1000;
    }

    /**
     * Gets refresh token expiration in seconds (for response).
     *
     * @return Expiration in seconds
     */
    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshTokenExpiration() / 1000;
    }
}
