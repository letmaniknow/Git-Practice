package com.mmva.newsapp.infrastructure.monetization.ads.local.enums;

/**
 * Enum representing the type/format of advertisement.
 * 
 * <p>
 * Follows IAB (Interactive Advertising Bureau) standard ad formats
 * for programmatic advertising compatibility.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum AdType {

    /**
     * Standard banner ads - horizontal rectangular.
     * Common sizes: 728x90 (leaderboard), 320x50 (mobile).
     */
    BANNER("Banner", 728, 90, false),

    /**
     * Leaderboard - wide horizontal banner.
     * Size: 728x90 (desktop), 320x50 (mobile).
     */
    LEADERBOARD("Leaderboard", 728, 90, false),

    /**
     * Medium rectangle - common sidebar ad.
     * Size: 300x250.
     */
    MEDIUM_RECTANGLE("Medium Rectangle", 300, 250, false),

    /**
     * Skyscraper - tall vertical sidebar ad.
     * Size: 160x600 (wide), 120x600 (standard).
     */
    SKYSCRAPER("Skyscraper", 160, 600, false),

    /**
     * Large rectangle - larger inline ad.
     * Size: 336x280.
     */
    LARGE_RECTANGLE("Large Rectangle", 336, 280, false),

    /**
     * In-feed native ad - blends with content feed.
     * Size: Responsive/Fluid.
     */
    IN_FEED("In-Feed Native", 0, 0, true),

    /**
     * In-newsapp ad - placed within newsapp body.
     * Size: Responsive.
     */
    IN_ARTICLE("In-Article", 0, 0, true),

    /**
     * Interstitial - full-screen overlay ad.
     * Size: Full screen (requires close button).
     */
    INTERSTITIAL("Interstitial", 0, 0, false),

    /**
     * Sticky footer - fixed at bottom of screen.
     * Size: Full width x 50-90px height.
     */
    STICKY_FOOTER("Sticky Footer", 0, 50, false),

    /**
     * Video pre-roll - video ad before content.
     * Duration: 15-30 seconds typical.
     */
    VIDEO_PREROLL("Video Pre-Roll", 0, 0, false),

    /**
     * Sponsored content card - native recommendation card.
     * Size: Card format, matches content cards.
     */
    SPONSORED_CARD("Sponsored Card", 0, 0, true);

    private final String displayName;
    private final int defaultWidth;
    private final int defaultHeight;
    private final boolean isNative;

    AdType(String displayName, int defaultWidth, int defaultHeight, boolean isNative) {
        this.displayName = displayName;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.isNative = isNative;
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
     * Returns default width in pixels (0 for responsive/native).
     *
     * @return width in pixels
     */
    public int getDefaultWidth() {
        return defaultWidth;
    }

    /**
     * Returns default height in pixels (0 for responsive/native).
     *
     * @return height in pixels
     */
    public int getDefaultHeight() {
        return defaultHeight;
    }

    /**
     * Returns dimensions as "WxH" string.
     *
     * @return dimension string, or "Responsive" for native ads
     */
    public String getDimensions() {
        if (defaultWidth == 0 || defaultHeight == 0) {
            return "Responsive";
        }
        return defaultWidth + "x" + defaultHeight;
    }

    /**
     * Checks if this is a native ad format.
     * Native ads blend with surrounding content.
     *
     * @return true if native ad format
     */
    public boolean isNative() {
        return isNative;
    }

    /**
     * Checks if this ad type is fixed size (not responsive).
     *
     * @return true if fixed dimensions
     */
    public boolean isFixedSize() {
        return defaultWidth > 0 && defaultHeight > 0;
    }

    /**
     * Checks if this is a video ad type.
     *
     * @return true if video ad
     */
    public boolean isVideo() {
        return this == VIDEO_PREROLL;
    }

    /**
     * Checks if this ad type is intrusive (overlays content).
     *
     * @return true if intrusive
     */
    public boolean isIntrusive() {
        return this == INTERSTITIAL;
    }

    /**
     * Checks if this ad type requires a close button.
     *
     * @return true if close button required
     */
    public boolean requiresCloseButton() {
        return this == INTERSTITIAL || this == STICKY_FOOTER;
    }
}
