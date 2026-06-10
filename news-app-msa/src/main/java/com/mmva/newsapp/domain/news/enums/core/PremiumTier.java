package com.mmva.newsapp.domain.news.enums.core;

/**
 * Enum representing premium subscription tiers for content access.
 * 
 * <p>
 * Used to determine which content a user can access based on their
 * subscription level. Higher tiers inherit access from lower tiers.
 * </p>
 * 
 * <h3>Access Hierarchy:</h3>
 * 
 * <pre>
 * User Tier     Can Access Content Marked As
 * ─────────     ────────────────────────────
 * FREE          FREE only
 * BASIC         FREE + BASIC
 * PRO           FREE + BASIC + PRO
 * ENTERPRISE    ALL content + API access
 * </pre>
 * 
 * <h3>Pricing Reference:</h3>
 * <ul>
 * <li>{@code FREE} - No subscription required</li>
 * <li>{@code BASIC} - Entry-level subscription</li>
 * <li>{@code PRO} - Professional subscription with premium features</li>
 * <li>{@code ENTERPRISE} - Business/API access for organizations</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum PremiumTier {

    /**
     * Free tier - accessible to all users without subscription.
     * Default tier for most content.
     */
    FREE(0, "Free content - no subscription required"),

    /**
     * Basic subscription tier.
     * Entry-level premium content access.
     */
    BASIC(1, "Basic subscription tier"),

    /**
     * Pro subscription tier.
     * Advanced premium content with additional features.
     */
    PRO(2, "Pro subscription tier"),

    /**
     * Enterprise tier.
     * Full access including API and business features.
     */
    ENTERPRISE(3, "Enterprise tier with full access");

    private final int level;
    private final String description;

    PremiumTier(int level, String description) {
        this.level = level;
        this.description = description;
    }

    /**
     * Returns the numeric level of this tier for comparison.
     * Higher numbers indicate higher access levels.
     * 
     * @return the tier level (0-3)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns a human-readable description of the tier.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this tier has access to content at the specified tier level.
     * 
     * @param contentTier the tier required for the content
     * @return true if this user tier can access the content tier
     */
    public boolean canAccess(PremiumTier contentTier) {
        if (contentTier == null) {
            return true; // null means FREE
        }
        return this.level >= contentTier.level;
    }

    /**
     * Gets PremiumTier from string value, with null safety.
     * 
     * @param value the string value
     * @return the corresponding PremiumTier, or FREE if null/invalid
     */
    public static PremiumTier fromString(String value) {
        if (value == null || value.isBlank()) {
            return FREE;
        }
        try {
            return PremiumTier.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FREE;
        }
    }
}
