package com.mmva.newsapp.infrastructure.monetization.campaign.enums;

/**
 * Enum representing the type of sponsorship campaign.
 * 
 * <p>
 * Different campaign types have different pricing models,
 * content requirements, and display rules.
 * </p>
 * 
 * <h3>Campaign Types:</h3>
 * <table border="1">
 * <tr>
 * <th>Type</th>
 * <th>Pricing</th>
 * <th>CPM Range</th>
 * </tr>
 * <tr>
 * <td>SPONSORED_ARTICLE</td>
 * <td>CPM/Flat</td>
 * <td>$15-50</td>
 * </tr>
 * <tr>
 * <td>NATIVE_AD</td>
 * <td>CPM/CPC</td>
 * <td>$5-20</td>
 * </tr>
 * <tr>
 * <td>BRAND_PARTNERSHIP</td>
 * <td>Flat Fee</td>
 * <td>Custom</td>
 * </tr>
 * <tr>
 * <td>AFFILIATE</td>
 * <td>Commission</td>
 * <td>5-20%</td>
 * </tr>
 * </table>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum SponsorshipCampaignType {

    /**
     * Sponsored newsapp - full articles paid by sponsor.
     * Looks like editorial content but marked as "Sponsored".
     * Highest CPM ($15-50), requires editorial quality content.
     * Examples: Forbes BrandVoice, NYT Paid Posts.
     */
    SPONSORED_ARTICLE("Sponsored Article", "Full sponsored articles", true),

    /**
     * Native ad - ad cards that blend with newsapp feed.
     * Matches the look and feel of surrounding content.
     * Medium CPM ($5-20), less intrusive than display ads.
     * Examples: Taboola, Outbrain recommendations.
     */
    NATIVE_AD("Native Ad", "Ad cards in content feed", false),

    /**
     * Brand partnership - long-term sponsorship deal.
     * May include exclusive sections, co-branding, events.
     * Custom pricing, typically quarterly/annual contracts.
     */
    BRAND_PARTNERSHIP("Brand Partnership", "Long-term sponsorship", true),

    /**
     * Affiliate content - product recommendations with affiliate links.
     * Revenue from purchases made through links.
     * Commission-based (5-20% of sale), requires FTC disclosure.
     * Examples: Wirecutter, product reviews.
     */
    AFFILIATE("Affiliate", "Affiliate product content", true),

    /**
     * Display ad - traditional banner/interstitial ads.
     * Standard IAB ad formats (banner, leaderboard, etc.).
     * Low-medium CPM ($2-10), programmatic or direct.
     */
    DISPLAY_AD("Display Ad", "Banner and display ads", false);

    private final String displayName;
    private final String description;
    private final boolean requiresEditorialContent;

    SponsorshipCampaignType(String displayName, String description, boolean requiresEditorialContent) {
        this.displayName = displayName;
        this.description = description;
        this.requiresEditorialContent = requiresEditorialContent;
    }

    /**
     * Returns a human-readable display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the type description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this type requires editorial/newsapp content.
     *
     * @return true if editorial content required
     */
    public boolean requiresEditorialContent() {
        return requiresEditorialContent;
    }

    /**
     * Checks if this type uses CPM pricing model.
     *
     * @return true if CPM-based
     */
    public boolean isCpmBased() {
        return this == SPONSORED_ARTICLE || this == NATIVE_AD || this == DISPLAY_AD;
    }

    /**
     * Checks if this type uses CPC (cost per click) pricing.
     *
     * @return true if CPC-based
     */
    public boolean isCpcBased() {
        return this == NATIVE_AD || this == DISPLAY_AD;
    }

    /**
     * Checks if this type uses commission/affiliate pricing.
     *
     * @return true if commission-based
     */
    public boolean isCommissionBased() {
        return this == AFFILIATE;
    }
}
