package com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;

/**
 * Base exception for all ad provider related errors
 * 
 * Naming Convention:
 * - Specific exceptions extend this base
 * - Example: ProviderAuthException, ProviderApiException, ProviderSyncException
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class AdProviderException extends RuntimeException {

    private ProviderType adProviderType;
    private String adProviderErrorCode;
    private String adProviderErrorDetails;

    public AdProviderException(String message) {
        super(message);
    }

    public AdProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdProviderException(
            String message,
            ProviderType adProviderType,
            String adProviderErrorCode) {
        super(message);
        this.adProviderType = adProviderType;
        this.adProviderErrorCode = adProviderErrorCode;
    }

    public AdProviderException(
            String message,
            ProviderType adProviderType,
            String adProviderErrorCode,
            String adProviderErrorDetails,
            Throwable cause) {
        super(message, cause);
        this.adProviderType = adProviderType;
        this.adProviderErrorCode = adProviderErrorCode;
        this.adProviderErrorDetails = adProviderErrorDetails;
    }

    public ProviderType getAdProviderType() {
        return adProviderType;
    }

    public String getAdProviderErrorCode() {
        return adProviderErrorCode;
    }

    public String getAdProviderErrorDetails() {
        return adProviderErrorDetails;
    }
}
