package com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums;

/**
 * Enumeration of supported ad provider types
 * 
 * Naming Convention:
 * - UPPERCASE_UNDERSCORE (enum constant)
 * - Lowercase with hyphens (code/key)
 * - CamelCase (display name)
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum ProviderType {
    GOOGLE_ADSENSE("google-adsense", "Google AdSense", "Google's AdSense platform for web publishers"),
    GOOGLE_ADMOB("google-admob", "Google AdMob", "Google's AdMob platform for mobile app publishers"),
    FACEBOOK_AUDIENCE_NETWORK("facebook-audience-network", "Facebook Audience Network",
            "Facebook's Audience Network for publishers"),
    CRITEO("criteo", "Criteo", "Criteo's native ad platform"),
    PUBMATIC("pubmatic", "PubMatic", "PubMatic header bidding platform");

    private final String code;
    private final String displayName;
    private final String description;

    /**
     * Constructor
     * 
     * @param code        Unique code for provider (lowercase-hyphenated)
     * @param displayName User-friendly display name
     * @param description Detailed description
     */
    ProviderType(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Get provider code (lowercase-hyphenated)
     * Example: "google-adsense", "google-admob"
     */
    public String getCode() {
        return code;
    }

    /**
     * Get display name for UI
     * Example: "Google AdSense", "Google AdMob"
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get description of provider
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get ad provider code (alias for getCode for consistency)
     */
    public String getAdProviderCode() {
        return code;
    }

    /**
     * Get ad provider display name (alias for getDisplayName for consistency)
     */
    public String getAdProviderDisplayName() {
        return displayName;
    }

    /**
     * Parse provider type from code
     * 
     * @param code The code string
     * @return ProviderType matching the code
     * @throws IllegalArgumentException if code not found
     */
    public static ProviderType fromCode(String code) {
        for (ProviderType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown provider code: " + code);
    }

    /**
     * Check if provider is enabled for platform
     * 
     * @return true if provider is currently supported
     */
    public boolean isSupported() {
        // Initially only GOOGLE_ADSENSE and GOOGLE_ADMOB are implemented
        return this == GOOGLE_ADSENSE || this == GOOGLE_ADMOB;
    }
}
