package com.mmva.newsapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration properties for image processing by domain.
 * Each domain (news, users, ads) has its own image processing configuration.
 *
 * Architecture: Domain-driven separation prevents cross-contamination of concerns.
 */
@Component
@ConfigurationProperties(prefix = "media.processing")
@Data
public class ImageProcessingProperties {

    /**
     * News domain image processing configuration
     */
    private DomainConfig news = new DomainConfig();

    /**
     * Users domain image processing configuration
     */
    private DomainConfig users = new DomainConfig();

    /**
     * Ads domain image processing configuration
     */
    private DomainConfig ads = new DomainConfig();

    /**
     * Configuration for a specific domain's image processing
     */
    @Data
    public static class DomainConfig {
        /**
         * Image processing settings
         */
        private ImageConfig image = new ImageConfig();
    }

    /**
     * Image processing configuration for a domain
     */
    @Data
    public static class ImageConfig {
        /**
         * Maximum width for processed images
         */
        private int maxWidth = 1920;

        /**
         * Maximum height for processed images
         */
        private int maxHeight = 1080;

        /**
         * Default quality setting (0.0-1.0)
         */
        private float quality = 0.85f;

        /**
         * Supported image formats
         */
        private String[] formats = {"jpg", "jpeg", "png", "gif", "webp"};

        /**
         * Individual size configurations with enable/disable flags
         */
        private Map<String, ImageSizeConfig> sizes = new HashMap<>();
    }

    /**
     * Configuration for each image size
     */
    @Data
    public static class ImageSizeConfig {
        /**
         * Whether to generate this size
         */
        private boolean enabled = true;

        /**
         * Width in pixels
         */
        private int width;

        /**
         * Height in pixels
         */
        private int height;

        /**
         * Quality setting (0.0-1.0, default 0.85)
         */
        private float quality = 0.85f;

        /**
         * Whether to keep aspect ratio (default true)
         */
        private boolean preserveAspectRatio = true;

        /**
         * Whether to allow cropping to fit dimensions (default false for thumbnails)
         */
        private boolean cropping = false;
    }

    // ========================================
    // Domain-Specific Helper Methods
    // ========================================

    /**
     * Get enabled sizes for news domain
     */
    public Map<String, ImageSizeConfig> getNewsEnabledSizes() {
        return getEnabledSizes(news.getImage().getSizes());
    }

    /**
     * Get enabled sizes for users domain
     */
    public Map<String, ImageSizeConfig> getUsersEnabledSizes() {
        return getEnabledSizes(users.getImage().getSizes());
    }

    /**
     * Get enabled sizes for ads domain
     */
    public Map<String, ImageSizeConfig> getAdsEnabledSizes() {
        return getEnabledSizes(ads.getImage().getSizes());
    }

    /**
     * Check if a news size is enabled
     */
    public boolean isNewsSizeEnabled(String size) {
        return isSizeEnabled(news.getImage().getSizes(), size);
    }

    /**
     * Check if a users size is enabled
     */
    public boolean isUsersSizeEnabled(String size) {
        return isSizeEnabled(users.getImage().getSizes(), size);
    }

    /**
     * Check if an ads size is enabled
     */
    public boolean isAdsSizeEnabled(String size) {
        return isSizeEnabled(ads.getImage().getSizes(), size);
    }

    /**
     * Get configuration for a news size
     */
    public ImageSizeConfig getNewsSizeConfig(String size) {
        return news.getImage().getSizes().get(size);
    }

    /**
     * Get configuration for a users size
     */
    public ImageSizeConfig getUsersSizeConfig(String size) {
        return users.getImage().getSizes().get(size);
    }

    /**
     * Get configuration for an ads size
     */
    public ImageSizeConfig getAdsSizeConfig(String size) {
        return ads.getImage().getSizes().get(size);
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private Map<String, ImageSizeConfig> getEnabledSizes(Map<String, ImageSizeConfig> sizes) {
        return sizes.entrySet().stream()
                .filter(entry -> entry.getValue().isEnabled())
                .collect(Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> entry.getValue()
                ));
    }

    private boolean isSizeEnabled(Map<String, ImageSizeConfig> sizes, String size) {
        ImageSizeConfig config = sizes.get(size);
        return config != null && config.isEnabled();
    }
}