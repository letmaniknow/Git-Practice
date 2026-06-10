package com.mmva.newsapp.infrastructure.monetization.ads.local.enums;

/**
 * Enum representing the type/format of advertisement creative.
 *
 * <p>
 * Defines the different types of creative assets that can be uploaded
 * and served in ad placements. Each type has different technical requirements
 * and serving considerations.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum AdCreativeType {

    /**
     * Static image creative (JPG, PNG, GIF, WebP).
     * Most common ad format, works across all placements.
     */
    IMAGE("Image", "800x600", false, new String[] { "image/jpeg", "image/png", "image/gif", "image/webp" }),

    /**
     * Video creative (MP4, WebM, etc.).
     * Requires video-compatible placements and player support.
     */
    VIDEO("Video", "640x360", true, new String[] { "video/mp4", "video/webm", "video/ogg" }),

    /**
     * HTML5 creative with interactive elements.
     * Allows rich media, animations, and custom interactions.
     */
    HTML5("HTML5", "Responsive", false, new String[] { "text/html", "application/zip" }),

    /**
     * Native ad creative optimized for content feed integration.
     * Adapts to surrounding content design.
     */
    NATIVE("Native", "Responsive", false, new String[] { "application/json" }),

    /**
     * Text-only creative for simple text ads.
     * Minimal bandwidth, high performance.
     */
    TEXT("Text", "Variable", false, new String[] { "text/plain" });

    private final String displayName;
    private final String defaultDimensions;
    private final boolean isVideo;
    private final String[] supportedMimeTypes;

    AdCreativeType(String displayName, String defaultDimensions, boolean isVideo, String[] supportedMimeTypes) {
        this.displayName = displayName;
        this.defaultDimensions = defaultDimensions;
        this.isVideo = isVideo;
        this.supportedMimeTypes = supportedMimeTypes;
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
     * Returns default dimensions for this creative type.
     *
     * @return dimension string (e.g., "728x90" or "Responsive")
     */
    public String getDefaultDimensions() {
        return defaultDimensions;
    }

    /**
     * Checks if this is a video creative type.
     *
     * @return true if video type
     */
    public boolean isVideo() {
        return isVideo;
    }

    /**
     * Returns array of supported MIME types for this creative type.
     *
     * @return array of MIME type strings
     */
    public String[] getSupportedMimeTypes() {
        return supportedMimeTypes.clone(); // Return copy to prevent modification
    }

    /**
     * Checks if the given MIME type is supported by this creative type.
     *
     * @param mimeType the MIME type to check
     * @return true if supported
     */
    public boolean supportsMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        for (String supported : supportedMimeTypes) {
            if (supported.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this creative type requires file upload.
     *
     * @return true if file upload is required
     */
    public boolean requiresFileUpload() {
        return this != TEXT; // Text creatives don't need file uploads
    }

    /**
     * Checks if this creative type supports rich media/interactivity.
     *
     * @return true if rich media capable
     */
    public boolean isRichMedia() {
        return this == HTML5 || this == NATIVE;
    }

    /**
     * Gets maximum recommended file size in MB for this creative type.
     *
     * @return max file size in MB
     */
    public int getMaxFileSizeMB() {
        switch (this) {
            case IMAGE:
                return 5; // 5MB for images
            case VIDEO:
                return 50; // 50MB for videos
            case HTML5:
                return 10; // 10MB for HTML5 packages
            case NATIVE:
                return 1; // 1MB for native specs
            case TEXT:
                return 0; // No file for text
            default:
                return 5;
        }
    }
}