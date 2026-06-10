package com.mmva.newsapp.infrastructure.monetization.ads.local.dto;

import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdCreativeType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating an Ad Creative.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdCreativeRequestDto {

    /**
     * Unique code for this creative.
     */
    @NotBlank(message = "Creative code is required")
    @Size(min = 3, max = 100, message = "Creative code must be between 3 and 100 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Creative code must contain only uppercase letters, numbers, and underscores")
    private String adCreativeCode;

    /**
     * Human-readable name for the creative.
     */
    @NotBlank(message = "Creative name is required")
    @Size(min = 1, max = 200, message = "Creative name must be between 1 and 200 characters")
    private String adCreativeName;

    /**
     * Detailed description of the creative content.
     */
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String adCreativeDescription;

    /**
     * Type of creative.
     */
    @NotNull(message = "Creative type is required")
    private AdCreativeType adCreativeType;

    /**
     * Alternative text for accessibility.
     */
    @Size(max = 300, message = "Alt text cannot exceed 300 characters")
    private String adCreativeAltText;

    /**
     * Click destination URL when ad is clicked.
     */
    @Pattern(regexp = "^https?://.*", message = "Click URL must be a valid HTTP/HTTPS URL")
    @Size(max = 1000, message = "Click URL cannot exceed 1000 characters")
    private String adCreativeClickUrl;

    /**
     * Width in pixels (optional, can be auto-detected).
     */
    @Min(value = 1, message = "Width must be at least 1 pixel")
    @Max(value = 4096, message = "Width cannot exceed 4096 pixels")
    private Integer adCreativeWidth;

    /**
     * Height in pixels (optional, can be auto-detected).
     */
    @Min(value = 1, message = "Height must be at least 1 pixel")
    @Max(value = 4096, message = "Height cannot exceed 4096 pixels")
    private Integer adCreativeHeight;

    /**
     * Whether this creative requires approval before use.
     */
    @Builder.Default
    private Boolean adCreativeRequiresApproval = false;

    /**
     * Additional metadata as JSON string.
     */
    private String adCreativeMetadataJson;

    /**
     * External creative ID from ad server.
     */
    @Size(max = 200, message = "External creative ID cannot exceed 200 characters")
    private String adCreativeExternalCreativeId;

    /**
     * External ad server name.
     */
    @Size(max = 100, message = "External ad server name cannot exceed 100 characters")
    private String adCreativeExternalAdServer;

    // ========================================
    // Native Ad Content (for NATIVE type creatives)
    // ========================================

    /**
     * Ad title/headline for native ads (separate from creative name).
     */
    @Size(max = 200, message = "Ad title cannot exceed 200 characters")
    private String adCreativeAdTitle;

    /**
     * Ad media URL for native ads (can be different from creative file).
     */
    @Size(max = 500, message = "Ad media URL cannot exceed 500 characters")
    private String adCreativeAdMediaUrl;

    /**
     * Ad summary/description for native ads.
     */
    @Size(max = 500, message = "Ad summary cannot exceed 500 characters")
    private String adCreativeAdSummary;
}