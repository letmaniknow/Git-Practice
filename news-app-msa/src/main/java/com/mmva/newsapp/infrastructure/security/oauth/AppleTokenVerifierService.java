package com.mmva.newsapp.infrastructure.security.oauth;

import com.mmva.newsapp.domain.appuser.dto.auth.AppUserOAuthLoginDto;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

/**
 * Service to verify Apple Sign In identity tokens.
 * 
 * <p>
 * This service verifies the Apple identity token (JWT) received from the
 * frontend
 * and extracts user information from it.
 * </p>
 * 
 * <p>
 * Flow:
 * <ol>
 * <li>Frontend authenticates with Apple Sign In and receives identity
 * token</li>
 * <li>Frontend sends identity token to backend</li>
 * <li>This service fetches Apple's public keys from
 * https://appleid.apple.com/auth/keys</li>
 * <li>Verifies the JWT signature using Apple's public key</li>
 * <li>Validates issuer, audience, and expiration</li>
 * <li>Extracts user info (email, subject ID)</li>
 * <li>Returns OAuthLoginDto for further processing</li>
 * </ol>
 * </p>
 * 
 * <p>
 * Note: Apple may only provide user's name on the first authentication.
 * After that, only the identity token is provided. The frontend should
 * pass the name in the request body on first sign in.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class AppleTokenVerifierService {

    private static final String PROVIDER_NAME = "apple";
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    // Cache for Apple's public keys
    private volatile JWKSet cachedJwkSet;
    private volatile long lastKeyFetchTime = 0;
    private static final long KEY_CACHE_TTL_MS = 3600000; // 1 hour

    @Value("${apple.client-id}")
    private String appleClientId;

    @PostConstruct
    public void init() {
        log.info("AppleTokenVerifierService: Initialized with client ID: {}...",
                appleClientId.substring(0, Math.min(15, appleClientId.length())));
    }

    /**
     * Verifies an Apple identity token and extracts user information.
     *
     * @param identityToken The Apple identity token (JWT) from the frontend
     * @param fullName      Optional full name (only provided on first Apple Sign
     *                      In)
     * @return AppUserOAuthLoginDto containing user information
     * @throws InvalidRequestException if the token is invalid or verification fails
     */
    public AppUserOAuthLoginDto verifyIdentityToken(String identityToken, String fullName) {
        log.debug("AppleTokenVerifierService: Verifying Apple identity token");

        if (identityToken == null || identityToken.isBlank()) {
            log.warn("AppleTokenVerifierService: Empty identity token provided");
            throw new InvalidRequestException("apple-auth", "Apple identity token is required");
        }

        try {
            // Refresh keys if cache is stale
            refreshKeysIfNeeded();

            // Create JWT processor
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

            // Create key source from cached JWK set
            JWKSource<SecurityContext> keySource = new ImmutableJWKSet<>(cachedJwkSet);

            // Create key selector that uses Apple's public keys
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                    JWSAlgorithm.RS256,
                    keySource);

            jwtProcessor.setJWSKeySelector(keySelector);

            // Process the token
            JWTClaimsSet claims = jwtProcessor.process(identityToken, null);

            // Validate issuer
            String issuer = claims.getIssuer();
            if (!APPLE_ISSUER.equals(issuer)) {
                log.warn("AppleTokenVerifierService: Invalid issuer: {}", issuer);
                throw new InvalidRequestException("apple-auth", "Invalid Apple token issuer");
            }

            // Validate audience (should be our app's client ID)
            if (!claims.getAudience().contains(appleClientId)) {
                log.warn("AppleTokenVerifierService: Invalid audience. Expected: {}, Got: {}",
                        appleClientId, claims.getAudience());
                throw new InvalidRequestException("apple-auth", "Invalid Apple token audience");
            }

            // Validate expiration
            Date expirationTime = claims.getExpirationTime();
            if (expirationTime == null || expirationTime.before(Date.from(Instant.now()))) {
                log.warn("AppleTokenVerifierService: Token expired");
                throw new InvalidRequestException("apple-auth", "Apple token has expired");
            }

            // Extract user information
            String subject = claims.getSubject(); // Apple's unique user ID (stable across sessions)
            String email = claims.getStringClaim("email");
            Boolean emailVerified = claims.getBooleanClaim("email_verified");
            Boolean isPrivateEmail = claims.getBooleanClaim("is_private_email");

            log.info("AppleTokenVerifierService: Token verified for subject: {}, email: {}, private: {}",
                    subject, email != null ? email.replaceAll("(.{3}).*(@.*)", "$1***$2") : "null",
                    isPrivateEmail);

            // Apple may not provide email on subsequent logins if user chose to hide it
            // In that case, we rely on the subject (Apple user ID) to identify the user
            if (email == null || email.isBlank()) {
                log.info("AppleTokenVerifierService: No email in token - user may have chosen to hide email");
            }

            // Validate email is verified (if email is present)
            if (email != null && emailVerified != null && !emailVerified) {
                log.warn("AppleTokenVerifierService: Email not verified for: {}", email);
                throw new InvalidRequestException("apple-auth", "Apple account email must be verified");
            }

            // Use provided fullName (only available on first sign-in) or fallback
            String name = (fullName != null && !fullName.isBlank())
                    ? fullName
                    : (email != null ? email.split("@")[0] : "Apple User");

            return AppUserOAuthLoginDto.builder()
                    .appUsersProvider(PROVIDER_NAME)
                    .appUsersProviderId(subject)
                    .appUsersEmail(email)
                    .appUsersName(name)
                    .appUsersAvatarUrl(null) // Apple doesn't provide avatar
                    .appUsersIdToken(identityToken)
                    .build();

        } catch (InvalidRequestException e) {
            throw e; // Re-throw our custom exceptions
        } catch (BadJOSEException | ParseException e) {
            log.error("AppleTokenVerifierService: Invalid token format: {}", e.getMessage());
            throw new InvalidRequestException("apple-auth", "Invalid Apple identity token format");
        } catch (Exception e) {
            log.error("AppleTokenVerifierService: Failed to verify Apple identity token: {}",
                    e.getMessage(), e);
            throw new InvalidRequestException("apple-auth",
                    "Failed to verify Apple identity token: " + e.getMessage());
        }
    }

    /**
     * Refreshes Apple's public keys if the cache is stale.
     */
    private void refreshKeysIfNeeded() throws IOException, ParseException {
        long now = System.currentTimeMillis();
        if (cachedJwkSet == null || (now - lastKeyFetchTime) > KEY_CACHE_TTL_MS) {
            refreshKeys();
        }
    }

    /**
     * Fetches Apple's public keys from their JWKS endpoint.
     */
    private synchronized void refreshKeys() throws IOException, ParseException {
        log.info("AppleTokenVerifierService: Fetching Apple public keys from {}", APPLE_KEYS_URL);
        try {
            JWKSet jwkSet = JWKSet.load(URI.create(APPLE_KEYS_URL).toURL());
            cachedJwkSet = jwkSet;
            lastKeyFetchTime = System.currentTimeMillis();
            log.info("AppleTokenVerifierService: Cached {} public keys", jwkSet.getKeys().size());
        } catch (Exception e) {
            log.error("AppleTokenVerifierService: Failed to fetch Apple public keys: {}", e.getMessage(), e);
            if (cachedJwkSet == null) {
                throw e; // Only throw if we have no cached keys
            }
            // If we have cached keys, continue using them
            log.warn("AppleTokenVerifierService: Using stale cached keys due to fetch failure");
        }
    }

    /**
     * Checks if the service is properly configured.
     *
     * @return true if Apple client ID is configured
     */
    public boolean isConfigured() {
        return appleClientId != null &&
                !appleClientId.isBlank() &&
                !appleClientId.contains("your-apple");
    }
}
