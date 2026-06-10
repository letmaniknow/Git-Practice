package com.mmva.newsapp.infrastructure.security.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserOAuthLoginDto;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service to verify Google ID tokens.
 * 
 * <p>
 * This service verifies the Google ID token received from the frontend
 * and extracts user information from it.
 * </p>
 * 
 * <p>
 * Flow:
 * <ol>
 * <li>Frontend authenticates with Google and receives ID token</li>
 * <li>Frontend sends ID token to backend</li>
 * <li>This service verifies the token with Google</li>
 * <li>Extracts user info (email, name, picture, provider ID)</li>
 * <li>Returns OAuthLoginDto for further processing</li>
 * </ol>
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class GoogleTokenVerifierService {

    private static final String PROVIDER_NAME = "google";

    @Value("${google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        log.info("GoogleTokenVerifierService: Initialized with client ID: {}...",
                googleClientId.substring(0, Math.min(10, googleClientId.length())));
    }

    /**
     * Verifies a Google ID token and extracts user information.
     *
     * @param idToken The Google ID token from the frontend
     * @return AppUserOAuthLoginDto containing user information
     * @throws InvalidRequestException if the token is invalid or verification fails
     */
    public AppUserOAuthLoginDto verifyIdToken(String idToken) {
        log.debug("GoogleTokenVerifierService: Verifying Google ID token");

        if (idToken == null || idToken.isBlank()) {
            log.warn("GoogleTokenVerifierService: Empty ID token provided");
            throw new InvalidRequestException("google-auth", "Google ID token is required");
        }

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                log.warn("GoogleTokenVerifierService: Invalid ID token - verification returned null");
                throw new InvalidRequestException("google-auth", "Invalid Google ID token");
            }

            Payload payload = googleIdToken.getPayload();

            // Extract user information from the token
            String providerId = payload.getSubject(); // Google's unique user ID
            String email = payload.getEmail();
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String givenName = (String) payload.get("given_name");
            String familyName = (String) payload.get("family_name");

            // Build full name if not present
            if (name == null || name.isBlank()) {
                name = ((givenName != null ? givenName : "") + " " +
                        (familyName != null ? familyName : "")).trim();
            }

            log.info("GoogleTokenVerifierService: Token verified for email: {}, providerId: {}",
                    email, providerId);

            // Validate email is verified
            if (!emailVerified) {
                log.warn("GoogleTokenVerifierService: Email not verified for: {}", email);
                throw new InvalidRequestException("google-auth",
                        "Google account email must be verified");
            }

            return AppUserOAuthLoginDto.builder()
                    .appUsersProvider(PROVIDER_NAME)
                    .appUsersProviderId(providerId)
                    .appUsersEmail(email)
                    .appUsersName(name)
                    .appUsersAvatarUrl(pictureUrl)
                    .appUsersIdToken(idToken)
                    .build();

        } catch (InvalidRequestException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            log.error("GoogleTokenVerifierService: Failed to verify Google ID token: {}",
                    e.getMessage(), e);
            throw new InvalidRequestException("google-auth",
                    "Failed to verify Google ID token: " + e.getMessage());
        }
    }

    /**
     * Checks if the service is properly configured.
     *
     * @return true if Google client ID is configured
     */
    public boolean isConfigured() {
        return googleClientId != null &&
                !googleClientId.isBlank() &&
                !googleClientId.contains("your-google-client-id");
    }
}
