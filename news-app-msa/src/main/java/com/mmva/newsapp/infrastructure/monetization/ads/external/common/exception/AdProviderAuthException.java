package com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;

/**
 * Exception thrown when authentication with ad provider fails
 * 
 * Scenarios:
 * - Invalid authorization code
 * - Expired credentials
 * - Token refresh failed
 * - Invalid API credentials
 * - User revoked access
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class AdProviderAuthException extends AdProviderException {

    public AdProviderAuthException(String message) {
        super(message);
    }

    public AdProviderAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdProviderAuthException(
            String message,
            ProviderType adProviderType,
            String adProviderErrorCode) {
        super(message, adProviderType, adProviderErrorCode);
    }

    public AdProviderAuthException(
            String message,
            ProviderType adProviderType,
            String adProviderErrorCode,
            String adProviderErrorDetails,
            Throwable cause) {
        super(message, adProviderType, adProviderErrorCode, adProviderErrorDetails, cause);
    }
}
