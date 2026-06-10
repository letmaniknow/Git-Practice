package com.mmva.newsapp.infrastructure.monetization.ads.local.enums;

/**
 * Enum representing ad placement positions in the application.
 * 
 * <p>
 * Defines where ads can appear in the newsapp application layout.
 * Each position has different viewability, CPM rates, and user impact.
 * </p>
 * 
 * <h3>Position Value (Relative CPM):</h3>
 * <ul>
 * <li>ABOVE_THE_FOLD: Highest value - visible without scrolling</li>
 * <li>IN_ARTICLE: High value - high newsengagement during reading</li>
 * <li>STICKY_BOTTOM: Medium-high value - always visible</li>
 * <li>SIDEBAR: Medium value - desktop only</li>
 * <li>BELOW_THE_FOLD: Lower value - requires scrolling</li>
 * <li>EXIT_INTENT: Variable - high attention capture</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum PlacementPosition {

    /**
     * Header area - above main content.
     * High visibility, typically leaderboard ads.
     * Above the fold on desktop and mobile.
     */
    HEADER("Header", 1, true, 1.5),

    /**
     * Below header, above content feed.
     * Highest viewability, premium inventory.
     */
    ABOVE_THE_FOLD("Above The Fold", 2, true, 1.8),

    /**
     * Between newsapp items in the feed.
     * Native ad placement, high newsengagement.
     */
    IN_FEED("In Feed", 3, true, 1.3),

    /**
     * Within newsapp body content.
     * Inserted between paragraphs, high attention.
     */
    IN_ARTICLE("In Article", 4, true, 1.4),

    /**
     * Right sidebar (desktop only).
     * Medium rectangle or skyscraper format.
     */
    SIDEBAR_RIGHT("Right Sidebar", 5, false, 1.0),

    /**
     * Left sidebar (desktop only).
     * Less common, lower value than right.
     */
    SIDEBAR_LEFT("Left Sidebar", 6, false, 0.8),

    /**
     * Below newsapp content.
     * Often recommendation widgets.
     */
    BELOW_ARTICLE("Below Article", 7, true, 1.1),

    /**
     * At the end of content, bottom of page.
     * Lower viewability but high intent users.
     */
    BELOW_THE_FOLD("Below The Fold", 8, true, 0.7),

    /**
     * Fixed to bottom of viewport.
     * Always visible, requires close button.
     */
    STICKY_BOTTOM("Sticky Bottom", 9, true, 1.2),

    /**
     * Fixed to top of viewport (after scroll).
     * High visibility but can be intrusive.
     */
    STICKY_TOP("Sticky Top", 10, true, 1.25),

    /**
     * Full-screen overlay, app open/close.
     * Highest impact, use sparingly.
     */
    INTERSTITIAL("Interstitial", 11, true, 2.0),

    /**
     * Triggered when user shows exit intent.
     * Desktop: mouse moves to close tab.
     * Mobile: back button detection.
     */
    EXIT_INTENT("Exit Intent", 12, false, 1.6),

    /**
     * Within newscategory listing pages.
     * Between newscategory content items.
     */
    CATEGORY_PAGE("Category Page", 13, true, 1.0),

    /**
     * Search results page.
     * High intent users, valuable inventory.
     */
    SEARCH_RESULTS("Search Results", 14, true, 1.3),

    /**
     * Newsletter/email embed position.
     * External to app, different tracking.
     */
    EMAIL_NEWSLETTER("Email Newsletter", 15, false, 1.1);

    private final String displayName;
    private final int sortOrder;
    private final boolean mobileSupported;
    private final double cpmMultiplier;

    PlacementPosition(String displayName, int sortOrder, boolean mobileSupported, double cpmMultiplier) {
        this.displayName = displayName;
        this.sortOrder = sortOrder;
        this.mobileSupported = mobileSupported;
        this.cpmMultiplier = cpmMultiplier;
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
     * Returns the sort order for UI display.
     *
     * @return sort order (lower = higher in page)
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Checks if this position is supported on mobile devices.
     *
     * @return true if mobile supported
     */
    public boolean isMobileSupported() {
        return mobileSupported;
    }

    /**
     * Returns the CPM rate multiplier relative to baseline.
     * Use this to calculate position-adjusted pricing.
     *
     * @return CPM multiplier (1.0 = baseline)
     */
    public double getCpmMultiplier() {
        return cpmMultiplier;
    }

    /**
     * Calculates adjusted CPM based on base rate.
     *
     * @param baseCpm the base CPM rate
     * @return position-adjusted CPM
     */
    public double calculateAdjustedCpm(double baseCpm) {
        return baseCpm * cpmMultiplier;
    }

    /**
     * Checks if this is a sidebar position (desktop-only).
     *
     * @return true if sidebar position
     */
    public boolean isSidebar() {
        return this == SIDEBAR_LEFT || this == SIDEBAR_RIGHT;
    }

    /**
     * Checks if this is a sticky/fixed position.
     *
     * @return true if position is sticky/fixed
     */
    public boolean isSticky() {
        return this == STICKY_BOTTOM || this == STICKY_TOP;
    }

    /**
     * Checks if this position overlays content.
     *
     * @return true if overlay/modal type
     */
    public boolean isOverlay() {
        return this == INTERSTITIAL || this == EXIT_INTENT;
    }

    /**
     * Checks if this is a premium/high-value position.
     *
     * @return true if CPM multiplier >= 1.5
     */
    public boolean isPremium() {
        return cpmMultiplier >= 1.5;
    }
}
