package com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;

/**
 * Exception thrown when API call to ad provider fails
 * 
 * Scenarios:
 * - Rate limit exceeded
 * - Invalid API request
 * - Provider API error (HTTP 4xx, 5xx)
 * - Network connectivity error
 * - Malformed response from provider
 * - Account not found or suspended
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class AdProviderApiException extends AdProviderException {

    private int adProviderHttpStatus;

    public AdProviderApiException(String message) {
        super(message);
    }

    public AdProviderApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdProviderApiException(
            String message,
            ProviderType adProviderType,
            String adProviderErrorCode,
            int adProviderHttpStatus) {
        super(message, adProviderType, adProviderErrorCode);
        this.adProviderHttpStatus = adProviderHttpStatus;
    }

    public AdProviderApiException(
            String message,
            ProviderType adProviderType,
            String adProviderErrorCode,
            String adProviderErrorDetails,
            int adProviderHttpStatus,
            Throwable cause) {
        super(message, adProviderType, adProviderErrorCode, adProviderErrorDetails, cause);
        this.adProviderHttpStatus = adProviderHttpStatus;
    }

    public int getAdProviderHttpStatus() {
        return adProviderHttpStatus;
    }
}
