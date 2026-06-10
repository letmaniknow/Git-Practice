package com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;

/**
 * Exception thrown when synchronization of metrics fails
 * 
 * Scenarios:
 * - Database connection lost
 * - Invalid data format from provider
 * - Data validation failed
 * - Duplicate sync attempts
 * - Storage error
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class AdProviderSyncException extends AdProviderException {

    public AdProviderSyncException(String message) {
        super(message);
    }

    public AdProviderSyncException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdProviderSyncException(
            String message,
            ProviderType adProviderType,
            String adProviderErrorCode) {
        super(message, adProviderType, adProviderErrorCode);
    }

    public AdProviderSyncException(
            String message,
            ProviderType adProviderType,
            String adProviderErrorCode,
            String adProviderErrorDetails,
            Throwable cause) {
        super(message, adProviderType, adProviderErrorCode, adProviderErrorDetails, cause);
    }
}
